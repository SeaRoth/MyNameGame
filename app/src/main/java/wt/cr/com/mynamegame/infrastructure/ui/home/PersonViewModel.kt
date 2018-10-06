package wt.cr.com.mynamegame.infrastructure.ui.home

import android.databinding.ObservableField
import android.databinding.ViewDataBinding
import com.xwray.groupie.databinding.BindableItem
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataActionWithData
import wt.cr.com.mynamegame.infrastructure.ui.BaseBindableViewModel

class PersonViewModel(private val person: MyModel.Person): BaseBindableViewModel(){

    //observables
    val name = ObservableField<String>(person.firstName)
    val url = ObservableField<String>("http:${person.headshot.url}")

    //actions
    val personClicked = LiveDataActionWithData<MyModel.Person>()

    override fun getItemFactory(): (BaseBindableViewModel) -> BindableItem<ViewDataBinding> {
        return { it -> PersonItem((it as PersonViewModel)) }
    }

    fun selectAnswer(){
        personClicked.actionOccurred(person)
    }
}