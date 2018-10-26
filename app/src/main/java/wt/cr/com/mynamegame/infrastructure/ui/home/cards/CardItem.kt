package wt.cr.com.mynamegame.infrastructure.ui.home.cards

import android.support.annotation.ColorInt
import com.squareup.picasso.Picasso

import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_card.*

import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.ui.home.PersonViewModel

val INSET_TYPE_KEY = "inset_type"
val INSET = "inset"
open class CardItem (@ColorInt private val colorRes: Int,
                     val text: CharSequence? = "",
                     val pvm: PersonViewModel?,
                     val callback: (String) -> Unit?) : Item() {
    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.item_card

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.root.setBackgroundColor(colorRes)
        viewHolder.root.setOnClickListener {
            callback(this.pvm?.id?.get()?:"")
        }
        val w = Math.ceil(viewHolder.root.context.resources.displayMetrics.widthPixels / 2.2).toInt()
        val h = Math.ceil(viewHolder.root.context.resources.displayMetrics.heightPixels / 4.4).toInt()
        viewHolder.layout_one_item.minWidth = w + 10
        viewHolder.layout_one_item.minHeight = h + 10
        viewHolder.text.text = text
        viewHolder.tv_number.text = text
        WTServiceLocator.resolve(Picasso::class.java)
                .load(pvm?.url?.get())
                .centerInside()
                .error(R.drawable.baseline_error_black_48)
                .placeholder(R.drawable.progress_animation)
                .resize(w,h)
                .into(viewHolder.iv_person)
    }
}