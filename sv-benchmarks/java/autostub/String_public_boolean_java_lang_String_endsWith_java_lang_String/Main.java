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
    String input_1_1 = Verifier.nondetString();
    String input_2_0 = Verifier.nondetString();
    String input_2_1 = Verifier.nondetString();

    // Perform computation
    Boolean output_1 = input_1_0.endsWith(input_1_1);
    Boolean output_2 = input_2_0.endsWith(input_2_1);

    // Assert the expected outputs
    assert !(output_1 == false && output_2 == true); // desired values
  }
}
