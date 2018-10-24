package wt.cr.com.mynamegame.infrastructure.ui.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.splash_activity.*
import wt.cr.com.mynamegame.infrastructure.ui.BaseActivity.BaseActivity
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.SplashActivityBinding

class SplashActivity : BaseActivity() {

    private val splashActivityViewModel: SplashActivityViewModel by lazy {
        ViewModelProviders.of(this).get(SplashActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<SplashActivityBinding>(this, R.layout.splash_activity).apply{
            viewModel = splashActivityViewModel
        }

        splashActivityViewModel.animationAction.observe(this){
            animateLogo()
        }
    }

    private fun animateLogo(){
        val centerY = (cv_splash.height/2).toFloat() - (cl_splash.height/2).toFloat()
        val animationMs:Long = 800
        val propertyPositionForward = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, centerY)
        val propertyAlphaForward    = PropertyValuesHolder.ofFloat(View.ALPHA, 0f,1f)
        val forward = ObjectAnimator.ofPropertyValuesHolder(cv_splash, propertyPositionForward, propertyAlphaForward)
        forward.duration = animationMs
        forward.start()
        forward.addListener(object: AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                animation?.removeListener(this)
                splashActivityViewModel.showTopLogo.set(false)
                splashActivityViewModel.cornerRadius.set(60)

                val imageViewObjectAnimator = ObjectAnimator.ofFloat(cv_splash,
                        "rotation", 0f, 360f)
                imageViewObjectAnimator.duration = 800
                imageViewObjectAnimator.start()

                imageViewObjectAnimator.addListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        animation?.removeListener(this)
                        navigateToHome()
                    }
                })
            }
        })
    }

    private fun navigateToHome(){
        startActivity(wt.cr.com.mynamegame.infrastructure.ui.home.HomeActivity.newIntent(this))
        finish()
    }
}