package xyz.tberghuis.floatingtimer.service

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.torrydo.screenez.ScreenEz
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import xyz.tberghuis.floatingtimer.EXTRA_COUNTDOWN_DURATION
import xyz.tberghuis.floatingtimer.FOREGROUND_SERVICE_NOTIFICATION_ID
import xyz.tberghuis.floatingtimer.INTENT_COMMAND
import xyz.tberghuis.floatingtimer.INTENT_COMMAND_COUNTDOWN_COMPLETE
import xyz.tberghuis.floatingtimer.INTENT_COMMAND_COUNTDOWN_CREATE
import xyz.tberghuis.floatingtimer.INTENT_COMMAND_EXIT
import xyz.tberghuis.floatingtimer.INTENT_COMMAND_RESET
import xyz.tberghuis.floatingtimer.INTENT_COMMAND_STOPWATCH_CREATE
import xyz.tberghuis.floatingtimer.MainActivity
import xyz.tberghuis.floatingtimer.NOTIFICATION_CHANNEL
import xyz.tberghuis.floatingtimer.R
import xyz.tberghuis.floatingtimer.REQUEST_CODE_EXIT
import xyz.tberghuis.floatingtimer.REQUEST_CODE_RESET
import xyz.tberghuis.floatingtimer.logd
import xyz.tberghuis.floatingtimer.service.countdown.TimerStateFinished

class FloatingService : Service() {
  private val job = SupervisorJob()
  val scope = CoroutineScope(Dispatchers.Default + job)       // コルーチンスコープを作成。この時、context として Dispatchers.Default に加え、SupervisorJob を与えている。
  val state = ServiceState(scope)       // ServiceState を生成。 ServiceState は次のような情報を持つ。サービス状態：in_service、out_of_service、emergency_only、power_off、デュプレックス・モード：不明、FDD、TDD、ローミングインジケータ、オペレーター名、ショートネーム、数字ID、ネットワーク選択モード
//  private var isStarted = false

  // todo make private
  lateinit var overlayController: OverlayController       // Overlay のコントロールをする変数や関数のクラスのインスタンスを作成
  lateinit var alarmController: AlarmController        // Alarm のコントロールをする変数や関数のクラスのインスタンスを作成

  override fun onCreate() {
    super.onCreate()
    ScreenEz.with(this.applicationContext)
    overlayController = OverlayController(this)
    alarmController = AlarmController(this)
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    logd("FloatingService onStartCommand")
    postOngoingActivityNotification()

    intent?.let {
      val command = intent.getStringExtra(INTENT_COMMAND)
      when (command) {        // command の値によって処理を分岐させる
        INTENT_COMMAND_COUNTDOWN_CREATE -> {
          val duration = intent.getIntExtra(EXTRA_COUNTDOWN_DURATION, 10)
          state.countdownState.resetTimerState(duration)
          state.countdownState.overlayState.isVisible.value = true        // この値変更がカウントダウンのオーバーレイ表示のトリガー
        }

        INTENT_COMMAND_COUNTDOWN_COMPLETE -> {
          logd("onStartCommand INTENT_COMMAND_COUNTDOWN_COMPLETE")
          state.countdownState.timerState.value = TimerStateFinished
        }

        INTENT_COMMAND_STOPWATCH_CREATE -> {
          state.stopwatchState.overlayState.isVisible.value = true
        }

        INTENT_COMMAND_EXIT -> {
          if (state.countdownState.overlayState.isVisible.value == true) {
            overlayController.exitCountdown()
          }
          if (state.stopwatchState.overlayState.isVisible.value == true) {
            overlayController.exitStopwatch()
          }
          return START_NOT_STICKY
        }

        INTENT_COMMAND_RESET -> {
          state.stopwatchState.resetStopwatchState()
          state.countdownState.resetTimerState()
        }
      }
    }
    return START_STICKY
  }

  private fun postOngoingActivityNotification() {
    // see if this reduces ANR's if I call for every onStartCommand
    startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, buildNotification())
//    if (!isStarted) {
//      isStarted = true
//      startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, buildNotification())
//    }
  }

  private fun buildNotification(): Notification {
    val pendingIntent: PendingIntent =
      Intent(this, MainActivity::class.java).let { notificationIntent ->
        PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
      }

    val exitIntent = Intent(applicationContext, FloatingService::class.java)
    exitIntent.putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
    val exitPendingIntent = PendingIntent.getService(
      applicationContext,
      REQUEST_CODE_EXIT,
      exitIntent,
      FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    val resetIntent = Intent(applicationContext, FloatingService::class.java)
    resetIntent.putExtra(INTENT_COMMAND, INTENT_COMMAND_RESET)
    val resetPendingIntent = PendingIntent.getService(
      applicationContext,
      REQUEST_CODE_RESET,
      resetIntent,
      FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    val notification: Notification =
      NotificationCompat.Builder(this, NOTIFICATION_CHANNEL).setContentTitle("Floating Timer")
        .setSmallIcon(R.drawable.ic_alarm).setContentIntent(pendingIntent).addAction(
          0, "Reset", resetPendingIntent
        ).addAction(
          0, "Exit", exitPendingIntent
        ).build()
    return notification
  }

  override fun onDestroy() {
    super.onDestroy()
    job.cancel()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    ScreenEz.refresh()
    scope.launch {
      state.configurationChanged.emit(Unit)
    }
  }

}