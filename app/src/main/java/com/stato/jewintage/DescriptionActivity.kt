package com.stato.jewintage

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.constance.MainConst
import com.stato.jewintage.databinding.ActivityDescriptionBinding
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.util.ImageManager
import com.stato.jewintage.viewmodel.FirebaseViewModel

class DescriptionActivity: AppCompatActivity(), SellButtonClickListener {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var auth = Firebase.auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        // Получение данных элемента
        @Suppress("DEPRECATION") val addNom = intent.getSerializableExtra(MainConst.AD) as? AddNom ?: return


        // Добавление слушателей кнопок
        binding.btnDesDel.setOnClickListener { onDeleteButtonClick(addNom) }
        binding.btnDesEdit.setOnClickListener { onEditButtonClick(addNom) }
        binding.btnDesSell.setOnClickListener { onSellButtonClick(addNom) }
        showItems(isOwner(addNom))
    }
    private fun init(){
        adapter = ImageAdapter()
        binding.apply {
            vpDes.adapter = adapter
        }
        getIntentFromMainAct()
    }

    private fun onDeleteButtonClick(addNom: AddNom) {
        firebaseViewModel.deleteItem(addNom)
    }
    private fun onEditButtonClick(addNom: AddNom) {
        val i = Intent(this, EditItemAct::class.java).apply {
            putExtra(MainActivity.EDIT_STATE, true)
            putExtra(MainActivity.ADS_DATA, addNom)
        }
        startActivity(i)
    }


    override fun onSellButtonClick(addNom: AddNom) {
        showSellDialog(auth,addNom)
    }


    private fun getIntentFromMainAct(){
        @Suppress("DEPRECATION") val ad = intent.getSerializableExtra(MainConst.AD) as AddNom
        updateUi(ad)
    }

    private fun updateUi(ad: AddNom){
        ImageManager.fillImageArray(ad, adapter)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: AddNom) = with(binding){
        if (ad.quantity == "0") {
            tvDesQuantity.setTextColor(Color.RED)
            btnDesSell.visibility = View.GONE
        }
        tvDesCat.text = ad.category
        "₾ ${ad.price}".also { tvDesPrcie.text = it }
        tvDesDesc.text = ad.description
        tvDesDate.text = ad.date
        "${ad.quantity} шт.".also { tvDesQuantity.text = it }
        "₾ ${getSumPriceItem(ad.price, ad.quantity)}".also { tvSum.text = it }
    }

    private fun getSumPriceItem(price: String?, quantity: String?): Int {
        val parsedPrice = price?.toIntOrNull() ?: 0
        val parsedQuantity = quantity?.toIntOrNull() ?: 0
        return parsedPrice * parsedQuantity
    }
    private fun isOwner(addNom: AddNom): Boolean {
        return addNom.uid == auth.uid
    }

    private fun showItems(isOwner: Boolean) {
        if (isOwner) {
            binding.btnDesDel.visibility = View.VISIBLE
            binding.btnDesEdit.visibility = View.VISIBLE
            binding.btnDesSell.visibility = View.VISIBLE
        } else {
            binding.btnDesDel.visibility = View.GONE
            binding.btnDesEdit.visibility = View.GONE
            binding.btnDesSell.visibility = View.GONE
        }
    }
}

interface SellButtonClickListener {
    fun onSellButtonClick(addNom: AddNom)
}
