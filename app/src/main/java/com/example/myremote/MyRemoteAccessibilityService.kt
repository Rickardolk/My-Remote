package com.example.myremote

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat

class MyRemoteAccessibilityService : AccessibilityService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    companion object {
        const val CHANNEL_ID = "MyRemoteServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        createNotificationChannel()
        startForegroundService()
        restartServiceIfStopped()
    }

    private fun restartServiceIfStopped() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isServiceRunning(MyRemoteAccessibilityService::class.java)) {
                startService(Intent(this, MyRemoteAccessibilityService::class.java))
            }
        }, 1000)
    }


    private fun isServiceRunning(serviceClass: Class<out Service>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = manager.getRunningServices(Int.MAX_VALUE)
        for (service in services) {
            if (service.service.className == serviceClass.name) {
                return true
            }
        }
        return false
    }


    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("My Remote")
            .setContentText("My Remote is running. Apps can be clicked and scrolled using the TV remote.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "My Remote Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            Log.d("MyRemote", "Received event: ${event.eventType}, source: ${event.source?.packageName}")
            val rootNode = rootInActiveWindow ?: return
            handleAccessibilityEvent(rootNode)
        }
    }

    private fun handleAccessibilityEvent(rootNode: AccessibilityNodeInfo) {
        Log.d("MyRemote", "Handling accessibility event")
        val nodes = findClickableNodes(rootNode)
        if (nodes.isNotEmpty()) {
            nodes.forEach { node ->
                Log.d("MyRemote", "Found clickable node: ${node.text} - ${node.isClickable}")
            }
            val nodeToClick = nodes.first()
            Log.d("MyRemote", "Clicking node: ${nodeToClick.text}")
            nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            Log.d("MyRemote", "No clickable nodes found")
        }
    }



    private fun findClickableNodes(node: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val clickableNodes = mutableListOf<AccessibilityNodeInfo>()
        if (node.isClickable) {
            clickableNodes.add(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            clickableNodes.addAll(findClickableNodes(child))
        }
        return clickableNodes
    }



    override fun onKeyEvent(event: KeyEvent?): Boolean {
        event?.let {
            val rootNode = rootInActiveWindow ?: return super.onKeyEvent(event)

            when (it.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    Log.d("MyRemote", "KEYCODE_DPAD_UP")
                    scroll(rootNode, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    Log.d("MyRemote", "KEYCODE_DPAD_DOWN")
                    scroll(rootNode, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    Log.d("MyRemote", "KEYCODE_DPAD_LEFT")
                    scrollHorizontally(rootNode, isScrollLeft = true)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    Log.d("MyRemote", "KEYCODE_DPAD_RIGHT")
                    scrollHorizontally(rootNode, isScrollLeft = false)
                    return true
                }
                KeyEvent.KEYCODE_ENTER -> {
                    Log.d("MyRemote", "KEYCODE_ENTER")
                    performClick(rootNode)
                    return true
                }
                else -> return super.onKeyEvent(event)
            }
        }
        return super.onKeyEvent(event)
    }


    private fun scroll(nodeInfo: AccessibilityNodeInfo, action: Int) {
        if (nodeInfo.isScrollable) {
            nodeInfo.performAction(action)
        } else {
            for (i in 0 until nodeInfo.childCount) {
                val child = nodeInfo.getChild(i) ?: continue
                scroll(child, action)
            }
        }
    }

    private fun performClick(nodeInfo: AccessibilityNodeInfo) {
        if (nodeInfo.isClickable) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            for (i in 0 until nodeInfo.childCount) {
                val child = nodeInfo.getChild(i) ?: continue
                performClick(child)
            }
        }
    }


    private fun scrollHorizontally(nodeInfo: AccessibilityNodeInfo, isScrollLeft: Boolean) {
        val action = if (isScrollLeft) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        else AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        scroll(nodeInfo, action)
    }

    override fun onInterrupt() {
        // LOg
    }
}
