package com.example.trackmytraining;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistroActivity extends AppCompatActivity {

    TextView tvFecha;
    TextInputEditText etEjercicio, etNumSerie, etReps, etPeso, etRir;
    View btnGuardar;
    MaterialButton btnEliminarSeleccionados;
    LinearLayout llContenedorTabla;
    String fechaRecibida;
    AdminSQLiteOpenHelper dbHelper;

    // Multi-select state
    Set<Integer> selectedIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        tvFecha = findViewById(R.id.tvTituloFecha);
        etEjercicio = findViewById(R.id.etEjercicio);
        etNumSerie = findViewById(R.id.etNumSerie);
        etReps = findViewById(R.id.etReps);
        etPeso = findViewById(R.id.etPeso);
        etRir = findViewById(R.id.etRir);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminarSeleccionados = findViewById(R.id.btnEliminarSeleccionados);

        llContenedorTabla = findViewById(R.id.llContenedorTabla);

        dbHelper = new AdminSQLiteOpenHelper(this);

        fechaRecibida = getIntent().getStringExtra("FECHA");
        if (fechaRecibida == null) {
            fechaRecibida = "Hoy";
        }
        tvFecha.setText("Entreno: " + fechaRecibida);

        cargarTabla();

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDatos();
            }
        });

        btnEliminarSeleccionados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoEliminarSeleccionados();
            }
        });
    }

    private void guardarDatos() {
        String ejercicio = etEjercicio.getText().toString();
        String serieStr = etNumSerie.getText().toString();
        String repsStr = etReps.getText().toString();
        String pesoStr = etPeso.getText().toString();
        String rirStr = etRir.getText().toString();

        if(!ejercicio.isEmpty() && !serieStr.isEmpty() && !repsStr.isEmpty()){
            int serie = Integer.parseInt(serieStr);
            int reps = Integer.parseInt(repsStr);
            double peso = pesoStr.isEmpty() ? 0.0 : Double.parseDouble(pesoStr);
            double rir = rirStr.isEmpty() ? 0.0 : Double.parseDouble(rirStr);

            dbHelper.agregarSerie(fechaRecibida, ejercicio, serie, reps, peso, rir);
            Toast.makeText(this, "Serie Agregada", Toast.LENGTH_SHORT).show();

            cargarTabla();

            etNumSerie.setText(String.valueOf(serie + 1));
            etReps.setText("");
            etRir.setText("");
            etReps.requestFocus();
        } else {
            Toast.makeText(this, "Faltan datos obligatorios (Ejercicio, Serie, Reps)", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarTabla() {
        llContenedorTabla.removeAllViews();
        selectedIds.clear();
        actualizarBotonEliminar();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor fila = db.rawQuery("SELECT id, ejercicio, num_serie, repeticiones, peso, rir FROM entrenamientos WHERE fecha = ? ORDER BY id ASC", new String[]{fechaRecibida});

        while(fila.moveToNext()){
            int idRegistro = fila.getInt(0);
            String nom = fila.getString(1);
            String num = fila.getString(2);
            String rep = fila.getString(3);
            double peso = fila.getDouble(4);
            double rir = fila.getDouble(5);

            crearTarjetaSerie(idRegistro, nom, num, rep, peso, rir);
        }
        fila.close();
        db.close();
    }

    private void crearTarjetaSerie(int id, String nom, String num, String rep, double peso, double rir) {
        // --- CARDVIEW CONTAINER ---
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.parseColor("#1A1A1A"));
        card.setRadius(40f);
        card.setCardElevation(0f);

        // Content Layout (Horizontal)
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding(24, 28, 32, 28);
        content.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(content);

        // 0. CHECKBOX
        CheckBox checkBox = new CheckBox(this);
        checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D0BCFF")));
        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cbParams.setMarginEnd(16);
        checkBox.setLayoutParams(cbParams);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedIds.add(id);
                } else {
                    selectedIds.remove(id);
                }
                actualizarBotonEliminar();
            }
        });

        // 1. INFO COLUMN (Exercise Name + Details)
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvNom = new TextView(this);
        tvNom.setText(nom);
        tvNom.setTextColor(Color.parseColor("#E6E1E5"));
        tvNom.setTypeface(null, Typeface.BOLD);
        tvNom.setTextSize(16f);

        TextView tvDetails = new TextView(this);
        String detailsText = peso + "kg x " + rep + " reps";
        if (rir > 0) {
            detailsText += " @ RIR " + rir;
        }
        tvDetails.setText(detailsText);
        tvDetails.setTextColor(Color.parseColor("#CAC4D0"));
        tvDetails.setTextSize(14f);
        tvDetails.setPadding(0, 8, 0, 0);

        infoLayout.addView(tvNom);
        infoLayout.addView(tvDetails);

        // 2. SERIES BADGE
        TextView tvSerie = new TextView(this);
        tvSerie.setText("#" + num);
        tvSerie.setTextColor(Color.parseColor("#D0BCFF"));
        tvSerie.setTypeface(null, Typeface.BOLD);
        tvSerie.setTextSize(18f);
        tvSerie.setPadding(16, 0, 0, 0);

        // Add to content: checkbox, info, badge
        content.addView(checkBox);
        content.addView(infoLayout);
        content.addView(tvSerie);

        // Long click to delete single item (still supported)
        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mostrarDialogoEliminar(id, nom, num);
                return true;
            }
        });

        llContenedorTabla.addView(card);
    }

    private void actualizarBotonEliminar() {
        if (selectedIds.isEmpty()) {
            btnEliminarSeleccionados.setVisibility(View.GONE);
        } else {
            btnEliminarSeleccionados.setVisibility(View.VISIBLE);
            btnEliminarSeleccionados.setText("ELIMINAR " + selectedIds.size() + " SELECCIONADOS");
        }
    }

    private void mostrarDialogoEliminarSeleccionados() {
        int count = selectedIds.size();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar");
        builder.setMessage("¿Borrar " + count + " serie" + (count > 1 ? "s" : "") + " seleccionada" + (count > 1 ? "s" : "") + "?");
        builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Integer> idsToDelete = new ArrayList<>(selectedIds);
                dbHelper.eliminarVariasSeries(idsToDelete);
                Toast.makeText(RegistroActivity.this, count + " serie" + (count > 1 ? "s" : "") + " eliminada" + (count > 1 ? "s" : ""), Toast.LENGTH_SHORT).show();
                cargarTabla();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
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