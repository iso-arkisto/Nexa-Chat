import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yourname.chat.R

@Composable
fun ConfirmationDialog(
    title: String = "",
    desc: String = "",
    onlyOK: Boolean = false,
    agreeLabel: String = "OK",
    onCancel: () -> Unit,
    onAgree: () -> Unit = {},
    textFieldValue: String = "",
    onAgreeWithText: (String) -> Unit = {},
    isDangerous: Boolean = false
) {
    var text by remember { mutableStateOf(textFieldValue) }

    AlertDialog(
        onDismissRequest = { onCancel() },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        title = {
            if(title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if(desc.isNotBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if(textFieldValue.isNotBlank()) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            text = it
                        },
                        placeholder = { Text(text = "${stringResource(R.string.enter_text)}...", color = Color.Gray) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(textFieldValue.isNotBlank()) {
                        onAgreeWithText(text)
                    } else {
                        onAgree()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if(isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(agreeLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if(!onlyOK) {
                TextButton(
                    onClick = { onCancel() }
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun DialogPreview() {
    ConfirmationDialog(
        title = "Edit message?",
        onCancel = {},
        onAgree = {},
        textFieldValue = "Hello, world!",
        agreeLabel = "Edit"
    )
}