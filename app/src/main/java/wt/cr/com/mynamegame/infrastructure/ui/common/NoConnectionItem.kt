package wt.cr.com.mynamegame.infrastructure.ui.common

import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.ItemNoConnectionBinding

class NoConnectionItem constructor(var noConnectionViewModel: NoConnectionViewModel?) :BindableItem<ViewDataBinding>(){
    override fun bind(viewBinding: ViewDataBinding, position: Int) {
        (viewBinding as ItemNoConnectionBinding).viewModel = noConnectionViewModel
    }

    override fun getLayout(): Int {
        return R.layout.item_no_connection
    }
}