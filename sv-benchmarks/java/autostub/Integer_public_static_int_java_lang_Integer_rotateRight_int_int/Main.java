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
    Integer input_1_1 = Verifier.nondetInt();
    Integer input_2_0 = Verifier.nondetInt();
    Integer input_2_1 = Verifier.nondetInt();

    // Perform computation
    Integer output_1 = Integer.rotateRight(input_1_0, input_1_1);
    Integer output_2 = Integer.rotateRight(input_2_0, input_2_1);

    // Assert the expected outputs
    assert !(output_1 == -1443 && output_2 == 1703936); // desired values
  }
}
