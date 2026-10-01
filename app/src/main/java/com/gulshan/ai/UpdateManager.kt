package com.gulshan.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    fun installUpdate(
        context: Context,
        apkUrl: String,
        onResult: (String) -> Unit
    ) {

        if (apkUrl.isBlank()) {
            onResult("Update link nahi mila.")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (!context.packageManager.canRequestPackageInstalls()) {

                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    )

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)

                    onResult(
                        "Gulshan ko update install permission do."
                    )

                } catch (e: Exception) {

                    onResult(
                        "Install permission settings nahi khul sake."
                    )
                }

                return
            }
        }

        Thread {

            var connection: HttpURLConnection? = null

            try {

                val url = URL(apkUrl)

                connection =
                    url.openConnection() as HttpURLConnection

                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode !in 200..299) {

                    throw Exception(
                        "Download failed: ${connection.responseCode}"
                    )
                }

                val updateFolder = File(
                    context.cacheDir,
                    "gulshan_update"
                )

                if (!updateFolder.exists()) {
                    updateFolder.mkdirs()
                }

                val apkFile = File(
                    updateFolder,
                    "gulshan-update.apk"
                )

                if (apkFile.exists()) {
                    apkFile.delete()
                }

                connection.inputStream.use { input ->

                    FileOutputStream(apkFile).use { output ->

                        val buffer = ByteArray(8192)

                        while (true) {

                            val count = input.read(buffer)

                            if (count == -1) {
                                break
                            }

                            output.write(
                                buffer,
                                0,
                                count
                            )
                        }

                        output.flush()
                    }
                }

                Handler(Looper.getMainLooper()).post {

                    try {

                        val authority =
                            "${context.packageName}.fileprovider"

                        val apkUri =
                            FileProvider.getUriForFile(
                                context,
                                authority,
                                apkFile
                            )

                        val installIntent =
                            Intent(
                                Intent.ACTION_VIEW
                            )

                        installIntent.setDataAndType(
                            apkUri,
                            "application/vnd.android.package-archive"
                        )

                        installIntent.addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                        installIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )

                        context.startActivity(
                            installIntent
                        )

                        onResult(
                            "Update download ho gaya. Installation shuru ho rahi hai."
                        )

                    } catch (e: Exception) {

                        onResult(
                            "Update install nahi ho saka."
                        )
                    }
                }

            } catch (e: Exception) {

                Handler(Looper.getMainLooper()).post {

                    onResult(
                        "Update download nahi ho saka."
                    )
                }

            } finally {

                connection?.disconnect()
            }

        }.start()
    }
}
