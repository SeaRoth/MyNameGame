package wt.cr.com.mynamegame.infrastructure.ui.stats

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.stats_activity.*
import timber.log.Timber
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.StatsActivityBinding
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.firestore.Firestore
import wt.cr.com.mynamegame.infrastructure.network.firestore.Firestore.Companion.DOC_ID_KEY
import wt.cr.com.mynamegame.infrastructure.ui.BaseActivity.BaseActivity

class StatsActivity : BaseActivity() {

    companion object {
        const val HIGH_SCORE_EXTRA = "high_score_extra"

        fun newIntent(context: Context, highScore: Int): Intent {
            val i = Intent(context, StatsActivity::class.java)
            i.putExtra(HIGH_SCORE_EXTRA, highScore)
            return i
        }
    }

    private val statsActivityViewModel : StatsActivityViewModel by lazy {
        ViewModelProviders.of(this).get(StatsActivityViewModel::class.java)
    }

    private val mainGroupAdapter = GroupAdapter<ViewHolder>()
    private var statsSection = Section()
    private var highScore: Int = 0
    private var docId: String = "-1"
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        highScore = intent.extras?.getInt(HIGH_SCORE_EXTRA)?:0
        prefs = WTServiceLocator.resolve(SharedPreferences::class.java)
        docId = prefs.getString(DOC_ID_KEY, "-1")?:"-1"

        DataBindingUtil.setContentView<StatsActivityBinding>(this, R.layout.stats_activity).apply {
            setSupportActionBar(toolbar)
            setupHomeAsUp(ActionBarStyle.UP_BUTTON)
            supportActionBar?.title = "Your Rank ..."
            activityViewModel = statsActivityViewModel
        }

        toolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        statsActivityViewModel.loadStatAction.observe(this, Observer {
            mainGroupAdapter.clear()
            statsSection = Section()
            statsSection.setHeader(HighScoreHeaderItem(statsActivityViewModel))
            mainGroupAdapter.add(statsSection)
            it?.let{ vm -> statsSection.update(vm) }
        })
        setupAdapter()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_add -> {
                if(docId == "-1")
                    addNewStatDialog(); return true
            }
            R.id.action_remove -> {
                if(docId != "-1")
                    areYouSureDeleteDialog(); return true
            }
            android.R.id.home -> {
                onBackPressed()
                finish(); return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun areYouSureDeleteDialog(){
        MaterialDialog(this)
        .positiveButton(R.string.yes) { dialog ->
            Firestore.removePlayer(retFirebaseModeString(), docId, this::addDeleteCallback)
        }
        .negativeButton(R.string.no) { dialog ->
            // Do something
        }
        .show()
    }

    private fun addNewStatDialog(){
        MaterialDialog(this)
                .icon(R.drawable.logo_blue)
                .positiveButton(R.string.add)
                .negativeButton(R.string.cancel)
                .message(R.string.menu_add_desc)
                .customView(R.layout.dialog_entry)
                .onShow {
                    val tvScore = it.findViewById<TextView>(R.id.tv_score)
                    tvScore.text = "$highScore"
                }
                .positiveButton {
                    val etName = it.findViewById<EditText>(R.id.et_name)
                    val etLoc = it.findViewById<EditText>(R.id.et_location)
                    val etTwoCents = it.findViewById<EditText>(R.id.et_two_cents)
                    val player = MyModel.Player(etName.text.toString(), etLoc.text.toString(), highScore, etTwoCents.text.toString())
                    Firestore.addAPlayer(player, retFirebaseModeString(), this::addDeleteCallback)
                }
                .show()
    }

    fun addDeleteCallback(){
        docId = prefs.getString(DOC_ID_KEY, "")?:""
        invalidateOptionsMenu()
        Timber.d("Just set to $docId")
    }
    private fun retFirebaseModeString(): String{
        return when {
            statsActivityViewModel.selectedFirebaseMode.get() == CurrentFirebaseMode.NORMAL -> getString(R.string.normal).toLowerCase()
            statsActivityViewModel.selectedFirebaseMode.get() == CurrentFirebaseMode.MATT -> getString(R.string.matt).toLowerCase()
            else -> getString(R.string.hint).toLowerCase()
        }
    }

    override fun inflateOptionsMenu(menu: Menu){
        return menuInflater.inflate(R.menu.menu_full, menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        inflateOptionsMenu(menu)
        if(docId == "-1") {
            menu.findItem(R.id.action_add).isVisible = true
            menu.findItem(R.id.action_remove).isVisible = false
        }else{
            menu.findItem(R.id.action_add).isVisible = false
            menu.findItem(R.id.action_remove).isVisible = true
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun setupAdapter(){
        rv_multi_item.layoutManager = LinearLayoutManager(this@StatsActivity)
        rv_multi_item.itemAnimator = DefaultItemAnimator()
        rv_multi_item.adapter = mainGroupAdapter
        mainGroupAdapter.add(statsSection)
    }
}