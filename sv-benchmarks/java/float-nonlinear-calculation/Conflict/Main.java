// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2024 The SV-Benchmarks Community
//
// SPDX-License-Identifier: MIT
/*
 * This benchmark task is a modification of the following original Benchmark:
 * Origin of the benchmark:
 * 	   license: MIT (see /java/float-nonlinear-calculation/LICENSE)
 *     repo: https://github.com/osl/concolic-walk
 *     branch: master
 *     root directory: experiments/src/programs/tsafe/
 * The benchmark was taken from the repo: 8 October 2024
 *
 * Following the original license model, modifications are as well licensed under the
 * MIT license.
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

// package tsafe;

// import gov.nasa.jpf.continuity.SymbolicRealVars;
import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {
  private static final double degToRad = Math.PI / 180.0;
  private static final double g = 68443.0;

  public static double snippet(
      double psi1, double vA, double vC, double xC0, double yC0, double psiC, double bank_ang) {
    String PATH = "";
    double dmin = 999;
    double dmst = 2;
    double psiA = psi1 * degToRad;
    int signA = 1;
    int signC = 1;

    if (psiA < 0) {
      PATH += "psiA<0\n";
      signA = -1;
    } else {
      PATH += "psiA>=0\n";
    }
    double rA = Math.pow(vA, 2.0) / Math.tan(bank_ang * degToRad) / g;
    double rC = Math.pow(vC, 2.0) / Math.tan(bank_ang * degToRad) / g;

    double t1 = Math.abs(psiA) * rA / vA;
    double dpsiC = signC * t1 * vC / rC;
    double xA = signA * rA * (1 - Math.cos(psiA));
    double yA = rA * signA * Math.sin(psiA);

    double xC = xC0 + signC * rC * (Math.cos(psiC) - Math.cos(psiC + dpsiC));
    double yC = yC0 - signC * rC * (Math.sin(psiC) - Math.sin(psiC + dpsiC));

    double xd1 = xC - xA;
    double yd1 = yC - yA;

    double d = Math.sqrt(Math.pow(xd1, 2.0) + Math.pow(yd1, 2.0));
    double minsep;

    // min sep in turn
    if (d < dmin) {
      PATH += "d < dmin\n";
      dmin = d;
    } else {
      PATH += "d >= dmin\n";
    }

    if (dmin < dmst) {
      PATH += "dmin < dmst\n";
      minsep = dmin;
    } else {
      PATH += "dmin >= dmst\n";
      minsep = dmst;
    }
    System.out.println(">>> PATH: " + PATH);
    return minsep;
  }

  public static void main(String[] args) {
    double aPsi = Verifier.nondetDouble();
    double aV = Verifier.nondetDouble();
    double c1X = Verifier.nondetDouble();
    double c1Y = Verifier.nondetDouble();
    double c1Psi = Verifier.nondetDouble();
    double c1V = Verifier.nondetDouble();
    double cpA0_1_bankAng = Verifier.nondetDouble();
    Verifier.assume(
        0.0 <= aPsi
            && aPsi < 360.0
            && 0.0 < aV
            && aV < 100.0
            && -100.0 <= c1X
            && c1X < 100.0
            && -100.0 <= c1Y
            && c1Y < 100.0
            && -100.0 <= c1Psi
            && c1Psi < 100.0
            && -100.0 <= c1V
            && c1V < 100.0
            && 0.0 < cpA0_1_bankAng
            && cpA0_1_bankAng < 50.0);
    double minsep_sec_result = snippet(aPsi, aV, c1X, c1Y, c1Psi, c1V, cpA0_1_bankAng);
    assert minsep_sec_result <= 2;
  }
}
