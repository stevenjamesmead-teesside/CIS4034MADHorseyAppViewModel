package com.stevenjamesmead.cis4034madhorseyappviewmodel

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class MyApplication : Application() {

    lateinit var authz: String
        private set
        get

    lateinit var requestQueue: RequestQueue
        private set
        get

    override fun onCreate() {
        super.onCreate()

        instance = this

        authz = getAuthz(this)

        requestQueue = Volley.newRequestQueue(this)

        Log.d(TAG, "Creating Application Instance - authz = $authz")
    }

    companion object {
        val TAG: String = MyApplication::class.simpleName!!

        lateinit var instance: MyApplication
            private set
    }

    private fun getAuthz(context: Context) : String {
        Log.d(TAG, "API Version = ${android.os.Build.VERSION.SDK_INT}")

        val pm = context.applicationContext.packageManager
        val applicationInfo: ApplicationInfo =
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                //
                // For more recent versions of the Android SDK
                //
                pm.getApplicationInfo(context.packageName, PackageManager.ApplicationInfoFlags.of(0))
            }
            else {
                //
                // For older versions of the Android SDK
                //
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            }

        val authzData = applicationInfo.metaData.getString("authz")!!

        Log.d(TAG, "Authz = $authzData")

        return authzData
    }
}