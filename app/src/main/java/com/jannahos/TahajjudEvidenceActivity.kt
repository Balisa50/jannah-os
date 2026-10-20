package com.jannahos

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahTheme

class TahajjudEvidenceActivity : ComponentActivity() {
    private lateinit var cameraLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var speechLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Save photo to internal storage as evidence
                val photo = result.data?.extras?.get("data") as? android.graphics.Bitmap
                photo?.let {
                    val file = java.io.File(filesDir, "tahajjud_evidence_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { out -> it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out) }
                }
                // Launch speech recognition next
                startSpeechRecognition()
            }
        }

        speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val wakeLog = WakeLog(this)
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.joinToString(" ") ?: ""
                val words = listOf("الحمد", "الله", "رب", "العالمين", "الرحمن", "الرحيم", "مالك", "يوم", "الدين", "إياك", "نعبد", "نستعين", "اهدنا", "الصراط", "المستقيم")
                val matchCount = words.count { spokenText.contains(it) }
                if (matchCount >= 4) {
                    wakeLog.recordVerified("prayed", true, spokenText)
                } else {
                    wakeLog.recordVerified("prayed", false, spokenText)
                }
            } else {
                wakeLog.recordVerified("prayed", false, null)
            }
            wakeLog.close()
            finish()
        }

        setContent {
            JannahTheme {
                var step by remember { mutableStateOf(0) } // 0 = camera, 1 = voice
                Column(
                    Modifier.fillMaxSize().background(JannahColors.Cream).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Tahajjud", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    Spacer(Modifier.height(16.dp))
                    Text("Take a photo of your prayer mat to confirm you woke up. Then recite Al-Fatihah.", style = com.jannahos.ui.theme.JannahType.body, color = JannahColors.Muted)
                    Spacer(Modifier.height(32.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(50))
                            .background(JannahColors.Orange)
                            .clickable {
                                if (ContextCompat.checkSelfPermission(this@TahajjudEvidenceActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    cameraLauncher.launch(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))
                                } else {
                                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Open Camera", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Recite Surah Al-Fatihah")
        }
        speechLauncher.launch(intent)
    }
}
