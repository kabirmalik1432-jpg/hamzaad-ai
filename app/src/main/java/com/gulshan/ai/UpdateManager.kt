package com.gulshan.ai
import androidx.core.content.FileProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    fun downloadAndInstall(
        context: Context,
        apkUrl: String
    ) {

        Thread {

            try {

                val connection =
                    URL(apkUrl).openConnection() as HttpURLConnection

                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    connection.disconnect()
                    return@Thread
                }

                val apkFile = File(
                    context.cacheDir,
                    "gulshan_update.apk"
                )

                connection.inputStream.use { input ->

                    apkFile.outputStream().use { output ->

                        val buffer = ByteArray(8192)

                        while (true) {

                            val count = input.read(buffer)

                            if (count == -1) {
                                break
                            }

                            output.write(buffer, 0, count)
                        }
                    }
                }

                connection.disconnect()

                installApk(context, apkFile)

            } catch (_: Exception) {
                // Update failed
            }

        }.start()
    }

    private fun installApk(
        context: Context,
        apkFile: File
    ) {

        val apkUri: Uri

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

        } else {

            apkUri = Uri.fromFile(apkFile)
        }

        val intent = Intent(
            Intent.ACTION_VIEW
        )

        intent.setDataAndType(
            apkUri,
            "application/vnd.android.package-archive"
        )

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )

        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        context.startActivity(intent)
    }
}
