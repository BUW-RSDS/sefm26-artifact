#!/bin/bash

# Getting BenchExec with the SlurmExec extension
git clone --depth 1 git@github.com:mmuesly/benchexec.git -b buw-slurm-array-aggregate

# Getting the Java SV-Benchmarks
git clone -n --depth=1 --filter=tree:0 https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks.git
pushd sv-benchmarks;
git sparse-checkout init --no-cone
git sparse-checkout set /java
git checkout 9cf9198156e4c8a6c517e474770158e1bb0b566d
popd;

# Getting the used GDart version
curl -o gdart.zip https://zenodo.org/records/17691525/files/gdart.zip?download=1
unzip gdart.zip
