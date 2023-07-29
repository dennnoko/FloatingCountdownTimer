package xyz.tberghuis.floatingtimer.tmp

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import xyz.tberghuis.floatingtimer.logd

class MaccasOverlayController(val service: MaccasService) {

  val bubbleParams: WindowManager.LayoutParams


  init {
    logd("MaccasOverlayController init")
    bubbleParams = initBubbleLayoutParams(service)
  }
}

///////////////////////////////////////////

private fun initBubbleLayoutParams(context: Context): WindowManager.LayoutParams {
  val density = context.resources.displayMetrics.density
  val overlaySizePx = (MC.OVERLAY_SIZE_DP * density).toInt()
  return WindowManager.LayoutParams(
    overlaySizePx,
    overlaySizePx,
    0,
    0,
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
    PixelFormat.TRANSLUCENT
  )
}
