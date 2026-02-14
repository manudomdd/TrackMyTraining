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

/**
 * Actividad de registro de series de entrenamiento.
 * <p>
 * Permite al usuario registrar series individuales de un ejercicio, indicando:
 * nombre del ejercicio, número de serie, repeticiones, peso (kg) y RIR
 * (Repeticiones en Reserva). Todas las series se almacenan en la base de datos
 * local mediante {@link AdminSQLiteOpenHelper}.
 * </p>
 *
 * <h3>Funcionalidades principales:</h3>
 * <ul>
 *     <li>Registro de series con auto-incremento del número de serie.</li>
 *     <li>Visualización del historial del día en tarjetas individuales.</li>
 *     <li>Eliminación individual mediante pulsación larga.</li>
 *     <li>Eliminación múltiple mediante checkboxes y botón de borrado.</li>
 * </ul>
 *
 * <p>
 * Recibe la fecha del entrenamiento desde {@link MainActivity} a través
 * del Intent con la clave {@code "FECHA"}.
 * </p>
 *
 * @author Manuel Dominguez
 * @version 2.0
 * @see MainActivity
 * @see AdminSQLiteOpenHelper
 */
public class RegistroActivity extends AppCompatActivity {

    /** TextView que muestra la fecha del entrenamiento seleccionado. */
    TextView tvFecha;

    /** Campos de entrada de datos del formulario. */
    TextInputEditText etEjercicio, etNumSerie, etReps, etPeso, etRir;

    /** Botón para guardar una nueva serie. */
    View btnGuardar;

    /** Botón para eliminar las series seleccionadas (visible solo cuando hay selección). */
    MaterialButton btnEliminarSeleccionados;

    /** Contenedor donde se añaden dinámicamente las tarjetas de cada serie. */
    LinearLayout llContenedorTabla;

    /** Fecha del entrenamiento recibida desde {@link MainActivity}. */
    String fechaRecibida;

    /** Helper para las operaciones de base de datos. */
    AdminSQLiteOpenHelper dbHelper;

    /**
     * Conjunto de IDs de las series actualmente seleccionadas para borrado múltiple.
     * Se utiliza un {@link HashSet} para garantizar unicidad y búsqueda en O(1).
     */
    Set<Integer> selectedIds = new HashSet<>();

    /**
     * Inicializa la actividad: enlaza las vistas, configura la base de datos,
     * recupera la fecha del Intent y establece los listeners de los botones.
     *
     * @param savedInstanceState Estado guardado de la instancia anterior, o null si es nueva.
     */
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

    /**
     * Valida y guarda una nueva serie de entrenamiento en la base de datos.
     * <p>
     * Campos obligatorios: ejercicio, número de serie y repeticiones.
     * Los campos peso y RIR son opcionales (se asigna 0.0 si están vacíos).
     * </p>
     * <p>
     * Tras guardar exitosamente, se recarga la tabla, se auto-incrementa
     * el número de serie y se limpian los campos de repeticiones y RIR
     * para facilitar el registro de la siguiente serie.
     * </p>
     */
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

    /**
     * Carga y muestra todas las series registradas para la fecha actual.
     * <p>
     * Elimina todas las vistas existentes del contenedor, reinicia la selección
     * y consulta la base de datos ordenando por ID ascendente (orden cronológico
     * de inserción). Por cada registro encontrado, crea una tarjeta visual
     * mediante {@link #crearTarjetaSerie(int, String, String, String, double, double)}.
     * </p>
     */
    private void cargarTabla() {
        llContenedorTabla.removeAllViews();
        selectedIds.clear();
        actualizarBotonEliminar();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor fila = db.rawQuery(
                "SELECT id, ejercicio, num_serie, repeticiones, peso, rir " +
                "FROM entrenamientos WHERE fecha = ? ORDER BY id ASC",
                new String[]{fechaRecibida}
        );

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

    /**
     * Crea programáticamente una tarjeta visual ({@link CardView}) para una serie.
     * <p>
     * Cada tarjeta contiene:
     * <ul>
     *     <li>Un {@link CheckBox} para selección múltiple (tintado en color primario).</li>
     *     <li>Nombre del ejercicio en negrita y detalles (peso × reps @ RIR).</li>
     *     <li>Badge del número de serie en color primario.</li>
     * </ul>
     * Además, se registra un {@code OnLongClickListener} para la eliminación
     * individual de la serie.
     * </p>
     *
     * @param id   Identificador único de la serie en la base de datos.
     * @param nom  Nombre del ejercicio.
     * @param num  Número de la serie (como String).
     * @param rep  Número de repeticiones (como String).
     * @param peso Peso utilizado en kilogramos.
     * @param rir  Repeticiones en reserva.
     */
    private void crearTarjetaSerie(int id, String nom, String num, String rep, double peso, double rir) {
        // --- CARDVIEW CONTAINER ---
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.parseColor("#1A1A1A"));
        card.setRadius(40f);
        card.setCardElevation(0f);

        // Layout horizontal del contenido
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding(24, 28, 32, 28);
        content.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(content);

        // Checkbox para selección múltiple
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

        // Columna de información (nombre del ejercicio + detalles)
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

        // Badge del número de serie
        TextView tvSerie = new TextView(this);
        tvSerie.setText("#" + num);
        tvSerie.setTextColor(Color.parseColor("#D0BCFF"));
        tvSerie.setTypeface(null, Typeface.BOLD);
        tvSerie.setTextSize(18f);
        tvSerie.setPadding(16, 0, 0, 0);

        // Ensamblar: checkbox + info + badge
        content.addView(checkBox);
        content.addView(infoLayout);
        content.addView(tvSerie);

        // Pulsación larga para eliminar serie individual
        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mostrarDialogoEliminar(id, nom, num);
                return true;
            }
        });

        llContenedorTabla.addView(card);
    }

    /**
     * Actualiza la visibilidad y el texto del botón de eliminación múltiple.
     * <p>
     * Si no hay series seleccionadas, oculta el botón ({@code View.GONE}).
     * En caso contrario, lo muestra con el texto indicando la cantidad
     * de series seleccionadas.
     * </p>
     */
    private void actualizarBotonEliminar() {
        if (selectedIds.isEmpty()) {
            btnEliminarSeleccionados.setVisibility(View.GONE);
        } else {
            btnEliminarSeleccionados.setVisibility(View.VISIBLE);
            btnEliminarSeleccionados.setText("ELIMINAR " + selectedIds.size() + " SELECCIONADOS");
        }
    }

    /**
     * Muestra un diálogo de confirmación para eliminar todas las series seleccionadas.
     * <p>
     * Al confirmar, delega la eliminación en
     * {@link AdminSQLiteOpenHelper#eliminarVariasSeries(List)} y recarga la tabla.
     * El mensaje del diálogo se adapta gramaticalmente al número de series
     * seleccionadas (singular/plural).
     * </p>
     */
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

    /**
     * Muestra un diálogo de confirmación para eliminar una serie individual.
     * <p>
     * Se invoca mediante pulsación larga sobre la tarjeta de la serie.
     * Al confirmar, delega la eliminación en
     * {@link AdminSQLiteOpenHelper#eliminarSerie(int)} y recarga la tabla.
     * </p>
     *
     * @param idEliminar      Identificador único de la serie a eliminar.
     * @param nombreEjercicio Nombre del ejercicio (se muestra en el diálogo).
     * @param numSerie        Número de la serie (se muestra en el diálogo).
     */
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