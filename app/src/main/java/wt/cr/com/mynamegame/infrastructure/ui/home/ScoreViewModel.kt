package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ObservableField
import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.domain.model.Score
import wt.cr.com.mynamegame.infrastructure.ui.BaseBindableViewModel
import kotlin.math.roundToInt

class ScoreViewModel(private val score: Score,
                     val callbackShare: () -> Unit,
                     val callbackReset: (ScoreViewModel) -> Unit,
                     val callbackResetTop: (ScoreViewModel) -> Unit)
    : BaseBindableViewModel(){

    //observables
    val current   = ObservableField<String>("${score.current}")
    val high      = ObservableField<String>("${score.high}")
    val mostKnown = ObservableField<String>("${score.mostKnownPerson}")
    val lifetimeCorrect    = ObservableField<String>("${score.lifetimeCorrect}")
    val lifetimeIncorrect  = ObservableField<String>("${score.lifetimeIncorrect}")
    val lifetimePercentage = ObservableField<String>()

    init {
        val v1 = score.lifetimeCorrect.toDouble()
        val v2 = score.lifetimeIncorrect.toDouble() + v1
        val percentage = (v1.div(v2)*100).roundToInt()
        lifetimePercentage.set("$percentage%")
    }

    override fun getItemFactory(): (BaseBindableViewModel) -> BindableItem<ViewDataBinding> {
        return { it -> ScoreItem((it as ScoreViewModel)) }
    }

    fun scoreShare(){
        callbackShare()
    }

    fun scoreReset(){
        callbackReset(this)
    }

    fun scoreResetTop(){
        callbackResetTop(this)
    }
}