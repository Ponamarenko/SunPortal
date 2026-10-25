package app.sunportal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.sunportal.api.RetrofitClient
import app.sunportal.ui.theme.SunPortalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)
        setContent {
            SunPortalTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NotificationPermissionEffect()
                    MainScreen()
                }
            }
        }
    }
}

/**
 * Запрос разрешения на уведомления (Android 13+).
 * На старых версиях разрешение не требуется — запрос не показывается.
 */
@Composable
private fun NotificationPermissionEffect() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* результат не критичен: без разрешения уведомления просто не придут */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
