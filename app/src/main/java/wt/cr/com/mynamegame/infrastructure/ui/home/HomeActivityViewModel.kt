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
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataAction
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataActionWithData
import wt.cr.com.mynamegame.infrastructure.common.utils.getString
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.client.ApiClient
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepo
import wt.cr.com.mynamegame.infrastructure.ui.home.cards.UpdatableItem
import java.util.*
import java.util.concurrent.ThreadLocalRandom

enum class CurrentGameMode {
    NORMAL, MATT, HINT, CUSTOM, ERROR
}
const val PREFS_SCORE = "wt.cr.com.mynamegame.scoreprefs"
const val FIRST_TIME_OPEN_KEY = "firstTimeOpenApp"
const val FIRST_TIME_MATT_KEY = "firstTimeMatt"
const val FIRST_TIME_STATS_KEY = "firstTimeStats"

const val HIGH_SCORE_NORMAL_KEY = "highscorenormal"
const val HIGH_SCORE_MATT_KEY = "highscorematt"
const val HIGH_SCORE_CUSTOM_KEY = "highscorehint"
const val LIFETIME_CORRECT_KEY = "lifetime.correct"
const val LIFETIME_INCORRECT_KEY = "lifetime.incorrect"
class HomeActivityViewModel(app: Application) : AndroidViewModel(app) {

    private var prefs: SharedPreferences = WTServiceLocator.resolve(SharedPreferences::class.java)
    private var disposable: Disposable? = null
    private lateinit var rainbow200: IntArray

    //actions
    val loadStatAction         = LiveDataAction()
    val apiErrorAction         = LiveDataActionWithData<String>()
    val normalErrorAction      = LiveDataActionWithData<String>()
    //Live data
    val loadPeopleAction       = MutableLiveData<List<UpdatableItem>>()
    val shuffleProfilesAction  = MutableLiveData<List<UpdatableItem>>()
    val loadScoreAction        = MutableLiveData<ScoreViewModel>()

    val numberCorrect          = MutableLiveData<Int>()
    val highScore              = MutableLiveData<Int>()
    val currentStreak          = MutableLiveData<Int>()
    val lifetimeCorrect        = MutableLiveData<Int>()
    val lifetimeIncorrect      = MutableLiveData<Int>()
    val hasUserGuessed         = MutableLiveData<Boolean>()
    var updatableItemList:   MutableList<UpdatableItem> = mutableListOf()

    private var profiles: MutableList<MyModel.Person> = mutableListOf()
    //Observables
    val selectedGameMode     = ObservableField<CurrentGameMode>(CurrentGameMode.NORMAL)
    val shuffleProfiles      = ObservableBoolean(true)
    val showLoadingIndicator = ObservableBoolean(true)
    val isGameStarted        = ObservableBoolean(false)
    val showButton0          = ObservableBoolean(true)
    val showButton1          = ObservableBoolean(true)
    val showButton2          = ObservableBoolean(true)
    val showButton3          = ObservableBoolean(true)
    val showButton4          = ObservableBoolean(true)
    val showButton5          = ObservableBoolean(true)
    val questionText         = ObservableField<String>()
    val numberCorrectField   = ObservableField<String>(getString(R.string.correct_field_ext, getString(R.string.zero)))
    val highScoreField       = ObservableField<String>(getString(R.string.high_score_field_ext, getString(R.string.zero)))
    val secondsLeftField     = ObservableField<String>()
    //Repo
    val humanRepo get() = WTServiceLocator.resolve(HumanRepo::class.java)

    init {
        rainbow200 = app.resources.getIntArray(R.array.rainbow_200)
        setCorrect(0)
        val high = prefs.getInt(HIGH_SCORE_NORMAL_KEY,0)
        highScore.postValue(high)
        setHigh(high)
        lifetimeCorrect.value = prefs.getInt(LIFETIME_CORRECT_KEY,0)
        lifetimeIncorrect.value = prefs.getInt(LIFETIME_INCORRECT_KEY,0)
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
                                setShuffleTimer()
                            },
                            { error ->
                                errorMode(error.localizedMessage)
                            }
                    )
        }
    }

    /**
     * UI
     */

    fun showScores(){
        loadStatAction.actionOccurred()
    }
    private fun onImageClick(person: UpdatableItem){
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
        val mViewModel = updatableItemList[index]
        mViewModel.let { onImageClick(it) }
    }

    fun onStartClicked(){
        if(isGameStarted.get()){
            //TURN OFF
            isGameStarted.set(false)
            hasUserGuessed.postValue(false)
        }else{
            if(selectedGameMode.get() == CurrentGameMode.CUSTOM){
                normalErrorAction.actionOccurred(getString(R.string.error_not_setup))
            }else {
                //TURN ON
                isGameStarted.set(true)
                if (hasUserGuessed.value == true) {
                    resetGame()
                }
                loadPeopleAction.postValue(updatableItemList)

                if (selectedGameMode.get() == CurrentGameMode.HINT) {
                    setHintTimer()
                }
            }
        }
    }

    private fun setHintTimer(){
        if(isGameStarted.get()){
            HintTimer(0, 8000)
        }
    }

    private fun setShuffleTimer(){
        ShuffleTimer(0,5000)
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
            prefs.edit().putInt(HIGH_SCORE_NORMAL_KEY,0).apply()
            setHigh(0)
        }
    }

    private fun onViewStatsClick(){
        loadStatAction.actionOccurred()
    }
    /**
     * UI
     */

    /**
     * QUESTION / ANSWER LOGIC
     */
    private lateinit var theAnswer: UpdatableItem
    private fun setQuestionText(){
        val num = (0 until updatableItemList.size-1).random()
        theAnswer = updatableItemList[num]
        questionText.set(getString(R.string.who_is_field, theAnswer.pvm?.full?.get()))
    }

    private fun showScoreSection(){
        hasUserGuessed.postValue(false)
        val sorted = correcTable.toList().sortedBy { (_, value) -> value }
        val vm = profiles.firstOrNull { it.id.equals(sorted[0].first) }
        launch(UI) {
            val score =
                    MyModel.Score(
                    numberCorrect.value?:0,
                    highScore.value?:0,
                    vm?.firstName,
                    lifetimeCorrect.value?:0,
                    lifetimeIncorrect.value?:0)
            val scoreViewModel = ScoreViewModel(
                    score,
                    this@HomeActivityViewModel::onViewStatsClick,
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
        lifetimeCorrect.value = lifetimeCorrect.value?.plus(1)

        if(correcTable.containsKey(theAnswer.pvm?.id?.get())) {
            val someAnswer = correcTable[theAnswer.pvm?.id?.get()?:""]?.plus(1)?:-1
            correcTable[theAnswer.pvm?.id?.get() ?: ""] = someAnswer
        }else{
            correcTable[theAnswer.pvm?.id?.get() ?: ""] = 1
        }

        launch(UI) {
            val newScore = numberCorrect.value?.plus(1)
            if(newScore?.compareTo(currentStreak.value?:0)?:0 > 0){
                currentStreak.value = newScore
                if(newScore?.compareTo(highScore.value?:0)?:0 > 0){ //
                    prefs.edit().putInt(HIGH_SCORE_NORMAL_KEY, newScore?:0).apply()
                    setHigh(newScore?:-1)
                }
            }
            setCorrect(newScore?:-1)
            isGameStarted.set(false)
            resetGame()
            showScoreSection()
        }
        prefs.edit().putInt(LIFETIME_CORRECT_KEY, lifetimeCorrect.value?:0).apply()
    }

    private fun answerIncorrect(person: UpdatableItem){
        if(selectedGameMode.get() == CurrentGameMode.HINT){
            setHintTimer()
        }

        lifetimeIncorrect.value = lifetimeIncorrect.value?.plus(1)
        setCorrect(0)
        if(updatableItemList.size < 2){
            resetGame()
        }else{
            val i = updatableItemList.indexOfFirst { it.id.equals(person.id) }
            showHideBottomButtons(i)
            updatableItemList.remove(updatableItemList[i])
            loadPeopleAction.postValue(updatableItemList)
        }
        prefs.edit().putInt(LIFETIME_INCORRECT_KEY, lifetimeIncorrect.value?:0).apply()
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
        updatableItemList.clear()
        updatableItemList.addAll(profiles
                .shuffled()
                .asSequence()
                .filter { !(it.headshot?.url?.contains("TEST1", true)?:false) }
                .take(6)
                .mapIndexed{i, it ->
                    UpdatableItem(rainbow200[Random().nextInt(rainbow200.size)], i+1, PersonViewModel(it), this::onPersonClick, this::imageErrorLoading) }
                .toList())
        setQuestionText()
        loadPeopleAction.postValue(updatableItemList)
    }

    private fun imageErrorLoading(id: String){
        val v = updatableItemList.firstOrNull { it.pvm?.id?.get() == (id) }
        normalErrorAction.actionOccurred(getString(R.string.error_loading_two, v?.pvm?.first?.get(), v?.pvm?.id?.get()))
        val indexForBottomButton = updatableItemList.indexOf(v)
        showHideBottomButtons(indexForBottomButton)
        v.let {
            val i = profiles.indexOfFirst { it.id == id }
            i.let {
                profiles.removeAt(i)
            }
            updatableItemList.remove(v)
            if(v == theAnswer) //MAKE SURE PERSON REMOVING WASNT THE ANSWER
                setQuestionText()
            loadPeopleAction.postValue(updatableItemList)
        }
    }

    fun mattMode() {
        selectedGameMode.set(CurrentGameMode.MATT)
        updatableItemList.clear()
        updatableItemList.addAll(profiles
                .shuffled()
                .asSequence()
                .filter {
                    it.firstName.equals(getString(R.string.matt), true) ||
                            it.firstName.equals(getString(R.string.matthew), true) ||
                            it.firstName.equals(getString(R.string.mat), true)
                }
                .mapIndexed{i, it ->
                    UpdatableItem(rainbow200[Random().nextInt(rainbow200.size)], i+1, PersonViewModel(it), this::onPersonClick, this::imageErrorLoading)}
                .take(6)
                .toList())
        setQuestionText()
        loadPeopleAction.postValue(updatableItemList)
    }

    private fun onPersonClick(id: String){
        val winner = updatableItemList.firstOrNull() {
            it.pvm?.id?.get() == id
        }
        if(isGameStarted.get()) {
            winner?.let { onImageClick(it) }
        }else {
            //TODO: Show name for five seconds


        }
    }

    fun hintMode(){
        selectedGameMode.set(CurrentGameMode.HINT)
    }

    fun fourMode(){
        selectedGameMode.set(CurrentGameMode.CUSTOM)
    }

    private fun errorMode(error: String){
        selectedGameMode.set(CurrentGameMode.ERROR)
        showLoadingIndicator.set(false)
        apiErrorAction.actionOccurred(error)
    }

    /**
     * GAME MODES
     */
    override fun onCleared() {

    }

    fun IntRange.random() =
            ThreadLocalRandom.current().nextInt((endInclusive + 1) - start) +  start


    private fun HintTimer(num: Int, time: Long) {

        val countDownTimer = object : CountDownTimer(time, 1000) {

            override fun onTick(secondsUntilDone: Long) {
                if(isGameStarted.get()) secondsLeftField.set("${secondsUntilDone/1000}")
                else this.cancel()
            }

            override fun onFinish() {
                if(updatableItemList.size > 2 && isGameStarted.get()){
                    val newList = updatableItemList.toMutableList()
                    newList.remove(theAnswer)
                    val personToRemove = updatableItemList.get((0 until updatableItemList.size-1).random())
                    updatableItemList.remove(personToRemove)
                    loadPeopleAction.postValue(updatableItemList)
                    setHintTimer()
                }else
                    this.cancel()
            }
        }.start()}

    fun shuffleVisibleProfiles() {
        ArrayList(updatableItemList).apply {
            shuffle()
            shuffleProfilesAction.postValue(this)
        }
        ShuffleTimer(0, (9000..20000).random().toLong())
    }

    private fun ShuffleTimer(num: Int, time: Long) {

        val countDownTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(secondsUntilDone: Long) {}
            override fun onFinish() {
                if(shuffleProfiles.get())
                    shuffleVisibleProfiles()
            }
        }.start()}
}
