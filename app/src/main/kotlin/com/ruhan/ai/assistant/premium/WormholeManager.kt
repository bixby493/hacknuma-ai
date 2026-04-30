package com.ruhan.ai.assistant.premium

import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class WormholeState { IDLE, SEARCHING, CONNECTED, TRANSFERRING, ERROR }

@Singleton
class WormholeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(WormholeState.IDLE)
    val state: StateFlow<WormholeState> = _state

    private var p2pManager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

    fun initialize() {
        p2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        channel = p2pManager?.initialize(context, context.mainLooper, null)
    }

    fun discoverPeers() {
        _state.value = WormholeState.SEARCHING
        try {
            p2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    _state.value = WormholeState.SEARCHING
                }

                override fun onFailure(reason: Int) {
                    _state.value = WormholeState.ERROR
                }
            })
        } catch (_: SecurityException) {
            _state.value = WormholeState.ERROR
        }
    }

    fun connectToPeer(deviceAddress: String) {
        val config = WifiP2pConfig().apply {
            this.deviceAddress = deviceAddress
        }
        try {
            p2pManager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    _state.value = WormholeState.CONNECTED
                }

                override fun onFailure(reason: Int) {
                    _state.value = WormholeState.ERROR
                }
            })
        } catch (_: SecurityException) {
            _state.value = WormholeState.ERROR
        }
    }

    fun disconnect() {
        p2pManager?.removeGroup(channel, null)
        _state.value = WormholeState.IDLE
    }
}
