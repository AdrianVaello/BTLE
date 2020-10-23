package com.example.advaepe.btle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.advaepe.btle.utilidades.Utilidades;

public class ConexionSQLite extends SQLiteOpenHelper {


    //Conexion a la base de datos Sqlite

    public ConexionSQLite(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //Crea la tabla de lecturas si no esta creada ya
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Utilidades.CREAR_TABLA_LECTURAS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAntiguaBBDD, int versionNuevaBBD) {
    db.execSQL("DROP TABLE IF EXISTS "+ Utilidades.TABLA_LECTURAS);
    onCreate(db);
    }
}
