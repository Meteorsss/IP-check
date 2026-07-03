package com.ipcheck.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ipcheck.app.ui.glassSurface
import com.ipcheck.app.ui.theme.IosBlue
import com.ipcheck.app.ui.theme.TextPrimary
import com.ipcheck.app.ui.theme.TextSecondary
import com.ipcheck.app.ui.theme.TextTertiary

/** ipdata.co 的 API Key 设置弹窗，液态玻璃风格。 */
@Composable
fun ApiKeyDialog(
    current: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(current) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier
                .fillMaxWidth()
                .then(glassSurface(cornerRadius = 28.dp, strong = true))
                .padding(22.dp)
        ) {
            Column {
                Text("设置", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "ipdata.co 需要 API Key。可前往 ipdata.co 免费注册获取；留空则跳过该数据源。",
                    color = TextSecondary, fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text("ipdata.co API Key", color = TextTertiary) },
                    keyboardOptions = KeyboardOptions.Default,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = Color.White.copy(alpha = 0.06f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                        focusedIndicatorColor = IosBlue,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.25f),
                        cursorColor = IosBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth()) {
                    DialogButton("取消", filled = false, modifier = Modifier.weight(1f)) { onDismiss() }
                    Spacer(Modifier.width(12.dp))
                    DialogButton("保存", filled = true, modifier = Modifier.weight(1f)) { onSave(text.trim()) }
                }
            }
        }
    }
}

@Composable
private fun DialogButton(
    label: String,
    filled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .height(48.dp)
            .then(
                if (filled) Modifier.background(IosBlue, shape)
                else glassSurface(cornerRadius = 16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}
