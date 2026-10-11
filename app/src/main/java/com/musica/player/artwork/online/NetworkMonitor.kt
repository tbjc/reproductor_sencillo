package com.musica.player.artwork.online

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Indica si hay conectividad. Se consulta antes de salir a Internet para no
 * esperar a un timeout cuando el dispositivo está sin red (offline-first).
 */
interface NetworkMonitor {
    fun isOnline(): Boolean
}

internal class ConnectivityNetworkMonitor(context: Context) : NetworkMonitor {

    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    override fun isOnline(): Boolean {
        val manager = connectivityManager ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
