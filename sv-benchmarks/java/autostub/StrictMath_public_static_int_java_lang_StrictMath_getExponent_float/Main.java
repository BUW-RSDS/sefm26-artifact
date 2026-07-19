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
    Float input_1_0 = Verifier.nondetFloat();
    Float input_2_0 = Verifier.nondetFloat();

    // Perform computation
    Integer output_1 = StrictMath.getExponent(input_1_0);
    Integer output_2 = StrictMath.getExponent(input_2_0);

    // Assert the expected outputs
    assert !(output_1 == 30 && output_2 == 128); // desired values
  }
}
