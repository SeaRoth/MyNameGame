package wt.cr.com.mynamegame.infrastructure.ui.home.cards

import android.support.annotation.ColorRes

class UpdatableItem(@ColorRes colorRes: Int,
                    private val index: Int,
                    private val theUrl: String) : SmallCardItem(colorRes, index.toString(), theUrl) {

    override fun getId(): Long {
        return index.toLong()
    }
}
