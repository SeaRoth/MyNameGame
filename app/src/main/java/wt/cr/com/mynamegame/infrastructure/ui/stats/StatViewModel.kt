package wt.cr.com.mynamegame.infrastructure.ui.stats

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.ui.BaseBindableViewModel

class StatViewModel(val isE: Boolean,
        private val player: MyModel.Player,
                    val personClickListener:
                    (StatViewModel) -> Unit): BaseBindableViewModel(){

    val name        = ObservableField<String>(player.name)
    val location    = ObservableField<String>("${player.location}")
    val score       = ObservableField<String>("${player.highScore}")
    val twoCents    = ObservableField<String>(player.twoCents)

    val isEven = ObservableBoolean(isE)

    override fun getItemFactory(): (BaseBindableViewModel) -> BindableItem<ViewDataBinding> {
        return { it -> StatItem((it as StatViewModel)) }
    }

    fun onClick(){
        personClickListener(this)
    }
}