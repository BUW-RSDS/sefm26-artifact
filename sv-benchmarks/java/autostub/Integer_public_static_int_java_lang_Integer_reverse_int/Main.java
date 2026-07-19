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
    Integer output_1 = Integer.reverse(input_1_0);
    Integer output_2 = Integer.reverse(input_2_0);

    // Assert the expected outputs
    assert !(output_1 == 1893337375 && output_2 == -1480589312); // desired values
  }
}
