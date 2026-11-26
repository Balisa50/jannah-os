package com.jannahos.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.provider.Settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QiblaActivity
import com.jannahos.QuranCache
import com.jannahos.QuranStore
import com.jannahos.SettingsActivity
import com.jannahos.siraj.actions.ActionExecutionResult
import com.jannahos.siraj.actions.AndroidDeviceActionExecutor
import com.jannahos.siraj.actions.DeviceActionPolicy
import com.jannahos.siraj.actions.SirajDeviceAction
import com.jannahos.siraj.actions.SirajActionPlan
import com.jannahos.siraj.actions.SirajActionPlanner
import com.jannahos.siraj.actions.ActionApproval
import com.jannahos.siraj.actions.SirajActionHistoryStore
import com.jannahos.siraj.memory.SirajMemoryCommandParser
import com.jannahos.siraj.memory.SirajMemoryStore
import com.jannahos.siraj.media.SirajImageTextExtractor
import com.jannahos.siraj.media.SirajImageContentPolicy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.jannahos.siraj.ai.OpenAiCompatibleProvider
import com.jannahos.siraj.ai.SirajChatMessage
import com.jannahos.siraj.ai.SirajInferenceResult
import com.jannahos.siraj.ai.SirajConversationPolicy
import com.jannahos.siraj.ai.SirajConversationContext
import com.jannahos.siraj.core.SirajAction
import com.jannahos.siraj.knowledge.IslamicReference
import com.jannahos.siraj.knowledge.QuranEvidenceRetriever
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

private data class SirajMessage(
    val text: String,
    val fromUser: Boolean,
    val localOnly: Boolean = false
)

@Composable
fun SirajScreen(onNavigate: (String) -> Unit = {}) {
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    val textToSpeech = remember(context) {
        TextToSpeech(context.applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }
    DisposableEffect(textToSpeech) {
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var pendingDeviceAction by remember { mutableStateOf<SirajDeviceAction?>(null) }
    var pendingActionPlan by remember { mutableStateOf<SirajActionPlan?>(null) }
    var pendingActionPrompt by remember { mutableStateOf("") }
    var pendingPermissionAction by remember { mutableStateOf<SirajDeviceAction?>(null) }
    var pendingPermissionReason by remember { mutableStateOf("") }
    var pendingActionQueue by remember { mutableStateOf<List<Pair<SirajDeviceAction, String>>>(emptyList()) }
    var showHistoryClearConfirmation by remember { mutableStateOf(false) }
    var pendingMemoryContent by remember { mutableStateOf<String?>(null) }
    var showMemorySaveConfirmation by remember { mutableStateOf(false) }
    var showMemoryClearConfirmation by remember { mutableStateOf(false) }
    var pendingMemoryDeleteId by remember { mutableStateOf<Long?>(null) }
    var pendingMemoryDeleteContent by remember { mutableStateOf("") }
    var pendingImageText by remember { mutableStateOf<String?>(null) }
    var imageTextConsentGranted by remember { mutableStateOf(false) }
    var showImageTextConsent by remember { mutableStateOf(false) }
    val messages = remember {
        mutableStateListOf(
            SirajMessage(
                "I can open a Surah, show prayer times, find the Qibla, read text from images on-device, take supported device actions, and keep private memories here. I won't invent religious answers or claim a source I haven't checked.",
                false
            )
        )
    }

    fun speakLatestResponse() {
        val latest = messages.lastOrNull { !it.fromUser }?.text
            ?.take(4000)
            ?.takeIf { it.isNotBlank() }
            ?: return
        val hasArabic = latest.any {
            Character.UnicodeScript.of(it.code) == Character.UnicodeScript.ARABIC
        }
        val locale = if (hasArabic) Locale.forLanguageTag("ar") else Locale.getDefault()
        val offlineVoice = textToSpeech.voices.orEmpty().firstOrNull { voice ->
            voice.locale.language == locale.language && !voice.isNetworkConnectionRequired
        }
        if (offlineVoice == null) {
            messages.add(
                SirajMessage(
                    "No offline voice is installed for this response language. Install offline voice data in Android settings; SIRAJ will not switch to a network voice.",
                    false
                )
            )
            return
        }
        textToSpeech.voice = offlineVoice
        textToSpeech.speak(
            latest,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "siraj-last-response"
        )
    }

    fun markLastUserMessageLocalOnly() {
        val index = messages.lastIndex
        if (index >= 0 && messages[index].fromUser) {
            messages[index] = messages[index].copy(localOnly = true)
        }
    }

    fun prepareDeviceAction(action: SirajDeviceAction, prompt: String) {
        val plan = SirajActionPlanner.prepare(
            action = action,
            summary = prompt,
            capabilities = AndroidDeviceActionExecutor.readCapabilities(context),
            nowMillis = System.currentTimeMillis()
        )
        when (plan.state) {
            SirajActionPlan.State.BLOCKED -> {
                pendingPermissionAction = action
                pendingActionPrompt = prompt
                pendingPermissionReason = plan.blockedReason.orEmpty()
            }
            SirajActionPlan.State.AWAITING_CONFIRMATION -> {
                pendingActionPlan = plan
                pendingDeviceAction = action
                pendingActionPrompt = prompt
            }
            else -> Unit
        }
        scope.launch {
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
        }
    }

    fun submit(raw: String) {
        val request = raw.trim()
        if (request.isBlank()) return
        messages.add(SirajMessage(request, true))
        input = ""

        val rememberContent = SirajMemoryCommandParser.rememberContent(request)
        if (rememberContent != null) {
            markLastUserMessageLocalOnly()
            pendingMemoryContent = rememberContent
            showMemorySaveConfirmation = true
            return
        }
        if (request.trim().lowercase().startsWith("remember:")) {
            markLastUserMessageLocalOnly()
            messages.add(SirajMessage("Add the text after 'remember:' so I know exactly what to save.", false))
            return
        }
        if (request.trim().lowercase().startsWith("remember ")) {
            markLastUserMessageLocalOnly()
            messages.add(
                SirajMessage(
                    "I won't guess what to save. Use 'remember: ...' or 'remember that ...' to propose a private memory.",
                    false
                )
            )
            return
        }
        if (SirajMemoryCommandParser.isShowMemoryRequest(request)) {
            markLastUserMessageLocalOnly()
            val entries = runCatching { SirajMemoryStore(context).recent(20) }
                .getOrElse { emptyList() }
            val reply = if (entries.isEmpty()) {
                "Your private SIRAJ memory is empty."
            } else {
                "Your private memory (stored encrypted on this device):\n" +
                    entries.joinToString("\n") { "• ${it.content}" }
            }
            messages.add(SirajMessage(reply, false))
            return
        }
        if (SirajMemoryCommandParser.isClearMemoryRequest(request)) {
            markLastUserMessageLocalOnly()
            showMemoryClearConfirmation = true
            return
        }
        val forgetQuery = SirajMemoryCommandParser.forgetQuery(request)
        if (request.trim().lowercase().startsWith("forget:") && forgetQuery == null) {
            markLastUserMessageLocalOnly()
            messages.add(SirajMessage("Add a specific phrase after 'forget:' so I know which memory to find.", false))
            return
        }
        if (forgetQuery != null) {
            markLastUserMessageLocalOnly()
            val matches = runCatching { SirajMemoryStore(context).search(forgetQuery) }
                .getOrElse { emptyList() }
            when (matches.size) {
                0 -> messages.add(SirajMessage("I couldn't find a saved memory matching that phrase.", false))
                1 -> {
                    pendingMemoryDeleteId = matches.first().id
                    pendingMemoryDeleteContent = matches.first().content
                }
                else -> messages.add(
                    SirajMessage(
                        "More than one memory matches. Use a more specific 'forget: ...' phrase so I don't delete the wrong one.",
                        false
                    )
                )
            }
            return
        }

        if (request.lowercase() in setOf("clear action history", "delete action history")) {
            markLastUserMessageLocalOnly()
            showHistoryClearConfirmation = true
            return
        }

        if (request.lowercase() in setOf(
                "action history", "show action history", "recent actions", "show recent actions"
            )
        ) {
            markLastUserMessageLocalOnly()
            val history = runCatching { SirajActionHistoryStore(context).recent(8) }
                .getOrElse { emptyList() }
            val reply = if (history.isEmpty()) {
                "No SIRAJ device actions have been recorded on this device yet."
            } else {
                val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                "Recent device actions:\n" + history.joinToString("\n") { entry ->
                    "${formatter.format(Date(entry.createdAt))} · ${entry.summary} — ${entry.status}"
                }
            }
            messages.add(SirajMessage(reply, false))
            scope.launch {
                if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
            }
            return
        }

        val actionSegments = request.split(Regex("\\s+then\\s+", RegexOption.IGNORE_CASE))
        if (actionSegments.size > 1 && actionSegments.any { parseDeviceAction(it) != null }) {
            markLastUserMessageLocalOnly()
            val sequence = parseDeviceActionSequence(request)
            if (sequence == null) {
                pendingActionQueue = emptyList()
                messages.add(
                    SirajMessage(
                        "I couldn't safely parse every step in that action sequence. No device action was taken. Separate supported actions with the word 'then'.",
                        false
                    )
                )
                return
            }
            pendingActionQueue = sequence.drop(1)
            val first = sequence.first()
            prepareDeviceAction(first.first, first.second)
            return
        }

        val requestedDeviceAction = parseDeviceAction(request)
        if (requestedDeviceAction != null) {
            markLastUserMessageLocalOnly()
            pendingActionQueue = emptyList()
            prepareDeviceAction(requestedDeviceAction.first, requestedDeviceAction.second)
            return
        }

        val route = com.jannahos.siraj.core.SirajOrchestrator.route(
            request,
            QuranCache.SURAHS
        )
        when (route) {
            is com.jannahos.siraj.core.SirajRoute.AppAction ->
                messages.add(SirajMessage(route.userMessage, false))

            is com.jannahos.siraj.core.SirajRoute.LocalReply ->
                messages.add(SirajMessage(route.message, false))

            is com.jannahos.siraj.core.SirajRoute.IslamicKnowledgeUnavailable -> {
                pendingImageText = null
                imageTextConsentGranted = false
                messages.add(
                    SirajMessage(
                        "I won't generate a religious answer from a general-purpose model. SIRAJ's verified Islamic research workflow is not ready yet, so I won't guess or invent a source. I can still retrieve exact Arabic Quran references from the bundled text.",
                        false
                    )
                )
            }

            is com.jannahos.siraj.core.SirajRoute.GeneralConversation -> {
                val imageText = pendingImageText.takeIf { imageTextConsentGranted }
                if (
                    imageText != null &&
                    (SirajImageContentPolicy.requiresIslamicReview(imageText) ||
                        SirajConversationPolicy.isReligiousRequest("${route.prompt} ${imageText}"))
                ) {
                    messages.add(
                        SirajMessage(
                            "This image text contains Arabic or Islamic references. I won't send it to a general-purpose AI. SIRAJ's verified Islamic research workflow is not ready yet.",
                            false,
                            localOnly = true
                        )
                    )
                    pendingImageText = null
                    imageTextConsentGranted = false
                    return
                }
                val providerPrompt = if (imageText != null) {
                    "${route.prompt}\n\nText extracted on-device from your selected image (OCR; not independently verified):\n$imageText"
                } else route.prompt
                if (imageText != null) {
                    pendingImageText = null
                    imageTextConsentGranted = false
                }
                messages.add(SirajMessage("Thinking…", false))
                val responseIndex = messages.lastIndex
                scope.launch {
                    val conversation = messages
                        .filterNot { it.text == "Thinking…" || it.localOnly }
                        .dropLast(1)
                        .map { message ->
                            SirajChatMessage(
                                role = if (message.fromUser) "user" else "assistant",
                                content = message.text
                            )
                        }
                    val response = OpenAiCompatibleProvider(context).complete(
                        listOf(
                            SirajChatMessage(
                                role = "system",
                                content = SirajConversationPolicy.GENERAL_ASSISTANT_SYSTEM_PROMPT
                            )
                        ) + SirajConversationContext.build(conversation, providerPrompt)
                    )
                    val reply = when (response) {
                        is SirajInferenceResult.Success ->
                            if (SirajConversationPolicy.isReligiousRequest(response.text)) {
                                "I blocked that response because it appears to make a religious claim. SIRAJ's verified Islamic research workflow is not ready yet, so I won't present it as guidance."
                            } else response.text
                        is SirajInferenceResult.Failure -> response.message
                    }
                    if (responseIndex in messages.indices) {
                        messages[responseIndex] = SirajMessage(reply, false)
                    }
                    if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
                }
            }
        }

        when (val action = (route as? com.jannahos.siraj.core.SirajRoute.AppAction)?.action) {
            is SirajAction.ShowAyah -> {
                scope.launch {
                    val evidence = withContext(Dispatchers.IO) {
                        QuranEvidenceRetriever.getAyah(context, action.surah, action.ayah)
                    }
                    val reference = evidence?.reference as? IslamicReference.Quran
                    val response = if (evidence != null && reference != null) {
                        "${evidence.text}\n\nSurah ${action.surahName} · Ayah ${reference.ayah}\nArabic text from the bundled Quran asset. Its edition/provenance is not yet recorded, and no English translation is shown because its translator and edition are unidentified."
                    } else {
                        "I couldn't verify that ayah in the bundled Quran text. Check the Surah and ayah number; I haven't substituted a nearby verse."
                    }
                    messages.add(SirajMessage(response, false))
                    if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
                }
            }
            is SirajAction.OpenSurah ->
                onNavigate("mushaf/${QuranStore.firstPageOfSurah(action.number)}/604")
            SirajAction.OpenQuran -> onNavigate("quran")
            SirajAction.OpenPrayerTimes -> onNavigate("pray")
            SirajAction.OpenQibla ->
                context.startActivity(Intent(context, QiblaActivity::class.java))
            SirajAction.OpenQuranSearch -> onNavigate("search")
            SirajAction.OpenAdhkar -> onNavigate("adhkar")
            SirajAction.OpenNotes -> onNavigate("notes")
            SirajAction.OpenSettings ->
                context.startActivity(Intent(context, SettingsActivity::class.java))
            null -> Unit
        }

        scope.launch {
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingPermissionAction
        if (granted && action != null) {
            val freshCapabilities = AndroidDeviceActionExecutor.readCapabilities(context)
            val decision = DeviceActionPolicy.evaluate(
                action = action,
                capabilities = freshCapabilities,
                userConfirmed = false,
                nowMillis = System.currentTimeMillis()
            )
            if (decision == com.jannahos.siraj.actions.ActionDecision.NeedsConfirmation) {
                pendingDeviceAction = action
                pendingActionPlan = SirajActionPlanner.prepare(
                    action, pendingActionPrompt.ifBlank { "Confirm this action?" },
                    freshCapabilities, System.currentTimeMillis()
                )
                pendingPermissionAction = null
                pendingPermissionReason = ""
            } else if (decision is com.jannahos.siraj.actions.ActionDecision.Blocked) {
                pendingPermissionReason = decision.reason
            }
        } else if (!granted) {
            pendingPermissionReason = "Notification permission was not granted. No action was taken."
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingImageText = null
            imageTextConsentGranted = false
            messages.add(SirajMessage("Reading image on this device…", false, localOnly = true))
            scope.launch {
                val extracted = try {
                    Result.success(SirajImageTextExtractor.extractText(context, uri))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Result.failure(error)
                }
                if (extracted.isFailure) {
                    messages.add(
                        SirajMessage(
                            "I couldn't read that image: ${extracted.exceptionOrNull()?.message ?: "unknown error"}",
                            false,
                            localOnly = true
                        )
                    )
                } else {
                    val text = extracted.getOrDefault("").trim()
                    if (text.isBlank()) {
                        messages.add(
                            SirajMessage("I couldn't find readable Latin-script text. Arabic-script OCR is not supported yet.", false, localOnly = true)
                        )
                    } else {
                        pendingImageText = text
                        val restricted = SirajImageContentPolicy.requiresIslamicReview(text)
                        messages.add(
                            SirajMessage(
                                "Latin-script text extracted on-device (the image was not uploaded):\n${text.take(1400)}${if (text.length > 1400) "…" else ""}",
                                false,
                                localOnly = true
                            )
                        )
                        if (restricted) {
                            messages.add(
                                SirajMessage(
                                    "This text contains Arabic or Islamic references. I can show the extracted text, but I won't send it to a general AI provider.",
                                    false,
                                    localOnly = true
                                )
                            )
                        } else {
                            showImageTextConsent = true
                        }
                    }
                }
                if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
            }
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenRequest = if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
        } else null

        if (!spokenRequest.isNullOrBlank()) {
            submit(spokenRequest)
        }
    }

    fun openPermissionSettings(action: SirajDeviceAction) {
        val capabilities = AndroidDeviceActionExecutor.readCapabilities(context)
        val currentDecision = DeviceActionPolicy.evaluate(
            action = action,
            capabilities = capabilities,
            userConfirmed = false,
            nowMillis = System.currentTimeMillis()
        )
        if (currentDecision == com.jannahos.siraj.actions.ActionDecision.NeedsConfirmation) {
            pendingDeviceAction = action
            pendingActionPlan = SirajActionPlanner.prepare(
                action, pendingActionPrompt.ifBlank { "Confirm this action?" },
                capabilities, System.currentTimeMillis()
            )
            pendingPermissionAction = null
            pendingPermissionReason = ""
            return
        }
        if (currentDecision is com.jannahos.siraj.actions.ActionDecision.Blocked) {
            pendingPermissionReason = currentDecision.reason
        }
        val needsNotificationPermission =
            !capabilities.notificationsAllowed &&
                (action is SirajDeviceAction.ScheduleReminder || action is SirajDeviceAction.SetQuranLock || action is SirajDeviceAction.SetBedtimeLock)

        if (needsNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        val settingsIntent = when {
            action is SirajDeviceAction.SetPrayerLock &&
                !capabilities.accessibilityServiceEnabled ->
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

            action is SirajDeviceAction.SetDoNotDisturb &&
                !capabilities.notificationPolicyAccess ->
                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

            (action is SirajDeviceAction.ScheduleReminder ||
                action is SirajDeviceAction.SetQuranLock ||
                action is SirajDeviceAction.SetBedtimeLock) &&
                !capabilities.exactAlarmsAllowed ->
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }

            else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }

        runCatching { context.startActivity(settingsIntent) }
            .onFailure {
                messages.add(SirajMessage("Android settings could not be opened. Open this app's settings manually and grant the required access.", false))
            }
    }

    fun startVoiceInput() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a request for SIRAJ")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        runCatching { speechLauncher.launch(speechIntent) }
            .onFailure {
                messages.add(
                    SirajMessage(
                        "Voice input isn't available through the speech service on this device. You can still type a request.",
                        false
                    )
                )
            }
    }

    if (showImageTextConsent && pendingImageText != null) {
        AlertDialog(
            onDismissRequest = {
                showImageTextConsent = false
                pendingImageText = null
                imageTextConsentGranted = false
            },
            title = { Text("Use extracted text with AI?") },
            text = {
                Text(
                    "This recognizer reads Latin-script text; Arabic-script OCR is not supported yet. Only extracted text will be sent to your configured AI provider when you ask a question. The original image stays on this device. Continue only if the text is safe to share.",
                    color = JannahColors.Body
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    imageTextConsentGranted = true
                    showImageTextConsent = false
                    messages.add(
                        SirajMessage(
                            "Consent granted. Ask your question; only the extracted text will be sent, not the image.",
                            false,
                            localOnly = true
                        )
                    )
                }) { Text("Allow extracted text", color = JannahColors.OrangeDeep) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageTextConsent = false
                    pendingImageText = null
                    imageTextConsentGranted = false
                    messages.add(
                        SirajMessage("Image text discarded. Nothing was sent to an AI provider.", false, localOnly = true)
                    )
                }) { Text("Discard", color = JannahColors.Muted) }
            },
            containerColor = JannahColors.Surface
        )
    }

    if (showMemorySaveConfirmation && pendingMemoryContent != null) {
        AlertDialog(
            onDismissRequest = {
                showMemorySaveConfirmation = false
                pendingMemoryContent = null
            },
            title = { Text("Save private memory?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${pendingMemoryContent}\n\nThis will be stored encrypted on this device only. It will not be added to AI provider prompts or the current partial backup.",
                    color = JannahColors.Body
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val saved = runCatching {
                        SirajMemoryStore(context).add(pendingMemoryContent.orEmpty())
                    }.getOrDefault(false)
                    messages.add(
                        SirajMessage(
                            if (saved) "Saved to your encrypted private memory on this device."
                            else "I couldn't save that memory. It may already exist or local storage may be unavailable.",
                            false
                        )
                    )
                    showMemorySaveConfirmation = false
                    pendingMemoryContent = null
                }) { Text("Save memory", color = JannahColors.OrangeDeep) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMemorySaveConfirmation = false
                    pendingMemoryContent = null
                }) { Text("Cancel", color = JannahColors.Muted) }
            },
            containerColor = JannahColors.Surface
        )
    }

    if (showMemoryClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showMemoryClearConfirmation = false },
            title = { Text("Clear private memory?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This permanently deletes your encrypted SIRAJ memory entries from this device. It does not change device settings, reminders or action history.",
                    color = JannahColors.Body
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleared = runCatching { SirajMemoryStore(context).clear() }.isSuccess
                    messages.add(
                        SirajMessage(
                            if (cleared) "Private memory cleared."
                            else "I couldn't clear private memory. No device settings were changed.",
                            false
                        )
                    )
                    showMemoryClearConfirmation = false
                }) { Text("Clear memory", color = JannahColors.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showMemoryClearConfirmation = false }) {
                    Text("Cancel", color = JannahColors.Muted)
                }
            },
            containerColor = JannahColors.Surface
        )
    }

    val memoryIdToDelete = pendingMemoryDeleteId
    if (memoryIdToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                pendingMemoryDeleteId = null
                pendingMemoryDeleteContent = ""
            },
            title = { Text("Forget this memory?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = { Text(pendingMemoryDeleteContent, color = JannahColors.Body) },
            confirmButton = {
                TextButton(onClick = {
                    val deleted = runCatching {
                        SirajMemoryStore(context).delete(memoryIdToDelete)
                    }.getOrDefault(false)
                    messages.add(
                        SirajMessage(
                            if (deleted) "That private memory was deleted."
                            else "I couldn't delete that memory.",
                            false
                        )
                    )
                    pendingMemoryDeleteId = null
                    pendingMemoryDeleteContent = ""
                }) { Text("Forget", color = JannahColors.Danger) }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingMemoryDeleteId = null
                    pendingMemoryDeleteContent = ""
                }) { Text("Cancel", color = JannahColors.Muted) }
            },
            containerColor = JannahColors.Surface
        )
    }

    if (showHistoryClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showHistoryClearConfirmation = false },
            title = {
                Text(
                    "Clear action history?",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink
                )
            },
            text = {
                Text(
                    "This permanently clears the encrypted on-device record of SIRAJ actions. It does not change device settings or cancel reminders.",
                    color = JannahColors.Body
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleared = runCatching {
                            SirajActionHistoryStore(context).clear()
                        }.isSuccess
                        messages.add(
                            SirajMessage(
                                if (cleared) "Action history cleared. Device settings and reminders were not changed."
                                else "I couldn't clear the action history. No device settings were changed.",
                                false
                            )
                        )
                        showHistoryClearConfirmation = false
                    }
                ) { Text("Clear history", color = JannahColors.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showHistoryClearConfirmation = false }) {
                    Text("Cancel", color = JannahColors.Muted)
                }
            },
            containerColor = JannahColors.Surface
        )
    }

    val actionToConfirm = pendingDeviceAction
    if (actionToConfirm != null) {
        AlertDialog(
            onDismissRequest = {
                pendingDeviceAction = null
                pendingActionPlan = null
                pendingActionPrompt = ""
                pendingActionQueue = emptyList()
            },
            title = {
                Text(
                    "Confirm action",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink
                )
            },
            text = {
                Text(
                    "$pendingActionPrompt\n\nSIRAJ will only perform this action after your confirmation.",
                    color = JannahColors.Body
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val plan = pendingActionPlan
                        val approval = plan?.let {
                            SirajActionPlanner.approve(
                                plan = it,
                                confirmed = true,
                                freshCapabilities = AndroidDeviceActionExecutor.readCapabilities(context),
                                nowMillis = System.currentTimeMillis()
                            )
                        }
                        val result = when (approval) {
                            is ActionApproval.Approved -> AndroidDeviceActionExecutor.execute(
                                context = context,
                                action = actionToConfirm,
                                userConfirmed = true
                            ).also { execution ->
                                pendingActionPlan = SirajActionPlanner.finish(approval.plan, execution)
                            }
                            is ActionApproval.Blocked -> {
                                pendingActionPlan = approval.plan
                                ActionExecutionResult.Blocked(approval.reason)
                            }
                            is ActionApproval.Cancelled, null ->
                                ActionExecutionResult.Blocked("This action was not approved.")
                        }
                        runCatching {
                            SirajActionHistoryStore(context).record(
                                actionToConfirm, pendingActionPrompt, result
                            )
                        }
                        val response = when (result) {
                            ActionExecutionResult.Completed -> "Done. Android accepted the action."
                            ActionExecutionResult.NeedsConfirmation -> "This action still needs confirmation."
                            is ActionExecutionResult.Blocked -> result.reason
                            is ActionExecutionResult.Failed -> "Android could not complete it: ${result.reason}"
                        }
                        messages.add(SirajMessage(response, false))

                        val nextStep = if (result == ActionExecutionResult.Completed) {
                            pendingActionQueue.firstOrNull()
                        } else null
                        if (nextStep != null) {
                            pendingActionQueue = pendingActionQueue.drop(1)
                            pendingDeviceAction = null
                            pendingActionPlan = null
                            pendingActionPrompt = nextStep.second
                            val nextPlan = SirajActionPlanner.prepare(
                                action = nextStep.first,
                                summary = nextStep.second,
                                capabilities = AndroidDeviceActionExecutor.readCapabilities(context),
                                nowMillis = System.currentTimeMillis()
                            )
                            when (nextPlan.state) {
                                SirajActionPlan.State.AWAITING_CONFIRMATION -> {
                                    pendingDeviceAction = nextStep.first
                                    pendingActionPlan = nextPlan
                                    pendingPermissionAction = null
                                    pendingPermissionReason = ""
                                }
                                SirajActionPlan.State.BLOCKED -> {
                                    pendingPermissionAction = nextStep.first
                                    pendingPermissionReason = nextPlan.blockedReason.orEmpty()
                                }
                                else -> {
                                    pendingActionQueue = emptyList()
                                    pendingActionPrompt = ""
                                }
                            }
                        } else {
                            pendingActionQueue = emptyList()
                            pendingDeviceAction = null
                            pendingActionPlan = null
                            pendingActionPrompt = ""
                        }
                        scope.launch {
                            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
                        }
                    }
                ) {
                    Text("Confirm", color = JannahColors.OrangeDeep)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        runCatching {
                            SirajActionHistoryStore(context).recordCancelled(
                                actionToConfirm, pendingActionPrompt
                            )
                        }
                        messages.add(SirajMessage("No action was taken. Remaining steps were cancelled.", false))
                        pendingDeviceAction = null
                        pendingActionPlan = null
                        pendingActionPrompt = ""
                        pendingActionQueue = emptyList()
                    }
                ) {
                    Text("Cancel", color = JannahColors.Muted)
                }
            },
            containerColor = JannahColors.Surface
        )
    }

    val actionNeedingPermission = pendingPermissionAction
    if (actionNeedingPermission != null) {
        AlertDialog(
            onDismissRequest = {
                pendingPermissionAction = null
                pendingPermissionReason = ""
                pendingActionPrompt = ""
                pendingActionQueue = emptyList()
            },
            title = { Text("Permission needed", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = { Text(pendingPermissionReason, color = JannahColors.Body) },
            confirmButton = {
                TextButton(onClick = { openPermissionSettings(actionNeedingPermission) }) {
                    Text("Open settings", color = JannahColors.OrangeDeep)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingPermissionAction = null
                    pendingPermissionReason = ""
                    pendingActionPrompt = ""
                    pendingActionQueue = emptyList()
                }) {
                    Text("Not now", color = JannahColors.Muted)
                }
            },
            containerColor = JannahColors.Surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
            .padding(horizontal = 22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "SIRAJ",
                    fontFamily = FontFamily.Serif,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    letterSpacing = 1.5.sp
                )
                Text(
                    "سِرَاج",
                    fontSize = 15.sp,
                    color = JannahColors.Orange,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            Text(
                "LOCAL COMMANDS",
                style = JannahType.micro,
                color = JannahColors.OrangeDeep,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(JannahColors.OrangeSoft)
                    .padding(horizontal = 11.dp, vertical = 8.dp)
            )
        }

        Text(
            "What do you need?",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink
        )
        Text(
            "Make a clear request. SIRAJ will take you there.",
            style = JannahType.bodySmall,
            color = JannahColors.Muted,
            modifier = Modifier.padding(top = 7.dp, bottom = 18.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Suggestion("Al-Kahf") { submit("Open Surah Al-Kahf") }
            Suggestion("Prayer times") { submit("Show prayer times") }
            Suggestion("Qibla") { submit("Find Qibla") }
            Suggestion("Image text") {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(JannahColors.Divider)
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(messages) { _, message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
                ) {
                    Text(
                        message.text,
                        modifier = Modifier
                            .widthIn(max = 310.dp)
                            .then(
                                if (message.fromUser) {
                                    Modifier
                                        .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                                        .background(JannahColors.OrangeSoft)
                                        .padding(horizontal = 15.dp, vertical = 12.dp)
                                } else {
                                    Modifier.padding(end = 20.dp, top = 4.dp, bottom = 4.dp)
                                }
                            ),
                        style = JannahType.body,
                        color = if (message.fromUser) JannahColors.Ink else JannahColors.Body,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Text(
            "Speech input uses Android recognition. Read-aloud uses an installed offline voice.",
            style = JannahType.micro,
            color = JannahColors.Muted,
            modifier = Modifier.padding(bottom = 7.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask SIRAJ to open…", color = JannahColors.Muted) },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { submit(input) })
            )
            TextButton(
                onClick = { startVoiceInput() },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, JannahColors.Border, RoundedCornerShape(16.dp))
            ) {
                Text("Speak", color = JannahColors.Ink, fontWeight = FontWeight.Medium)
            }
            TextButton(
                onClick = {
                    if (textToSpeech.isSpeaking) textToSpeech.stop()
                    else speakLatestResponse()
                },
                enabled = ttsReady && messages.any { !it.fromUser },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, JannahColors.Border, RoundedCornerShape(16.dp))
            ) {
                Text("Listen", color = JannahColors.Ink, fontWeight = FontWeight.Medium)
            }
            TextButton(
                onClick = { submit(input) },
                enabled = input.isNotBlank(),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (input.isNotBlank()) JannahColors.Orange else JannahColors.Border)
            ) {
                Text("Go", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Suggestion(label: String, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, JannahColors.Border, RoundedCornerShape(50))
            .background(JannahColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        style = JannahType.caption,
        color = JannahColors.Ink
    )
}

internal fun parseDeviceAction(request: String): Pair<SirajDeviceAction, String>? {
    val normalized = request.trim().lowercase().replace(Regex("\\s+"), " ")

    fun matches(vararg phrases: String) = phrases.any { normalized == it }

    return when {
        matches("enable prayer lock", "turn on prayer lock", "activate prayer lock") ->
            SirajDeviceAction.SetPrayerLock(true) to "Enable Prayer Lock?"

        matches("disable prayer lock", "turn off prayer lock", "stop prayer lock") ->
            SirajDeviceAction.SetPrayerLock(false) to "Disable Prayer Lock?"

        matches("enable quran lock", "enable quran reading lock", "turn on quran lock") ->
            SirajDeviceAction.SetQuranLock(true) to "Enable the Quran reading lock?"

        matches("disable quran lock", "disable quran reading lock", "turn off quran lock") ->
            SirajDeviceAction.SetQuranLock(false) to "Disable the Quran reading lock?"

        matches("enable bedtime lock", "turn on bedtime lock", "activate bedtime lock") ->
            SirajDeviceAction.SetBedtimeLock(true) to "Enable the bedtime lock?"

        matches("disable bedtime lock", "turn off bedtime lock", "stop bedtime lock") ->
            SirajDeviceAction.SetBedtimeLock(false) to "Disable the bedtime lock?"

        matches("enable do not disturb", "turn on do not disturb", "activate do not disturb") ->
            SirajDeviceAction.SetDoNotDisturb(true) to "Turn on Do Not Disturb?"

        matches("disable do not disturb", "turn off do not disturb", "turn off dnd") ->
            SirajDeviceAction.SetDoNotDisturb(false) to "Turn off Do Not Disturb and restore the previous interruption setting?"

        else -> {
            val reminder = Regex("""remind me in (\d{1,4}) minutes? to (.+)""")
                .matchEntire(normalized)
            if (reminder != null) {
                val minutes = reminder.groupValues[1].toIntOrNull()
                val title = reminder.groupValues[2].trim()
                if (minutes != null && minutes in 1..10080 && title.isNotBlank()) {
                    SirajDeviceAction.ScheduleReminder(
                        title = title,
                        triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L
                    ) to "Schedule a reminder in $minutes minute(s): $title?"
                } else null
            } else null
        }
    }
}
internal fun parseDeviceActionSequence(
    request: String
): List<Pair<SirajDeviceAction, String>>? {
    val segments = request.trim().split(
        Regex("\\s+then\\s+", RegexOption.IGNORE_CASE)
    )
    if (segments.size < 2) return null
    val parsed = segments.map { segment -> parseDeviceAction(segment.trim()) }
    if (parsed.any { it == null }) return null
    return parsed.filterNotNull()
}

