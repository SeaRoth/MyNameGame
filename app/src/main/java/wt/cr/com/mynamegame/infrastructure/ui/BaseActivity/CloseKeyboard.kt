package wt.cr.com.mynamegame.infrastructure.ui.BaseActivity

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun closeKeyboard(view: View, applicationContext: Context) {
    val inputMethodManager = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}