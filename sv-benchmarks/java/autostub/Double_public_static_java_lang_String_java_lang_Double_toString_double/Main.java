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
    String output_1 = Double.toString(input_1_0);
    String output_2 = Double.toString(input_2_0);

    // Assert the expected outputs
    assert !(output_1.equals("-Infinity")
        && output_2.equals("-1.7873264083464328E15")); // desired values
  }
}
