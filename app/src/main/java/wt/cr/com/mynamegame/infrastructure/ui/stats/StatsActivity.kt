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
        const val RC_SIGN_IN = 13
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
                    tvScore.text = "$highScore"
                    val btnLocation = materialDialog.findViewById<Button>(R.id.btn_location)
                    val btnSignIn = materialDialog.findViewById<Button>(R.id.btn_sign_in)
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
                        val locationProvider: String = LocationManager.NETWORK_PROVIDER
                        val permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        if (permissionLocation == PackageManager.PERMISSION_GRANTED){
                            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            locationManager.requestLocationUpdates(locationProvider, 0, 0f, locationListener)
                            Timber.d("WHAT ")
                        } else
                            checkLocationPermissions()
                    }
                }
                .positiveButton { materialDialog ->
                    val btnLocation = materialDialog.findViewById<Button>(R.id.btn_location)
                    val etTwoCents = materialDialog.findViewById<EditText>(R.id.et_two_cents)
                    user.displayName?.let {
                        val player = MyModel.Player(
                                it,
                                btnLocation.text.toString(),
                                highScore,
                                etTwoCents.text.toString()
                        )
                        Firestore.addAPlayer(player, retFirebaseModeString(), this::addDeleteCallback)
                    }
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
        Timber.d("we found something")
    }

    private val locationCallbackId = 31
    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            makePermissionRequests(locationCallbackId, Manifest.permission.ACCESS_COARSE_LOCATION)
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

    lateinit var user: FirebaseUser
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Companion.RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                FirebaseAuth.getInstance().currentUser?.let {
                    user = it
                    setDialogUsername()
                }
                // ...
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
        docId = prefs.getString(DOC_ID_KEY, "")?:""
        invalidateOptionsMenu()
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

    // Define a listener that responds to location updates
    val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            Timber.d("onLocationChanged")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }

        override fun onProviderEnabled(provider: String) {
            Timber.d("enabled")
        }

        override fun onProviderDisabled(provider: String) {
            Timber.d("disabled")
        }
    }

// Register the listener with the Location Manager to receive location updates
    //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f,locationListener)
}