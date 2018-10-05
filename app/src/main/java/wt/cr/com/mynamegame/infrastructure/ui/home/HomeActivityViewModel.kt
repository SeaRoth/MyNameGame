package wt.cr.com.mynamegame.infrastructure.ui.home

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableBoolean
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
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepo

class HomeActivityViewModel(app: Application) : AndroidViewModel(app) {

//    private var disposable: Disposable? = null

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

    val normalSelected = ObservableBoolean(true)
    val mattSelected = ObservableBoolean(false)
    val hintSelected = ObservableBoolean(false)
    val fourSelected = ObservableBoolean(false)


    //data
    private lateinit var peopleViewModelList: ArrayList<PersonViewModel>

    //REPO
    val humanRepo get() = WTServiceLocator.resolve(HumanRepo::class.java)

    init {
        loadData()
    }

    fun loadData() {
        launch(UI) {
            showLoadingIndicator.set(true)
            val p = MyModel.Person("1337", "Curry")
            val vm = PersonViewModel(p)
            peopleViewModelList = ArrayList()

            peopleViewModelList.add(vm)
            normalMode()
//            disposable = WTServiceLocator.resolve(ApiClient::class.java).getProfiles()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            { result ->
//                                myPeople = result.query.coworker
//                                showLoadingIndicator.set(false)
//                                normalMode()
//                            },
//                            { error -> Timber.w("Sorry does not compute") }
//                    )
        }
    }

    fun selectClicked(){
        Timber.w("User clicked")
    }

    fun normalMode(){
        normalSelected.set(true)
        hintSelected.set(false)
        mattSelected.set(false)
        fourSelected.set(false)

        loadPeopleAction.postValue(peopleViewModelList)
    }

    fun mattMode(){
        normalSelected.set(false)
        hintSelected.set(false)
        mattSelected.set(true)
        fourSelected.set(false)
        launch(UI) {
            showLoadingIndicator.set(true)

        }
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
