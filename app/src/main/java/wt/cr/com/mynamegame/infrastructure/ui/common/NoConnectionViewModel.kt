package wt.cr.com.mynamegame.infrastructure.ui.common

import android.databinding.ObservableField
import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.infrastructure.ui.BaseBindableViewModel

class NoConnectionViewModel(val msg: String, val callbackRetry: () -> Unit) : BaseBindableViewModel(){

    val messageField = ObservableField<String>(msg)

    override fun getItemFactory(): (BaseBindableViewModel) -> BindableItem<ViewDataBinding> {
        return { it -> NoConnectionItem((it as NoConnectionViewModel))}
    }

    fun retryConnectionClick(){
        callbackRetry()
    }

}