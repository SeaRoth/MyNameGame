package wt.cr.com.mynamegame.infrastructure.ui.home

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.os.CountDownTimer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataActionWithData
import wt.cr.com.mynamegame.infrastructure.common.utils.getString
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.client.ApiClient
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepo
import java.util.concurrent.ThreadLocalRandom

enum class CurrentGameMode {
    NORMAL, MATT, HINT, CUSTOM
}
const val PREFS_SCORE = "wt.cr.com.mynamegame.scoreprefs"
const val HIGH_SCORE_KEY = "highscore"
class HomeActivityViewModel(app: Application) : AndroidViewModel(app) {

    private var prefs: SharedPreferences = WTServiceLocator.resolve(SharedPreferences::class.java)
            //app().getSharedPreferences(PREFS_SCORE, 0)
    private var disposable: Disposable? = null

    //actions
    val apiErrorAction         = LiveDataActionWithData<String>()
    val normalErrorAction      = LiveDataActionWithData<String>()
    //Live data
    val loadPeopleAction       = MutableLiveData<List<PersonViewModel>>()
    val loadScoreAction        = MutableLiveData<ScoreViewModel>()
    val numberCorrect          = MutableLiveData<Int>()
    val highScore              = MutableLiveData<Int>()
    val currentStreak          = MutableLiveData<Int>()
    val hasUserGuessed         = MutableLiveData<Boolean>()
    var peopleViewModelList:      MutableList<PersonViewModel> = mutableListOf()
    var peopleViewModelListFresh: MutableList<PersonViewModel> = mutableListOf()

    private lateinit var profiles: MutableList<MyModel.Person>
    //Observables
    val selectedGameMode    = ObservableField<CurrentGameMode>(CurrentGameMode.NORMAL)
    val showLoadingIndicator = ObservableBoolean(true)
    val isGameStarted        = ObservableBoolean(false)
    val showButton0          = ObservableBoolean(true)
    val showButton1          = ObservableBoolean(true)
    val showButton2          = ObservableBoolean(true)
    val showButton3          = ObservableBoolean(true)
    val showButton4          = ObservableBoolean(true)
    val showButton5          = ObservableBoolean(true)
    val questionText       = ObservableField<String>()
    val numberCorrectField = ObservableField<String>(getString(R.string.correct_field_ext, getString(R.string.zero)))
    val highScoreField     = ObservableField<String>(getString(R.string.high_score_field_ext, getString(R.string.zero)))
    val secondsLeftField   = ObservableField<String>()
    //Repo
    val humanRepo get() = WTServiceLocator.resolve(HumanRepo::class.java)

    init {
        setCorrect(0)
        val high = prefs.getInt(HIGH_SCORE_KEY,0)
        highScore.postValue(high)
        setHigh(high)
        loadData()
    }

    fun loadData() {
        launch(UI) {
            showLoadingIndicator.set(true)
            disposable = WTServiceLocator.resolve(ApiClient::class.java).getProfiles()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                showLoadingIndicator.set(false)
                                profiles = result.data
                                normalMode()
                            },
                            { error ->
                                showLoadingIndicator.set(false)
                                apiErrorAction.actionOccurred(error.localizedMessage)
                            }
                    )
        }
    }

    /**
     * UI
     */
    private fun onImageClick(person: PersonViewModel){
        hasUserGuessed.postValue(true)
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
        val userId = peopleViewModelListFresh[index].id.get()
        val mViewModel = peopleViewModelList.first{ it.id.get() == userId }
        onImageClick(mViewModel)
    }

    fun onStartClicked(){
        if(isGameStarted.get()){
            //TURN OFF
            isGameStarted.set(false)
            hasUserGuessed.postValue(false)
        }else{
            if(selectedGameMode.get() == CurrentGameMode.CUSTOM){
                normalErrorAction.actionOccurred("NOT SETUP!")
            }else {
                //TURN ON
                isGameStarted.set(true)
                if (hasUserGuessed.value == true) {
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
            HintTimer(0, 8000)
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
        hasUserGuessed.postValue(false)

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
            showScoreSection()
        }
    }

    private fun answerIncorrect(person: PersonViewModel){
        setCorrect(0)
        if(peopleViewModelList.size < 2){
            resetGame()
        }else{
            var index = peopleViewModelListFresh.indexOf(person)
            showHideBottomButtons(index)
            peopleViewModelList.remove(person)
            loadPeopleAction.postValue(peopleViewModelList)
        }
    }

    private fun showHideBottomButtons(hide: Int){
        when (hide) {
            6 -> {
                showButton0.set(true)
                showButton1.set(true)
                showButton2.set(true)
                showButton3.set(true)
                showButton4.set(true)
                showButton5.set(true)
            }5 -> {
                showButton5.set(false)
            }4 -> {
                showButton4.set(false)
            }3 -> {
                showButton3.set(false)
            }2 -> {
                showButton2.set(false)
            }1 -> {
                showButton1.set(false)
            }0 -> {
                showButton0.set(false)
            }
        }
    }
    /**
     * CORRECT / INCORRECT
     */

    /**
     * GAME MODES
     */
    private fun resetGame(){
        showHideBottomButtons(6)
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
                .filter { !(it.headshot.url?.contains("TEST1", true)?:false) }
                .take(6)
                .map { PersonViewModel("${i++}", it, this::onImageClick) }
                .toList())
        setFresh()
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
        setFresh()
        setQuestionText()
        loadPeopleAction.postValue(peopleViewModelList)
    }

    fun hintMode(){
        selectedGameMode.set(CurrentGameMode.HINT)
        setFresh()
        launch(UI) {
        }
    }

    fun fourMode(){
        selectedGameMode.set(CurrentGameMode.CUSTOM)
        setFresh()
    }

    private fun setFresh(){
        peopleViewModelListFresh.clear()
        peopleViewModelListFresh.addAll(peopleViewModelList)
    }
    /**
     * GAME MODES
     */

    override fun onCleared() {

    }

    fun IntRange.random() =
            ThreadLocalRandom.current().nextInt((endInclusive + 1) - start) +  start


    fun HintTimer(num: Int, time: Long) {

        val countDownTimer = object : CountDownTimer(time, 1000) {

            override fun onTick(secondsUntilDone: Long) {
                secondsLeftField.set("${secondsUntilDone/1000}")
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
