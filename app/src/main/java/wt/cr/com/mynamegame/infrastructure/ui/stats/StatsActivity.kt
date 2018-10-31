package wt.cr.com.mynamegame.infrastructure.ui.stats

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.stats_activity.*
import timber.log.Timber
import wt.cr.com.mynamegame.R
import wt.cr.com.mynamegame.R.id.save_update_view_act_stats
import wt.cr.com.mynamegame.R.id.toolbar
import wt.cr.com.mynamegame.databinding.StatsActivityBinding
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.firestore.Firestore
import wt.cr.com.mynamegame.infrastructure.network.firestore.Firestore.Companion.DOC_ID_KEY
import wt.cr.com.mynamegame.infrastructure.ui.BaseActivity.BaseActivity

class StatsActivity : BaseActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, StatsActivity::class.java)
        }
        const val RC_SIGN_IN = 13
    }

    private val statsActivityViewModel : StatsActivityViewModel by lazy {
        ViewModelProviders.of(this).get(StatsActivityViewModel::class.java)
    }

    private val mainGroupAdapter = GroupAdapter<ViewHolder>()
    private var statsSection = Section()
    private lateinit var prefs: SharedPreferences
    lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = WTServiceLocator.resolve(SharedPreferences::class.java)

        DataBindingUtil.setContentView<StatsActivityBinding>(this, R.layout.stats_activity).apply {
            setSupportActionBar(toolbar)
            setupHomeAsUp(ActionBarStyle.UP_BUTTON)
            supportActionBar?.title = getString(R.string.your_score)
            activityViewModel = statsActivityViewModel
        }

        auth = FirebaseAuth.getInstance()

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

        statsActivityViewModel.changeTitleAction.observe(this) {
            supportActionBar?.title = getString(R.string.score_rank, it, statsActivityViewModel.rank)
            invalidateOptionsMenu()
        }

        setupAdapter()
    }

    private fun areYouSureDeleteDialog(){
        MaterialDialog(this)
        .positiveButton(R.string.yes) { dialog ->
            Firestore.removePlayer(retFirebaseModeString(), statsActivityViewModel.docId, this::addDeleteCallback)
        }
        .negativeButton(R.string.no) { dialog ->
            // Do something
        }
        .show()
    }

    private lateinit var auth: FirebaseAuth
    public override fun onStart() {
        super.onStart()
        auth.currentUser?.let {
            user = it
        }
    }

    lateinit var dialog: MaterialDialog
    private fun addNewStatDialog(){
        MaterialDialog(this)
                .icon(R.drawable.logo_blue)
                .positiveButton(R.string.add)
                .negativeButton(R.string.cancel)
                .message(R.string.menu_add_desc)
                .customView(R.layout.dialog_entry)
                .onShow { materialDialog ->
                    dialog = materialDialog
                    val tvScore = materialDialog.findViewById<TextView>(R.id.tv_score)
                    tvScore.text = "${statsActivityViewModel.highScore}"
                    val btnLocation = materialDialog.findViewById<Button>(R.id.btn_location)
                    val btnSignIn = materialDialog.findViewById<Button>(R.id.btn_sign_in)
                    auth.currentUser?.let {
                        user = it
                        btnSignIn.text = getString(R.string.sign_out, user.displayName)
                    }

                    btnSignIn.setOnClickListener {
                        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setAvailableProviders(providers)
                                        .setLogo(R.drawable.web_hi_res_512)
                                        .setTheme(R.style.RandomTheme)
                                        .build(),
                                RC_SIGN_IN)
                    }

                    btnLocation.setOnClickListener {
                        saveUpdateView(save_update_view_act_stats, getString(R.string.error_disabled))
//
//                        val locationProvider: String = LocationManager.NETWORK_PROVIDER
//                        val permissionCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                        val permissionFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//
//                        if (permissionCoarse == PackageManager.PERMISSION_GRANTED
//                                &&
//                                permissionFine == PackageManager.PERMISSION_GRANTED
//                        ){
//                            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//                            try{
//                                locationManager.requestLocationUpdates(locationProvider, 0L, 0f, locationListener)
//                                //val lastKnownLocation: Location = locationManager.getLastKnownLocation(locationProvider)
//                                Timber.d("WHAT ")
//                            }catch(e: Exception){
//                                Timber.d("error")
//                            }
//                            Timber.d("WHAT ")
//                        } else
//                            checkLocationPermissions()
                    }
                }
                .positiveButton { materialDialog ->
                    val btnLocation = materialDialog.findViewById<Button>(R.id.btn_location)
                    val etTwoCents = materialDialog.findViewById<EditText>(R.id.et_two_cents)
                    val etLocation = materialDialog.findViewById<EditText>(R.id.et_location)


                    if(auth.currentUser != null)
                        user.displayName?.let {
                            val player = MyModel.Player(
                                    it,
                                    user.email?:"",
                                    etLocation.text.toString(),
                                    statsActivityViewModel.highScore,
                                    etTwoCents.text.toString()
                            )
                            Firestore.addNewPlayer(player, retFirebaseModeString(), this::addDeleteCallback)
                        }
                    else
                        saveUpdateView(save_update_view_act_stats, getString(R.string.error_please_sign_in))
                }
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationCallbackId -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Permission has been denied by user")
                } else {
                    Timber.i("Permission has been granted by user")
                }
            }
        }
    }

    private val locationCallbackId = 31
    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ||
                ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            makePermissionRequests(locationCallbackId, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        }else{
            val btnLocation = dialog.findViewById<Button>(R.id.btn_location)
            btnLocation.text = user.displayName
        }
    }

    private fun makePermissionRequests(callbackId: Int, vararg permissionsId: String) {
        var permissions = true
        for (p in permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
        }

        if (!permissions)
            ActivityCompat.requestPermissions(this, permissionsId, callbackId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Companion.RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                FirebaseAuth.getInstance().currentUser?.let {
                    user = it
                    setDialogUsername()
                }
            } else {
                showApiError("ERROR")
            }
        }
    }

    private fun setDialogUsername(){
        val btnName = dialog.findViewById<Button>(R.id.btn_sign_in)
        btnName.text = getString(R.string.sign_out, user.displayName)
    }

    private fun addDeleteCallback(){
        statsActivityViewModel.docId = prefs.getString(DOC_ID_KEY, "")?:""
        invalidateOptionsMenu()
    }
    private fun retFirebaseModeString(): String{
        return when {
            statsActivityViewModel.selectedFirebaseMode.get() == CurrentFirebaseMode.NORMAL -> getString(R.string.normal).toLowerCase()
            statsActivityViewModel.selectedFirebaseMode.get() == CurrentFirebaseMode.MATT -> getString(R.string.matt).toLowerCase()
            else -> getString(R.string.custom).toLowerCase()
        }
    }

    override fun inflateOptionsMenu(menu: Menu){
        return menuInflater.inflate(R.menu.menu_full, menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        inflateOptionsMenu(menu)
        if(statsActivityViewModel.rank == getString(R.string.NA)) {
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

    private val locationListener: LocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            Timber.d("onLocationChanged ${location.latitude} ${location.longitude}")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Timber.d("hmm")
        }

        override fun onProviderEnabled(provider: String) {
            Timber.d("enabled")
        }

        override fun onProviderDisabled(provider: String) {
            Timber.d("disabled")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_add -> {
                if(statsActivityViewModel.highScore == 0)
                    saveUpdateView(save_update_view_act_stats, "BRUH, you have no high score")
                else if(statsActivityViewModel.rank == "NA")
                    addNewStatDialog(); return true
            }
            R.id.action_remove -> {
                if(statsActivityViewModel.docId != "-1")
                    areYouSureDeleteDialog(); return true
            }
            android.R.id.home -> {
                onBackPressed()
                finish(); return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

// Register the listener with the Location Manager to receive location updates
    //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f,locationListener)
}