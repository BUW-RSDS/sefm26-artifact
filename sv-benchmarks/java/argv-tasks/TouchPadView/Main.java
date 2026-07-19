// SPDX-FileCopyrightText: Copyright (c) 2010 Jacek Fedorynski
// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (C) 2025 The ARG-V Project

/*
 * Copyright (c) 2010 Jacek Fedorynski
 *
 * Licensed under the Apache License , Version 2.0 ( the " License ") ;
 * you may not use this file except in compliance with the License .
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing , software
 * distributed under the License is distributed on an " AS IS " BASIS ,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND , either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License .
 */

import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {

  protected static void onSizeChanged(int w, int h, int oldw, int oldh) {
    float mOffset = Verifier.nondetFloat();
    float mRadius = Verifier.nondetFloat();
    float mCy = Verifier.nondetFloat();
    float mCx = Verifier.nondetFloat();
    int mHeight = Verifier.nondetInt();
    int mWidth = Verifier.nondetInt();
    mWidth = w;
    mHeight = h;
    mCx = mWidth / 2;
    mCy = mHeight / 2;
    // added new for assertion
    float widthRadius = 0.9f * mWidth * 0.5f;
    float heightRadius = 0.9f * mHeight * 5f / 12f;
    assert (widthRadius > 0);
    assert (heightRadius > 0);

    if (mHeight >= 1.2f * mWidth) {
      mRadius = widthRadius;
    } else {
      mRadius = heightRadius;
    }

    assert (mRadius == widthRadius || mRadius == heightRadius);

    mOffset = mRadius * 0.2f;
    assert (mOffset > 0);
  }

  public static void main(String[] args) {

    int w = Verifier.nondetInt();
    int h = Verifier.nondetInt();
    int oldw = Verifier.nondetInt();
    int oldh = Verifier.nondetInt();

    if (w > 0 && h > 0 && oldh > 0 && oldw > 0) {
      onSizeChanged(w, h, oldw, oldh);
    }
  }
}
