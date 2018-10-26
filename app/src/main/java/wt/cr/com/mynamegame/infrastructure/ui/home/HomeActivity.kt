package wt.cr.com.mynamegame.infrastructure.ui.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.xwray.groupie.GroupAdapter
import wt.cr.com.mynamegame.infrastructure.ui.BaseActivity.BaseActivity
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.home_activity.*
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.HomeActivityBinding
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.network.firestore.Firestore
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
            spanCount = if(heightPixels > widthPixels)
                6
            else
                12
        }

        groupLayoutManager = GridLayoutManager(this, personGroupAdapter.spanCount).apply {
            spanSizeLookup = personGroupAdapter.spanSizeLookup
        }
        setupAdapter()

        homeActivityViewModel.normalErrorAction.observe(this){it ->
            saveUpdateView(save_update_view_act_list_detail, it)
        }

        homeActivityViewModel.loadPeopleAction.observe(this, Observer {
            personGroupAdapter.clear()
            peopleSection = Section()
            updatingGroup = Section()
            updatableItems.clear()
            var i = 1
            it?.forEach {pvm ->
                //updatableItems.add(UpdatableItem(rainbow200[i], i, pvm, this::theCb))
                updatableItems.add(pvm)
                i++
            }
            updatingGroup.update(updatableItems)
            peopleSection.add(updatingGroup)
            personGroupAdapter.add(peopleSection)
        })

        homeActivityViewModel.loadScoreAction.observe(this, Observer {
            scoreSection = Section()
            personGroupAdapter.clear()
            personGroupAdapter.add(scoreSection)
            it?.let { scoreViewModel -> scoreSection.add(scoreViewModel) }
        })

        homeActivityViewModel.apiErrorAction.observe(this){ it ->
            personGroupAdapter.clear()
            personGroupAdapter.add(errorSection)
            showApiError(it)
            errorSection.add(NoConnectionViewModel(it, this::callbackRetry))
        }

        homeActivityViewModel.loadStatAction.observe(this){
            startActivity(StatsActivity.newIntent(this, homeActivityViewModel.highScore.value?:0))
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