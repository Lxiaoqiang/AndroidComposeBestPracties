package com.example.composemvibestpracties.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.ranges.until
import kotlin.text.all
import kotlin.text.isDigit


@Composable
fun BaseVerificationCodeTextField(
    modifier: Modifier = Modifier,
    codeLength: Int = 6,
    onVerify: (String) -> Unit = {},
    codeBox: @Composable RowScope.(codeLength: Int, index: Int, code: String) -> Unit
) {

    var text by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    val focusRequester = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    BasicTextField(
        value = text,
        singleLine = true,
        onValueChange = { newText ->
            if (newText.length <= codeLength && newText.all { it.isDigit() }) {
                text = newText

                if (newText.length == codeLength) {
                    onVerify(newText)
                    focusManager.clearFocus()
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        modifier = modifier
            .padding(horizontal = 26.dp)
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused) {
                    keyboardController?.show()
                }
            }
            .wrapContentHeight(),
        readOnly = false,
        decorationBox = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (i in 0 until codeLength) {
                    codeBox(codeLength, i, text)
                }
            }
        }
    )
}