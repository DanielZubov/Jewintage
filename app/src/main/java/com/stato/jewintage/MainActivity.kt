package com.stato.jewintage

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.stato.jewintage.adapters.AddRcAdapter
import com.stato.jewintage.adapters.CostAdapter
import com.stato.jewintage.adapters.SalesAdapter
import com.stato.jewintage.databinding.ActivityMainBinding
import com.stato.jewintage.dialogHelper.DialogConst
import com.stato.jewintage.dialogHelper.DialogHelper
import com.stato.jewintage.fragments.SalesFragment
import com.stato.jewintage.model.AddCost
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.viewmodel.FirebaseViewModel

class MainActivity : AppCompatActivity(), SalesAdapter.DeleteItemListener, NavigationView.OnNavigationItemSelectedListener, AddRcAdapter.DeleteItemListener, CostAdapter.DeleteItemListener  {
    private lateinit var binding : ActivityMainBinding
    private lateinit var tvAccount : TextView
    private lateinit var userPhotoImageView : ImageView
    private lateinit var conf: AppBarConfiguration
    private lateinit var navController: NavController
    var auth = Firebase.auth
    private val dialogHelper = DialogHelper(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            "theme" -> {
                val theme = sharedPreferences.getString(key, "")
                when (theme) {
                    "auto" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        init()
        firebaseViewModel.loadAllAds()
        firebaseViewModel.loadAllCost()

        getSharedPreferences("root_preferences", Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        navController = findNavController(R.id.placeScreen)
        conf = AppBarConfiguration(
            setOf(
                R.id.costFragment,
                R.id.salesFragment,
                R.id.nomenclatureFragment,
                R.id.salesGroupFragment,
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, conf)
        binding.btmMenu.setupWithNavController(navController)
    }

    private fun onActivityResult() {
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    dialogHelper.accountHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("MyLog", "Api error : ${e.message}")
            }
        }
    }
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.placeScreen) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments[0]

        if (currentFragment is SalesFragment) {
            currentFragment.findNavController().navigate(R.id.action_salesFragment_to_salesGroupFragment)
        } else {
            AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены что хотите закрыть Jewintage?")
                .setNegativeButton("Нет", null)
                .setPositiveButton("Да") { _, _ -> super.onBackPressed() }
                .create().show()
        }
    }


    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e("MainActivity", "Update flow failed! Result code: $resultCode")
            }
        }
    }


    override fun onStart() {
        super.onStart()
        uiUpdate(auth.currentUser)
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences("root_preferences", Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    private fun init(){
        checkUpdateApp()
        setSupportActionBar(binding.includeToolbar.toolbar)
        onActivityResult()
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout,binding.includeToolbar.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.titleHeader)
        userPhotoImageView = binding.navView.getHeaderView(0).findViewById(R.id.profileImg)

    }

    private fun checkUpdateApp(){
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    MY_REQUEST_CODE)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.logout -> {
                uiUpdate(null)
                auth.signOut()
                dialogHelper.accountHelper.signOutGoogle()
            }

            R.id.signin -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }

            R.id.register -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    fun uiUpdate(user:FirebaseUser?){
        val signInItem = binding.navView.menu.findItem(R.id.signin)
        val logOutItem = binding.navView.menu.findItem(R.id.logout)
        val registerItem = binding.navView.menu.findItem(R.id.register)
        if (user == null){
            signInItem.isVisible = true
            registerItem.isVisible = true
            logOutItem.isVisible = false
            tvAccount.text = resources.getString(R.string.reg_not)
            userPhotoImageView.setImageResource(R.drawable.ic_acc)
        } else {
            signInItem.isVisible = false
            registerItem.isVisible = false
            logOutItem.isVisible = true
            // Получаем ссылку на авторизованного пользователя Firebase
            val currentUser = FirebaseAuth.getInstance().currentUser
// Если пользователь авторизован, заполняем Views данными из его профиля
            currentUser?.let {
                tvAccount.text = it.displayName
                Glide.with(this)
                    .load(it.photoUrl)
                    .circleCrop()
                    .into(userPhotoImageView)
            }
        }
    }

    companion object{
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        private const val MY_REQUEST_CODE = 999
    }

    override fun onDeleteItem(addNom: AddNom) {
        firebaseViewModel.deleteItem(addNom)
    }

    override fun onDeleteSellItem(sale: AddSales) {
        firebaseViewModel.deleteSellItem(sale)
    }

    override fun onDeleteCostItem(cost: AddCost) {
        firebaseViewModel.deleteCostItem(cost)
    }


}