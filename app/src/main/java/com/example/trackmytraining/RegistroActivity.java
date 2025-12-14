package com.example.trackmytraining;  // <--- PON TU PAQUETE AQUÍ

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout; // Usamos LinearLayout, más fiable que TableRow
import android.widget.TextView;
import android.widget.Toast;

public class RegistroActivity extends AppCompatActivity {

    TextView tvFecha;
    EditText etEjercicio, etNumSerie, etReps;
    Button btnGuardar;
    LinearLayout llContenedorTabla; // Cambiado de TableLayout a LinearLayout
    String fechaRecibida;
    AdminSQLiteOpenHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        tvFecha = findViewById(R.id.tvTituloFecha);
        etEjercicio = findViewById(R.id.etEjercicio);
        etNumSerie = findViewById(R.id.etNumSerie);
        etReps = findViewById(R.id.etReps);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Referencia al contenedor vertical
        llContenedorTabla = findViewById(R.id.llContenedorTabla);

        dbHelper = new AdminSQLiteOpenHelper(this);

        fechaRecibida = getIntent().getStringExtra("FECHA");
        tvFecha.setText("Entreno: " + fechaRecibida);

        cargarTabla();

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDatos();
            }
        });
    }

    private void guardarDatos() {
        String ejercicio = etEjercicio.getText().toString();
        String serieStr = etNumSerie.getText().toString();
        String repsStr = etReps.getText().toString();

        if(!ejercicio.isEmpty() && !serieStr.isEmpty() && !repsStr.isEmpty()){
            int serie = Integer.parseInt(serieStr);
            int reps = Integer.parseInt(repsStr);

            dbHelper.agregarSerie(fechaRecibida, ejercicio, serie, reps);
            Toast.makeText(this, "Añadido", Toast.LENGTH_SHORT).show();

            cargarTabla();

            // Autoincrementar y limpiar
            etNumSerie.setText(String.valueOf(serie + 1));
            etReps.setText("");
            etReps.requestFocus();
        } else {
            Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarTabla() {
        // Limpiamos todas las vistas anteriores del contenedor
        llContenedorTabla.removeAllViews();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Orden ASCENDENTE (id ASC) para que los nuevos salgan abajo
        Cursor fila = db.rawQuery("SELECT id, ejercicio, num_serie, repeticiones FROM entrenamientos WHERE fecha = ? ORDER BY id ASC", new String[]{fechaRecibida});

        while(fila.moveToNext()){
            int idRegistro = fila.getInt(0);
            String nom = fila.getString(1);
            String num = fila.getString(2);
            String rep = fila.getString(3);

            // --- CREAMOS LA FILA COMO LINEARLAYOUT (Igual que la cabecera en XML) ---
            LinearLayout row = new LinearLayout(this);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(rowParams);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 20, 0, 20); // Espacio vertical
            row.setBackgroundColor(Color.parseColor("#1E1E1E")); // Fondo oscuro

            // Pulsación larga para borrar
            row.setTag(idRegistro);
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mostrarDialogoEliminar((int) v.getTag(), nom, num);
                    return true;
                }
            });

            // 1. COLUMNA EJERCICIO (Peso 2)
            TextView tvNom = new TextView(this);
            tvNom.setText(nom);
            tvNom.setTextColor(Color.WHITE);
            tvNom.setPadding(20,0,0,0);
            // Params: ancho 0, alto wrap, PESO 2f
            LinearLayout.LayoutParams paramsNom = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f);
            tvNom.setLayoutParams(paramsNom);


            // 2. COLUMNA SERIE (Peso 1)
            TextView tvNum = new TextView(this);
            tvNum.setText(num);
            tvNum.setTextColor(Color.WHITE);
            tvNum.setGravity(Gravity.CENTER);
            // Params: ancho 0, alto wrap, PESO 1f
            LinearLayout.LayoutParams paramsNum = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tvNum.setLayoutParams(paramsNum);


            // 3. COLUMNA REPS (Peso 1)
            TextView tvRep = new TextView(this);
            tvRep.setText(rep);
            tvRep.setTextColor(Color.WHITE);
            tvRep.setGravity(Gravity.CENTER);
            // Params: ancho 0, alto wrap, PESO 1f
            LinearLayout.LayoutParams paramsRep = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tvRep.setLayoutParams(paramsRep);

            // Añadir textos a la fila
            row.addView(tvNom);
            row.addView(tvNum);
            row.addView(tvRep);

            // Añadir fila al contenedor principal
            llContenedorTabla.addView(row);

            // Añadir una línea separadora fina (Opcional, para estética)
            View linea = new View(this);
            linea.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            linea.setBackgroundColor(Color.parseColor("#333333"));
            llContenedorTabla.addView(linea);
        }
        fila.close();
        db.close();
    }

    private void mostrarDialogoEliminar(final int idEliminar, String nombreEjercicio, String numSerie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar");
        builder.setMessage("¿Borrar " + nombreEjercicio + " (Serie " + numSerie + ")?");
        builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.eliminarSerie(idEliminar);
                Toast.makeText(RegistroActivity.this, "Eliminado", Toast.LENGTH_SHORT).show();
                cargarTabla();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}