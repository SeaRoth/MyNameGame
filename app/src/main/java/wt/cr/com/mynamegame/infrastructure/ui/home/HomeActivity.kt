package wt.cr.com.mynamegame.infrastructure.ui.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.Gravity
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import com.xwray.groupie.GroupAdapter
import wt.cr.com.mynamegame.infrastructure.ui.BaseActivity.BaseActivity
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.home_activity.*
import tourguide.tourguide.Overlay
import tourguide.tourguide.TourGuide
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.R.id.rv_multi_item
import wt.cr.com.mynamegame.databinding.HomeActivityBinding
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.ui.common.NoConnectionViewModel
import wt.cr.com.mynamegame.infrastructure.ui.home.cards.UpdatableItem
import wt.cr.com.mynamegame.infrastructure.ui.stats.StatsActivity

class HomeActivity : BaseActivity(){

    companion object {
        var widthPixels: Int = 0
        var heightPixels: Int = 0
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }

    private val personGroupAdapter = GroupAdapter<ViewHolder>()
    private lateinit var groupLayoutManager: GridLayoutManager
    private var peopleSection  = Section()
    private var updatingGroup    = Section()
    private var updatableItems = ArrayList<UpdatableItem>()
    private var scoreSection = Section()
    private var errorSection = Section()
    private val prefs = WTServiceLocator.resolve(SharedPreferences::class.java)
    lateinit var tourGuide: TourGuide

    private val homeActivityViewModel : HomeActivityViewModel by lazy {
        ViewModelProviders.of(this).get(HomeActivityViewModel::class.java)
    }

    private fun callbackRetry(){
        personGroupAdapter.clear()
        homeActivityViewModel.loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<HomeActivityBinding>(this, R.layout.home_activity).apply {
            activityViewModel = homeActivityViewModel
        }

        personGroupAdapter.apply {
            widthPixels = resources.displayMetrics.widthPixels
            heightPixels = resources.displayMetrics.heightPixels
            spanCount = if (heightPixels > widthPixels)
                6
            else
                12
        }

        groupLayoutManager = GridLayoutManager(this, personGroupAdapter.spanCount).apply {
            spanSizeLookup = personGroupAdapter.spanSizeLookup
        }
        setupAdapter()

        homeActivityViewModel.normalErrorAction.observe(this) { it ->
            saveUpdateView(save_update_view_act_list_detail, it)
        }

        homeActivityViewModel.loadPeopleAction.observe(this, Observer {
            personGroupAdapter.clear()
            peopleSection = Section()
            updatingGroup = Section()
            updatableItems.clear()
            var i = 1
            it?.forEach { pvm ->
                updatableItems.add(pvm)
                i++
            }
            updatingGroup.update(updatableItems)
            peopleSection.add(updatingGroup)
            personGroupAdapter.add(peopleSection)
        })

        homeActivityViewModel.shuffleProfilesAction.observe(this, Observer { list ->
            list?.let {
                updatingGroup.update(it)
            }
        })

        homeActivityViewModel.loadScoreAction.observe(this, Observer {
            scoreSection = Section()
            personGroupAdapter.clear()
            personGroupAdapter.add(scoreSection)
            it?.let { scoreViewModel -> scoreSection.add(scoreViewModel) }
        })

        homeActivityViewModel.apiErrorAction.observe(this) { it ->
            personGroupAdapter.clear()
            personGroupAdapter.add(errorSection)
            showApiError(it)
            errorSection.add(NoConnectionViewModel(it, this::callbackRetry))
        }

        homeActivityViewModel.loadStatAction.observe(this) {
            startActivity(StatsActivity.newIntent(this))
        }

        //TODO: CHECK IF FIRST TIME
        if(prefs.getBoolean(FIRST_TIME_OPEN_KEY, true)){
            TourGuide.create(this) {
                toolTip {
                    title { "Welcome to The Name Game" }
                    description { "To get started press the play button!" }
                    textColor { getColor(R.color.red) }
                    backgroundColor { getColor(R.color.orange) }
                    shadow { true }
                    gravity { Gravity.TOP or Gravity.START }
                    overlay {
                        disableClick { false }
                        disableClickThroughHole { false }
                        style { Overlay.Style.RECTANGLE }
                        backgroundColor { Color.parseColor("#AAFF0000") }
                        onClickListener {
                            View.OnClickListener {
                                prefs.edit().putBoolean(FIRST_TIME_OPEN_KEY, true).apply()
                                tourGuide.cleanUp()
                            }
                        }
                    }
                    enterAnimation {
                        TranslateAnimation(0f, 0f, 200f, 0f).apply {
                            duration = 1000
                            fillAfter = true
                            interpolator = BounceInterpolator()
                        }
                    }
                }
            }.playOn(fab)
        }
    }

    private fun setupAdapter(){
        rv_multi_item.apply {
            layoutManager = groupLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = personGroupAdapter
        }
    }
}