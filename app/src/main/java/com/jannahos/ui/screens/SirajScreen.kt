package com.jannahos.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.provider.Settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import kotlinx.coroutines.withContext

private data class SirajMessage(
    val text: String,
    val fromUser: Boolean
)

@Composable
fun SirajScreen(onNavigate: (String) -> Unit = {}) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var pendingDeviceAction by remember { mutableStateOf<SirajDeviceAction?>(null) }
    var pendingActionPlan by remember { mutableStateOf<SirajActionPlan?>(null) }
    var pendingActionPrompt by remember { mutableStateOf("") }
    var pendingPermissionAction by remember { mutableStateOf<SirajDeviceAction?>(null) }
    var pendingPermissionReason by remember { mutableStateOf("") }
    var showHistoryClearConfirmation by remember { mutableStateOf(false) }
    val messages = remember {
        mutableStateListOf(
            SirajMessage(
                "I can open a Surah, show prayer times, find the Qibla, start Quran search, or take you to adhkar, notes and settings. I won't invent religious answers or claim a source I haven't checked.",
                false
            )
        )
    }

    fun submit(raw: String) {
        val request = raw.trim()
        if (request.isBlank()) return
        messages.add(SirajMessage(request, true))
        input = ""

        if (request.lowercase() in setOf("clear action history", "delete action history")) {
            showHistoryClearConfirmation = true
            return
        }

        if (request.lowercase() in setOf(
                "action history", "show action history", "recent actions", "show recent actions"
            )
        ) {
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

        val requestedDeviceAction = parseDeviceAction(request)
        if (requestedDeviceAction != null) {
            val (deviceAction, prompt) = requestedDeviceAction
            val plan = SirajActionPlanner.prepare(
                action = deviceAction,
                summary = prompt,
                capabilities = AndroidDeviceActionExecutor.readCapabilities(context),
                nowMillis = System.currentTimeMillis()
            )
            when (plan.state) {
                SirajActionPlan.State.BLOCKED -> {
                    pendingPermissionAction = deviceAction
                    pendingActionPrompt = prompt
                    pendingPermissionReason = plan.blockedReason.orEmpty()
                }
                SirajActionPlan.State.AWAITING_CONFIRMATION -> {
                    pendingActionPlan = plan
                    pendingDeviceAction = deviceAction
                    pendingActionPrompt = prompt
                }
                else -> Unit
            }
            scope.launch {
                if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
            }
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

            is com.jannahos.siraj.core.SirajRoute.IslamicKnowledgeUnavailable ->
                messages.add(
                    SirajMessage(
                        "I won't generate a religious answer from a general-purpose model. SIRAJ's verified Islamic research workflow is not ready yet, so I won't guess or invent a source. I can still retrieve exact Arabic Quran references from the bundled text.",
                        false
                    )
                )

            is com.jannahos.siraj.core.SirajRoute.GeneralConversation -> {
                messages.add(SirajMessage("Thinking…", false))
                val responseIndex = messages.lastIndex
                scope.launch {
                    val conversation = messages
                        .filterNot { it.text == "Thinking…" }
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
                        ) + SirajConversationContext.build(conversation, route.prompt)
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
                        pendingDeviceAction = null
                        pendingActionPlan = null
                        pendingActionPrompt = ""
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
                        messages.add(SirajMessage("No action was taken.", false))
                        pendingDeviceAction = null
                        pendingActionPrompt = ""
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
            "Voice recognition is provided by your Android speech service.",
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
