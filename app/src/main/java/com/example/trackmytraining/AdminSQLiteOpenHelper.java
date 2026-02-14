package com.example.trackmytraining; // Asegúrate de usar tu nombre de paquete

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    public AdminSQLiteOpenHelper(Context context) {
        super(context, "GymDB", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla: ID, FECHA, EJERCICIO, NUMERO_SERIE, REPETICIONES, PESO, RIR
        db.execSQL("CREATE TABLE entrenamientos (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT, ejercicio TEXT, num_serie INTEGER, repeticiones INTEGER, peso REAL, rir REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy: Drop and recreate. 
        // Warning: This deletes data. For a real production app, use ALTER TABLE.
        db.execSQL("DROP TABLE IF EXISTS entrenamientos");
        onCreate(db);
    }

    // Método para insertar una serie individual
    public void agregarSerie(String fecha, String ejercicio, int numSerie, int reps, double peso, double rir) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fecha", fecha);
        values.put("ejercicio", ejercicio);
        values.put("num_serie", numSerie);
        values.put("repeticiones", reps);
        values.put("peso", peso);
        values.put("rir", rir);

        db.insert("entrenamientos", null, values);
        db.close();
    }

    public void eliminarSerie(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Borramos la fila donde el 'id' coincida
        db.delete("entrenamientos", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Método para eliminar varias series a la vez
    public void eliminarVariasSeries(java.util.List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int id : ids) {
                db.delete("entrenamientos", "id=?", new String[]{String.valueOf(id)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }
}
