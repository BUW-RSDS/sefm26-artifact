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
    Character input_1_0 = Verifier.nondetChar();
    Character input_2_0 = Verifier.nondetChar();

    // Perform computation
    Byte output_1 = Character.getDirectionality(input_1_0);
    Byte output_2 = Character.getDirectionality(input_2_0);

    // Assert the expected outputs
    assert !(output_1 == 13 && output_2 == 0); // desired values
  }
}
