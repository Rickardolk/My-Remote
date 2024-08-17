package com.example.myremote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyRemoteApp()
        }
    }
}

@Composable
fun MyRemoteApp() {
    var isServiceActive by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Remote",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Button to start/stop the service
        Button(
            onClick = {
                if (isServiceActive) {
                    context.stopService(Intent(context, MyRemoteAccessibilityService::class.java))
                } else {
                    context.startService(Intent(context, MyRemoteAccessibilityService::class.java))
                    if (!Settings.canDrawOverlays(context)) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                        )
                        context.startActivity(intent)
                    }
                }
                isServiceActive = !isServiceActive
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isServiceActive) Color.Red else Color.Blue
            )
        ) {
            Text(if (isServiceActive) "Stop My Remote" else "Start My Remote")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isServiceActive) {
                "My Remote is active. Apps like Netflix can now be clicked and scrolled using the TV remote."
            } else {
                "My Remote is inactive."
            },
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            modifier = Modifier.padding(horizontal = 32.dp),
            color = Color.DarkGray
        )
    }
}
