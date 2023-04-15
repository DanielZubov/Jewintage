package com.stato.jewintage.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.stato.jewintage.EditItemAct
import com.stato.jewintage.R
import com.stato.jewintage.databinding.FragmentImageListBinding
import com.stato.jewintage.dialogHelper.ProgressDialog
import com.stato.jewintage.util.AdapterCallback
import com.stato.jewintage.util.ImageManager
import com.stato.jewintage.util.ImagePicker
import com.stato.jewintage.util.ItemMoveCallBack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFragment(private val FragCloseInterface: FragmentCloseInterface
) : BaseFrag(), AdapterCallback {
    lateinit var binding: FragmentImageListBinding
//    private var btnAdd : FloatingActionButton? = null
    private var job: Job? = null
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            activity,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            activity,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            activity,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            activity,
            R.anim.to_bottom_anim
        )
    }
    private var clicked = false
    private val adapter = SelectImageRvAdapter(this)
    private val dragCallback = ItemMoveCallBack(adapter)
    private val touchHelper = ItemTouchHelper(dragCallback)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
        touchHelper.attachToRecyclerView(rcViewSelectImage)
        rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
        rcViewSelectImage.adapter = adapter
        }



        //OnClick

        binding.btnSave.setOnClickListener {
            onClose()
        }

        binding.btnImage.setOnClickListener {
            onImageButtonClicked()
        }

        binding.btnDel.setOnClickListener {
            binding.btnAdd.visibility = View.VISIBLE
            adapter.updateAdapter(ArrayList(), true)
        }

        binding.btnAdd.setOnClickListener {
            val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePicker.addImages(activity as EditItemAct, imageCount)
        }


    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>){
        adapter.updateAdapter(bitmapList, true)
    }


    fun updateAdapter(newList: ArrayList<Uri>, activity : Activity) {
        resizeSelectedImages(newList, false, activity)
    }

    fun setSingleImage(uri : Uri, pos : Int){
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.progressBarItem)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), activity as Activity)
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }
    }

    private fun onImageButtonClicked() {
        setClickable(clicked)
        setAnimation(clicked)
        setVisibility(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.btnAdd.visibility = View.VISIBLE
            binding.btnSave.visibility = View.VISIBLE
            binding.btnDel.visibility = View.VISIBLE
        } else {
            binding.btnAdd.visibility = View.INVISIBLE
            binding.btnSave.visibility = View.INVISIBLE
            binding.btnDel.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.btnAdd.startAnimation(fromBottom)
            binding.btnSave.startAnimation(fromBottom)
            binding.btnDel.startAnimation(fromBottom)
            binding.btnImage.startAnimation(rotateOpen)
        } else {
            binding.btnAdd.startAnimation(toBottom)
            binding.btnSave.startAnimation(toBottom)
            binding.btnDel.startAnimation(toBottom)
            binding.btnImage.startAnimation(rotateClose)
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            binding.btnAdd.isClickable = true
            binding.btnSave.isClickable = true
            binding.btnDel.isClickable = true
        } else {
            binding.btnAdd.isClickable = false
            binding.btnSave.isClickable = false
            binding.btnDel.isClickable = false
        }
    }




    fun resizeSelectedImages(newList: ArrayList<Uri>, needClear : Boolean, activity: Activity){
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity)
            val bitmapList = ImageManager.imageResize(newList, activity)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if (adapter.mainArray.size > 2) binding.btnAdd.visibility = View.GONE
        }
    }

    override fun onItemDel() {
        binding.btnAdd.visibility = View.VISIBLE
    }

    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFragment)?.commit()
        FragCloseInterface.onFragClose(adapter.mainArray)
        job?.cancel()
    }




}