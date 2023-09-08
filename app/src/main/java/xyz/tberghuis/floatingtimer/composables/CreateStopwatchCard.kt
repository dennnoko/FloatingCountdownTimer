package xyz.tberghuis.floatingtimer.composables

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.tberghuis.floatingtimer.INTENT_COMMAND
import xyz.tberghuis.floatingtimer.INTENT_COMMAND_STOPWATCH_CREATE
import xyz.tberghuis.floatingtimer.R
import xyz.tberghuis.floatingtimer.logd
import xyz.tberghuis.floatingtimer.service.FloatingService
import xyz.tberghuis.floatingtimer.viewmodels.HomeViewModel

// アプリ内のストップウォッチのメニュー
@Composable
fun CreateStopwatchCard() {
  val context = LocalContext.current
  val vm: HomeViewModel = viewModel()
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(15.dp),
  ) {
    Row(
      modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
      Text(stringResource(id = R.string.stopwatch) , fontSize = 20.sp)        // 文字列は stringResource での管理なんだな～
    }
    Row(
      modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
      Button(modifier = Modifier.padding(top = 10.dp), onClick = {
        logd("start stopwatch")       // 動作関係は実行時にlogの出力をさせる
        if (!Settings.canDrawOverlays(context)) {       // アプリの上に重ねて表示が許可されているかの確認をしている
          vm.showGrantOverlayDialog = true        // HomeViewModel で状態を保持している rememberMutableStateOf の変数。これでButtonが表示されているかを管理しているっぽい
          return@Button       // ラベル構文 4行上の Button に戻る。 @Button 無しでどうなるか確認したところ、ここではreturnが許可されていないという結果になった
        }
        startStopwatchService(context)        // 下で定義された関数。
      }) {
        Text(stringResource(id = R.string.create))
      }
    }
  }
}

fun startStopwatchService(context: Context) {
  val intent = Intent(context.applicationContext, FloatingService::class.java)
  intent.putExtra(INTENT_COMMAND, INTENT_COMMAND_STOPWATCH_CREATE)
  context.startForegroundService(intent)
}