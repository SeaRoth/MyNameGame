package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ObservableField
import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.ui.BaseBindableViewModel

class ScoreViewModel(private val score: MyModel.Score,
                     val callbackShare: (ScoreViewModel) -> Unit,
                     val callbackReset: (ScoreViewModel) -> Unit,
                     val callbackResetTop: (ScoreViewModel) -> Unit)
    : BaseBindableViewModel(){

    //observables
    val current   = ObservableField<String>("${score.current}")
    val high      = ObservableField<String>("${score.high}")
    val mostKnown = ObservableField<String>("${score.mostKnownPerson}")
    val lifetimeCorrect    = ObservableField<String>("${score.lifetimeCorrect}")
    val lifetimeIncorrect  = ObservableField<String>("${score.lifetimeIncorrect}")
    val lifetimePercentage = ObservableField<String>((score.lifetimeCorrect/score.lifetimeIncorrect*100).toString())

    override fun getItemFactory(): (BaseBindableViewModel) -> BindableItem<ViewDataBinding> {
        return { it -> ScoreItem((it as ScoreViewModel)) }
    }

    fun scoreShare(){
        callbackShare(this)
    }

    fun scoreReset(){
        callbackReset(this)
    }

    fun scoreResetTop(){
        callbackResetTop(this)
    }
}