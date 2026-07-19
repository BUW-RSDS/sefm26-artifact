// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2024 The SV-Benchmarks Community
//
// SPDX-License-Identifier: Apache-2.0
/*
 * This benchmark task is a modification of the following original Benchmark:
 * Origin of the benchmark:
 *     repo: https://github.com/SymbolicPathFinder/jpf-symbc
 *     branch: master
 *     root directory: src/examples/concolic
 * The benchmark was taken from the repo: 8 October 2024
 */
/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * Symbolic Pathfinder (jpf-symbc) is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {
  public static void runSymbolic(double y) {
    if (y > runConcrete(y)) {
      System.out.println("greater than the input ");
      assert false;
    } else {
      System.out.println("less than the input");
    }
  }

  public static double runConcrete(double z) {
    if (z == 10) {
      return z / 1.2;
    }
    return z * 1.2;
  }

  public static void main(String[] args) {
    double x = Verifier.nondetDouble();
    Verifier.assume(5.0 < x && x < 10.0);
    runSymbolic(x);
  }
}
