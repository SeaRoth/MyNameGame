package wt.cr.com.mynamegame.infrastructure.common.utils

import android.arch.lifecycle.AndroidViewModel
import android.support.annotation.StringRes
import android.widget.EditText
import wt.cr.com.mynamegame.infrastructure.NameGameApplication

fun AndroidViewModel.app(): NameGameApplication {
    return getApplication()
}

fun AndroidViewModel.getString(@StringRes stringRes: Int): String {
    return app().getString(stringRes)
}

fun AndroidViewModel.getString(@StringRes stringRes: Int, vararg args: Any?): String {
    return app().getString(stringRes, *args)
}

fun EditText.on(actionId: Int, func: () -> Unit) {
    setOnEditorActionListener { _, receivedActionId, _ ->

        if (actionId == receivedActionId) {
            func()
        }

        true
    }
}
