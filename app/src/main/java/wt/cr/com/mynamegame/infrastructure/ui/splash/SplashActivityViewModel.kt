package wt.cr.com.mynamegame.infrastructure.ui.splash

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableBoolean
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataAction
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class SplashActivityViewModel(app: Application): AndroidViewModel(app){

    val navigateAction = LiveDataAction()
    val showLoadingIndicator = ObservableBoolean(false)

    init {
        displayLoadingIndicator()
    }

    private fun displayLoadingIndicator(){
        launch(UI){
            delay(3000)
            showLoadingIndicator.set(true)
            goToHomeActivity()
        }
    }

    private fun goToHomeActivity(){
        launch(UI) {
            delay(2000)
            showLoadingIndicator.set(false)
            navigateAction.actionOccurred()
        }
    }
}