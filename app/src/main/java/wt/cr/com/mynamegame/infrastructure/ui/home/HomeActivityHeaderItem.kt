package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.HomeActivityHeaderItemBinding

class HomeActivityHeaderItem(private val homeActivityViewModel: HomeActivityViewModel) : BindableItem<ViewDataBinding>(){

    override fun getLayout(): Int {
        return R.layout.home_activity_header_item
    }

    override fun bind(viewBinding: ViewDataBinding, position: Int){
        (viewBinding as HomeActivityHeaderItemBinding).viewModel = homeActivityViewModel
    }
}
