package wt.cr.com.mynamegame.infrastructure.ui.splash

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataAction
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class SplashActivityViewModel(app: Application): AndroidViewModel(app){
    //Actions
    val animationAction = LiveDataAction()
    //Observables
    val showTopLogo          = ObservableBoolean(true)
    val cornerRadius         = ObservableInt(0)

    init {
        displayLoadingIndicator()
    }

    private fun displayLoadingIndicator(){
        launch(UI){
            animationAction.actionOccurred()        }
    }
}