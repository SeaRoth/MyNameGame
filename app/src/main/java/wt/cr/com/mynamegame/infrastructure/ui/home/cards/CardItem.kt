package wt.cr.com.mynamegame.infrastructure.ui.home.cards

import android.support.annotation.ColorInt
import com.squareup.picasso.Picasso

import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_card.*

import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.ItemCardBinding
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator

val INSET_TYPE_KEY = "inset_type"
val INSET = "inset"
open class CardItem (@ColorInt private val colorRes: Int,
                     val text: CharSequence? = "",
                     val url: String? = "") : Item() {
    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.item_card

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.root.setBackgroundColor(colorRes)

        viewHolder.layout_one_item.minWidth = 480
        viewHolder.layout_one_item.minHeight = 480

        viewHolder.text.text = text
        viewHolder.tv_number.text = text

        WTServiceLocator.resolve(Picasso::class.java)
                .load(url)
                .error(R.drawable.baseline_error_black_48)
                .placeholder(R.drawable.progress_animation)
                .resize(470,470)
                .centerCrop()
                .into(viewHolder.iv_person)
    }
}