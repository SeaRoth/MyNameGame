package wt.cr.com.mynamegame.infrastructure.ui.common

import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.infrastructure.ui.BaseBindableViewModel

class NoConnectionViewModel(val callbackRetry: () -> Unit)
    : BaseBindableViewModel(){
    override fun getItemFactory(): (BaseBindableViewModel) -> BindableItem<ViewDataBinding> {
        return { it -> NoConnectionItem((it as NoConnectionViewModel))}
    }

    fun retryConnectionClick(){
        callbackRetry()
    }

}