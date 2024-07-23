package com.jlobatonm.stores

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jlobatonm.stores.databinding.ItemStoreBinding

class StoreAdapter(private var stores: MutableList<StoreEntity> , private var listener: MainActivity) :
        RecyclerView.Adapter<StoreAdapter.ViewHolder>()
{
    
    private lateinit var mContext: Context // Estandar de buenas practicas
    
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val binding = ItemStoreBinding.bind(view)
        
        fun setListener(storeEntity: StoreEntity)
        {
            with(binding.root){
                setOnLongClickListener {
                    listener.onDeleteStore(storeEntity)
                    true
                }
                setOnClickListener { listener.onClick(storeEntity.id) }
                
            }
           
            
            binding.cbFavorite.setOnClickListener { listener.onFavoriteStore(storeEntity) }
            
           
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder
    {
        mContext = parent.context
        
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_store , parent , false)
        
        return ViewHolder(view)
        
    }
    
    override fun onBindViewHolder(holder: ViewHolder , position: Int)
    {
        val store = stores[position]
        
        with(holder)
        {
            setListener(store)
            
            binding.tvName.text = store.name
            binding.cbFavorite.isChecked = store.isFavorite
            binding.cbFavorite.setOnClickListener {
                listener.onFavoriteStore(store)
            }
            Glide.with(mContext)
                .load(store.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.ic_image)
                .into(binding.imgPhoto)
        }
    }
    
    override fun getItemCount(): Int = stores.size
    @SuppressLint("NotifyDataSetChanged")
    fun add(storeEntity: StoreEntity)
    {
        if(!stores.contains(storeEntity)){
            stores.add(storeEntity)
            notifyItemInserted(stores.size - 1)
        }
        
    }
    
    @SuppressLint("NotifyDataSetChanged")
    fun setStores(stores: MutableList<StoreEntity>)
    {
        this.stores = stores
        notifyDataSetChanged()
    }
    
    fun update(store: StoreEntity?)
    {
        val index = stores.indexOf(store)
        if(index != -1)
        {
            stores[index] = store!!
            notifyItemChanged(index)
        }
    }
    
    fun delete(store: StoreEntity?)
    {
        val index = stores.indexOf(store)
        if(index != -1)
        {
            stores.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    
}