@file:OptIn(ExperimentalMaterial3Api::class)

package de.motivationcode.app.ui.settings

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.motivationcode.app.data.settings.ReminderSettings
import de.motivationcode.app.reminders.ReminderNotifications
import de.motivationcode.app.ui.components.SystemBarsAppearance
import de.motivationcode.app.ui.theme.TagesmotivationTheme
import de.motivationcode.app.widget.QuoteWidgetReceiver
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Bei jeder Rückkehr in die App neu prüfen (man kann es in den Systemeinstellungen ändern).
    var notificationsAllowed by remember { mutableStateOf(ReminderNotifications.areAllowed(context)) }
    LifecycleResumeEffect(Unit) {
        notificationsAllowed = ReminderNotifications.areAllowed(context)
        onPauseOrDispose { }
    }
    // Auch bei „Ablehnen“ einschalten: Dann erscheint der Hinweis mit dem Weg in die Systemeinstellungen.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsAllowed = ReminderNotifications.areAllowed(context)
        viewModel.setRemindersEnabled(true)
    }

    SettingsScreen(
        uiState = uiState,
        notificationsAllowed = notificationsAllowed,
        canPinWidget = remember { AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported },
        onRemindersEnabledChange = { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ReminderNotifications.hasPermission(context)
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.setRemindersEnabled(enabled)
            }
        },
        onPerDayChange = viewModel::setRemindersPerDay,
        onStartChange = viewModel::setStart,
        onEndChange = viewModel::setEnd,
        onOpenNotificationSettings = { openNotificationSettings(context) },
        onPinWidget = {
            scope.launch {
                GlanceAppWidgetManager(context).requestPinGlanceAppWidget(QuoteWidgetReceiver::class.java)
            }
        },
        onBack = onBack,
    )
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    context.startActivity(intent)
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    notificationsAllowed: Boolean,
    canPinWidget: Boolean,
    onRemindersEnabledChange: (Boolean) -> Unit,
    onPerDayChange: (Int) -> Unit,
    onStartChange: (LocalTime) -> Unit,
    onEndChange: (LocalTime) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onPinWidget: () -> Unit,
    onBack: () -> Unit,
) {
    SystemBarsAppearance(lightBackground = !isSystemInDarkTheme())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                SettingsUiState.Loading -> CircularProgressIndicator(Modifier.padding(32.dp))
                is SettingsUiState.Content -> Column(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                ) {
                    ReminderSection(
                        settings = uiState.reminders,
                        notificationsAllowed = notificationsAllowed,
                        onEnabledChange = onRemindersEnabledChange,
                        onPerDayChange = onPerDayChange,
                        onStartChange = onStartChange,
                        onEndChange = onEndChange,
                        onOpenNotificationSettings = onOpenNotificationSettings,
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    WidgetSection(canPinWidget = canPinWidget, onPinWidget = onPinWidget)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    AboutSection()
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

@Composable
private fun ReminderSection(
    settings: ReminderSettings,
    notificationsAllowed: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onPerDayChange: (Int) -> Unit,
    onStartChange: (LocalTime) -> Unit,
    onEndChange: (LocalTime) -> Unit,
    onOpenNotificationSettings: () -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf<String?>(null) }

    SectionTitle("Erinnerungen")
    ListItem(
        headlineContent = { Text("Tägliche Zitate") },
        supportingContent = { Text("Zitate aus deiner gewählten Kategorie als Benachrichtigung") },
        trailingContent = {
            Switch(checked = settings.enabled, onCheckedChange = null)
        },
        modifier = Modifier.toggleable(
            value = settings.enabled,
            role = Role.Switch,
            onValueChange = onEnabledChange,
        ),
    )

    if (settings.enabled && !notificationsAllowed) {
        ListItem(
            leadingContent = { Icon(Icons.Filled.Warning, contentDescription = null) },
            headlineContent = { Text("Benachrichtigungen sind blockiert") },
            supportingContent = { Text("Ohne Erlaubnis kommen keine Erinnerungen an.") },
            trailingContent = {
                TextButton(onClick = onOpenNotificationSettings) { Text("Erlauben") }
            },
        )
    }

    if (settings.enabled) {
        ListItem(
            headlineContent = { Text("Anzahl pro Tag") },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalIconButton(
                        onClick = { onPerDayChange(settings.perDay - 1) },
                        enabled = settings.perDay > ReminderSettings.MIN_PER_DAY,
                    ) { Text("−", style = MaterialTheme.typography.titleLarge) }
                    Text("${settings.perDay}", style = MaterialTheme.typography.titleMedium)
                    FilledTonalIconButton(
                        onClick = { onPerDayChange(settings.perDay + 1) },
                        enabled = settings.perDay < ReminderSettings.MAX_PER_DAY,
                    ) { Text("+", style = MaterialTheme.typography.titleLarge) }
                }
            },
        )
        ListItem(
            headlineContent = { Text("Von") },
            trailingContent = { Text(settings.start.format(TIME_FORMAT), style = MaterialTheme.typography.titleMedium) },
            modifier = Modifier.clickable { editing = "start" },
        )
        ListItem(
            headlineContent = { Text("Bis") },
            trailingContent = { Text(settings.end.format(TIME_FORMAT), style = MaterialTheme.typography.titleMedium) },
            modifier = Modifier.clickable { editing = "end" },
        )
        Text(
            text = reminderSummary(settings),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }

    when (editing) {
        "start" -> TimeDialog(
            title = "Erste Erinnerung",
            initial = settings.start,
            onConfirm = { onStartChange(it); editing = null },
            onDismiss = { editing = null },
        )
        "end" -> TimeDialog(
            title = "Letzte Erinnerung",
            initial = settings.end,
            onConfirm = { onEndChange(it); editing = null },
            onDismiss = { editing = null },
        )
    }
}

private fun reminderSummary(settings: ReminderSettings): String {
    val start = settings.start.format(TIME_FORMAT)
    val end = settings.end.format(TIME_FORMAT)
    return if (settings.perDay == 1) {
        "Du bekommst täglich um $start Uhr ein Zitat."
    } else {
        "Du bekommst ${settings.perDay} Zitate am Tag, verteilt zwischen $start und $end Uhr."
    }
}

@Composable
private fun TimeDialog(
    title: String,
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}

@Composable
private fun WidgetSection(canPinWidget: Boolean, onPinWidget: () -> Unit) {
    SectionTitle("Widget")
    Text(
        text = "Zeigt ein Zitat auf deinem Startbildschirm und wechselt alle paar Stunden. " +
            "Tippe im Widget auf den Pfeil für ein neues Zitat.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
    if (canPinWidget) {
        FilledTonalButton(
            onClick = onPinWidget,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) { Text("Widget hinzufügen") }
    } else {
        Text(
            text = "So geht's: Lange auf den Startbildschirm tippen → Widgets → Tagesmotivation.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun AboutSection() {
    SectionTitle("Über die Zitate")
    Text(
        text = "Alle Zitate sind in der App gespeichert, es werden keine Daten ins Internet gesendet. " +
            "Zitate, die ursprünglich nicht auf Deutsch sind, tragen den Hinweis „Übersetzung“.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Preview(showSystemUi = true)
@Composable
private fun SettingsPreview() {
    TagesmotivationTheme {
        SettingsScreen(
            uiState = SettingsUiState.Content(ReminderSettings(enabled = true)),
            notificationsAllowed = false,
            canPinWidget = true,
            onRemindersEnabledChange = {}, onPerDayChange = {}, onStartChange = {}, onEndChange = {},
            onOpenNotificationSettings = {}, onPinWidget = {}, onBack = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsLoadingPreview() {
    TagesmotivationTheme {
        SettingsScreen(
            uiState = SettingsUiState.Loading,
            notificationsAllowed = true,
            canPinWidget = true,
            onRemindersEnabledChange = {}, onPerDayChange = {}, onStartChange = {}, onEndChange = {},
            onOpenNotificationSettings = {}, onPinWidget = {}, onBack = {},
        )
    }
}
