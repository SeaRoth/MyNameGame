package wt.cr.com.mynamegame.infrastructure.ui.stats

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.stats_activity.*
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.databinding.StatsActivityBinding
import wt.cr.com.mynamegame.infrastructure.ui.BaseActivity.BaseActivity

class StatsActivity : BaseActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, StatsActivity::class.java)
        }
    }

    private val statsActivityViewModel : StatsActivityViewModel by lazy {
        ViewModelProviders.of(this).get(StatsActivityViewModel::class.java)
    }

    private val mainGroupAdapter = GroupAdapter<ViewHolder>()
    private var statsSection = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<StatsActivityBinding>(this, R.layout.stats_activity).apply {
            supportActionBar?.title = "Your Rank ..."
            activityViewModel = statsActivityViewModel
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