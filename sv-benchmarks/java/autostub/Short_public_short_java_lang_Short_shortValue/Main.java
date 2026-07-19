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
    Short input_1_0 = Verifier.nondetShort();
    Short input_2_0 = Verifier.nondetShort();

    // Perform computation
    Short output_1 = input_1_0.shortValue();
    Short output_2 = input_2_0.shortValue();

    // Assert the expected outputs
    assert !(output_1 == -44 && output_2 == -420); // desired values
  }
}
