package unipd.dei.cashelper.helpers

import android.appwidget.AppWidgetManager
import android.content.*
import android.content.ContentValues.TAG
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlin.collections.ArrayList
import unipd.dei.cashelper.WidgetApp


class DBHelper(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private val appContext = context

    override fun onCreate(db: SQLiteDatabase) {
        val categoryTable = "CREATE TABLE Categoria(\n" +
                "Nome VARCHAR(30) NOT NULL PRIMARY KEY\n"+
                ");"

        val dateTable = "CREATE TABLE Data(\n" +
                "Giorno INTEGER NOT NULL, \n" +
                "Mese VARCHAR(10) NOT NULL, \n" +
                "Anno INTEGER NOT NULL,\n" +
                "PRIMARY KEY(Giorno, Mese, Anno)\n" +
                ");"

        val itemTable = "CREATE TABLE Item(\n" +
                "Id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, \n" +
                "Descrizione VARCHAR(50), \n" +
                "Prezzo REAL NOT NULL, \n" +
                "Tipo VARCHAR(10) NOT NULL, \n" +
                "Nome VARCHAR(20) NOT NULL, \n" +
                "Giorno INTEGER NOT NULL, \n" +
                "Mese VARCHAR(10) NOT NULL, \n" +
                "Anno INTEGER NOT NULL,\n" +
                "FOREIGN KEY(Nome) REFERENCES Categoria(Nome)\n" +
                "ON DELETE CASCADE ON UPDATE CASCADE, \n" +
                "FOREIGN KEY (Giorno, Mese, Anno) REFERENCES Data(Giorno, Mese, Anno)\n" +
                "ON DELETE CASCADE ON UPDATE CASCADE\n" +
                ");"

        val categoryValues = "INSERT INTO Categoria(Nome) VALUES\n" +
                "('Salario'), \n" +
                "('Alimentari'), \n" +
                "('Trasporti'), \n" +
                "('Shopping'), \n" +
                "('Viaggi'), \n" +
                "('Bollette'), \n" +
                "('Lavoro'), \n" +
                "('Sport/Hobby'), \n" +
                "('Automobile'), \n" +
                "('Regali'), \n" +
                "('Altro');"

        db.execSQL(categoryTable)
        db.execSQL(dateTable)
        db.execSQL(itemTable)
        db.execSQL(categoryValues)
    }

    // no updates planned
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    // enable the use of foreign keys
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }
    private fun sendWidgetUpdateBroadcast(context: Context) {

        val updateIntent = Intent(context, WidgetApp::class.java)
        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        context.sendBroadcast(updateIntent)
    }

    fun addCategory(name: String): Boolean {
        val cv = ContentValues()
        if(getCategoryName().contains(name))
            return false
        cv.put("Nome", name)
        return writableDatabase.insert("Categoria", null, cv) != -1L
    }

    fun getCategoryName(): ArrayList<String> {
        val cursor = readableDatabase.rawQuery("SELECT Nome FROM Categoria", null)
        val names = ArrayList<String>()

        while(cursor.moveToNext())
            names.add(cursor.getString(0))
        cursor.close()
        return names
    }

    fun addItem(description: String, price: Double, type: String, category: String, day: Int, month: String, year: Int): Boolean {
        val cv = ContentValues()
        cv.put("Giorno", day)
        cv.put("Mese", month)
        cv.put("Anno", year)
        writableDatabase.insertWithOnConflict("Data", null, cv, SQLiteDatabase.CONFLICT_IGNORE) != -1L
        val values = ContentValues().apply {
            put("Descrizione", description)
            put("Prezzo", price)
            put("Tipo", type)
            put("Nome", category)
            put("Giorno", day)
            put("Mese", month)
            put("Anno", year)
        }
        Log.d(TAG, "aggiunta item")
        val check = writableDatabase.insert("Item", null, values) != -1L
        sendWidgetUpdateBroadcast(appContext)
        return check
    }

    fun updateItem(originalId: Int = -1, description: String = "", price: Double = -1.0, type: String = "", category: String = "", day: Int = -1, month: String = "", year: Int = -1): Boolean {
        if(originalId == -1)
            return false
        val cv = ContentValues()
        cv.put("Giorno", day)
        cv.put("Mese", month)
        cv.put("Anno", year)
        writableDatabase.insertWithOnConflict("Data", null, cv, SQLiteDatabase.CONFLICT_IGNORE) != -1L
        val values = ContentValues().apply {
            if(description != "")
                put("Descrizione", description)
            if(price != -1.0)
                put("Prezzo", price)
            if(type != "")
                put("Tipo", type)
            if(category != "")
                put("Nome", category)
            if(day != -1)
                put("Giorno", day)
            if(month != "")
                put("Mese", month)
            if(year != -1)
                put("Anno", year)
        }
        val check = writableDatabase.update("Item", values, "Id=?", arrayOf("$originalId")) > 0
        // notifyWidgetChange()
        sendWidgetUpdateBroadcast(appContext)
        return check
    }

    fun removeItem(originalId: Int): Boolean {
        val check = writableDatabase.delete("Item", "Id=?", arrayOf("$originalId")) > 0
        // notifyWidgetChange()
        sendWidgetUpdateBroadcast(appContext)
        return check
    }

    fun removeCategory(nomeCategoria: String): Boolean {
        val check = writableDatabase.delete("Categoria", "Nome=?", arrayOf(nomeCategoria)) > 0
        sendWidgetUpdateBroadcast(appContext)
        return check
    }

    fun getItem(month: String, year: Int): ArrayList<ItemInfo> {
        val cursor = readableDatabase.rawQuery("SELECT * FROM Item WHERE Mese ='${month}' AND Anno ='${year}'", null)
        val ret = ArrayList<ItemInfo>()

        while(cursor.moveToNext()) {
            val item = ItemInfo("")
            item.id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"))
            item.category = cursor.getString(cursor.getColumnIndexOrThrow("Nome"))
            item.price = cursor.getDouble(cursor.getColumnIndexOrThrow("Prezzo"))
            item.day = cursor.getInt(cursor.getColumnIndexOrThrow("Giorno"))
            item.month = cursor.getString(cursor.getColumnIndexOrThrow("Mese"))
            item.year = cursor.getInt(cursor.getColumnIndexOrThrow("Anno"))
            item.type = cursor.getString(cursor.getColumnIndexOrThrow("Tipo"))
            item.description = cursor.getString(cursor.getColumnIndexOrThrow("Descrizione"))
            ret.add(item)
        }
        cursor.close()
        return ret
    }

    fun getItemById(id: Int): ItemInfo {
        val cursor = readableDatabase.rawQuery("SELECT * FROM Item WHERE Id ='${id}'", null)

        val item = ItemInfo("")
        while(cursor.moveToNext()) {
            item.id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"))
            item.description = cursor.getString(cursor.getColumnIndexOrThrow("Descrizione"))
            item.category = cursor.getString(cursor.getColumnIndexOrThrow("Nome"))
            item.price = cursor.getDouble(cursor.getColumnIndexOrThrow("Prezzo"))
            item.day = cursor.getInt(cursor.getColumnIndexOrThrow("Giorno"))
            item.month = cursor.getString(cursor.getColumnIndexOrThrow("Mese"))
            item.year = cursor.getInt(cursor.getColumnIndexOrThrow("Anno"))
            item.type = cursor.getString(cursor.getColumnIndexOrThrow("Tipo"))
        }
        cursor.close()
        return item
    }
    
    fun getItemsByType(type: String, month: String, year:Int): ArrayList<ItemInfo> {
        val cursor = readableDatabase.rawQuery("SELECT * FROM Item WHERE Mese ='${month}' AND Anno ='${year}' AND Tipo='${type}'", null)
        val ret = ArrayList<ItemInfo>()

        while(cursor.moveToNext()) {
            val item = ItemInfo("")
            item.id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"))
            item.category = cursor.getString(cursor.getColumnIndexOrThrow("Nome"))
            item.price = cursor.getDouble(cursor.getColumnIndexOrThrow("Prezzo"))
            item.day = cursor.getInt(cursor.getColumnIndexOrThrow("Giorno"))
            item.month = cursor.getString(cursor.getColumnIndexOrThrow("Mese"))
            item.year = cursor.getInt(cursor.getColumnIndexOrThrow("Anno"))
            item.type = cursor.getString(cursor.getColumnIndexOrThrow("Tipo"))
            item.description = cursor.getString(cursor.getColumnIndexOrThrow("Descrizione"))
            ret.add(item)
        }
        cursor.close()
        return ret
    }

    fun getDefaultCategories(): ArrayList<String> {
        return DEFAULT_CAT
    }

    companion object
    {
        private const val DB_NAME = "database.db"
        private const val DB_VERSION = 1
        private val DEFAULT_CAT: ArrayList<String> = ArrayList()
        init {
            DEFAULT_CAT.add("Salario")
            DEFAULT_CAT.add("Alimentari")
            DEFAULT_CAT.add("Trasporti")
            DEFAULT_CAT.add("Shopping")
            DEFAULT_CAT.add("Viaggi")
            DEFAULT_CAT.add("Bollette")
            DEFAULT_CAT.add("Lavoro")
            DEFAULT_CAT.add("Sport/Hobby")
            DEFAULT_CAT.add("Automobile")
            DEFAULT_CAT.add("Regali")
            DEFAULT_CAT.add("Altro")
        }
    }

    data class ItemInfo(var changes: String) {
        var id: Int = -1
        var category: String = ""
        var price: Double = -1.0
        var day: Int = -1
        var month: String = ""
        var year: Int = -1
        var type: String = ""
        var description: String = ""
    }
}