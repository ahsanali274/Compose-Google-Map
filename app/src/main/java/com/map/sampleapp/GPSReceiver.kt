package com.map.sampleapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class GPSReceiver(private val locationCallBack: () -> Unit) : BroadcastReceiver() {

    /**
     * triggers on receiving external broadcast
     * @param context Context
     * @param intent Intent
     */
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == "android.location.PROVIDERS_CHANGED") {
            locationCallBack.invoke()
        }
    }
}