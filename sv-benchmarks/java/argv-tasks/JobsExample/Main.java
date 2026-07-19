// SPDX-FileCopyrightText: Copyright (C) 2021 Google LLC
// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (C) 2025 The ARG-V Project

/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// [START cloudrun_jobs_quickstart]

import org.sosy_lab.sv_benchmarks.Verifier;

/** filtered by ARG-V */
class Main {
  // [START cloudrun_jobs_env_vars]
  // These values are provided automatically by the Cloud Run Jobs runtime.
  private static String CLOUD_RUN_TASK_INDEX =
      System.getenv().getOrDefault("CLOUD_RUN_TASK_INDEX", "0");
  // [END cloudrun_jobs_env_vars]

  static void runTask(int sleepTime, float failureRate) throws InterruptedException {
    // Simulate work
    if (sleepTime > 0) {
      Thread.sleep(sleepTime);
    }

    // Simulate errors
    if (failureRate < 0 || failureRate > 1) {
      System.err.println(
          String.format(
              "Invalid FAIL_RATE value: %s. Must be a float between 0 and 1 inclusive.",
              failureRate));
      return;
    }
    if (Verifier.nondetDouble() < failureRate) {
      throw new RuntimeException("Task Failed.");
    }
    System.out.println(String.format("Completed Task #%s", CLOUD_RUN_TASK_INDEX));
  }

  public static void main(String[] args) throws InterruptedException {
    runTask(Verifier.nondetInt(), Verifier.nondetFloat());
  }
}
// [END cloudrun_jobs_quickstart]
