#!/bin/bash
date
mkdir scratch_tmp
./benchexec/contrib/slurm-benchmark.py --buw-array --apptainer ./sv-comp26.sif --scratchdir ./scratch_tmp --benchdefs_folder $PWD -N 3 --retry-killed 1 --no-hyperthreading --no-container  --tool-directory $PWD/gdart/ gdart.xml

date
