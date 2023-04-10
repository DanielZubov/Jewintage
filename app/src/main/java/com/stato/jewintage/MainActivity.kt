package com.stato.jewintage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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
import com.stato.jewintage.databinding.ActivityMainBinding
import com.stato.jewintage.dialogHelper.DialogConst
import com.stato.jewintage.dialogHelper.DialogHelper
import com.stato.jewintage.dialogHelper.GoogleAccConst
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.viewmodel.FirebaseViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AddRcAdapter.DeleteItemListener  {
    private lateinit var binding : ActivityMainBinding
    private lateinit var tvAccount : TextView
    private lateinit var userPhotoImageView : ImageView
    private lateinit var conf: AppBarConfiguration
    private lateinit var navController: NavController
    var auth = Firebase.auth
    private val dialogHelper = DialogHelper(this)
    val adapter = AddRcAdapter(this)
    private val firebaseViewModel : FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        init()
//        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadAllAds()

        navController = findNavController(R.id.placeScreen)
        conf = AppBarConfiguration(
            setOf(
                R.id.costFragment,
                R.id.salesFragment,
                R.id.nomenclatureFragment,
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, conf)
        binding.btmMenu.setupWithNavController(navController)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.newAd){
            val i = Intent(this, EditItemAct :: class.java)
            startActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GoogleAccConst.GOOGLE_REQUEST_CODE){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException :: class.java)
                if (account != null){
                    dialogHelper.accountHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException){
                Log.d("MyLog", "Api error : ${e.message}")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(auth.currentUser)
    }
    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this) {
            adapter.updateAdapter(it)
        }
    }

    private fun init(){
        setSupportActionBar(binding.includeToolbar.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout,binding.includeToolbar.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.titleHeader)
        userPhotoImageView = binding.navView.getHeaderView(0).findViewById(R.id.profileImg)
    }

//    private fun initRecyclerView() = with(binding){
//            includeToolbar.contentNum.rcViewCM.layoutManager = LinearLayoutManager(this@MainActivity)
//            includeToolbar.contentNum.rcViewCM.adapter = adapter


//    }






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
        if (user == null){
            tvAccount.text = resources.getString(R.string.reg_not)
            userPhotoImageView.setImageResource(R.drawable.ic_acc)
        } else {
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


}