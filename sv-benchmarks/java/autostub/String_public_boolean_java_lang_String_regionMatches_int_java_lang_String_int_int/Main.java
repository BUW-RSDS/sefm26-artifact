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
    String input_1_2 = Verifier.nondetString();
    Integer input_1_3 = Verifier.nondetInt();
    Integer input_1_4 = Verifier.nondetInt();
    String input_2_0 = Verifier.nondetString();
    Integer input_2_1 = Verifier.nondetInt();
    String input_2_2 = Verifier.nondetString();
    Integer input_2_3 = Verifier.nondetInt();
    Integer input_2_4 = Verifier.nondetInt();

    // Perform computation
    Boolean output_1 = input_1_0.regionMatches(input_1_1, input_1_2, input_1_3, input_1_4);
    Boolean output_2 = input_2_0.regionMatches(input_2_1, input_2_2, input_2_3, input_2_4);

    // Assert the expected outputs
    assert !(output_1 == false && output_2 == true); // desired values
  }
}
