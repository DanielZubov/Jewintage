package com.stato.jewintage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.stato.jewintage.adapters.AddRcAdapter
import com.stato.jewintage.adapters.CostAdapter
import com.stato.jewintage.databinding.ActivityMainBinding
import com.stato.jewintage.dialogHelper.DialogConst
import com.stato.jewintage.dialogHelper.DialogHelper
import com.stato.jewintage.adapters.SalesAdapter
import com.stato.jewintage.fragments.CostFragment
import com.stato.jewintage.fragments.NomenclatureFragment
import com.stato.jewintage.fragments.SalesFragment
import com.stato.jewintage.fragments.SalesGroupFragment
import com.stato.jewintage.model.AddCost
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.viewmodel.FirebaseViewModel

class MainActivity : AppCompatActivity(), SalesGroupFragment.OnSalesGroupDateSelectedListener, SalesAdapter.DeleteItemListener, NavigationView.OnNavigationItemSelectedListener, AddRcAdapter.DeleteItemListener, CostAdapter.DeleteItemListener  {
    private lateinit var binding : ActivityMainBinding
    private lateinit var tvAccount : TextView
    private lateinit var userPhotoImageView : ImageView
    private lateinit var conf: AppBarConfiguration
    private lateinit var navController: NavController
    var auth = Firebase.auth
    private val dialogHelper = DialogHelper(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel : FirebaseViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        init()
        firebaseViewModel.loadAllAds()
        firebaseViewModel.loadAllSales()
        firebaseViewModel.loadAllCost()

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
        if (savedInstanceState == null) {
            replaceFragment(SalesGroupFragment(), false)
        }

        binding.btmMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.salesGroupFragment -> {
                    replaceFragment(SalesGroupFragment(), false)
                    true
                }
                R.id.nomenclatureFragment -> {
                    replaceFragment(NomenclatureFragment(), false)
                    true
                }
                R.id.costFragment -> {
                    replaceFragment(CostFragment(), false)
                    true
                }
                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.placeScreen, fragment)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onSalesGroupDateSelected(date: String) {
        val salesFragment = SalesFragment().apply {
            arguments = Bundle().apply {
                putString("selected_date", date)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.placeScreen, salesFragment)
            .addToBackStack(null)
            .commit()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.placeScreen)
        if (currentFragment is SalesFragment) {
            replaceFragment(SalesGroupFragment(), false)
        } else {
            super.onBackPressed()
        }
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

    override fun onStart() {
        super.onStart()
        uiUpdate(auth.currentUser)
    }

    private fun init(){
        setSupportActionBar(binding.includeToolbar.toolbar)
        onActivityResult()
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout,binding.includeToolbar.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.titleHeader)
        userPhotoImageView = binding.navView.getHeaderView(0).findViewById(R.id.profileImg)

    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout ->{
                uiUpdate(null)
                auth.signOut()
                dialogHelper.accountHelper.signOutGoogle()
            }
            R.id.signin ->{
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }
            R.id.register ->{
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