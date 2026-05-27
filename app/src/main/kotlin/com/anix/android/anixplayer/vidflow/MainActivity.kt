package com.anix.android.anixplayer.vidflow

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anix.android.anixplayer.vidflow.player.OverlayControlService
import com.anix.android.anixplayer.vidflow.player.OverlayPermissionHelper
import com.anix.android.anixplayer.vidflow.ui.theme.AnixPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var permissionRefreshKey by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnixPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlayerStartScreen(
                        refreshKey = permissionRefreshKey,
                        onRefreshPermissions = { permissionRefreshKey++ },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionRefreshKey++
    }
}

@Composable
fun PlayerStartScreen(
    refreshKey: Int,
    onRefreshPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val canDrawOverlays = remember(refreshKey) {
        OverlayPermissionHelper.canDrawOverlays(context)
    }
    val hasMediaAccess = remember(refreshKey) {
        OverlayPermissionHelper.isNotificationListenerEnabled(context)
    }
    val hasNotifications = remember(refreshKey) {
        OverlayPermissionHelper.hasPostNotificationsPermission(context)
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        onRefreshPermissions()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "AnixPlayer",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Start the overlay after opening a video in another player.",
            style = MaterialTheme.typography.bodyLarge,
        )

        Button(
            enabled = canDrawOverlays && hasMediaAccess,
            onClick = {
                if (!hasNotifications) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    OverlayControlService.start(context)
                }
            },
        ) {
            Text("Start Player")
        }

        if (!canDrawOverlays) {
            OutlinedButton(
                onClick = {
                    context.startActivity(OverlayPermissionHelper.overlaySettingsIntent(context))
                },
            ) {
                Text("Allow overlay")
            }
        }

        if (!hasMediaAccess) {
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        OverlayPermissionHelper.notificationListenerSettingsIntent(context),
                    )
                },
            ) {
                Text("Enable media access")
            }
        }

        if (!hasNotifications) {
            OutlinedButton(
                onClick = {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
            ) {
                Text("Allow notifications")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerStartScreenPreview() {
    AnixPlayerTheme {
        PlayerStartScreen(
            refreshKey = 0,
            onRefreshPermissions = {},
        )
    }
}
