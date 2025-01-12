package com.horizam.pro.elean.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.R
import android.os.Build
import com.horizam.pro.elean.channelId


class NotificationUtils {

    companion object {

        val VIBRATE_PATTERN = longArrayOf(1000, 1000, 1000, 1000, 1000)


        fun createNotification(context: Context, file: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    Constants.channelId,
                    Constants.DOWNLOAD_NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(notificationChannel)

                // Configure the notification channel.
                notificationChannel.apply {
                    enableLights(false)
                    setSound(null, null)
                    enableVibration(true)
                    vibrationPattern = VIBRATE_PATTERN
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val notificationBuilder =
                NotificationCompat.Builder(context, channelId)
            notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(context.getString(R.string.str_file_downloaded))
                .setContentText(
                    file.plus(" ").plus(context.getString(R.string.str_has_been_downloaded))
                )
                .setWhen(System.currentTimeMillis())
                .setDefaults(0)
                .setVibrate(VIBRATE_PATTERN)
                .setSound(null)
                .setSmallIcon(R.mipmap.ic_launcher_round)


            notificationManager.notify(0, notificationBuilder.build())
        }

        fun showNotification(
            id: String,
            context: Context,
            title: String,
            description: String,
            pendingIntent: PendingIntent?
        ) {

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    id,
                    Constants.DOWNLOAD_NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                // Configure the notification channel.
                notificationChannel.apply {
                    enableLights(false)
                    setSound(null, null)
                    enableVibration(true)
                    vibrationPattern = VIBRATE_PATTERN
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(notificationChannel)
            }
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
            notificationBuilder
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(title)
                .setContentText(description)
                .setWhen(System.currentTimeMillis())
                .setDefaults(0)
                .setVibrate(VIBRATE_PATTERN)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
            notificationManager.notify(0,notificationBuilder.build())
        }
    }
}