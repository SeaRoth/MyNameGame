package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.ItemPersonBinding

class PersonItem constructor(var personViewModel: PersonViewModel?) : BindableItem<ViewDataBinding>() {
    override fun getLayout(): Int {
        return R.layout.item_person
    }

    override fun bind(viewBinding: ViewDataBinding, position: Int) {
        (viewBinding as ItemPersonBinding).viewModel = personViewModel
    }

    override fun getSpanSize(spanCount: Int, position: Int): Int {
        return spanCount / 2
    }
}