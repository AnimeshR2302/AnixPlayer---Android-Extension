package com.anix.android.anixplayer.vidflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
                val mainViewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val subtitlePath by mainViewModel.subtitlePath.collectAsState()

                var currentScreen by rememberSaveable { mutableStateOf("gallery") }
                var selectedVideoUri by rememberSaveable { mutableStateOf<android.net.Uri?>(null) }

                BackHandler(enabled = currentScreen == "player") {
                    currentScreen = "gallery"
                }

                when (currentScreen) {
                    "gallery" -> {
                        com.anix.android.anixplayer.gallery.VideoGalleryScreen(
                            refreshKey = permissionRefreshKey,
                            onVideoSelected = { uri, title ->
                                selectedVideoUri = uri
                                mainViewModel.loadVideoInfo(uri.toString(), title)
                                currentScreen = "player"
                            }
                        )
                    }
                    "player" -> {
                        com.anix.android.anixplayer.videoplayer.PlayerScreen(
                            videoUri = selectedVideoUri,
                            subtitleUri = subtitlePath?.let { android.net.Uri.parse(it) },
                            onNavigateBack = { currentScreen = "gallery" },
                            onSubtitleSelected = { subUri ->
                                selectedVideoUri?.let { uri ->
                                    mainViewModel.updateSubtitle(uri.toString(), subUri?.toString())
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionRefreshKey++
    }
}
