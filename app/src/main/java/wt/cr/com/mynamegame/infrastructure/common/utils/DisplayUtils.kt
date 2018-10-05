package wt.cr.com.mynamegame.infrastructure.common.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue




fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun spToPx(sp: Float, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
}