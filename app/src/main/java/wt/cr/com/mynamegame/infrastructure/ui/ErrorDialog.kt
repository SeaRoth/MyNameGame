package wt.cr.com.mynamegame.infrastructure.ui

import android.content.Context
import android.support.v7.app.AlertDialog
import wt.cr.com.mynamegame.R

fun showErrorDialog(context: Context, retryAction: () -> Unit) {
    AlertDialog.Builder(context).apply {
        setMessage(context.resources.getString(R.string.standard_error_message))
        setPositiveButton(context.resources.getString(R.string.try_again)) { dialog, which ->
            dialog.dismiss()
            retryAction()
        }
        setNegativeButton(android.R.string.cancel) { dialog, which ->
            dialog.cancel()
        }
        show()
    }
}

fun showErrorDialogWithOK(context: Context, messageStringId: Int) {
    AlertDialog.Builder(context).apply {
        setMessage(context.resources.getString(messageStringId))
        setPositiveButton(android.R.string.ok) { dialog, which ->
            dialog.dismiss()
        }
        show()
    }
}