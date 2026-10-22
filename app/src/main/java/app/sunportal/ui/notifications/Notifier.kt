package app.sunportal.ui.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.sunportal.MainActivity
import app.sunportal.R

/**
 * Уведомления SunPortal: один канал «Сповіщення станції».
 *
 * На Android 13+ (API 33) требуется разрешение POST_NOTIFICATIONS —
 * его запрашивает MainActivity при первом запуске.
 */
object Notifier {
    /** ID канала уведомлений. */
    const val CHANNEL_ID = "sunportal_alerts"

    private const val CHANNEL_NAME = "Сповіщення станції"

    /** Уведомление: началось отключение. */
    const val ID_BLACKOUT = 1001

    /** Уведомление: скоро отключение. */
    const val ID_BLACKOUT_SOON = 1002

    /** Уведомление: аккумулятор заряжен. */
    const val ID_BATTERY_FULL = 1003

    /** Уведомление: низкий заряд аккумулятора во время отключения. */
    const val ID_BATTERY_LOW = 1004

    /** Создать канал (безопасно вызывать многократно). */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Відключення світла, заряд батареї, станція"
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Показать уведомление. Если разрешения нет (Android 13+ / пользователь отказал) —
     * тихо ничего не делаем.
     */
    fun show(context: Context, id: Int, title: String, text: String) {
        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sun)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: Exception) {
            // Нет разрешения или канал недоступен — молча игнорируем
        }
    }
}
