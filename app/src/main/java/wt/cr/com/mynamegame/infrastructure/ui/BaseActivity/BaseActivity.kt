package wt.cr.com.mynamegame.infrastructure.ui.BaseActivity

import android.animation.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.infrastructure.common.utils.dpToPx

const val ERROR_REGION = "Sorry, this feature is currently not available in your region."
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    fun showSnackBar(root: View? = null) {
        val rootView = this.window.decorView.findViewById<View>(android.R.id.content)

        Snackbar.make(rootView, R.string.connectivity_error, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings) {
                    startActivityForResult(Intent(android.provider.Settings.ACTION_SETTINGS), 0) }
                .show()
    }

    enum class ActionBarStyle {
        NO_BUTTON, UP_BUTTON, NAV_BUTTON, CLOSE_BUTTON
    }

    fun showApiError(result: String){
        val rootView = this.window.decorView.findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, R.string.standard_error_api, Snackbar.LENGTH_LONG) .show()
    }

    fun setupHomeAsUp(actionBarStyle : ActionBarStyle, iconOverride: Int? = null, statusColor: Int? = null) {
        supportActionBar?.let {
            when (actionBarStyle) {
                ActionBarStyle.UP_BUTTON -> {
                    it.setDisplayHomeAsUpEnabled(true)
                    it.setHomeAsUpIndicator(iconOverride ?: R.drawable.ic_back_white_normal)
                }
                ActionBarStyle.CLOSE_BUTTON -> {
                    it.setDisplayHomeAsUpEnabled(true)
                    it.setHomeAsUpIndicator(iconOverride ?: R.drawable.ic_close_white_normal)
                }
                ActionBarStyle.NAV_BUTTON -> {
                    it.setDisplayHomeAsUpEnabled(true)
                    it.setHomeAsUpIndicator(iconOverride ?: R.drawable.ic_menu_normal)
                }
                ActionBarStyle.NO_BUTTON -> {
                    it.setDisplayHomeAsUpEnabled(false)
                    // If we use this, need to scale the margin
                }

            }
        }
        changeStatusBarColor(statusColor)
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }

    fun changeStatusBarColor(color: Int?){
        color?.let {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, color)
        }
    }

    inline fun <reified T : Activity> createNavIntent(): Intent = Intent(this, T::class.java).addFlags(
            FLAG_ACTIVITY_REORDER_TO_FRONT
    )

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return true
    }

    fun saveUpdateView(view: TextView,label: String){
        view.text = label
        view.visibility = View.VISIBLE
        val animPos =  dpToPx(21).toFloat()
        val animationMs:Long = 800
        val startDelay:Long = 1000
        val propertyPositionForward = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, animPos)
        val propertyAlphaForward    = PropertyValuesHolder.ofFloat(View.ALPHA, 0f,1f)

        val forward = ObjectAnimator.ofPropertyValuesHolder(view, propertyPositionForward, propertyAlphaForward)
        forward.duration = animationMs
        forward.start()
        forward.addListener(object: AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                animation?.removeListener(this)
                val propertyPositionBack = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -animPos)
                val propertyAlphaBack    = PropertyValuesHolder.ofFloat(View.ALPHA, 1f,0f)
                val backward = ObjectAnimator.ofPropertyValuesHolder(view, propertyPositionBack, propertyAlphaBack)
                backward.duration = animationMs
                backward.startDelay = startDelay
                backward.start()
                backward.addListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        animation?.removeListener(this)
                        view.visibility = View.GONE
                    }
                })
            }
        })
    }

    fun closeSoftKeyboard(view: View){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    private fun appVersion(): String{
        return packageManager.getPackageInfo(packageName, 0).versionName
    }
}
typealias RetryFunction = (() -> Unit)