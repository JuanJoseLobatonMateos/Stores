package com.jlobatonm.stores

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [StoreEntity::class] , version = 2)
abstract class StoreDatabase : RoomDatabase()
{
    abstract fun storeDAO(): StoreDAO
}
