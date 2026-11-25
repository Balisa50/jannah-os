package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.jannahos.siraj.ai.SirajProviderSettings
import com.jannahos.ui.theme.JannahColors

@Composable
fun SirajAiSettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val saved = remember { SirajProviderSettings.load(context) }
    var endpoint by remember { mutableStateOf(saved?.endpoint ?: "https://openrouter.ai/api/v1") }
    var model by remember { mutableStateOf(saved?.model ?: "qwen/qwen3-4b:free") }
    var apiKey by remember { mutableStateOf("") }
    var storedKey by remember { mutableStateOf(saved?.apiKey) }
    var message by remember { mutableStateOf("") }
    var hasSavedKey by remember { mutableStateOf(!saved?.apiKey.isNullOrBlank()) }

    Column(
        Modifier.fillMaxSize()
            .background(JannahColors.Cream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back", color = JannahColors.OrangeDeep) }
        Spacer(Modifier.height(12.dp))
        Text(
            "SIRAJ intelligence",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = JannahColors.Ink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Connect an OpenAI-compatible provider. This is optional: Quran navigation and other offline features continue to work without an AI connection.",
            color = JannahColors.Body
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Provider endpoint") },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Model ID") },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (hasSavedKey) "Replace API key (leave blank to keep current)" else "API key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (hasSavedKey) "A credential is stored encrypted on this device. It is not displayed here."
            else "Your key is stored encrypted using Android Keystore.",
            color = JannahColors.Muted,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                runCatching {
                    val keyToSave = apiKey.takeIf { it.isNotBlank() } ?: storedKey
                    SirajProviderSettings.save(context, endpoint, model, keyToSave)
                    storedKey = keyToSave
                    hasSavedKey = !keyToSave.isNullOrBlank()
                    apiKey = ""
                    message = "Provider settings saved on this device."
                }.onFailure { error ->
                    message = error.message ?: "Settings could not be saved."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save provider")
        }
        TextButton(
            onClick = {
                SirajProviderSettings.clear(context)
                endpoint = "https://openrouter.ai/api/v1"
                model = "qwen/qwen3-4b:free"
                apiKey = ""
                storedKey = null
                hasSavedKey = false
                message = "Provider credentials and settings cleared."
            }
        ) {
            Text("Remove provider and credential", color = JannahColors.OrangeDeep)
        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(message, color = JannahColors.Body)
        }
        Spacer(Modifier.height(28.dp))
        Text(
            "Privacy note: when connected, messages sent to the provider leave your device and are subject to that provider's policies. Do not send private notes or sensitive personal information unless you are comfortable with the provider receiving them. No AI request is made until the assistant feature is explicitly invoked.",
            color = JannahColors.Muted,
            fontSize = 13.sp
        )
    }
}
