# Artifact for SlurmExec under Review for SEFM 2026

## Files contained in the artifact:

- The modified version of BenchExec contains the newly introduced SlurmExec scheduler, which is the artifact’s main contribution.
It is also available here: https://github.com/mmuesly/benchexec/tree/buw-slurm-array-aggregate.

- The SV-COMP Java benchmark was taken from here: https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks

- The used GDart version is published here: https://zenodo.org/records/17691525.

- The official GDart results for SV-COMP 2026 are available here: https://zenodo.org/records/19736446.
- All other files were originally created for this artifact.

## Not contained components that are required to reproduce

- An HPC cluster with Apptainer and a Slurm manager.

## How to run it

Sadly, I have not figured out a way to reproduce running software on HPC systems in a virtual machine.
Instead, this README.md describes how I used the components to obtain the results in the result folder.

#### Preparation:

Apptainer is going to need a sif file as a container image that matches your machine type. The sif provided in this artifact is built for AMD64 CPUs.
However, using the sv-comp.def file, it is possible to rebuild the sif for other CPUs as well.
Be aware that the GDart binary is built for amd64 as well.
You might use SlurmExec for other tools and machine layouts if all your components support it.
The create_sif.sh shows how to convert the \*.def file into a \*.sif that can be used as image for executing tools.
As the experiment in this paper is based on SV-COMP tasks, the sv-comp26.def file is an adaptation of the official SV-COMP 26 Dockerfile.

I have loaded on the HPC system Python/3.13.1 and GCCcore/14.2.0 in addition to the standard environment at PLEIADES at BUW. BenchExec requires a relatively modern version of Python. The scripts have been tested with Python 3.13.1.

Using this Python version, it is necessary to create a virtual environment on your machine and install benchexec into this environment by running pip install -e . inside the benchexec folder.

#### Executing the experiment

Now you should be good to go to run the run_benchexec.sh script. If the execution is successful, you should find the results in a new results folder in a format similar to that of any BenchExec task.

The paper requires running the run_benchexec.sh command three times to get three runs.

There are a few assumptions I currently make about the HPC system:

- There is an SSD mounted on every worker node, accessible at/tmp, for processing local files during computation.

- There is an overall file system shared at the same path across all workers, and the artifact is located on it. So it is possible to copy results from the /tmp folder back to the overall file system where the final results are also archived. The intermediate results in the /tmp folder are removed after every task.

#### Debugging

It is possible to set the delete option of the temporary folder created in benchexec/contrib/slurm/buwarrayexecutor.py on line 334 to hardcoded False to make sure that all intermediate results are available for debugging after completion of the run. Currently, there is no flag for this in the tool.

## Create the paper plot:

The folder paper_results contains the results demonstrated in the paper. Run create_figure3.sh to generate LaTeX code that reproduces Figure 3. You will need an installed LaTeX distribution to compile this code. The artifact does not ship this code.

The code is generated into paper_results/figure3.tex. If pdflatex is installed, the figure3.pdf is compiled and moved to the toplevel directory.
