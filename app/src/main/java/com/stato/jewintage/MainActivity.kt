package com.stato.jewintage

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
import com.stato.jewintage.fragments.CostFragment
import com.stato.jewintage.fragments.NomenclatureFragment
import com.stato.jewintage.fragments.SalesFragment
import com.stato.jewintage.model.AddCost
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.model.AddSales
import com.stato.jewintage.model.DbManager
import com.stato.jewintage.viewmodel.FirebaseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity(), SalesAdapter.DeleteItemListener, NavigationView.OnNavigationItemSelectedListener, AddRcAdapter.DeleteItemListener, CostAdapter.DeleteItemListener  {
    private lateinit var binding : ActivityMainBinding
    private lateinit var tvAccount : TextView
    private lateinit var userPhotoImageView : ImageView
    private lateinit var conf: AppBarConfiguration
    private lateinit var navController: NavController
    var auth = Firebase.auth
    private val dialogHelper = DialogHelper(this)
    private val dbManager = DbManager()
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            "theme" -> {
                when (sharedPreferences.getString(key, "")) {
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

        getSharedPreferences("root_preferences", Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        navController = findNavController(R.id.placeScreen)
        conf = AppBarConfiguration(
            setOf(
                R.id.salesGroupFragment,
                R.id.nomGroupFragment,
                R.id.costGroupFragment,
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, conf)
        binding.btmMenu.setupWithNavController(navController)
    }
    private fun init(){
        checkUpdateApp()
        setSupportActionBar(binding.includeToolbar.toolbar)
        onActivityResult()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.titleHeader)
        userPhotoImageView = binding.navView.getHeaderView(0).findViewById(R.id.profileImg)

    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.placeScreen) as NavHostFragment

        when (val currentFragment = navHostFragment.childFragmentManager.fragments[0]) {
            is SalesFragment -> {
                currentFragment.findNavController().navigate(R.id.action_salesFragment_to_salesGroupFragment)
            }
            is NomenclatureFragment -> {
                currentFragment.findNavController().navigate(R.id.action_nomenclatureFragment_to_nomGroupFragment)
            }
            is CostFragment -> {
                currentFragment.findNavController().navigate(R.id.action_costFragment_to_costGroupFragment)
            }
            else -> {
                val layout = LayoutInflater.from(this).inflate(R.layout.dialog_exit, null)
                val btnYes: Button = layout.findViewById(R.id.btnYes)
                val btnNo: Button = layout.findViewById(R.id.btnNo)

                val dialog = AlertDialog.Builder(this)
                    .setView(layout)
                    .setCancelable(true)
                    .create()
                btnYes.setOnClickListener {
                    finishAffinity()
                }
                btnNo.setOnClickListener{
                    dialog.dismiss()
                }
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(conf) || super.onSupportNavigateUp()
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

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences("root_preferences", Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
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
            R.id.about -> {
                showAboutDialog()
            }
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            R.id.logout -> {
                auth.signOut()
                uiUpdate(null)
                dialogHelper.accountHelper.signOutGoogle()
            }

            R.id.revenue -> {
                showRevenueDialog()
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

    private fun showDatePickerDialog(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val formatDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val selectedDateStr = formatDate.format(selectedDate.time)
                editText.setText(selectedDateStr)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun validateFields(dialog: AlertDialog): Boolean {
        var isValid = true
        val dateFromEditText = dialog.findViewById<TextInputEditText>(R.id.dateFrom)
        val dateToEditText = dialog.findViewById<TextInputEditText>(R.id.dateTo)
        val dateFromLayout = dialog.findViewById<TextInputLayout>(R.id.dateFromLayout)
        val dateToLayout = dialog.findViewById<TextInputLayout>(R.id.dateToLayout)

        if (dateFromEditText?.text?.isEmpty() == true) {
            dateFromLayout?.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            dateFromLayout?.error = null
        }

        if (dateToEditText?.text?.isEmpty() == true) {
            dateToLayout?.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            dateToLayout?.error = null
        }
        return isValid
    }


    private fun showRevenueDialog() {
        val layout = LayoutInflater.from(this).inflate(R.layout.dialog_revenue, null)

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .setCancelable(true)
            .create()

        val applyButton = layout.findViewById<Button>(R.id.applyFiltersButton)
        val resetButton = layout.findViewById<Button>(R.id.resetButton)
        val tvTotalRev = layout.findViewById<TextView>(R.id.tvTotalRev)
        val dateFromEditText = layout.findViewById<TextInputEditText>(R.id.dateFrom)
        val dateToEditText = layout.findViewById<TextInputEditText>(R.id.dateTo)

        dateFromEditText.setOnClickListener {
            showDatePickerDialog(dateFromEditText)
        }

        dateToEditText.setOnClickListener {
            showDatePickerDialog(dateToEditText)
        }

        applyButton.setOnClickListener {
            if (validateFields(dialog)) {
                val startDate = dateFromEditText.text?.toString()
                val endDate = dateToEditText.text?.toString()

                if (startDate != null && endDate != null) {
                    dbManager.calculateTotalPriceByDateRange(startDate, endDate) { totalPrice ->
                        tvTotalRev.text = totalPrice.toString()
                    }
                }
            } else {
                tvTotalRev.text = getString(R.string._0_0)
            }
        }


        resetButton.setOnClickListener {
            dateFromEditText.text = null
            dateToEditText.text = null
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    fun uiUpdate(user:FirebaseUser?){
        // Очищаем текущее меню
        binding.navView.menu.clear()

        if (user == null){
            // Загружаем меню для незарегистрированных пользователей
            binding.navView.inflateMenu(R.menu.menu_logged_out)
            tvAccount.text = resources.getString(R.string.reg_not)
            userPhotoImageView.setImageResource(R.drawable.ic_acc)
        } else {
            // Загружаем меню для зарегистрированных пользователей
            binding.navView.inflateMenu(R.menu.menu_logged_in)
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


    @Suppress("DEPRECATION")
    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.about))

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        val message = """
        ${getString(R.string.name_app)}: ${getString(R.string.app_name)}
        ${getString(R.string.version)}: $versionName
        ${getString(R.string.developer)}: Daniel Zubov
        ${getString(R.string.contacts)}: statoxxl@gmail.com
    """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
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