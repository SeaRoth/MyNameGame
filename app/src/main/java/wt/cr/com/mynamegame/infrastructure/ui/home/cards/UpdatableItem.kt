package wt.cr.com.mynamegame.infrastructure.ui.home.cards

import android.support.annotation.ColorRes
import wt.cr.com.mynamegame.infrastructure.ui.home.PersonViewModel

class UpdatableItem(@ColorRes colorRes: Int,
                    private val index: Int,
                    private val theUrl: PersonViewModel,
                    private val theCb: (String) -> Unit) : SmallCardItem(colorRes, index.toString(), theUrl, theCb) {

    override fun getId(): Long {
        return index.toLong()
    }
}
