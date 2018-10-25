package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.ui.BaseBindableViewModel

class PersonViewModel(numerino: String, person: MyModel.Person, val personClickListener: (PersonViewModel) -> Unit): BaseBindableViewModel(){
    //observables
    val number = ObservableField<String>(numerino)
    val id = ObservableField<String>(person.id)
    val first = ObservableField<String>(person.firstName)
    val last = ObservableField<String>(person.lastName)
    val full = ObservableField<String>(person.firstName + " " + person.lastName)
    val url = ObservableField<String>("http:${person.headshot?.url}")
    var visible = ObservableBoolean(true)

    override fun getItemFactory(): (BaseBindableViewModel) -> BindableItem<ViewDataBinding> {
        return { it -> PersonItem((it as PersonViewModel)) }
    }

    override fun getSpanSize(spanCount: Int, position: Int): Int {
        return spanCount / 3
    }

    fun onClick() {
        personClickListener(this)
    }


}