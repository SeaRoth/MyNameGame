package wt.cr.com.mynamegame.infrastructure.ui.splash

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
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
        splashActivityViewModel.navigateAction.observe(this){
            navigateToHome()
        }
    }

    private fun navigateToHome(){
        startActivity(wt.cr.com.mynamegame.infrastructure.ui.home.HomeActivity.newIntent(this))
        finish()
    }
}