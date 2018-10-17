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

class HomeActivity : BaseActivity(){

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }

    private val personGroupAdapter = GroupAdapter<ViewHolder>()

    private val mainSection = Section()
    private val peopleSection = Section()
    private var scoreSection = Section()

    private val homeActivityViewModel : HomeActivityViewModel by lazy {
        ViewModelProviders.of(this).get(HomeActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<HomeActivityBinding>(this, R.layout.home_activity).apply {
            activityViewModel = homeActivityViewModel
        }

        homeActivityViewModel.apiErrorAction.observe(this){ it ->
            showApiError(it)
        }

        homeActivityViewModel.networkErrorAction.observe(this){
            showSnackBar()
        }

        homeActivityViewModel.normalErrorAction.observe(this){it ->
            saveUpdateView(save_update_view_act_list_detail, it)
        }

        homeActivityViewModel.loadPeopleAction.observe(this, Observer {
            it?.let{ personViewModels -> peopleSection.update(personViewModels) }
        })

        homeActivityViewModel.loadScoreAction.observe(this, Observer {
            if(homeActivityViewModel.showingPeople.value == true)
                mainSection.remove(peopleSection)
            scoreSection = Section()
            it?.let { scoreViewModel -> scoreSection.add(scoreViewModel) }
            mainSection.add(scoreSection)
        })

        homeActivityViewModel.removeScoreSection.observe(this){
            if(homeActivityViewModel.showingPeople.value == false){
                mainSection.remove(scoreSection)
                mainSection.add(peopleSection)
            }
        }

        homeActivityViewModel.removePeopleSection.observe(this){
            mainSection.remove(peopleSection)
        }
        setupAdapter()
    }

    private fun setupAdapter(){
        rv_multi_item.layoutManager = LinearLayoutManager(this@HomeActivity)
        rv_multi_item.itemAnimator = DefaultItemAnimator()
        rv_multi_item.adapter = personGroupAdapter
        mainSection.add(peopleSection)
        personGroupAdapter.add(mainSection)
    }
}

