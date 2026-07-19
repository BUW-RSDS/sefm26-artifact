# This file is part of BenchExec, a framework for reliable benchmarking:
# https://github.com/sosy-lab/benchexec
#
# SPDX-FileCopyrightText: 2007-2020 Dirk Beyer <https://www.sosy-lab.org>
# SPDX-FileCopyrightText: 2024 Levente Bajczi
# SPDX-FileCopyrightText: 2026 Malte Mues
# SPDX-FileCopyrightText: Critical Systems Research Group
# SPDX-FileCopyrightText: Budapest University of Technology and Economics <https://www.ftsrg.mit.bme.hu>
# SPDX-FileCopyrightText: Bergische Universität Wuppertal
#
# SPDX-License-Identifier: Apache-2.0
import glob
import logging
import math
import os
import re
import shlex
import shutil
import subprocess
import sys
import tempfile
import time
import unittest
import zipfile

from datetime import datetime


from benchexec import tooladapter
from benchexec.tablegenerator import parse_results_file
from benchexec.util import ProcessExitCode, relative_path
from contrib.slurm.utils import (
    version_in_container,
    get_system_info_srun,
)

sys.dont_write_bytecode = True  # prevent creation of .pyc files

STOPPED_BY_INTERRUPT = False
singularity = None
RESTART_COUNTER = 0

WORKDIR_LOGIC_1 = """
# Taken from: https://pleiadesbuw.github.io/PleiadesUserDocumentation/slurm/exampleTmp.html
# Ensures that files in the local workdirectory are cleanded up on exit.
workdir="/tmp/${USER:?}_${SLURM_JOB_ID:?}"
submitdir="${SLURM_SUBMIT_DIR:?}"

mkdir -p "${workdir}"

function clean_up {
"""
WORKDIR_LOGIC_2 = """
    # Leave ${workdir}
    cd "${submitdir}" || exit
    # Use :? to only remove if the variable is defined. Otherwise exit
    rm -rf "${workdir:?}"
    exit
}

# Always call "clean_up" when script ends
# This even executes on job failure/cancellation
trap 'clean_up' EXIT

cd "${workdir}" || exit
"""


def init(config, benchmark):
    global apptainer
    assert (
        benchmark.config.apptainer
    ), "apptainer is required for array-based SLURM jobs."
    apptainer = benchmark.config.apptainer

    tool_locator = tooladapter.create_tool_locator(config)
    benchmark.tool.version = version_in_container(apptainer, benchmark.tool_module)
    benchmark.executable = benchmark.tool.executable(tool_locator)
    try:
        benchmark.tool_version = benchmark.tool.version(benchmark.executable)
    except Exception as e:
        logging.warning(
            "could not determine version due to error: %s",
            e,
        )


def get_system_info():
    return get_system_info_srun(apptainer)


def execute_benchmark(benchmark, output_handler):
    if benchmark.config.use_hyperthreading:
        sys.exit(
            "SLURM can only work properly without hyperthreading enabled, by passing the --no-hyperthreading option. See README.md for details."
        )

    if not benchmark.config.scratchdir:
        sys.exit("No scratchdir present. Please specify using --scratchdir <path>.")
    elif not os.path.exists(benchmark.config.scratchdir):
        os.makedirs(benchmark.config.scratchdir)
        logging.debug(f"Created scratchdir: {benchmark.config.scratchdir}")
    elif not os.path.isdir(benchmark.config.scratchdir):
        sys.exit(
            f"Scratchdir {benchmark.config.scratchdir} not a directory. Please specify using --scratchdir <path>."
        )

    # First we execute the tests
    runs = []
    for runSet in benchmark.run_sets:
        if STOPPED_BY_INTERRUPT:
            break

        if not runSet.should_be_executed():
            output_handler.output_for_skipping_run_set(runSet)

        elif not runSet.runs:
            output_handler.output_for_skipping_run_set(
                runSet, "because it has no files"
            )

        else:
            output_handler.output_before_run_set(runSet)
            if benchmark.config.continue_interrupted:
                runs.extend(filter_previous_results(runSet, benchmark, output_handler))
            else:
                runs.extend(runSet.runs)

    for i in range(0, len(runs), benchmark.config.batch_size):
        if not STOPPED_BY_INTERRUPT:
            chunk = runs[i : min(i + benchmark.config.batch_size, len(runs))]
            execute_batch(chunk, benchmark, output_handler)

    # Second we set the outputs
    for runSet in benchmark.run_sets:
        if STOPPED_BY_INTERRUPT:
            break

        if not runSet.should_be_executed():
            output_handler.output_for_skipping_run_set(runSet)

        elif not runSet.runs:
            output_handler.output_for_skipping_run_set(
                runSet, "because it has no files"
            )

        else:
            output_handler.output_after_run_set(runSet)

    time.sleep(5)

    output_handler.output_after_benchmark(STOPPED_BY_INTERRUPT)


sbatch_pattern = re.compile(r"Submitted batch job (\d+)")


def filter_previous_results(run_set, benchmark, output_handler):
    prefix_base = f"{benchmark.config.output_path}{benchmark.name}."
    files = list(
        filter(
            lambda file: file != benchmark.log_zip,
            glob.glob(f"{prefix_base}*.logfiles.zip"),
        )
    )
    if files and len(files) > 0:
        prefix = str(max(files, key=os.path.getmtime))[0 : -(len(".logfiles.zip"))]
    else:
        logging.warning("No logfile zip found. Giving up recovery.")
        return run_set.runs
    logging.info(f"Logfile zip found with prefix {prefix}. Attempting recovery.")

    logfile_zip = prefix + ".logfiles.zip"
    file_zip = prefix + ".files.zip"

    if not os.path.isfile(file_zip):
        logging.warning(f"No {file_zip} found. Giving up recovery.")
        return run_set.runs

    with zipfile.ZipFile(logfile_zip, "r") as logfile_zip_ref:
        with zipfile.ZipFile(file_zip, "r") as file_zip_ref:
            xml_filename_base = prefix + ".results." + run_set.name
            xml = xml_filename_base + ".xml"
            xml_bz2 = xml_filename_base + ".xml.bz2"
            if os.path.exists(xml):
                result_file = xml
            elif os.path.exists(xml_bz2):
                result_file = xml_bz2
            else:
                logging.warning(
                    ".xml or .xml.bz2 must exist for previous run. Giving up recovery."
                )
                return run_set.runs

            previous_results = parse_results_file(result_file)

            old_version = previous_results.get("version")
            new_version = benchmark.tool_version
            if old_version != new_version:
                logging.warning(
                    f"Mismatch in tool version: old version={old_version}, current version: {new_version}"
                )
                return run_set.runs

            old_options = previous_results.get("options")
            new_options = " ".join(run_set.options)
            if old_options != new_options:
                logging.warning(
                    f"Mismatch in tool options: old options='{old_options}', current options: '{new_options}'"
                )
                return run_set.runs

            previous_runs = {}
            for elem in previous_results:
                if elem.tag == "run":
                    values = {}
                    for col in elem:
                        if col.tag == "column":
                            if "walltime" == col.get("title"):
                                values["walltime"] = float(
                                    str(col.get("value"))[:-1]
                                )  # ends in 's'
                            elif "cputime" == col.get("title"):
                                values["cputime"] = float(
                                    str(col.get("value"))[:-1]
                                )  # ends in 's'
                            elif "memory" == col.get("title"):
                                values["memory"] = int(
                                    str(col.get("value"))[:-1]
                                )  # ends in 'B'
                            elif "returnvalue" == col.get("title"):
                                values["exitcode"] = ProcessExitCode.create(
                                    value=int(col.get("value"))
                                )
                            elif "exitsignal" == col.get("title"):
                                values["exitcode"] = ProcessExitCode.create(
                                    signal=int(col.get("value"))
                                )
                            elif "terminationreason" == col.get("title"):
                                values["terminationreason"] = col.get("value")
                    # I think 'name' and 'properties' are enough to uniquely identify runs, but this should probably be more extensible
                    if values != {}:
                        previous_runs[
                            (elem.get("name"), elem.get("properties"))
                        ] = values

            missing_runs = []
            for run in run_set.runs:
                props = " ".join(sorted([prop.name for prop in run.properties]))
                name = relative_path(run.identifier, result_file)
                key = (name, props)
                if key in previous_runs:
                    old_log = str(
                        os.path.join(
                            str(os.path.basename(logfile_zip))[0 : -(len(".zip"))],
                            run_set.real_name
                            + "."
                            + os.path.basename(run.identifier)
                            + ".log",
                        )
                    )
                    if old_log in logfile_zip_ref.namelist():
                        with logfile_zip_ref.open(old_log) as zipped_log, open(
                            run.log_file, "wb"
                        ) as target_log:
                            shutil.copyfileobj(zipped_log, target_log)

                        old_files_prefix = (
                            str(
                                os.path.join(
                                    str(os.path.basename(file_zip))[0 : -(len(".zip"))],
                                    run_set.real_name,
                                    os.path.basename(run.identifier),
                                )
                            )
                            + "/"
                        )

                        files_in_zip = [
                            f
                            for f in file_zip_ref.namelist()
                            if f.startswith(old_files_prefix)
                        ]
                        if files_in_zip and len(files_in_zip) > 0:
                            os.makedirs(run.result_files_folder, exist_ok=True)
                            for file_in_zip in files_in_zip:
                                if not file_in_zip.endswith("/"):
                                    with file_zip_ref.open(
                                        file_in_zip
                                    ) as source_file, open(
                                        os.path.join(
                                            run.result_files_folder,
                                            os.path.basename(file_in_zip),
                                        ),
                                        "wb",
                                    ) as target_file:
                                        shutil.copyfileobj(source_file, target_file)

                            run.cmdline()  # we need to call this, because it sets the _cmdline value
                            run.set_result(previous_runs[key])
                            output_handler.output_after_run(run)
                        else:
                            logging.warning(
                                f"Old files directory {old_files_prefix} does not exist. Skipping run {name}."
                            )
                            missing_runs.append(run)
                    else:
                        logging.warning(
                            f"Old log {old_log} does not exist. Skipping run {name}."
                        )
                        missing_runs.append(run)
                else:
                    logging.warning(
                        f"Run with key {key} not found in results. Skipping run {name}."
                    )
                    missing_runs.append(run)

            logging.info(
                f"Successfully recovered {len(run_set.runs) - len(missing_runs)} runs, still missing {len(missing_runs)} more."
            )
            return missing_runs


def execute_batch(
    runs,
    benchmark,
    output_handler,
    counter=0,
):
    global STOPPED_BY_INTERRUPT, RESTART_COUNTER
    number_of_bins = int(len(runs) / benchmark.config.aggregation_factor) + 1

    with tempfile.TemporaryDirectory(
        dir=benchmark.config.scratchdir, delete=not benchmark.config.generate_only
    ) as tempdir:
        batch_lines = ["#!/bin/bash"]

        limits = get_resource_limits(benchmark)
        sbatch_ressource_limits = [
            "--time=" + str(limits.srun_timelimit()),
            "--cpus-per-task=" + str(limits.cpus),
            "--mem=" + limits.mem_in_mega_as_str() + "M",
            "--threads-per-core=1",  # --use_hyperthreading=False is always given here
            "--mincpus=" + str(limits.cpus),
            "--ntasks=1",
        ]
        for setting in sbatch_ressource_limits:
            batch_lines.extend(["\n#SBATCH " + str(setting)])

        bins = {}
        jobid_bins = {}
        # put all runs into a queue
        for i, run in enumerate(runs):
            if i % number_of_bins not in bins:
                bins[i % number_of_bins] = []
            bins[i % number_of_bins].append((i, run))

        for bin in bins:
            print(f"processing bin {bin}, {benchmark.config.generate_only}")
            bintmpdir = os.path.join(tempdir, f"bin{bin}")
            os.makedirs(bintmpdir)

            ### Write the config for the different tasks
            taskfile_name = f"bin{str(bin)}.tasks"
            taskfile = os.path.join(tempdir, taskfile_name)
            with open(taskfile, "w") as f:
                task_lines = ["declare -a jobs=(\n"]
                task_count = 0
                for i, (_, run) in enumerate(bins[bin]):
                    os.makedirs(os.path.join(bintmpdir, f"tmp{i}", "upper"))
                    os.makedirs(os.path.join(bintmpdir, f"tmp{i}", "work"))
                    task_lines.extend(
                        [
                            str(
                                get_run_cli(
                                    benchmark,
                                    run.cmdline(),
                                )
                            )
                            + "\n"
                        ]
                    )
                    task_count += 1
                task_lines.append(")\n")
                f.writelines(task_lines)

            ### Write the array filesgit s

            absolut_image_path = os.path.abspath(benchmark.config.apptainer)

            batch_lines.extend([f"\n#SBATCH --array=0-{task_count - 1}"])
            batch_lines.extend(f"\n{WORKDIR_LOGIC_1}\n")
            batch_lines.extend(f"cp runlog.out {bintmpdir}/tmp$SLURM_ARRAY_TASK_ID/\n")
            batch_lines.extend(
                f"cp *.graphml {bintmpdir}/tmp$SLURM_ARRAY_TASK_ID/ || :\n"
            )
            batch_lines.extend(f"\n{WORKDIR_LOGIC_2}\n")
            batch_lines.extend(f"source {taskfile}\n\n")
            batch_lines.extend("COMMAND=${jobs[$SLURM_ARRAY_TASK_ID]}\n\n")
            batch_lines.extend(
                f"""COMMAND="$(echo $COMMAND | sed 's;../sv-benchmarks;{benchmark.config.mount_point}/sv-benchmarks;g' )"\n"""
            )
            batch_lines.extend(
                f"""printf '\n-NEW-\n\n%s\n\n--------------------------\n\n\n' "$COMMAND"> runlog.out\n"""
            )
            batch_lines.extend(
                (
                    f"srun --exact --quit-on-interrupt "
                    f"-t {limits.srun_timelimit()} -c {limits.cpus} "
                    f"--mem {limits.mem_in_mega_as_str()}M --threads-per-core=1 "
                    "--ntasks=1 apptainer exec "
                    f"-B {benchmark.config.mount_point}:/lower --no-home "
                    f"-B {bintmpdir}/tmp$SLURM_ARRAY_TASK_ID:/overlay "
                    "--fusemount 'container:fuse-overlayfs -o lowerdir=/lower -o upperdir=/overlay/upper -o workdir=/overlay/work /beegfs/mues/sv-comp/bench-defs/' "
                    f'{absolut_image_path} sh -c "$COMMAND" &>> runlog.out\n'
                )
            )

            batchfile = os.path.join(tempdir, f"array{bin}.sbatch")
            with open(batchfile, "w") as f:
                f.writelines(batch_lines)

            if benchmark.config.generate_only:
                print(f"Go to next bin. Finished {bin}")
                continue

            logfolder = os.path.join(
                tempdir, f'arraytasks_logs_{datetime.now().strftime("%y%m%d-%H_%M_%S")}'
            )
            logging.debug(f"Generating logfolder: {logfolder}")
            os.makedirs(logfolder)

            try:
                sbatch_cmd = [
                    "sbatch",
                    "--wait",
                    "-o",
                    f"{logfolder}/slurm-%A_%a.out",
                    str(batchfile),
                ]
                logging.debug("Command to run: %s", shlex.join(sbatch_cmd))
                logpath = os.path.join(bintmpdir, f"bin{bin}batch.log")
                with open(logpath, "w") as logfile:
                    logging.debug(f"starting subprocess with logfile {logfile}")
                    sbatch_result = subprocess.run(
                        sbatch_cmd,
                        stdout=logfile,
                        stderr=subprocess.STDOUT,
                    )
                jobid_pattern = re.compile(r"Submitted batch job (\d*)")
                with open(logpath) as logfile:
                    for line in logfile:
                        jobid_matched = jobid_pattern.search(line)
                        if jobid_matched:
                            jobid = jobid_matched.group(1)
                            logging.debug(
                                f"Analysing results for jobarray: {jobid} for bin: {bin}"
                            )
                            jobid_bins[bin] = jobid
                            break

            except KeyboardInterrupt:
                STOPPED_BY_INTERRUPT = True

            if STOPPED_BY_INTERRUPT:
                logging.debug("Canceling sbatch job if already started")
                if sbatch_result and sbatch_result.stdout:
                    for line in sbatch_result.stdout.splitlines():
                        jobid_match = sbatch_pattern.search(str(line))
                        if jobid_match:
                            jobid = int(jobid_match.group(1))
                            logging.debug(f"Canceling sbatch job #{jobid}")
                            subprocess.run(["scancel", str(jobid)])
        if benchmark.config.generate_only:
            print("Generating done")
            return

        time.sleep(5)

        success_runs, missing_runs = process_run_results(
            bins, jobid_bins, tempdir, benchmark
        )

        time.sleep(10)

        for run, result in success_runs:
            try:
                run.set_result(result)
                output_handler.output_after_run(run)
            except Exception as e:
                logging.warning(
                    "could not set result due to error, and won't retry: %s", e
                )

        if len(missing_runs) > 0 and not STOPPED_BY_INTERRUPT:
            logging.info(
                f"Retrying {len(missing_runs)} runs due to errors. Current retry count for this batch: {counter}"
            )
            RESTART_COUNTER += 1
            execute_batch(missing_runs, benchmark, output_handler, counter + 1)


def process_run_results(bins, jobid_bins, tempdir, benchmark):
    success_runs = []
    missing_runs = []
    for bin in bins:
        bintmpdir = os.path.join(tempdir, f"bin{bin}")
        jobid = jobid_bins[bin]
        for i, (_, run) in enumerate(bins[bin]):
            try:
                result = get_run_result(
                    f"{jobid}_{i}",
                    os.path.join(bintmpdir, f"tmp{i}"),
                    run,
                    benchmark.result_files_patterns
                    + ["*witness*"],  # e.g., deagle uses mismatched naming
                )
                success_runs.append((run, result))
            except Exception as e:
                logging.warning("could not set result due to error: %s", e)
                if (
                    RESTART_COUNTER < benchmark.config.retry
                    or benchmark.config.retry < 0
                ):
                    missing_runs.append(run)
                else:
                    if not STOPPED_BY_INTERRUPT:
                        logging.debug("preserving log(s) due to error with run")
                        for file in glob.glob(f"{tempdir}/logs/*_{bin}.out"):
                            os.makedirs(benchmark.result_files_folder, exist_ok=True)
                            shutil.copy(
                                file,
                                os.path.join(
                                    benchmark.result_files_folder,
                                    os.path.basename(file) + ".error",
                                ),
                            )
    return success_runs, missing_runs


def stop():
    global STOPPED_BY_INTERRUPT
    STOPPED_BY_INTERRUPT = True


class RessourceLimits:
    def __init__(self, time, cpus, mem):
        print("Ressource limit time", time)
        self.timelimit = time
        self.cpus = cpus
        self.mem = mem

    def srun_timelimit(self):
        logging.debug("configured time:" + str(self.timelimit))
        srun_timelimit_h = int(self.timelimit / 3600)
        srun_timelimit_m = int((self.timelimit % 3600) / 60)
        srun_timelimit_s = int(self.timelimit % 60)
        return f"{srun_timelimit_h:02d}:{srun_timelimit_m:02d}:{srun_timelimit_s:02d}"

    def mem_in_mega_as_str(self):
        return str(int(self.mem / 1000000))


def get_resource_limits(benchmark):
    # There are more timelimits currently not considered: benchmark.rlimits.walltime, benchmark.rlimits.cputime_hard
    ret = RessourceLimits(
        benchmark.rlimits.cputime, benchmark.rlimits.cpu_cores, benchmark.rlimits.memory
    )
    return ret


def get_run_cli(benchmark, args):
    cli = f'"{shlex.join(args)}"'
    logging.debug("Command to run: %s", cli)

    return cli


def wait_for(func, timeout_sec=None, poll_interval_sec=1):
    """
    Waits until the func() returns non-None
    :param func: function to call until a value is returned
    :param timeout_sec: How much time to give up after
    :param poll_interval_sec: How frequently to check the result
    """
    start_time = time.monotonic()

    while True:
        ret = func()
        if ret is not None:
            return ret

        if timeout_sec is not None and time.monotonic() - start_time > timeout_sec:
            raise BenchExecException(
                "Timeout exceeded for waiting for job to realize it has finished. Scheduler may be failing."
            )

        time.sleep(poll_interval_sec)


def get_run_result(jobid, tempdir, run, result_files_patterns):
    tmp_log = f"{tempdir}/runlog.out"

    seff_command = ["seff", str(jobid)]

    def get_checked_seff_result():
        seff_result = subprocess.run(
            seff_command,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
        )
        if "exit code" in str(seff_result.stdout):
            return seff_result
        else:
            return None

    # sometimes `seff` needs a few extra seconds to realize the task has ended
    result = wait_for(get_checked_seff_result, 30, 2)

    ret = create_return_value(str(result.stdout))
    shutil.copy(tmp_log, run.log_file)

    if os.path.exists(tempdir):
        os.makedirs(run.result_files_folder, exist_ok=True)
        for result_files_pattern in result_files_patterns:
            logging.debug(
                f"Globbing {tempdir}/**/{result_files_pattern} for witness files"
            )
            for file_name in glob.glob(
                f"{tempdir}/**/{result_files_pattern}", recursive=True
            ):
                logging.debug(f"Found a witness file: {file_name}")
                if os.path.isfile(file_name):
                    logging.debug(f"copy {file_name} to {run.result_files_folder}")
                    shutil.copy(file_name, run.result_files_folder)
        # shutil.rmtree(tempdir) #makes sense, if everything works

    return ret


exit_code_pattern = re.compile(r"State: ([A-Z-_]*) \(exit code (\d+)\)")
cpu_time_pattern = re.compile(r"CPU Utilized: (\d+):(\d+):(\d+)")
wall_time_pattern = re.compile(r"Job Wall-clock time: (\d+):(\d+):(\d+)")
memory_pattern = re.compile(r"Memory Utilized: (\d+\.\d+) (MB|GB)")


def parse_seff(result):
    # logging.debug(f"Got output from seff: {result}")
    exit_code_match = exit_code_pattern.search(result)
    cpu_time_match = cpu_time_pattern.search(result)
    wall_time_match = wall_time_pattern.search(result)
    memory_match = memory_pattern.search(result)
    exit_code = None
    if exit_code_match:
        slurm_status = str(exit_code_match.group(1))
        exit_code = int(exit_code_match.group(2))
    else:
        slurm_status = "ERROR"
    cpu_time = None
    if cpu_time_match:
        hours, minutes, seconds = map(int, cpu_time_match.groups())
        cpu_time = hours * 3600 + minutes * 60 + seconds
    wall_time = None
    if wall_time_match:
        hours, minutes, seconds = map(int, wall_time_match.groups())
        wall_time = hours * 3600 + minutes * 60 + seconds
    memory_usage = (
        float(memory_match.group(1))
        * (1000000 if memory_match.group(2) == "MB" else 1000000000)
        if memory_match
        else None
    )

    logging.debug(
        f" Parsing result: Jobid: {result} Exit code: {exit_code}, memory usage: {memory_usage}, walltime: {wall_time}, cpu time: {cpu_time}"
    )

    return slurm_status, exit_code, cpu_time, wall_time, memory_usage


def create_return_value(seff_output):
    slurm_status, exit_code, cpu_time, wall_time, memory_usage = parse_seff(seff_output)

    ret = {}
    ret["walltime"] = float(wall_time)  # ends in 's'
    ret["cputime"] = float(cpu_time)  # ends in 's'
    ret["memory"] = int(memory_usage) if memory_usage else -1  # ends in 'B'
    ret["exitcode"] = ProcessExitCode.create(value=exit_code)

    if slurm_status != "COMPLETED":
        ret["terminationreason"] = {
            "OUT_OF_MEMORY": "memory",
            "OUT_OF_ME+": "memory",
            "TIMEOUT": "cputime",
            "ERROR": "failed",
            "FAILED": "killed",
            "CANCELLED": "killed",
        }.get(slurm_status, slurm_status)
    return ret


class TestResultParsing(unittest.TestCase):
    def test_parse_timeout_example(self):
        in_text = """
        State: TIMEOUT (exit code 0)
        Nodes: 1
        Cores per node: 8
        CPU Utilized: 00:01:23
        CPU Efficiency: 14.02% of 00:09:52 core-walltime
        Job Wall-clock time: 00:01:14
        Memory Utilized: 1.28 GB
        Memory Efficiency: 8.73% of 14.65 GB"""

        slurm_status, exit_code, cpu_time, wall_time, memory_usage = parse_seff(in_text)
        self.assertTrue("TIMEOUT" in slurm_status)
        self.assertEqual(memory_usage, 1280000000)

    def test_create_ret(self):
        in_text = """
        State: TIMEOUT (exit code 0)
        Nodes: 1
        Cores per node: 8
        CPU Utilized: 00:01:23
        CPU Efficiency: 14.02% of 00:09:52 core-walltime
        Job Wall-clock time: 00:01:14
        Memory Utilized: 1.28 GB
        Memory Efficiency: 8.73% of 14.65 GB"""

        ret = create_return_value(in_text)

        print(ret)
        self.assertEqual("cputime", ret["terminationreason"])
