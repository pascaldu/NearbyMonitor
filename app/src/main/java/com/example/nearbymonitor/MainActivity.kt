package com.example.nearbymonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.nearbymonitor.ui.MonitorScreen
import com.example.nearbymonitor.ui.MonitorViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MonitorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()

        setContent {
            MaterialTheme {
                PermissionAwareMonitor(viewModel)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersiveMode()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopMonitoring()
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    @Composable
    private fun PermissionAwareMonitor(viewModel: MonitorViewModel) {
        var permissionGeneration by remember { mutableIntStateOf(0) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            permissionGeneration++
            if (allPermissionsGranted()) {
                viewModel.startMonitoring()
            }
        }

        val permissionsGranted = remember(permissionGeneration) {
            allPermissionsGranted()
        }

        LaunchedEffect(permissionsGranted) {
            if (permissionsGranted) {
                viewModel.startMonitoring()
            }
        }

        MonitorScreen(
            viewModel = viewModel,
            permissionsGranted = permissionsGranted,
            onRequestPermissions = {
                launcher.launch(requiredPermissions())
            },
            onStart = {
                if (allPermissionsGranted()) {
                    viewModel.startMonitoring()
                } else {
                    launcher.launch(requiredPermissions())
                }
            }
        )
    }

    private fun allPermissionsGranted(): Boolean = requiredPermissions().all { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requiredPermissions(): Array<String> = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        if (Build.VERSION.SDK_INT >= 37) {
            add("android.permission.ACCESS_LOCAL_NETWORK")
        }
    }.toTypedArray()
}
