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
    Byte input_1_0 = Verifier.nondetByte();
    Byte input_2_0 = Verifier.nondetByte();

    // Perform computation
    String output_1 = input_1_0.toString();
    String output_2 = input_2_0.toString();

    // Assert the expected outputs
    assert !(output_1.equals("-13") && output_2.equals("-61")); // desired values
  }
}
