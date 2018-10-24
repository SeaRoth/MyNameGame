package wt.cr.com.mynamegame.infrastructure.ui.stats

import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.ItemHighScoreHeaderBinding

class HighScoreHeaderItem constructor(var statsActivityViewModel: StatsActivityViewModel) : BindableItem<ViewDataBinding>() {

    override fun bind(viewBinding: ViewDataBinding, position: Int) {
        (viewBinding as ItemHighScoreHeaderBinding).activityViewModel = statsActivityViewModel
    }

    override fun getLayout(): Int {
        return R.layout.item_high_score_header
    }
}