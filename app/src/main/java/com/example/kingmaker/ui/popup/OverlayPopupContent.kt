package com.example.kingmaker.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kingmaker.R
import com.example.kingmaker.service.NextAction
import com.example.kingmaker.ui.theme.KingmakerTheme

@Composable
fun OverlayPopupContent(
    action: NextAction,
    onSend: () -> Unit,
    onSkip: () -> Unit
) {
    var draftFieldValue by remember(action) {
        mutableStateOf(TextFieldValue(text = action.draftMessage, selection = TextRange(action.draftMessage.length)))
    }
    val textPrimary = colorResource(R.color.popup_text_primary)
    val draftFocusRequester = remember { FocusRequester() }

    KingmakerTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { draftFocusRequester.requestFocus() },
            shape = RoundedCornerShape(24.dp),
            color = colorResource(R.color.popup_card_background),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = action.contactName,
                            color = textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = action.contactTitle,
                            color = colorResource(R.color.popup_text_secondary),
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    text = action.reason,
                    color = textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.popup_draft_label),
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorResource(R.color.popup_draft_background),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            colorResource(R.color.popup_handle),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = draftFieldValue,
                        onValueChange = { draftFieldValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(draftFocusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    draftFieldValue = draftFieldValue.copy(
                                        selection = TextRange(draftFieldValue.text.length)
                                    )
                                }
                            },
                        textStyle = TextStyle(color = textPrimary, fontSize = 15.sp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    Button(
                        onClick = onSkip,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.popup_secondary_button_background),
                            contentColor = textPrimary
                        )
                    ) {
                        Text(text = stringResource(R.string.popup_skip), fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onSend,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.popup_accent),
                            contentColor = colorResource(R.color.white)
                        )
                    ) {
                        Text(text = stringResource(R.string.popup_send), fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun OverlayPopupContentPreview() {
    OverlayPopupContent(
        action = NextAction(
            contactName = "Sarah Chen",
            contactTitle = "VP Product - Intercom",
            reason = "She replied yesterday and asked about your enterprise pilot.",
            draftMessage = "Hey Sarah - yes, happy to share the pilot flow. Want me to send the short version? Hey Sarah - yes, happy to share the pilot flow. Want me to send the short version? Hey Sarah - yes, happy to share the pilot flow. Want me to send the short version? Hey Sarah - yes, happy to share the pilot flow. Want me to send the short version?"
        ),
        onSend = {},
        onSkip = {}
    )
}
