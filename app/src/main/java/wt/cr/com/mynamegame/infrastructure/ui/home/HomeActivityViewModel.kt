package wt.cr.com.mynamegame.infrastructure.ui.home

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.os.CountDownTimer
import android.os.Handler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataAction
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataActionWithData
import wt.cr.com.mynamegame.infrastructure.common.utils.app
import wt.cr.com.mynamegame.infrastructure.common.utils.getString
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.client.ApiClient
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepo
import java.util.*
import java.util.concurrent.ThreadLocalRandom

enum class CurrentGameMode {
    NONE, NORMAL, MATT, HINT, CUSTOM
}
const val PREFS_SCORE = "wt.cr.com.mynamegame.scoreprefs"
const val HIGH_SCORE_KEY = "highscore"
class HomeActivityViewModel(app: Application) : AndroidViewModel(app) {

    var prefs: SharedPreferences = app().getSharedPreferences(PREFS_SCORE, 0)
    private var disposable: Disposable? = null

    //actions
    val apiErrorAction      = LiveDataActionWithData<String>()
    val networkErrorAction  = LiveDataAction()
    val removeScoreSection  = LiveDataAction()
    val removePeopleSection = LiveDataAction()
    val normalErrorAction   = LiveDataActionWithData<String>()
    //live data
    val loadPeopleAction = MutableLiveData<List<PersonViewModel>>()
    val loadScoreAction  = MutableLiveData<ScoreViewModel>()
    val numberCorrect    = MutableLiveData<Int>()
    val highScore        = MutableLiveData<Int>()
    val currentStreak    = MutableLiveData<Int>()
    val showingPeople    = MutableLiveData<Boolean>()
    //data
    private var peopleViewModelList: MutableList<PersonViewModel>

    //booleans
    val showLoadingIndicator = ObservableBoolean(true)
    val isGameStarted        = ObservableBoolean(false)
    val hasUserGuessed       = ObservableBoolean(false)
    //strings
    val questionText       = ObservableField<String>("Who is: Mambo #5?")
    val numberCorrectField = ObservableField<String>(getString(R.string.correct_field_ext, getString(R.string.zero)))
    val highScoreField     = ObservableField<String>(getString(R.string.high_score_field_ext, getString(R.string.zero)))
    //game mode
    val selectedGameMode = ObservableField<CurrentGameMode>(CurrentGameMode.NONE)

    //repo
    val humanRepo get() = WTServiceLocator.resolve(HumanRepo::class.java)

    init {
        setCorrect(0)
        val high = prefs.getInt(HIGH_SCORE_KEY,0)
        highScore.postValue(high)
        setHigh(high)
        peopleViewModelList = mutableListOf()
        loadData()
    }

    private lateinit var profiles: ArrayList<MyModel.Person>
    private fun loadData() {
        launch(UI) {
            showLoadingIndicator.set(true)
            disposable = WTServiceLocator.resolve(ApiClient::class.java).getProfiles()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                profiles = result.data
                                normalMode()
                                showLoadingIndicator.set(false)
                            },
                            { error ->
                                apiErrorAction.actionOccurred(error.localizedMessage)
                                showLoadingIndicator.set(false)
                            }
                    )
        }
    }

    /**
     * UI
     */
    private fun onImageClick(person: PersonViewModel){
        hasUserGuessed.set(true)
        if(isGameStarted.get()) {
            if (person == theAnswer) {
                answerCorrect()
            } else {
                answerIncorrect(person)
            }
        }else{
            normalErrorAction.actionOccurred(getString(R.string.start_game_first))
        }
    }

    fun onBottomBarClick(index: Int){
        onImageClick(peopleViewModelList[index])
    }

    fun onStartClicked(){
        if(isGameStarted.get()){
            //TURN OFF
            isGameStarted.set(false)
            hasUserGuessed.set(false)
        }else{
            if(selectedGameMode.get() == CurrentGameMode.CUSTOM){
                normalErrorAction.actionOccurred("NOT SETUP!")
            }else {
                //TURN ON
                isGameStarted.set(true)
                if (showingPeople.value == false)
                    removeScoreSection.actionOccurred()
                showingPeople.value = true
                if (hasUserGuessed.get()) {
                    resetGame()
                }
                loadPeopleAction.postValue(peopleViewModelList)

                if (selectedGameMode.get() == CurrentGameMode.HINT) {
                    setTimer()
                }
            }
        }
    }

    private fun setTimer(){
        if(isGameStarted.get()){
            WordChanger(0, 8000)
        }
    }

    private fun onResetClick(scoreViewModel: ScoreViewModel){
        launch(UI){
            val zero = 0
            currentStreak.value = zero
            setCorrect(zero)
        }
    }

    private fun onResetTopClick(scoreViewModel: ScoreViewModel){
        launch(UI){
            prefs.edit().putInt(HIGH_SCORE_KEY,0).apply()
            setHigh(0)
        }
    }

    private fun onShareClick(scoreViewModel: ScoreViewModel){
        Timber.d("CLICKED!")
    }
    /**
     * UI
     */

    /**
     * QUESTION / ANSWER LOGIC
     */
    private lateinit var theAnswer: PersonViewModel
    private fun setQuestionText(){
        val num = (0 until peopleViewModelList.size-1).random()
        theAnswer = peopleViewModelList[num]
        questionText.set(getString(R.string.who_is_field, theAnswer.full.get()))
    }

    private fun showScoreSection(){
        showingPeople.value = false
        hasUserGuessed.set(false)

        val sorted = correcTable.toList().sortedBy { (_, value) -> value }
        val vm = profiles.firstOrNull { it.id.equals(sorted[0].first) }

        launch(UI) {
            val score = MyModel.Score(
                    numberCorrect.value?:0,
                    highScore.value?:0,
                    vm?.firstName)
            val scoreViewModel = ScoreViewModel(
                    score,
                    this@HomeActivityViewModel::onShareClick,
                    this@HomeActivityViewModel::onResetClick,
                    this@HomeActivityViewModel::onResetTopClick)
            loadScoreAction.postValue(scoreViewModel)
        }
    }

    private fun setCorrect(correct: Int){
        numberCorrect.value = correct
        numberCorrectField.set(getString(R.string.correct_field_ext, "$correct"))
    }

    private fun setHigh(high: Int){
        highScore.value = high
        highScoreField.set(getString(R.string.high_score_field_ext, "$high"))
    }
    /**
     * QUESTION / ANSWER LOGIC
     */

    /**
     *  CORRECT / INCORRECT
     */
    val correcTable = mutableMapOf<String, Int>()
    private fun answerCorrect(){

        if(correcTable.containsKey(theAnswer.id.get())) {
            val someAnswer = correcTable[theAnswer.id.get()?:""]?.plus(1)?:-1
            correcTable[theAnswer.id.get() ?: ""] = someAnswer
        }else{
            correcTable[theAnswer.id.get() ?: ""] = 1
        }

        launch(UI) {
            val newScore = numberCorrect.value?.plus(1)
            if(newScore?.compareTo(currentStreak.value?:0)?:0 > 0){
                currentStreak.value = newScore
                if(newScore?.compareTo(highScore.value?:0)?:0 > 0){ //
                    prefs.edit().putInt(HIGH_SCORE_KEY, newScore?:0).apply()
                    setHigh(newScore?:-1)
                }
            }
            setCorrect(newScore?:-1)
            isGameStarted.set(false)
            resetGame()
            removePeopleSection.actionOccurred()
            showScoreSection()
        }
    }

    private fun answerIncorrect(person: PersonViewModel){
        setCorrect(0)
        if(peopleViewModelList.size < 2){
            resetGame()
        }else{
            peopleViewModelList.remove(person)
            loadPeopleAction.postValue(peopleViewModelList)
        }
    }
    /**
     * CORRECT / INCORRECT
     */

    /**
     * GAME MODES
     */
    private fun resetGame(){
        when {
            selectedGameMode.get() == CurrentGameMode.NORMAL -> {
                normalMode()
            }
            selectedGameMode.get() == CurrentGameMode.MATT -> {
                mattMode()
            }
            selectedGameMode.get() == CurrentGameMode.HINT -> {
                hintMode()
            }
            else -> {

            }
        }
    }

    fun normalMode() {
        selectedGameMode.set(CurrentGameMode.NORMAL)
        peopleViewModelList.clear()
        var i = 1
        peopleViewModelList.addAll(profiles
                .shuffled()
                .asSequence()
                .take(6)
                .map { PersonViewModel("${i++}", it, this::onImageClick) }
                .toList())
        setQuestionText()
        loadPeopleAction.postValue(peopleViewModelList)
    }

    fun mattMode() {
        selectedGameMode.set(CurrentGameMode.MATT)
        peopleViewModelList.clear()
        var i = 1
        peopleViewModelList.addAll(profiles
                .shuffled()
                .asSequence()
                .filter {
                    it.firstName.equals(getString(R.string.matt), true) ||
                            it.firstName.equals(getString(R.string.matthew), true)
                }
                .map {PersonViewModel("${i++}", it, this::onImageClick)}
                .take(6)
                .toList())
        setQuestionText()
        loadPeopleAction.postValue(peopleViewModelList)
    }

    fun hintMode(){
        selectedGameMode.set(CurrentGameMode.HINT)
        launch(UI) {
        }
    }

    fun fourMode(){
        selectedGameMode.set(CurrentGameMode.CUSTOM)
    }
    /**
     * GAME MODES
     */

    override fun onCleared() {

    }

    fun IntRange.random() =
            ThreadLocalRandom.current().nextInt((endInclusive + 1) - start) +  start


    fun WordChanger(num: Int, time: Long) {

        val countDownTimer = object : CountDownTimer(time, 1000) {

            override fun onTick(secondsUntilDone: Long) {
            }

            override fun onFinish() {
                if(peopleViewModelList.size > 2){
                    val newList = peopleViewModelList.toMutableList()
                    newList.remove(theAnswer)
                    val personToRemove = peopleViewModelList.get((0 until peopleViewModelList.size-1).random())
                    peopleViewModelList.remove(personToRemove)
                    loadPeopleAction.postValue(peopleViewModelList)
                    setTimer()
                }
            }
        }.start()}
}

class Run {
    companion object {
        fun after(delay: Long, process: () -> Unit) {
            Handler().postDelayed({
                process()
            }, delay)
        }
    }
}
