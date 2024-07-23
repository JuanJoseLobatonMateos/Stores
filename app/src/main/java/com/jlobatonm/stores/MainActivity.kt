package com.jlobatonm.stores

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jlobatonm.stores.databinding.ActivityMainBinding
import java.util.concurrent.LinkedBlockingQueue

class MainActivity : AppCompatActivity() , OnClickListener, MainAux
{
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.containerMain) { view , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Adjust only the top padding, keep other paddings as they are
            view.setPadding(
                view.paddingLeft ,
                systemBars.top ,
                view.paddingRight ,
                view.paddingBottom
            )
            insets
        }
        
        mBinding.fab.setOnClickListener { launchEditFragment() }
        
        setupRecycledView()
    }
    
    
    private fun launchEditFragment(args: Bundle? = null)
    {
        val fragment = EditStoreFragment()
        if(args != null)
            fragment.arguments = args
        
        supportFragmentManager.beginTransaction()
            .add(R.id.containerMain , fragment)
            .addToBackStack(null)
            .commit()
        
        hideFab()
    }
    
    private fun setupRecycledView()
    {
        mAdapter = StoreAdapter(mutableListOf() , this)
        mGridLayout = GridLayoutManager(this , 2)
        getStores()
        
        mBinding.rvStores.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }
    
    private fun getStores()
    {
        val queue = LinkedBlockingQueue<MutableList<StoreEntity>>()
        Thread {
            val stores = StoreApplication.database.storeDAO().getAllStores()
            queue.add(stores)
        }.start()
        
        mAdapter.setStores(queue.take())
    }
    
    override fun onFavoriteStore(storeEntity: StoreEntity)
    {
        storeEntity.isFavorite = !storeEntity.isFavorite
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread {
            StoreApplication.database.storeDAO().updateStore(storeEntity)
            queue.add(storeEntity)
        }.start()
        updateStore(queue.take())
    }
    
    private fun enableEdgeToEdge()
    {
        // Implementation for edge-to-edge display if required
    }
    
    override fun onClick(storeId: Long)
    {
        val args = Bundle()
        args.putLong(getString(R.string.arg_id) , storeId)
        
        launchEditFragment(args)
    }
    
    override fun onDeleteStore(storeEntity: StoreEntity)
    {
       val items = arrayOf("Eliminar","LLamar","Ir al sitio web")
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_options_title))
            .setItems(items) { _ , which ->
                when(which){
                    0 -> confirmDialog(storeEntity)
                    1 -> callStore(storeEntity.phone)
                    2 -> openWebSite(storeEntity.website)
                }
            }
            .show()
        
    }
    
    private fun openWebSite(webSite: String)
    {
        if (webSite.isEmpty())
        {
            Toast.makeText(this, getString(R.string.main_error_no_website), Toast.LENGTH_LONG).show()
        } else
        {
            try
            {
                val webSiteIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(webSite)
                }
                startActivity(intent)
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }
    
    private fun callStore(phone : String)
    {
        
        try
        {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phone")
            }
            startActivity(callIntent)
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
    
    private fun confirmDialog(storeEntity: StoreEntity)
    {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_title_delete))
            .setPositiveButton(getString(R.string.dialog_delete_positive)) { _ , _ ->
                val queue = LinkedBlockingQueue<StoreEntity>()
                Thread {
                    StoreApplication.database.storeDAO().deleteStore(storeEntity)
                    queue.add(storeEntity)
                }.start()
                mAdapter.delete(queue.take())
            }
            .setNegativeButton(getString(R.string.dialog_delete_negative),null)
            .show()
    }
    
    /**
     * Main Aux
     */
    override fun hideFab(isVisible: Boolean){
        if (isVisible)
            mBinding.fab.show()
        else
            mBinding.fab.hide()
    }
    
    override fun addStore(storeEntity: StoreEntity)
    {
        mAdapter.add(storeEntity)
    }
    
    override fun updateStore(storeEntity: StoreEntity)
    {
        mAdapter.update(storeEntity)
    }
}