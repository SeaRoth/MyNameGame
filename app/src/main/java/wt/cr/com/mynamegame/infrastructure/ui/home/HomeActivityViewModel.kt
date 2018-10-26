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
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataAction
import wt.cr.com.mynamegame.infrastructure.common.utils.LiveDataActionWithData
import wt.cr.com.mynamegame.infrastructure.common.utils.getString
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.client.ApiClient
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepo
import wt.cr.com.mynamegame.infrastructure.ui.home.cards.UpdatableItem
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Predicate

enum class CurrentGameMode {
    NORMAL, MATT, HINT, CUSTOM, ERROR
}
const val PREFS_SCORE = "wt.cr.com.mynamegame.scoreprefs"
const val HIGH_SCORE_KEY = "highscore"
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
        rainbow200 = app.resources.getIntArray(R.array.rainbow_200)
        setCorrect(0)
        val high = prefs.getInt(HIGH_SCORE_KEY,0)
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
        //val mViewModel = updatableItemList.first{ it.id.get() == userId }
        mViewModel.let { onImageClick(it) }
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
                loadPeopleAction.postValue(updatableItemList)

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
                    prefs.edit().putInt(HIGH_SCORE_KEY, newScore?:0).apply()
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
        lifetimeIncorrect.value = lifetimeIncorrect.value?.plus(1)
        setCorrect(0)
        if(updatableItemList.size < 2){
            resetGame()
        }else{
            showHideBottomButtons(updatableItemList.size - 1)
            //updatableItemList.remove(person)
            updatableItemList.remove(updatableItemList.get(updatableItemList.indexOfFirst { it.id.equals(person.id) }))
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
        var i = 0
        updatableItemList.addAll(profiles
                .shuffled()
                .asSequence()
                .filter { !(it.headshot?.url?.contains("TEST1", true)?:false) }
                .take(6)
                .map { UpdatableItem(rainbow200[i], i++, PersonViewModel(it), this::onPersonClick)}
                .toList())
        setQuestionText()
        loadPeopleAction.postValue(updatableItemList)
    }

    fun mattMode() {
        selectedGameMode.set(CurrentGameMode.MATT)
        updatableItemList.clear()
        var i = 0
        updatableItemList.addAll(profiles
                .shuffled()
                .asSequence()
                .filter {
                    it.firstName.equals(getString(R.string.matt), true) ||
                            it.firstName.equals(getString(R.string.matthew), true)
                }
                .map { UpdatableItem(rainbow200[i], i++, PersonViewModel(it), this::onPersonClick)}
                .take(6)
                .toList())

        setQuestionText()
        loadPeopleAction.postValue(updatableItemList)
    }

    fun onPersonClick(id: String){
        val winner = updatableItemList.firstOrNull() {
            it.pvm?.id?.get() == id
        }
        Timber.d("You just clicked ${winner?.pvm?.first?.get()}")
        winner?.let { onImageClick(it) }
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


    fun HintTimer(num: Int, time: Long) {

        val countDownTimer = object : CountDownTimer(time, 1000) {

            override fun onTick(secondsUntilDone: Long) {
                secondsLeftField.set("${secondsUntilDone/1000}")
            }

            override fun onFinish() {
                if(updatableItemList.size > 2){
                    val newList = updatableItemList.toMutableList()
                    newList.remove(theAnswer)
                    val personToRemove = updatableItemList.get((0 until updatableItemList.size-1).random())
                    updatableItemList.remove(personToRemove)
                    loadPeopleAction.postValue(updatableItemList)
                    setTimer()
                }
            }
        }.start()}
}
