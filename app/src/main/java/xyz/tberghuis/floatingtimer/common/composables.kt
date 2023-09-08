package xyz.tberghuis.floatingtimer.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

// 時間を秒で受け取って 00:00 の形式で表示するコンポーザブル
@Composable
fun TimeDisplay(totalSeconds: Int) {
  val minutes = totalSeconds / 60 // 分
  val seconds = totalSeconds % 60 // 秒
  Text("${formatIntTimerDisplay(minutes)}:${formatIntTimerDisplay(seconds)}")
}

// 数値を2桁にして返す関数
fun formatIntTimerDisplay(t: Int): String {
  return t.toString().padStart(2, '0') // padStart の length で String の長さを指定し、padChar で空いた空間を何で埋めるか指定する(Char型)
}