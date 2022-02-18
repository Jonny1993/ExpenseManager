package model

import android.content.Context
import androidx.room.Room

class DbFactory {
    companion object {
        private var db: AppDatabase?=null
        private fun initiateDb(applicationContext: Context) {
            db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "MainDatabase2"
            ).allowMainThreadQueries().build()
        }
        fun getDbContext(applicationContext: Context): AppDatabase? {
            if(db ==null){
                initiateDb(applicationContext)
            }
            return db
        }
    }
}