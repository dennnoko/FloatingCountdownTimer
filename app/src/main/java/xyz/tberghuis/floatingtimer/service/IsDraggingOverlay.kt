package xyz.tberghuis.floatingtimer.service

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import xyz.tberghuis.floatingtimer.composables.Trash
import xyz.tberghuis.floatingtimer.testComposables.TestDraggingOverlay

@Composable
fun IsDraggingOverlay(overlayState: OverlayState) {
  Column(
    Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Bottom,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // オーバーレイ表示されたタイマーをドラッグしている時に表示されるゴミ箱
    Trash(overlayState)
  }
}