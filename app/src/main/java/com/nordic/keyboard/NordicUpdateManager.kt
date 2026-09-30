package com.nordic.keyboard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class NordicUpdateManager(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private val metadataUrl = "https://raw.githubusercontent.com/atdenaiforeve/NordicKeyboard/main/update/update.json"

    fun checkForUpdate(manual: Boolean = false) {
        Thread {
            try {
                val connection = URL(metadataUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                val json = JSONObject(text)
                val remoteCode = json.getInt("versionCode")
                val remoteName = json.getString("versionName")
                val apkUrl = json.getString("apkUrl")
                handler.post {
                    if (remoteCode > BuildConfig.VERSION_CODE) showUpdate(remoteName, apkUrl)
                    else if (manual) AlertDialog.Builder(context).setTitle("Nordic Keyboard").setMessage("NORDIC is up to date.\nInstalled version: " + BuildConfig.VERSION_NAME).setPositiveButton("OK", null).show()
                }
            } catch (e: Exception) {
                if (manual) handler.post { AlertDialog.Builder(context).setTitle("Update check failed").setMessage(e.message ?: "Could not check for an update.").setPositiveButton("OK", null).show() }
            }
        }.start()
    }

    private fun showUpdate(versionName: String, apkUrl: String) {
        AlertDialog.Builder(context).setTitle("Nordic Keyboard update").setMessage("Version $versionName is available. Download and install it?").setNegativeButton("Later", null).setPositiveButton("UPDATE") { _, _ -> downloadAndInstall(apkUrl) }.show()
    }

    private fun downloadAndInstall(apkUrl: String) {
        Thread {
            try {
                val dir = File(context.cacheDir, "updates").apply { mkdirs() }
                val apk = File(dir, "NordicKeyboard.apk")
                val connection = URL(apkUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.requestMethod = "GET"
                connection.inputStream.use { input -> apk.outputStream().use { output -> input.copyTo(output) } }
                connection.disconnect()
                val uri = FileProvider.getUriForFile(context, "com.nordic.keyboard.fileprovider", apk)
                handler.post {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:" + context.packageName)))
                        AlertDialog.Builder(context).setTitle("Allow installation").setMessage("Allow Nordic Keyboard to install its downloaded updates, then press UPDATE again.").setPositiveButton("OK", null).show()
                        return@post
                    }
                    context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                }
            } catch (e: Exception) {
                handler.post { AlertDialog.Builder(context).setTitle("Download failed").setMessage(e.message ?: "Could not download the APK.").setPositiveButton("OK", null).show() }
            }
        }.start()
    }
}