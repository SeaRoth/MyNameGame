package wt.cr.com.mynamegame.infrastructure.ui.home.cards

import android.support.annotation.ColorInt
import wt.cr.com.mynamegame.infrastructure.ui.home.PersonViewModel

open class SmallCardItem : CardItem {

    constructor(@ColorInt colorRes: Int,
                text: CharSequence,
                url: PersonViewModel,
                cb: (String) -> Unit,
                cbErrorImage: (String) -> Unit
    ) :
            super(colorRes, text, url, cb, cbErrorImage) {}

    override fun getSpanSize(spanCount: Int, position: Int): Int {
        return spanCount / 2
    }
}
