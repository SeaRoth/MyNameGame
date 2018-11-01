package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ObservableField
import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import timber.log.Timber
import wt.cr.com.mynamegame.domain.model.Score
import wt.cr.com.mynamegame.infrastructure.ui.BaseBindableViewModel

class HowViewModel(private val score: Score,
                   val callbackViewLocal: (HowViewModel) -> Unit,
                   val callbackUpload: (HowViewModel) -> Unit,
                   val callbackViewWorld: (HowViewModel) -> Unit)
    : BaseBindableViewModel(){

    //observables
    val lifetimePercentage = ObservableField<String>()

    init {
        val percentage = score.lifetimeCorrect/score.lifetimeIncorrect
        lifetimePercentage.set("$percentage")
    }

    override fun getItemFactory(): (BaseBindableViewModel) -> BindableItem<ViewDataBinding> {
        return { it -> HowItem((it as HowViewModel)) }
    }

    fun onStartClicked(){
        Timber.i( "WHJAT")
    }

    fun onClickScoreView(){
        callbackViewLocal(this)
    }

    fun onClickScoreUpload(){
        callbackUpload(this)
    }

    fun onClickScoreViewWorld(){
        callbackViewWorld(this)
    }
}