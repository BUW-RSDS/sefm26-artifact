// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2024 The SV-Benchmarks Community
//
// SPDX-License-Identifier: Apache-2.0

import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {
  public static void main(String[] args) {
    // Fetch inputs using Verifier.nondet* methods
    Integer input_1_0 = Verifier.nondetInt();
    Integer input_2_0 = Verifier.nondetInt();

    // Perform computation
    Double output_1 = input_1_0.doubleValue();
    Double output_2 = input_2_0.doubleValue();

    // Assert the expected outputs
    assert !(output_1 == 1975.0 && output_2 == 9.0); // desired values
  }
}
