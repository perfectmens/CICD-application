package com.example.remoteupdatedemo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.remoteupdatedemo.data.model.Greeting
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.remoteupdatedemo.data.model.UpdateInfo
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
import java.io.File

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
                    onStartDownload = { updateInfo ->
                        val targetFile = File(cacheDir, "apk_updates/RemoteUpdateDemo-v${updateInfo.latestVersionName}.apk")
                        viewModel.startUpdateDownload(targetFile, updateInfo)
                    },
                    onInstallApk = { apkFile ->
                        triggerInstallApk(apkFile)
                    },
                    onOpenSettings = { viewModel.openSettingsDialog() },
                    onSaveBackendUrl = { viewModel.updateBackendUrl(it) },
                    onDismissSettings = { viewModel.dismissSettingsDialog() },
                    onRefreshGreetings = { viewModel.fetchGreetings() }
                )
            }
        }
    }

    /**
     * Hands off the downloaded APK to Android's PackageInstaller via FileProvider.
     */
    private fun triggerInstallApk(apkFile: File) {
        if (!apkFile.exists()) {
            Toast.makeText(this, "APK file not found on disk", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val apkUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to launch installer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    onCheckForUpdate: () -> Unit,
    onStartDownload: (UpdateInfo) -> Unit,
    onInstallApk: (File) -> Unit,
    onOpenSettings: () -> Unit,
    onSaveBackendUrl: (String) -> Unit,
    onDismissSettings: () -> Unit,
    onRefreshGreetings: () -> Unit = {}
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
            Image(
                painter = painterResource(id = R.drawable.app_logo),
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
                enabled = uiState.updateStatus !is UpdateStatus.Loading &&
                          uiState.updateStatus !is UpdateStatus.Downloading,
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
                UpdateStatusCard(
                    status = uiState.updateStatus,
                    onStartDownload = onStartDownload,
                    onInstallApk = onInstallApk
                )
            }

            // Docker Backend Greetings Section
            DockerGreetingsSection(
                greetings = uiState.greetings,
                isLoading = uiState.isLoadingGreetings,
                errorMessage = uiState.greetingsError,
                onRefresh = onRefreshGreetings
            )
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
fun UpdateStatusCard(
    status: UpdateStatus,
    onStartDownload: (UpdateInfo) -> Unit,
    onInstallApk: (File) -> Unit
) {
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
                            text = "Distribution: GitHub Releases (Internet)",
                            fontSize = 11.sp,
                            color = Slate700,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = status.updateInfo.downloadUrl,
                        fontSize = 10.sp,
                        color = ElectricTeal,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onStartDownload(status.updateInfo) },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Download Update (Internet)")
                    }
                }
            }
        }
        is UpdateStatus.Downloading -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricTeal.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Downloading v${status.updateInfo.latestVersionName} from GitHub...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { status.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ElectricTeal,
                        trackColor = Slate100
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(status.progress * 100).toInt()}% completed",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }
        }
        is UpdateStatus.Downloaded -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "APK Downloaded & Verified!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "v${status.updateInfo.latestVersionName} is ready to be installed.",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onInstallApk(status.file) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Install Update Now")
                    }
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

@Composable
fun DockerGreetingsSection(
    greetings: List<Greeting>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Docker Broadcasts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElectricTeal.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "10 Live",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElectricTeal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Random greetings fetched from Docker backend over LAN",
                        fontSize = 12.sp,
                        color = Slate500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Button(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate100,
                        contentColor = Slate800
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Slate800
                        )
                    } else {
                        Text(text = "Refresh", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isLoading && greetings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = ElectricTeal
                    )
                }
            } else if (errorMessage != null && greetings.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoseRed.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, RoseRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Failed to load greetings",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseRed
                        )
                        Text(
                            text = errorMessage,
                            fontSize = 12.sp,
                            color = Slate700
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    greetings.forEachIndexed { index, greeting ->
                        GreetingItemRow(index = index + 1, greeting = greeting)
                    }
                }
            }
        }
    }
}

@Composable
fun GreetingItemRow(
    index: Int,
    greeting: Greeting
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Slate50,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = CardBackground,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = greeting.emoji, fontSize = 20.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = greeting.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate900,
                    lineHeight = 18.sp
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Slate100
                ) {
                    Text(
                        text = "#$index ${greeting.category}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

