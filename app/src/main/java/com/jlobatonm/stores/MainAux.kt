package com.jlobatonm.stores


/*********************************************************
 * Proyecto : Stores
 * From : com.jlobatonm.stores
 * Creado por Juan Lobatón en 23/7/2024 a las 22:19
 * Más info:  https://www.linkedin.com/in/jjlobatonmateos
 *            https://github.com/JuanJoseLobatonMateos
 * Todos los derechos reservados 2024
 **********************************************************/
interface MainAux {
    fun hideFab(isVisible : Boolean = false)
    
    fun addStore(storeEntity: StoreEntity)
    
    fun updateStore(storeEntity: StoreEntity)
}