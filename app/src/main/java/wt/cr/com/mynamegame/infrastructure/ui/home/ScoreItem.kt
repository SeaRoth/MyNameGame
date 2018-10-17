package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.ItemScoreBinding

class ScoreItem constructor(var scoreViewModel: ScoreViewModel?) : BindableItem<ViewDataBinding>() {
    override fun getLayout(): Int {
        return R.layout.item_score
    }

    override fun bind(viewBinding: ViewDataBinding, position: Int) {
        (viewBinding as ItemScoreBinding).viewModel = scoreViewModel
    }
}