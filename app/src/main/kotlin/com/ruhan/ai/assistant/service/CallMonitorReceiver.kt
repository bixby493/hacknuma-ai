package com.ruhan.ai.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import com.ruhan.ai.assistant.MainActivity

class CallMonitorReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (state == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber != null) {
            val callerName = getContactName(context, incomingNumber)
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("incoming_call", true)
                putExtra("caller_number", incomingNumber)
                putExtra("caller_name", callerName ?: "Unknown")
            }
            context.startActivity(mainIntent)
        }
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
            "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?",
            arrayOf(phoneNumber),
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(
                    it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                )
            }
        }
        return null
    }
}
