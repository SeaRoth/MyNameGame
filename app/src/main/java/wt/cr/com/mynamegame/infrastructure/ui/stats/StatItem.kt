package wt.cr.com.mynamegame.infrastructure.ui.stats

import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.ItemStatBinding
import wt.cr.com.mynamegame.infrastructure.ui.stats.StatViewModel

class StatItem constructor(var statViewModel: StatViewModel?) : BindableItem<ViewDataBinding>(){
    override fun getLayout(): Int {
        return R.layout.item_stat
    }

    override fun bind(viewBinding: ViewDataBinding, position: Int) {
        (viewBinding as ItemStatBinding).viewModel = statViewModel
    }
}