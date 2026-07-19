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
    Character input_1_1 = Verifier.nondetChar();
    Character input_1_2 = Verifier.nondetChar();
    String input_2_0 = Verifier.nondetString();
    Character input_2_1 = Verifier.nondetChar();
    Character input_2_2 = Verifier.nondetChar();

    // Perform computation
    String output_1 = input_1_0.replace(input_1_1, input_1_2);
    String output_2 = input_2_0.replace(input_2_1, input_2_2);

    // Assert the expected outputs
    assert !(output_1.equals("SIRp~UM") && output_2.equals("")); // desired values
  }
}
