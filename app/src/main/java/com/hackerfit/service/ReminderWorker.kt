package com.hackerfit.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val CHANNEL_ID = "hackerfit_reminder"
    private const val WORK_NAME = "daily_reminder"

    fun schedule(context: Context, hour: Int, minute: Int) {
        require(hour in 0..23) { "Hora invalida: $hour" }
        require(minute in 0..59) { "Minuto invalido: $minute" }
        val now = LocalTime.now()
        val target = LocalTime.of(hour, minute)
        var delay = Duration.between(now, target).toMinutes()
        if (delay < 0) delay += 24 * 60

        val work = PeriodicWorkRequestBuilder<ReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            work
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lembrete de Treino",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifica\u00e7\u00e3o di\u00e1ria para lembrar do treino"
            }
            manager.createNotificationChannel(channel)
        }
    }
}

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        ReminderScheduler.ensureChannel(applicationContext)

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_today)
                .setContentTitle("HackerFit")
                .setContentText("Hora do seu treino!")
                .setAutoCancel(true)
                .build()

            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(1, notification)
        }

        return Result.success()
    }
}
