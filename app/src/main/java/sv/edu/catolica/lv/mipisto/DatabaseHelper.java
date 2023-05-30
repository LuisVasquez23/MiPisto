package sv.edu.catolica.lv.mipisto;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MipistoDB";
    private static final int DATABASE_VERSION = 1;

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE User (user_id INTEGER PRIMARY KEY AUTOINCREMENT, user_name VARCHAR(60), password VARCHAR(60), user_image BLOB, email VARCHAR(60), presupuesto_mensual DECIMAL, plazo_registros INTEGER, dia_inicio_mes DATETIME);");
        db.execSQL("CREATE TABLE Categoria (category_id INTEGER PRIMARY KEY AUTOINCREMENT, category_name VARCHAR(60), category_image BLOB, user_id INTEGER, FOREIGN KEY (user_id) REFERENCES User(user_id));");
        db.execSQL("CREATE TABLE Transacciones (Transaction_Id INTEGER PRIMARY KEY AUTOINCREMENT, Description VARCHAR(160), Data_registred DATETIME, Amount DECIMAL, Transaction_type VARCHAR(60), category_image BLOB, category_id INTEGER, user_id INTEGER, FOREIGN KEY (category_id) REFERENCES Categoria(category_id), FOREIGN KEY (user_id) REFERENCES User(user_id));");
        db.execSQL("CREATE TABLE Historial (historial_id INTEGER PRIMARY KEY AUTOINCREMENT, fondos_iniciales DECIMAL, fondos_finales DECIMAL, fecha_final DATETIME, user_id INTEGER, FOREIGN KEY (user_id) REFERENCES User(user_id));");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
