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
    String output_1 = Float.toHexString(input_1_0);
    String output_2 = Float.toHexString(input_2_0);

    // Assert the expected outputs
    assert !(output_1.equals("-0x1.6347ep18")
        && output_2.equals("0x1.36f7bp-63")); // desired values
  }
}
