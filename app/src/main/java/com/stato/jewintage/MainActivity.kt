package com.stato.jewintage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.stato.jewintage.adapters.AddRcAdapter
import com.stato.jewintage.databinding.ActivityMainBinding
import com.stato.jewintage.dialogHelper.DialogConst
import com.stato.jewintage.dialogHelper.DialogHelper
import com.stato.jewintage.dialogHelper.GoogleAccConst
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.viewmodel.FirebaseViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AddRcAdapter.DeleteItemListener, AddRcAdapter.SellButtonClickListener  {
    private lateinit var binding : ActivityMainBinding
    private lateinit var tvAccount : TextView
    private lateinit var userPhotoImageView : ImageView
    private lateinit var conf: AppBarConfiguration
    private lateinit var navController: NavController
    var auth = Firebase.auth
    private val dialogHelper = DialogHelper(this)
    val adapter = AddRcAdapter(this, this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private lateinit var database: FirebaseDatabase
    private lateinit var salesRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        init()
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
    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this) {
            adapter.updateAdapter(it)
        }
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

    private fun showSellDialog(addNom: AddNom) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_sell, null)

        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val rgPaymentMethod = dialogView.findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val btnSubmitSell = dialogView.findViewById<Button>(R.id.btnSubmitSell)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        btnSubmitSell.setOnClickListener {
            val quantity = etQuantity.text.toString()
            val selectedPaymentMethodId = rgPaymentMethod.checkedRadioButtonId
            val radioButton = dialogView.findViewById<RadioButton>(selectedPaymentMethodId)
            val paymentMethod = radioButton.text.toString()

            if (quantity.isNotBlank() && selectedPaymentMethodId != -1) {
                saveSaleData(addNom, quantity, paymentMethod)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSaleData(addNom: AddNom, quantity: String, paymentMethod: String) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val uid = firebaseUser.uid
            val sale = AddSales(
                category = addNom.category,
                description = addNom.description,
                price = addNom.price,
                date = addNom.date,
                mainImage = addNom.mainImage,
                image2 = addNom.image2,
                image3 = addNom.image3,
                soldQuantity = quantity,
                paymentMethod = paymentMethod,
                id = addNom.id,
                uid = uid
            )

            val dbManager = DbManager()
            dbManager.saveSale(sale, object : DbManager.FinishWorkListener {
                override fun onFinish(isDone: Boolean) {
                    if (isDone) {
                        Toast.makeText(this@MainActivity, "Данные продажи успешно сохранены", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Ошибка при сохранении данных продажи", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
        }
    }



    companion object{
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }

    override fun onDeleteItem(addNom: AddNom) {
        firebaseViewModel.deleteItem(addNom)
    }

    override fun onSellButtonClick(addNom: AddNom) {
        showSellDialog(addNom)
    }


}