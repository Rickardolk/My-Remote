package com.example.myremote

import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo

fun scroll(nodeInfo: AccessibilityNodeInfo, action: Int) {
    if (nodeInfo.isScrollable) {
        Log.d("MyRemoteService", "Scrolling: ${nodeInfo.packageName}")
        nodeInfo.performAction(action)
    } else {
        for (i in 0 until nodeInfo.childCount) {
            val child = nodeInfo.getChild(i) ?: continue
            scroll(child, action)
        }
    }
}

fun performClick(nodeInfo: AccessibilityNodeInfo) {
    if (nodeInfo.isClickable) {
        Log.d("MyRemoteService", "Performing Click on: ${nodeInfo.packageName}")
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    } else {
        for (i in 0 until nodeInfo.childCount) {
            val child = nodeInfo.getChild(i) ?: continue
            performClick(child)
        }
    }
}

fun scrollHorizontally(nodeInfo: AccessibilityNodeInfo, isScrollLeft: Boolean) {
    val action = if (isScrollLeft) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
    else AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
    Log.d("MyRemoteService", "Scrolling Horizontally: ${if (isScrollLeft) "Left" else "Right"}")
    scroll(nodeInfo, action)
}

fun handleScrollAndClick(nodeInfo: AccessibilityNodeInfo, keyCode: Int) {
    Log.d("MyRemoteService", "Handling key code: $keyCode")
    when (keyCode) {
        KeyEvent.KEYCODE_DPAD_UP -> scroll(nodeInfo, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        KeyEvent.KEYCODE_DPAD_DOWN -> scroll(nodeInfo, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        KeyEvent.KEYCODE_DPAD_LEFT -> scrollHorizontally(nodeInfo, isScrollLeft = true)
        KeyEvent.KEYCODE_DPAD_RIGHT -> scrollHorizontally(nodeInfo, isScrollLeft = false)
        KeyEvent.KEYCODE_ENTER -> {
            Log.d("MyRemoteService", "Enter key pressed")
            performClick(nodeInfo)
        }
        else -> {
            // Log the unhandled key event for debugging purposes
            Log.d("MyRemoteService", "Unhandled key code: $keyCode")
        }
    }
}
