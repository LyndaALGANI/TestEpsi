package com.simplecity.amp_library.playback

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.simplecity.amp_library.utils.AnalyticsManager
import com.simplecity.amp_library.utils.SettingsManager

class BluetoothManager(
    private val playbackManager: PlaybackManager,
    private val analyticsManager: AnalyticsManager,
    private val musicServiceCallbacks: MusicService.Callbacks,
    private val settingsManager: SettingsManager
) {
    private var bluetoothReceiver: BroadcastReceiver? = null
    private var a2dpReceiver: BroadcastReceiver? = null

    fun registerBluetoothReceiver(context: Context) {
        val filter = IntentFilter().apply {
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
        }

        bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleBluetoothIntent(intent)
            }
        }

        context.registerReceiver(bluetoothReceiver, filter)
    }

    private fun handleBluetoothIntent(intent: Intent) {
        val action = intent.action ?: return
        val extras = intent.extras ?: return

        if (settingsManager.bluetoothPauseDisconnect) {
            handlePauseOnDisconnect(action, extras)
        }

        if (settingsManager.bluetoothResumeConnect) {
            handleResumeOnConnect(action, extras)
        }
    }

    private fun handlePauseOnDisconnect(action: String, extras: Bundle) {
        when (action) {
            BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                val state = extras.getInt(BluetoothA2dp.EXTRA_STATE)
                val previousState = extras.getInt(BluetoothA2dp.EXTRA_PREVIOUS_STATE)
                if ((state == BluetoothA2dp.STATE_DISCONNECTED || state == BluetoothA2dp.STATE_DISCONNECTING)
                    && previousState == BluetoothA2dp.STATE_CONNECTED
                ) {
                    analyticsManager.dropBreadcrumb(TAG, "A2DP disconnected. State: $state")
                    playbackManager.pause(false)
                }
            }

            BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                val state = extras.getInt(BluetoothHeadset.EXTRA_STATE)
                val previousState = extras.getInt(BluetoothHeadset.EXTRA_PREVIOUS_STATE)
                if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED
                    && previousState == BluetoothHeadset.STATE_AUDIO_CONNECTED
                ) {
                    analyticsManager.dropBreadcrumb(TAG, "Headset audio disconnected. State: $state")
                    playbackManager.pause(false)
                }
            }
        }
    }

    private fun handleResumeOnConnect(action: String, extras: Bundle) {
        when (action) {
            BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                val state = extras.getInt(BluetoothA2dp.EXTRA_STATE)
                if (state == BluetoothA2dp.STATE_CONNECTED) {
                    playbackManager.play()
                }
            }

            BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                val state = extras.getInt(BluetoothHeadset.EXTRA_STATE)
                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    playbackManager.play()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BluetoothManager"
    }
}