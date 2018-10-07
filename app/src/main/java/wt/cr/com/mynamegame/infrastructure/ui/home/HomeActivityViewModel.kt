package wt.cr.com.mynamegame.infrastructure.ui.home

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataAction
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.disposables.Disposable
//import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataActionWithData
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.client.ApiClient
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepo
import java.util.*

class HomeActivityViewModel(app: Application) : AndroidViewModel(app) {

    private var disposable: Disposable? = null

    //actions
    val apiErrorAction = LiveDataActionWithData<String>()
    val networkErrorAction = LiveDataAction()
    val loadPeopleAction = MutableLiveData<List<PersonViewModel>>()

    //observables
    val showLoadingIndicator = ObservableBoolean(true)
    val showPerson1 = ObservableBoolean(true)
    val showPerson2 = ObservableBoolean(true)
    val showPerson3 = ObservableBoolean(true)
    val showPerson4 = ObservableBoolean(true)
    val showPerson5 = ObservableBoolean(true)
    val showPerson6 = ObservableBoolean(true)

    val numberCorrectField = ObservableField<String>("Correct: 0")
    val numberIncorrectField = ObservableField<String>("Incorrect: 0")

    val normalSelected = ObservableBoolean(true)
    val mattSelected = ObservableBoolean(false)
    val hintSelected = ObservableBoolean(false)
    val fourSelected = ObservableBoolean(false)

    //data
    private lateinit var peopleViewModelList: ArrayList<PersonViewModel>

    //REPO
    val humanRepo get() = WTServiceLocator.resolve(HumanRepo::class.java)

    init {
        peopleViewModelList = ArrayList()
        loadData()
    }

    private lateinit var profiles: ArrayList<MyModel.Person>
    private fun loadData() {
        launch(UI) {
            showLoadingIndicator.set(true)
//            disposable = WTServiceLocator.resolve(ApiClient::class.java).getProfiles()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            { result ->
//                                profiles = result.data
//                                normalMode()
//                            },
//                            { error ->
//                                apiErrorAction.actionOccurred(error.localizedMessage)
//                            }
//                    )
            val something = humanRepo.getProfiles()
            disposable.
            something?.data?.let { profiles.addAll(it) }
            showLoadingIndicator.set(false)
        }
    }

    fun selectClicked(){
        Timber.w("User clicked")
    }

    fun normalMode(){
        peopleViewModelList.clear()
        normalSelected.set(true)
        hintSelected.set(false)
        mattSelected.set(false)
        fourSelected.set(false)
        var i = 1
        peopleViewModelList.addAll( profiles
                .map { PersonViewModel("${i++}",it,this::onPersonClick) }
                .take(6))
        loadPeopleAction.postValue(peopleViewModelList)
    }

    private fun onPersonClick(person: PersonViewModel){

    }

    fun mattMode(){
        normalSelected.set(false)
        hintSelected.set(false)
        mattSelected.set(true)
        fourSelected.set(false)
        showLoadingIndicator.set(true)
        peopleViewModelList.clear()
        peopleViewModelList.addAll(profiles.asSequence().
                filter { it.firstName
                        .equals("Matt", true)
                }
                .map {
                    PersonViewModel("1",it, this::onPersonClick)
                }
                .toList())
        loadPeopleAction.postValue(peopleViewModelList)
    }

    fun hintMode(){
        normalSelected.set(false)
        hintSelected.set(true)
        mattSelected.set(false)
        fourSelected.set(false)
        launch(UI) {
            showLoadingIndicator.set(true)
        }
    }

    fun fourMode(){
        normalSelected.set(false)
        hintSelected.set(false)
        mattSelected.set(false)
        fourSelected.set(true)
    }

    override fun onCleared() {

    }
}
