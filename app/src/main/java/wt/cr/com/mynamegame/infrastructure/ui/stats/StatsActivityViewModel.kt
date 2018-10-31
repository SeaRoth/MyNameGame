package wt.cr.com.mynamegame.infrastructure.ui.stats

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataActionWithData
import wt.cr.com.mynamegame.infrastructure.common.utils.getString
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.firestore.Firestore
import wt.cr.com.mynamegame.infrastructure.network.firestore.Firestore.Companion.DEFAULT_DOC_ID
import wt.cr.com.mynamegame.infrastructure.network.firestore.Firestore.Companion.DOC_ID_KEY
import wt.cr.com.mynamegame.infrastructure.ui.home.HIGH_SCORE_CUSTOM_KEY
import wt.cr.com.mynamegame.infrastructure.ui.home.HIGH_SCORE_MATT_KEY
import wt.cr.com.mynamegame.infrastructure.ui.home.HIGH_SCORE_NORMAL_KEY

enum class CurrentSortMode {
    NAME, LOCATION, TWOCENTS, SCORE
}

enum class CurrentFirebaseMode {
    NORMAL, MATT, CUSTOM
}

class StatsActivityViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = WTServiceLocator.resolve(SharedPreferences::class.java)

    //Data
    val loadStatAction                                 = MutableLiveData<MutableList<StatViewModel>>()
    private var highScores: MutableList<StatViewModel> = mutableListOf()
    private var players: MutableList<MyModel.Player>   = mutableListOf()
    var highScore: Int = 0
    var rank: String = "NA"
    var changeTitleAction = LiveDataActionWithData<Int>()
    var errorAction       = LiveDataActionWithData<String>()

    //Observables
    val showLoadingIndicator = ObservableBoolean(true)
    val selectedFirebaseMode = ObservableField<CurrentFirebaseMode>(CurrentFirebaseMode.NORMAL)
    val selectedSortMode     = ObservableField<CurrentSortMode>(CurrentSortMode.SCORE)

    private lateinit var auth: FirebaseAuth

    var found: DocumentSnapshot? = null
    var docId: String
    init {
        auth = FirebaseAuth.getInstance()
        docId = prefs.getString(DOC_ID_KEY, DEFAULT_DOC_ID)?:DEFAULT_DOC_ID
        loadData()
    }

    private fun setHighScoresAndLoadStats(){
        highScores.clear()
        var index = 0
        var mHighScore = highScore
        rank = getString(R.string.NA)

        players.forEach { vm ->
            index++
            if(vm.email == found?.getString(getString(R.string.email))) {
                rank = "$index"
                mHighScore = vm.highScore
            }
            highScores.add(StatViewModel(index%2==0, vm, this::statClicked))
        }
        loadStatAction.postValue(highScores)
        changeTitleAction.actionOccurred(mHighScore)
    }

    private fun loadData(){
        onNormalClick()
    }

    /**
     * normal, matt, custom
     */

    private fun makeRequest(collectionPath: String){
        launch(UI) {
            WTServiceLocator.resolve(FirebaseFirestore::class.java)
                .collection(collectionPath)
                .orderBy(getString(R.string.high_score_camel), Query.Direction.ASCENDING)
                .limit(10)
                .addSnapshotListener { documentSnapshot, error ->
                    players.clear()
                    showLoadingIndicator.set(false)
                    if (error != null) {
                        Timber.d("")
                    } else if (documentSnapshot != null) {
                        highScores.clear()
                        found = null

                        for ((index, i) in documentSnapshot.documents.withIndex()) {
                            if(docId == i.id)
                                found = i
                            auth.currentUser?.let {
                                val player = MyModel.Player(
                                        i.get(getString(R.string.name).toLowerCase()).toString(),
                                        it.email?:"",
                                                i.get(getString(R.string.location).toLowerCase()).toString(),
                                        Integer.parseInt(i.get(getString(R.string.high_score_camel)).toString()),
                                        i.get(getString(R.string.two_cents_camel)).toString())
                                players.add(player)
                            }

                        }
                        sortByScore()
                    }
                }
        }
    }

    private fun statClicked(statViewModel: StatViewModel){

    }

    fun onNormalClick(){
        highScore = prefs.getInt(HIGH_SCORE_NORMAL_KEY, 0)
        selectedFirebaseMode.set(CurrentFirebaseMode.NORMAL)
        makeRequest(getString(R.string.normal).toLowerCase())
    }

    fun onMattClick(){
        highScore = prefs.getInt(HIGH_SCORE_MATT_KEY, 0)
        selectedFirebaseMode.set(CurrentFirebaseMode.MATT)
        makeRequest(getString(R.string.matt).toLowerCase())
    }

    fun onCustomClick(){
        highScore = prefs.getInt(HIGH_SCORE_CUSTOM_KEY, 0)
        selectedFirebaseMode.set(CurrentFirebaseMode.CUSTOM)
        makeRequest(getString(R.string.custom).toLowerCase())
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