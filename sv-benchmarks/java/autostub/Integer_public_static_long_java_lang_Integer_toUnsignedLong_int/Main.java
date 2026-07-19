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
    Long output_1 = Integer.toUnsignedLong(input_1_0);
    Long output_2 = Integer.toUnsignedLong(input_2_0);

    // Assert the expected outputs
    assert !(output_1 == 3255132770L && output_2 == 1313L); // desired values
  }
}
