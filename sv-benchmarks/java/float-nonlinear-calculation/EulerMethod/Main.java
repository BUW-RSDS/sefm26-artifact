/*
 * Origin of the benchmark:
 *     repo: https://github.com/elizabethzhenliu/ScientificComputation
 *     branch: master
 *     root directory: .
 * The benchmark was taken from the repo: 8 October 2024
 */
// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2024 The SV-Benchmarks Community
//
// SPDX-License-Identifier: Apache-2.0

import org.sosy_lab.sv_benchmarks.Verifier;

/*
 * Uses Euler's method to solve ODE
 * with given initial value
 */
public class Main {

  public static double myDeriv(double function) {
    double derivative;
    // the function
    derivative = -9.8 - .002 * (Math.pow(function, 2) / .11);
    // derivative = .1*.02*(1-function);
    return derivative;
  }

  public static void euler(double y) {
    double step = .1;
    double n = 10;
    for (int i = 0; i < n; i++) {
      y = y + step * myDeriv(y);
      System.out.println(y);
    }
    if (y > myDeriv(y)) assert false;
  }

  public static void main(String[] args) {
    double d = Verifier.nondetDouble();
    euler(d);
  }
}
