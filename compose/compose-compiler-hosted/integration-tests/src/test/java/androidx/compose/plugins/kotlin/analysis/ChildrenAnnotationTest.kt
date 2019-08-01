/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.plugins.kotlin.analysis

import androidx.compose.plugins.kotlin.AbstractComposeDiagnosticsTest

class ChildrenAnnotationTest : AbstractComposeDiagnosticsTest() {

    fun testReportChildrenOnWrongParameter() {
        doTest("""
            import androidx.compose.*;

            @Composable fun MyWidget(<!CHILDREN_MUST_BE_LAST!>@Children children: ()->Unit<!>, value: Int) {
                System.out.println(""+children+value)
            }
        """)
    }
}