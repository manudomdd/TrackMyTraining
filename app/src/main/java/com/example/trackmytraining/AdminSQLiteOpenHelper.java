package com.example.trackmytraining; // Asegúrate de usar tu nombre de paquete

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    public AdminSQLiteOpenHelper(Context context) {
        super(context, "GymDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla: ID, FECHA, EJERCICIO, NUMERO_SERIE, REPETICIONES
        db.execSQL("CREATE TABLE entrenamientos (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT, ejercicio TEXT, num_serie INTEGER, repeticiones INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS entrenamientos");
        onCreate(db);
    }

    // Método para insertar una serie individual
    public void agregarSerie(String fecha, String ejercicio, int numSerie, int reps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fecha", fecha);
        values.put("ejercicio", ejercicio);
        values.put("num_serie", numSerie);
        values.put("repeticiones", reps);

        db.insert("entrenamientos", null, values);
        db.close();
    }

    public void eliminarSerie(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Borramos la fila donde el 'id' coincida
        db.delete("entrenamientos", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }
}
