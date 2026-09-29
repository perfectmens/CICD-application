package com.example.remoteupdatedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remoteupdatedemo.ui.BackendHealthState
import com.example.remoteupdatedemo.ui.MainUiState
import com.example.remoteupdatedemo.ui.MainViewModel
import com.example.remoteupdatedemo.ui.UpdateStatus
import com.example.remoteupdatedemo.ui.theme.AmberOrange
import com.example.remoteupdatedemo.ui.theme.BorderSubtle
import com.example.remoteupdatedemo.ui.theme.CardBackground
import com.example.remoteupdatedemo.ui.theme.ElectricTeal
import com.example.remoteupdatedemo.ui.theme.EmeraldDark
import com.example.remoteupdatedemo.ui.theme.EmeraldGreen
import com.example.remoteupdatedemo.ui.theme.RemoteUpdateDemoTheme
import com.example.remoteupdatedemo.ui.theme.RoseRed
import com.example.remoteupdatedemo.ui.theme.Slate100
import com.example.remoteupdatedemo.ui.theme.Slate50
import com.example.remoteupdatedemo.ui.theme.Slate500
import com.example.remoteupdatedemo.ui.theme.Slate700
import com.example.remoteupdatedemo.ui.theme.Slate800
import com.example.remoteupdatedemo.ui.theme.Slate900

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemoteUpdateDemoTheme {
                val uiState by viewModel.uiState.collectAsState()
                MainScreen(
                    uiState = uiState,
                    onCheckForUpdate = { viewModel.checkForUpdate() },
                    onOpenSettings = { viewModel.openSettingsDialog() },
                    onSaveBackendUrl = { viewModel.updateBackendUrl(it) },
                    onDismissSettings = { viewModel.dismissSettingsDialog() }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    onCheckForUpdate: () -> Unit,
    onOpenSettings: () -> Unit,
    onSaveBackendUrl: (String) -> Unit,
    onDismissSettings: () -> Unit
) {
    Scaffold(
        containerColor = Slate50
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Logo
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                    .shadow(6.dp, RoundedCornerShape(18.dp))
            )

            // Header / App Title
            Text(
                text = uiState.appTitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                textAlign = TextAlign.Center
            )

            // App Description
            Text(
                text = uiState.appDescription,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Slate700,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Backend Connectivity Status Pill
            BackendStatusPill(
                healthState = uiState.backendHealth,
                backendUrl = uiState.backendUrl,
                onClick = onOpenSettings
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current Version Card
            CurrentVersionCard(
                versionName = uiState.currentVersionName,
                versionCode = uiState.currentVersionCode
            )

            // Update Action Button
            Button(
                onClick = onCheckForUpdate,
                enabled = uiState.updateStatus !is UpdateStatus.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate900,
                    contentColor = Color.White,
                    disabledContainerColor = Slate700,
                    disabledContentColor = Slate500
                ),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
            ) {
                if (uiState.updateStatus is UpdateStatus.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Checking for Update...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Check for Update",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Status & Feedback Feedback Box
            AnimatedVisibility(
                visible = uiState.updateStatus !is UpdateStatus.Idle &&
                          uiState.updateStatus !is UpdateStatus.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                UpdateStatusCard(status = uiState.updateStatus)
            }
        }
    }

    if (uiState.isSettingsDialogOpen) {
        BackendSettingsDialog(
            currentUrl = uiState.backendUrl,
            onSave = onSaveBackendUrl,
            onDismiss = onDismissSettings
        )
    }
}

@Composable
fun CurrentVersionCard(
    versionName: String,
    versionCode: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Version",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Slate500,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = versionName,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Slate100,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Build Code: $versionCode (Dynamic from BuildConfig)",
                    fontSize = 12.sp,
                    color = Slate700,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun BackendStatusPill(
    healthState: BackendHealthState,
    backendUrl: String,
    onClick: () -> Unit
) {
    val (statusColor, statusText) = when (healthState) {
        is BackendHealthState.Connected -> Pair(EmeraldGreen, "Backend Connected")
        is BackendHealthState.Checking -> Pair(AmberOrange, "Probing Backend...")
        is BackendHealthState.Disconnected -> Pair(RoseRed, "Backend Offline")
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Slate100,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$statusText ($backendUrl)",
                fontSize = 12.sp,
                color = Slate700,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun UpdateStatusCard(status: UpdateStatus) {
    when (status) {
        is UpdateStatus.UpToDate -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "You are running the latest version (${status.latestVersionName}).",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = status.message,
                        fontSize = 13.sp,
                        color = Slate700,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        is UpdateStatus.UpdateAvailable -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricTeal.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = ElectricTeal.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "New Update Available: v${status.updateInfo.latestVersionName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build Code: ${status.updateInfo.latestVersionCode}",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = status.updateInfo.releaseNotes,
                        fontSize = 13.sp,
                        color = Slate800,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Download via Internet: GitHub Releases",
                            fontSize = 11.sp,
                            color = Slate700,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = status.updateInfo.downloadUrl,
                        fontSize = 11.sp,
                        color = ElectricTeal,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Remote APK download & installer will be connected in future stage.",
                        fontSize = 12.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        is UpdateStatus.Error -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RoseRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = RoseRed.copy(alpha = 0.06f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = status.message,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate800,
                        textAlign = TextAlign.Center
                    )
                    if (status.technicalDetail != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Detail: ${status.technicalDetail}",
                            fontSize = 11.sp,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        else -> Unit
    }
}

@Composable
fun BackendSettingsDialog(
    currentUrl: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Backend Server URL", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column {
                Text(
                    text = "Configure the target backend URL for the Remote Update Demo server:",
                    fontSize = 13.sp,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Quick Presets:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { textValue = "http://192.168.68.64:8080/" },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("LAN (192.168.68.64)", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { textValue = "http://10.0.2.2:8080/" },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Emulator (10.0.2.2)", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(textValue) },
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Save & Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate700)
            }
        }
    )
}
