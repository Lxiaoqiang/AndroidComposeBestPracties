package com.example.composemvibestpracties.widgetpage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composemvibestpracties.widget.BaseVerificationCodeTextField


@Composable
fun WidgetPage() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        val codeBoxStyle = CodeBoxStyle(
            enteredColor = Color(0xFF67B05D),
            defaultColor = Color(0xFFE0E0E0),
            textColor = Color.Black,
            codeTextStyle = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
        ValidateCodeWidget(codeBoxStyle) { code ->
            Log.d("WidgetPage", "code: $code")
        }
    }
}


@Composable
fun ValidateCodeWidget(
    codeBoxStyle: CodeBoxStyle,
    onVerify: (String) -> Unit = {},
) {

    BaseVerificationCodeTextField(codeLength = 6, onVerify = onVerify) { codeLength: Int, index: Int, code: String ->
       VerificationCodeBox(codeLength, index, code, codeBoxStyle)
    }
}



// Data class to hold all configurable styles for the verification code box.
data class CodeBoxStyle(
    // Color when the digit is entered
    val enteredColor: Color = Color(0xFF67B05D),
    // Color when the box is empty
    val defaultColor: Color = Color(0xFFE0E0E0),
    // Color of the digit text
    val textColor: Color = Color.Black,
    // Border width for entered boxes
    val enteredBorderWidth: Dp = 2.dp,
    // Border width for empty boxes
    val defaultBorderWidth: Dp = 1.dp,
    // Size of the box
    val boxSize: Dp = 48.dp,
    // TextStyle for the digit
    val codeTextStyle: TextStyle = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    ),
    // Corner shape for the box
    val shape: RoundedCornerShape = RoundedCornerShape(18.dp)
)

@Composable
fun VerificationCodeBox(
    codeLength: Int,
    index: Int,
    code: String,
    style: CodeBoxStyle = CodeBoxStyle()
) {
    // Get the character for the current index, or empty if not yet entered
    val char = when {
        index < code.length -> code[index].toString()
        else -> ""
    }

    // Determine if the box has been entered (for green border)
    val isEntered = char.isNotEmpty()

    // Apply colors and border widths based on the entered state
    val borderColor = if (isEntered) {
        style.enteredColor
    } else {
        style.defaultColor
    }
    val borderWidth = if (isEntered) style.enteredBorderWidth else style.defaultBorderWidth

    // Base modifier for the box
    val boxModifier = Modifier
        .size(style.boxSize)
        .border(borderWidth, borderColor, shape = style.shape)
        .background(Color.White, style.shape)

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        // Display the digit with the configured style
        Text(
            text = char,
            style = style.codeTextStyle.copy(color = style.textColor)
        )
    }

    // Draw the dash separator between the two halves (assuming codeLength = 6)
    if (index == (codeLength / 2) - 1) {
        Text(
            text = "-",
            color = Color.Gray,
            fontSize = 28.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}