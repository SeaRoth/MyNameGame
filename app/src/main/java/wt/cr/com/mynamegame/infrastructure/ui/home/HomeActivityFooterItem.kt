package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.HomeActivityFooterItemBinding

class HomeActivityFooterItem(private val homeActivityViewModel: HomeActivityViewModel) : BindableItem<ViewDataBinding>(){

    override fun getLayout(): Int {
        return R.layout.home_activity_footer_item
    }

    override fun bind(viewBinding: ViewDataBinding, position: Int){
        (viewBinding as HomeActivityFooterItemBinding).viewModel = homeActivityViewModel
    }
}
