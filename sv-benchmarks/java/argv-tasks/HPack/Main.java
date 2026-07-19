// SPDX-FileCopyrightText: Copyright (C) Square, Inc.
// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (C) 2025 The ARG-V Project
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {
  public static int readInt(int firstByte, int prefixMask) throws Exception {

    int prefix = firstByte & prefixMask;
    assert (prefix == (firstByte & prefixMask));
    if (prefix < prefixMask) {
      return prefix; // This was a single byte value.
    }
    // This is a multibyte value. Read 7 bits at a time.
    int result = prefixMask;
    int shift = 0;
    while (true) {
      int b = Verifier.nondetInt();
      if ((b & 0x80) != 0) { // Ensure b is in [0..255]
        result += (b & 0x7F) << shift;
        shift += 7;
      } else {
        result += b << shift; // Last byte.
        break;
      }
    }
    assert (shift % 7 == 0);
    return result;
  }

  public static void main(String[] args) {
    int firstByte = Verifier.nondetInt();
    int prefixMask = Verifier.nondetInt();

    try {
      readInt(firstByte, prefixMask);
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
