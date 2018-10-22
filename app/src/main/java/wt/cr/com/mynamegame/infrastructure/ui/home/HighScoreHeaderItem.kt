package wt.cr.com.mynamegame.infrastructure.ui.home

import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_high_score_header.*
import wt.cr.com.mynamegame.R

class HighScoreHeaderItem constructor(var text: String? = "") : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.text.text = text
    }

    override fun getLayout(): Int {
        return R.layout.item_high_score_header
    }
}