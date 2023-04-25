package unipd.dei.cashelper.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val categoryTable = "CREATE TABLE Categoria(\n" +
                "Nome VARCHAR(30) NOT NULL PRIMARY KEY\n"+
                ");"

        val dateTable = "CREATE TABLE Data(\n" +
                "Giorno INTEGER NOT NULL, \n" +
                "Mese INTEGER NOT NULL, \n" +
                "Anno INTEGER NOT NULL,\n" +
                "PRIMARY KEY(Giorno, Mese, Anno)\n" +
                ");"

        val itemTable = "CREATE TABLE Item(\n" +
                "Id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, \n" +
                "Descrizione VARCHAR(50), \n" +
                "Prezzo REAL NOT NULL, \n" +
                "Tipo VARCHAR(10) NOT NULL, \n" +
                "FOREIGN KEY(Nome) REFERENCES Categoria(Nome)\n" +
                "ON DELETE CASCADE ON UPDATE CASCADE, \n" +
                "FOREIGN KEY (Giorno, Mese, Anno) REFERENCES Data(Giorno, Mese, Anno)\n" +
                "ON DELETE CASCADE ON UPDATE CASCADE\n" +
                ");"
        /*
        val monthValues = "INSERT INTO Data(Mese) VALUES\n" +
                "('Gennaio'), \n" +
                "('Febbraio'), \n" +
                "('Marzo'), \n" +
                "('Aprile'), \n" +
                "('Maggio'), \n" +
                "('Giugno'), \n" +
                "('Luglio'), \n" +
                "('Agosto'), \n" +
                "('Settembre'), \n" +
                "('Ottobre'), \n" +
                "('Novembre'), \n" +
                "('Dicembre');"
        */
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
        // db.execSQL(monthValues)
        db.execSQL(categoryValues)
    }

    // non previsti aggiornamenti
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    // abilito l'utilizzo delle foreign keys
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
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


    /*

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        String date = day + "/" + (month+1) + "/" + year;

    */

    // DA CONTROLLARE !!!!!!!!
    // se ritorna false vuol dire che la data non deve essere aggiornata
    fun updateDate(day: Int, month: String, year: Int): Boolean {
        val cursor = readableDatabase.rawQuery("SELECT Giorno, Mese, Anno FROM Data", null)
        val ret = mutableListOf<DateInfo>()
        var date: DateInfo

        while(cursor.moveToNext()) {
            date = DateInfo("")
            date.day = cursor.getInt(cursor.getColumnIndexOrThrow("Giorno"))
            date.month = cursor.getString(cursor.getColumnIndexOrThrow("Mese"))
            date.year = cursor.getInt(cursor.getColumnIndexOrThrow("Anno"))
            ret.add(date)
        }
        cursor.close()
        // data da controllare
        date = DateInfo("")
        date.day = day
        date.month = month
        date.year = year
        if(ret.contains(date))
            return false
        else {
            val cv = ContentValues()
            val values = ContentValues().apply {
                put("Giorno", day)
                put("Mese", month)
                put("Anno", year)
            }
            return writableDatabase.insert("Data", null, values) != -1L
        }
    }
    /*
    fun addItem() {

    }

    fun updateItem() {

    }

    fun removeItem() {

    }
    */


    companion object
    {
        private const val DB_NAME = "database.db"
        private const val DB_VERSION = 1
    }

    data class DateInfo(var changes: String) {
        var day : Int = -1
        var month: String = ""
        var year: Int = -1
    }
}