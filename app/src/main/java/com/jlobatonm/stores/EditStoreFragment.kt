package com.jlobatonm.stores

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.jlobatonm.stores.databinding.FragmentEditStoreBinding
import java.util.concurrent.LinkedBlockingQueue

class EditStoreFragment : Fragment() {
    private lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity: MainActivity? = null
    private var mIsEditMode = false
    private var mStoreEntity: StoreEntity? = null
    

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val id = arguments?.getLong(getString(R.string.arg_id),0)
        if(id != null && id != 0L){
            mIsEditMode = true
            getStore(id)
        }else{
            mIsEditMode = false
            mStoreEntity = StoreEntity(name = "", phone = "",photoUrl = "")
        }

        setupActionBar()
        
        setupTextFields()

        
        
       
    }
    
    private fun setupActionBar()
    {
        mActivity = activity as? MainActivity
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title =
            if(mIsEditMode)
                getString(R.string.edit_store_title_edit)
            else
                getString(R.string.edit_store_title_add)
            
        
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_save, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        mActivity?.onBackPressedDispatcher?.onBackPressed()
                        
                        true
                    }
                    
                    R.id.action_save -> {
                        
                        if(mStoreEntity!= null &&
                            validateFields(mBinding.tilPhotoUrl, mBinding.tilPhone, mBinding.tilName)){
                            with(mStoreEntity!!){
                                name = mBinding.etName.text.toString().trim()
                                phone = mBinding.etPhone.text.toString().trim()
                                website = mBinding.etWebsite.text.toString().trim()
                                photoUrl = mBinding.etPhotoUrl.text.toString().trim()
                            }
                            
                            val queue = LinkedBlockingQueue<StoreEntity>()
                            
                            Thread{
                                hideKeyboard()
                                if(mIsEditMode){
                                    StoreApplication.database.storeDAO().updateStore(mStoreEntity!!)
                                    
                                }else{
                                    mStoreEntity!!.id = StoreApplication.database.storeDAO().addStore(mStoreEntity!!)
                                }
                                queue.add(mStoreEntity)
                            }.start()
                            
                            with(queue.take()){
                                
                                mActivity?.addStore(this)
                                mActivity?.updateStore(mStoreEntity!!)
                                if(mIsEditMode){
                                    Toast.makeText(
                                        mActivity,
                                        getString(R.string.edit_store_message_update_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }else{
                                    Toast.makeText(
                                        mActivity,
                                        getString(R.string.edit_store_message_save_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                mActivity?.onBackPressedDispatcher?.onBackPressed()
                                
                            }
                        }
                        
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    private fun setupTextFields()
    {
        with(mBinding){
            etPhotoUrl.addTextChangedListener {
                loadImage(etPhotoUrl.text.toString())
            }
            etName.addTextChangedListener {
                validateFields(tilName)
            }
            etPhone.addTextChangedListener {
                validateFields(tilPhone)
            }
            etPhotoUrl.addTextChangedListener {
                validateFields(tilPhotoUrl)
                loadImage(it.toString().trim())
            }
        }
        
    }
    
    private fun loadImage(url: String)
    {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .error(R.drawable.ic_image)
            .into(mBinding.imgPhoto)
    }
    
    
    private fun validateFields(vararg textFields: TextInputLayout): Boolean{
        var isValid = true
        for (textField in textFields){
            if(textField.editText?.text.toString().trim().isEmpty()){
                textField.error = getString(R.string.helper_required)
                isValid = false
                textField.editText?.requestFocus()
            }else{
                textField.error = null
            }
        }
        if (!isValid) Snackbar.make(
            mBinding.root ,
            getString(R.string.edit_store_message_valid) ,
            Snackbar.LENGTH_SHORT
        ).show()
        return isValid
    }
    
    /**
     * Función para validar los campos del formulario
     */
    private fun validateFields(): Boolean
    {
        var isValid = true
        
        if(mBinding.etPhotoUrl.text.toString().trim().isEmpty()){
            mBinding.tilPhotoUrl.error = getString(R.string.helper_required)
            isValid = false
            mBinding.etPhotoUrl.requestFocus()
        }
        if(mBinding.etPhone.text.toString().trim().isEmpty()){
            mBinding.tilPhone.error = getString(R.string.helper_required)
            isValid = false
            mBinding.etPhone.requestFocus()
        }
        if(mBinding.etName.text.toString().trim().isEmpty()){
            mBinding.tilName.error = getString(R.string.helper_required)
            isValid = false
            mBinding.etName.requestFocus()
        }
        
        return isValid
    }
    
    private fun getStore(id: Long)
    {
        val queue = LinkedBlockingQueue<StoreEntity?>()
        Thread {
            mStoreEntity = StoreApplication.database.storeDAO().getStoreById(id)
            queue.add(mStoreEntity)
        }.start()
        queue.take()?.let {
            setUiSore(mStoreEntity!!)
        }
    }
    
    private fun setUiSore(storeEntity: StoreEntity)
    {
        with(mBinding){
            etName.text = storeEntity.name.editable()
            etPhone.text = storeEntity.phone.editable()
            etWebsite.text = storeEntity.website.editable()
            etPhotoUrl.text = storeEntity.photoUrl.editable()
            Glide.with(this@EditStoreFragment)
                .load(storeEntity.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.ic_image)
                .into(imgPhoto)
        
        }
    }
    private fun String.editable(): Editable = Editable.Factory.getInstance().newEditable(this)
    
    
    override fun onDestroyView()
    {
        hideKeyboard()
        super.onDestroyView()
    }
    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        mActivity?.hideFab(true)
       
        super.onDestroy()
        
    }
    
    private fun hideKeyboard() {
        val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
    
}