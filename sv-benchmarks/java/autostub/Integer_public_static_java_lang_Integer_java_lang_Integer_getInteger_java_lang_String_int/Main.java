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
    String input_2_0 = Verifier.nondetString();
    Integer input_2_1 = Verifier.nondetInt();

    // Perform computation
    Integer output_1 = Integer.getInteger(input_1_0, input_1_1);
    Integer output_2 = Integer.getInteger(input_2_0, input_2_1);

    // Assert the expected outputs
    assert !(output_1 == -218883751 && output_2 == -77216387); // desired values
  }
}
