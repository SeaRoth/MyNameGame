package wt.cr.com.mynamegame.infrastructure.ui.home.cards

import android.support.annotation.ColorInt

open class SmallCardItem : CardItem {

    constructor(@ColorInt colorRes: Int) : super(colorRes) {}

    constructor(@ColorInt colorRes: Int, text: CharSequence, url: String) : super(colorRes, text, url) {}

    override fun getSpanSize(spanCount: Int, position: Int): Int {
        return spanCount / 2
    }
}
