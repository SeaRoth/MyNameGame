package wt.cr.com.mynamegame.infrastructure.ui.home

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import wt.cr.com.mynamegame.domain.model.Profile

class PersonViewModel(profile: Profile): ViewModel(){
    //observables
    val number = ObservableField<String>("-1")
    val id = ObservableField<String>(profile.id)
    val first = ObservableField<String>(profile.firstName)
    val last = ObservableField<String>(profile.lastName)
    val full = ObservableField<String>(profile.firstName + " " + profile.lastName)
    val url = ObservableField<String>("http:${profile.headshot?.url}")
    var visible = ObservableBoolean(true)
}