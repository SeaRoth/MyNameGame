package wt.cr.com.mynamegame.infrastructure.ui.home

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import wt.cr.com.mynamegame.domain.model.MyModel

class PersonViewModel(person: MyModel.Person): ViewModel(){
    //observables
    val number = ObservableField<String>("-1")
    val id = ObservableField<String>(person.id)
    val first = ObservableField<String>(person.firstName)
    val last = ObservableField<String>(person.lastName)
    val full = ObservableField<String>(person.firstName + " " + person.lastName)
    val url = ObservableField<String>("http:${person.headshot?.url}")
    var visible = ObservableBoolean(true)
}