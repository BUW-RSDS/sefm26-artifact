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
    Double input_1_0 = Verifier.nondetDouble();
    Double input_2_0 = Verifier.nondetDouble();

    // Perform computation
    String output_1 = Double.toHexString(input_1_0);
    String output_2 = Double.toHexString(input_2_0);

    // Assert the expected outputs
    assert !(output_1.equals("0x1.a13d791f98698p161")
        && output_2.equals("-0x1.db1308d076f5bp-165")); // desired values
  }
}
