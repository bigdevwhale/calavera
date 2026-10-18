package app.calavera.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.calavera.MainActivity
import app.calavera.R
import app.calavera.container
import app.calavera.domain.Rules
import app.calavera.localized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Catrina's visit: one quiet notice a day with her word, at the hour chosen. Nothing else ever pings. */
object Alarms {
    private const val CHANNEL = "catrina_v1"
    private const val ID = 5
    private const val ACTION = "app.calavera.notify.VISIT"

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val l = context.localized()
        if (nm.getNotificationChannel(CHANNEL) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, l.getString(R.string.channel_catrina), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = l.getString(R.string.channel_catrina_desc)
                }
            )
        }
    }

    private fun pi(context: Context) = PendingIntent.getBroadcast(
        context, 1,
        Intent(context, AlarmReceiver::class.java).setAction(ACTION),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /** The next moment at [minutes] after midnight, strictly after [now]. */
    fun next(minutes: Int, now: LocalDateTime, zone: ZoneId): Long {
        var at = now.toLocalDate().atTime(minutes / 60, minutes % 60)
        if (!at.isAfter(now)) at = at.plusDays(1)
        return at.atZone(zone).toInstant().toEpochMilli()
    }

    suspend fun reschedule(context: Context) {
        val s = context.container.settings.now()
        val am = context.getSystemService(AlarmManager::class.java)
        val p = pi(context)
        if (!s.notify || !s.onboarded) {
            am.cancel(p)
            return
        }
        val zone = ZoneId.systemDefault()
        // Catrina doesn't need to-the-second precision; this avoids the exact-alarm permission.
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next(s.notifyAt, LocalDateTime.now(zone), zone), p)
    }

    suspend fun fire(context: Context) {
        val s = context.container.settings.now()
        if (!s.notify || !s.onboarded || !canNotify(context)) return
        val l = context.localized()
        val today = LocalDate.now()
        val words = l.resources.getStringArray(R.array.catrina_words)
        val text = when {
            Rules.fiesta(today) -> l.getString(R.string.notif_fiesta)
            Rules.isBirthday(Rules.birth(s.birth), today) -> l.getString(R.string.notif_birthday)
            else -> words[Rules.wordOfDay(today, words.size)]
        }
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .setContentTitle(l.getString(R.string.notif_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(
                PendingIntent.getActivity(
                    context, 100,
                    Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(ID, n)
        } catch (_: SecurityException) {
        }
    }

    fun clear(context: Context) = NotificationManagerCompat.from(context).cancel(ID)

    internal const val VISIT = ACTION
}

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Alarms.VISIT) return
        val pending = goAsync()
        receiverScope.launch {
            try {
                Alarms.fire(context)
                Alarms.reschedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    private val actions = setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in actions) return
        val pending = goAsync()
        receiverScope.launch {
            try {
                Alarms.reschedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}
