package appnet.tech.parkingofappnet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ConexionSQLiteHelper extends SQLiteOpenHelper {



    public ConexionSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void truncate (SQLiteDatabase db)
    {
        db.execSQL("delete from "+Utilidades.TABLA_CONFIGURACION);
        db.execSQL("delete from "+Utilidades.TABLA_VEHICULOS);
        db.execSQL("delete from "+Utilidades.TABLA_PARKING);
        db.execSQL("delete from "+Utilidades.TABLA_TIPOPAGO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try
        {

            db.execSQL(Utilidades.CREAR_TABLA_CONFIGURACION);
            db.execSQL(Utilidades.CREAR_TABLA_VEHICULOS);
            db.execSQL(Utilidades.CREAR_TABLA_TIPOPAGO);
            db.execSQL(Utilidades.CREAR_TABLA_PARKING);
        }catch (Exception e)
        {
            e.toString();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists  "+Utilidades.TABLA_CONFIGURACION);
        db.execSQL("drop table if exists  "+Utilidades.TABLA_VEHICULOS);
        db.execSQL("drop table if exists  "+Utilidades.TABLA_PARKING);
        db.execSQL("drop table if exists  "+Utilidades.TABLA_TIPOPAGO);
    }
}
