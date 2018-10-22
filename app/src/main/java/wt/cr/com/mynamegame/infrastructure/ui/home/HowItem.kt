package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.ItemHowBinding
import wt.cr.com.mynamegame.databinding.ItemStatBinding

class HowItem constructor(var howViewModel: HowViewModel?) : BindableItem<ViewDataBinding>(){
    override fun getLayout(): Int {
        return R.layout.item_how
    }

    override fun bind(viewBinding: ViewDataBinding, position: Int) {
        (viewBinding as ItemHowBinding).viewModel = howViewModel
    }
}