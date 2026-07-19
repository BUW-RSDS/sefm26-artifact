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
    String input_1_0 = Verifier.nondetString();
    Integer input_1_1 = Verifier.nondetInt();
    Integer input_1_2 = Verifier.nondetInt();
    String input_2_0 = Verifier.nondetString();
    Integer input_2_1 = Verifier.nondetInt();
    Integer input_2_2 = Verifier.nondetInt();

    // Perform computation
    Integer output_1 = input_1_0.indexOf(input_1_1, input_1_2);
    Integer output_2 = input_2_0.indexOf(input_2_1, input_2_2);

    // Assert the expected outputs
    assert !(output_1 == -1 && output_2 == 2); // desired values
  }
}
