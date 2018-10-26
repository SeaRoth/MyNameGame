package wt.cr.com.mynamegame.infrastructure.ui.stats

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator

enum class CurrentSortMode {
    NAME, LOCATION, TWOCENTS, SCORE
}
enum class CurrentFirebaseMode {
    NORMAL, MATT, HINT
}
class StatsActivityViewModel(app: Application) : AndroidViewModel(app) {

    //Data
    val loadStatAction                                 = MutableLiveData<MutableList<StatViewModel>>()
    private var highScores: MutableList<StatViewModel> = mutableListOf()
    var players: MutableList<MyModel.Player>           = mutableListOf()
    //Observables
    val showLoadingIndicator = ObservableBoolean(true)
    val selectedFirebaseMode = ObservableField<CurrentFirebaseMode>(CurrentFirebaseMode.NORMAL)
    val selectedSortMode     = ObservableField<CurrentSortMode>(CurrentSortMode.SCORE)

    init {
        loadData()
    }

    private fun setHighScoresAndLoadStats(){
        highScores.clear()
        var index = 0
        players.forEach { vm ->
            highScores.add(StatViewModel(index%2==0, vm, this::statClicked))
            index++
        }
        loadStatAction.postValue(highScores)
    }

    private fun loadData(){
        makeRequest("normal")
    }

    /**
     * normal, hint, matt
     */
    fun makeRequest(collectionPath: String){
        players.clear()
        launch(UI) {
            WTServiceLocator.resolve(FirebaseFirestore::class.java)
                    .collection(collectionPath)
                    .orderBy("highScore", Query.Direction.ASCENDING)
                    .limit(10)
                    .addSnapshotListener { documentSnapshot, error ->
                        showLoadingIndicator.set(false)
                        if (error != null) {
                            Timber.d("")
                        } else if (documentSnapshot != null) {
                            highScores.clear()
                            for ((index, i) in documentSnapshot.documents.withIndex()) {
                                val player = MyModel.Player(i.get("name").toString(), i.get("location").toString(), Integer.parseInt(i.get("highScore").toString()), i.get("twoCents").toString())
                                players.add(player)
                            }
                            sortByScore()
                        }
                    }
        }
    }

    private fun statClicked(statViewModel: StatViewModel){

    }

    fun onNormalClick(){
        selectedFirebaseMode.set(CurrentFirebaseMode.NORMAL)
        makeRequest("normal")
    }

    fun onMattClick(){
        selectedFirebaseMode.set(CurrentFirebaseMode.MATT)
        makeRequest("matt")
    }

    fun onHintClick(){
        selectedFirebaseMode.set(CurrentFirebaseMode.HINT)
        makeRequest("hint")
    }

    /**
     * SORTING
     */
    fun sortByName(){
        selectedSortMode.set(CurrentSortMode.NAME)
        val temp: MutableList<MyModel.Player> = mutableListOf()
        temp.addAll(players)
        players.clear()
        players.addAll(temp.sortedWith(compareBy<MyModel.Player>{ it.name }))
        setHighScoresAndLoadStats()
    }

    fun sortByLocation(){
        selectedSortMode.set(CurrentSortMode.LOCATION)
        val temp: MutableList<MyModel.Player> = mutableListOf()
        temp.addAll(players)
        players.clear()
        players.addAll(temp.sortedWith(compareBy<MyModel.Player>{ it.location }))
        setHighScoresAndLoadStats()
    }

    fun sortByCents(){
        selectedSortMode.set(CurrentSortMode.TWOCENTS)
        val temp: MutableList<MyModel.Player> = mutableListOf()
        temp.addAll(players)
        players.clear()
        players.addAll(temp.sortedWith(compareBy<MyModel.Player>{ it.twoCents }))
        setHighScoresAndLoadStats()
    }

    fun sortByScore(){
        selectedSortMode.set(CurrentSortMode.SCORE)
        val temp: MutableList<MyModel.Player> = mutableListOf()
        temp.addAll(players)
        players.clear()
        players.addAll(temp.sortedByDescending{ it.highScore })
        setHighScoresAndLoadStats()
    }

}