package wt.cr.com.mynamegame.infrastructure.ui.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import wt.cr.com.mynamegame.infrastructure.ui.BaseActivity.BaseActivity
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.home_activity.*
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.HomeActivityBinding
import wt.cr.com.mynamegame.infrastructure.ui.common.NoConnectionViewModel

class HomeActivity : BaseActivity(){

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }

    private val personGroupAdapter = GroupAdapter<ViewHolder>()
    private val peopleSection = Section()
    private var statSection = Section()
    private var errorSection = Section()

    private val homeActivityViewModel : HomeActivityViewModel by lazy {
        ViewModelProviders.of(this).get(HomeActivityViewModel::class.java)
    }

    private fun callbackRetry(){
        homeActivityViewModel.loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<HomeActivityBinding>(this, R.layout.home_activity).apply {
            activityViewModel = homeActivityViewModel
        }

        homeActivityViewModel.normalErrorAction.observe(this){it ->
            saveUpdateView(save_update_view_act_list_detail, it)
        }

        homeActivityViewModel.loadPeopleAction.observe(this, Observer {
            personGroupAdapter.clear()
            personGroupAdapter.add(peopleSection)
            it?.let{ personViewModels -> peopleSection.update(personViewModels) }
        })

        homeActivityViewModel.loadScoreAction.observe(this, Observer {
            statSection = Section()
            personGroupAdapter.clear()
            personGroupAdapter.add(statSection)
            it?.let { scoreViewModel -> statSection.add(scoreViewModel) }
        })

        homeActivityViewModel.apiErrorAction.observe(this){ it ->
            personGroupAdapter.clear()
            personGroupAdapter.add(errorSection)
            showApiError(it)
            errorSection.add(NoConnectionViewModel(this::callbackRetry))
        }
        setupAdapter()
    }

    private fun setupAdapter(){
        rv_multi_item.layoutManager = LinearLayoutManager(this@HomeActivity)
        rv_multi_item.itemAnimator = DefaultItemAnimator()
        rv_multi_item.adapter = personGroupAdapter
        personGroupAdapter.add(peopleSection)
    }
}

