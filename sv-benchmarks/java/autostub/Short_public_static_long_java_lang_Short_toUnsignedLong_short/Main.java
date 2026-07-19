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
    Long output_1 = Short.toUnsignedLong(input_1_0);
    Long output_2 = Short.toUnsignedLong(input_2_0);

    // Assert the expected outputs
    assert !(output_1 == 23L && output_2 == 65517L); // desired values
  }
}
