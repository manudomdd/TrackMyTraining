package com.example.trackmytraining;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

/**
 * Clase auxiliar para la gestión de la base de datos SQLite de la aplicación.
 * <p>
 * Se encarga de crear, actualizar y proporcionar métodos CRUD para la tabla
 * {@code entrenamientos}, donde se almacenan todas las series de entrenamiento
 * registradas por el usuario.
 * </p>
 *
 * <h3>Esquema de la tabla {@code entrenamientos}:</h3>
 * <table>
 *   <tr><th>Columna</th><th>Tipo</th><th>Descripción</th></tr>
 *   <tr><td>id</td><td>INTEGER (PK, AUTOINCREMENT)</td><td>Identificador único</td></tr>
 *   <tr><td>fecha</td><td>TEXT</td><td>Fecha del entrenamiento (dd/M/yyyy)</td></tr>
 *   <tr><td>ejercicio</td><td>TEXT</td><td>Nombre del ejercicio</td></tr>
 *   <tr><td>num_serie</td><td>INTEGER</td><td>Número de la serie</td></tr>
 *   <tr><td>repeticiones</td><td>INTEGER</td><td>Número de repeticiones</td></tr>
 *   <tr><td>peso</td><td>REAL</td><td>Peso utilizado en kg</td></tr>
 *   <tr><td>rir</td><td>REAL</td><td>Repeticiones en reserva (RIR)</td></tr>
 * </table>
 *
 * @author Manuel Dominguez
 * @version 2.0
 */
public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    /** Nombre de la base de datos local. */
    private static final String DB_NAME = "GymDB";

    /** Versión actual de la base de datos. Incrementar al modificar el esquema. */
    private static final int DB_VERSION = 2;

    /**
     * Constructor del helper de base de datos.
     *
     * @param context Contexto de la aplicación o actividad.
     */
    public AdminSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Crea la estructura inicial de la base de datos.
     * <p>
     * Se ejecuta automáticamente la primera vez que se accede a la base de datos.
     * Define la tabla {@code entrenamientos} con sus columnas.
     * </p>
     *
     * @param db Instancia de la base de datos sobre la que se ejecuta la creación.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE entrenamientos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "fecha TEXT, "
                + "ejercicio TEXT, "
                + "num_serie INTEGER, "
                + "repeticiones INTEGER, "
                + "peso REAL, "
                + "rir REAL)");
    }

    /**
     * Actualiza la base de datos cuando se detecta un cambio de versión.
     * <p>
     * <strong>Advertencia:</strong> La estrategia actual elimina la tabla existente
     * y la recrea desde cero, lo que implica la pérdida de todos los datos previos.
     * En una aplicación de producción se recomienda usar {@code ALTER TABLE}.
     * </p>
     *
     * @param db         Instancia de la base de datos.
     * @param oldVersion Versión anterior del esquema.
     * @param newVersion Nueva versión del esquema.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS entrenamientos");
        onCreate(db);
    }

    /**
     * Inserta una nueva serie de entrenamiento en la base de datos.
     *
     * @param fecha     Fecha del entrenamiento en formato "dd/M/yyyy".
     * @param ejercicio Nombre del ejercicio realizado.
     * @param numSerie  Número de la serie dentro del ejercicio.
     * @param reps      Número de repeticiones realizadas.
     * @param peso      Peso utilizado en kilogramos.
     * @param rir       Repeticiones en reserva (Reps In Reserve).
     */
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

    /**
     * Elimina una serie individual de la base de datos.
     *
     * @param id Identificador único de la serie a eliminar.
     */
    public void eliminarSerie(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("entrenamientos", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * Elimina múltiples series de la base de datos en una única transacción.
     * <p>
     * Utiliza una transacción SQL para garantizar la atomicidad de la operación:
     * o se eliminan todas las series correctamente, o no se elimina ninguna.
     * </p>
     *
     * @param ids Lista de identificadores de las series a eliminar.
     *            Si es {@code null} o está vacía, no se realiza ninguna operación.
     */
    public void eliminarVariasSeries(List<Integer> ids) {
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
