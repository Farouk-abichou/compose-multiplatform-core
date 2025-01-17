/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.compose.foundation.copyPasteAndroidTests.textfield

import androidx.compose.foundation.assertThat
import androidx.compose.foundation.isEqualTo
import androidx.compose.foundation.isFalse
import androidx.compose.foundation.isTrue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.CoreTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TextFieldFocusTest {

    @Composable
    private fun TextFieldApp(dataList: List<FocusTestData>) {
        for (data in dataList) {
            val editor = remember { mutableStateOf("") }
            BasicTextField(
                value = editor.value,
                modifier = Modifier
                    .focusRequester(data.focusRequester)
                    .onFocusChanged { data.focused = it.isFocused }
                    .requiredWidth(10.dp),
                onValueChange = {
                    editor.value = it
                }
            )
        }
    }

    data class FocusTestData(val focusRequester: FocusRequester, var focused: Boolean = false)

    @Test
    fun requestFocus() = runSkikoComposeUiTest {
        lateinit var testDataList: List<FocusTestData>

        setContent {
            testDataList = listOf(
                FocusTestData(FocusRequester()),
                FocusTestData(FocusRequester()),
                FocusTestData(FocusRequester())
            )

            TextFieldApp(testDataList)
        }

        runOnIdle { testDataList[0].focusRequester.requestFocus() }

        runOnIdle {
            assertThat(testDataList[0].focused).isTrue()
            assertThat(testDataList[1].focused).isFalse()
            assertThat(testDataList[2].focused).isFalse()
        }

        runOnIdle { testDataList[1].focusRequester.requestFocus() }
        runOnIdle {
            assertThat(testDataList[0].focused).isFalse()
            assertThat(testDataList[1].focused).isTrue()
            assertThat(testDataList[2].focused).isFalse()
        }

        runOnIdle { testDataList[2].focusRequester.requestFocus() }
        runOnIdle {
            assertThat(testDataList[0].focused).isFalse()
            assertThat(testDataList[1].focused).isFalse()
            assertThat(testDataList[2].focused).isTrue()
        }
    }

    @Test
    fun noCrushWhenSwitchingBetweenEnabledFocusedAndDisabledTextField() = runSkikoComposeUiTest {
        val enabled = mutableStateOf(true)
        var focused = false
        val tag = "textField"
        setContent {
            CoreTextField(
                value = TextFieldValue(),
                onValueChange = {},
                enabled = enabled.value,
                modifier = Modifier.testTag(tag).onFocusChanged {
                    focused = it.isFocused
                }
            )
        }
        // bring enabled text field into focus
        onNodeWithTag(tag)
            .performClick()
        runOnIdle {
            assertThat(focused).isTrue()
        }

        // make text field disabled
        enabled.value = false
        runOnIdle {
            assertThat(focused).isFalse()
        }

        // make text field enabled again, it must not crash
        enabled.value = true
        runOnIdle {
            assertThat(focused).isFalse()
        }
    }

    @Test
    fun wholeDecorationBox_isBroughtIntoView_whenFocused() = runSkikoComposeUiTest {
        var outerCoordinates: LayoutCoordinates? = null
        var innerCoordinates: LayoutCoordinates? = null
        val focusRequester = FocusRequester()
        setContent {
            Column(
                Modifier
                    .height(100.dp)
                    .onPlaced { outerCoordinates = it }
                    .verticalScroll(rememberScrollState())
            ) {
                // Place the text field way out of the viewport.
                Spacer(Modifier.height(10000.dp))
                CoreTextField(
                    value = TextFieldValue(),
                    onValueChange = {},
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onPlaced { innerCoordinates = it },
                    decorationBox = { innerTextField ->
                        Box(Modifier.padding(20.dp)) {
                            innerTextField()
                        }
                    }
                )
            }
        }

        runOnIdle {
            // Text field should start completely clipped.
            assertThat(
                outerCoordinates!!.localBoundingBoxOf(
                    innerCoordinates!!,
                    clipBounds = true
                ).size
            ).isEqualTo(Size.Zero)

            focusRequester.requestFocus()
        }

        runOnIdle {
            // Text field should be completely visible.
            assertThat(
                outerCoordinates!!.localBoundingBoxOf(
                    innerCoordinates!!,
                    clipBounds = true
                ).size
            ).isEqualTo(innerCoordinates!!.size.toSize())
        }
    }
}
