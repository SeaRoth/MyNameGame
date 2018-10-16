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
    private val mainListGroup = Section()
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

        homeActivityViewModel.loadPeopleAction.observe(this, Observer {
            it?.let{ personViewModels -> mainListGroup.update(personViewModels) }
        })

        personGroupAdapter.spanCount = 2

        setupAdapter()
    }

    private fun setupAdapter(){
        rv_multi_item.layoutManager = LinearLayoutManager(this@HomeActivity)
        rv_multi_item.itemAnimator = DefaultItemAnimator()
        rv_multi_item.adapter = personGroupAdapter
        //mainListGroup.apply { setHeader(HomeActivityHeaderItem(homeActivityViewModel)) }
        //mainListGroup.apply { setFooter(HomeActivityFooterItem(homeActivityViewModel)) }
        personGroupAdapter.add(mainListGroup)
    }
}

