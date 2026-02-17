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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * <li>Registro de series con auto-incremento del número de serie.</li>
 * <li>Visualización del historial del día AGRUPADO POR EJERCICIO.</li>
 * <li>Eliminación individual mediante pulsación larga.</li>
 * <li>Eliminación múltiple mediante checkboxes y botón de borrado.</li>
 * </ul>
 *
 * <p>
 * Recibe la fecha del entrenamiento desde {@link MainActivity} a través
 * del Intent con la clave {@code "FECHA"}.
 * </p>
 *
 * @author Manuel Dominguez
 * @version 3.0 (Agrupación por tipo de ejercicio)
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

    /**
     * Botón para eliminar las series seleccionadas (visible solo cuando hay
     * selección).
     */
    MaterialButton btnEliminarSeleccionados;

    /** Contenedor donde se añaden dinámicamente las tarjetas de cada serie. */
    LinearLayout llContenedorTabla;

    /** Fecha del entrenamiento recibida desde {@link MainActivity}. */
    String fechaRecibida;

    /** Helper para las operaciones de base de datos. */
    AdminSQLiteOpenHelper dbHelper;

    /**
     * Conjunto de IDs de las series actualmente seleccionadas para borrado
     * múltiple.
     * Se utiliza un {@link HashSet} para garantizar unicidad y búsqueda en O(1).
     */
    Set<Integer> selectedIds = new HashSet<>();

    /**
     * Clase interna para representar los datos de una serie.
     * Facilita el manejo y agrupación de los datos.
     */
    private static class SerieRegistro {
        int id;
        String ejercicio;
        String numSerie;
        String reps;
        double peso;
        double rir;

        public SerieRegistro(int id, String ejercicio, String numSerie, String reps, double peso, double rir) {
            this.id = id;
            this.ejercicio = ejercicio;
            this.numSerie = numSerie;
            this.reps = reps;
            this.peso = peso;
            this.rir = rir;
        }
    }

    /**
     * Inicializa la actividad: enlaza las vistas, configura la base de datos,
     * recupera la fecha del Intent y establece los listeners de los botones.
     *
     * @param savedInstanceState Estado guardado de la instancia anterior, o null si
     *                           es nueva.
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

        if (!ejercicio.isEmpty() && !serieStr.isEmpty() && !repsStr.isEmpty()) {
            int serie = Integer.parseInt(serieStr);
            int reps = Integer.parseInt(repsStr);
            double peso = pesoStr.isEmpty() ? 0.0 : Double.parseDouble(pesoStr);
            // Si el campo RIR está vacío, guardamos -1.0 para diferenciarlo de un 0
            // explícito (Fallo)
            double rir = rirStr.isEmpty() ? -1.0 : Double.parseDouble(rirStr);

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
     * Consulta la base de datos, agrupa las series por nombre de ejercicio
     * y genera las tarjetas correspondientes.
     * <br>
     * Estructura visual:
     * CardView (Por Ejercicio)
     * Titulo Ejercicio
     * Lista de Series (Filas)
     * </p>
     */
    private void cargarTabla() {
        llContenedorTabla.removeAllViews();
        selectedIds.clear();
        actualizarBotonEliminar();

        // 1. Obtener todos los registros
        List<SerieRegistro> listaSeries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor fila = db.rawQuery(
                "SELECT id, ejercicio, num_serie, repeticiones, peso, rir " +
                        "FROM entrenamientos WHERE fecha = ? ORDER BY id ASC",
                new String[] { fechaRecibida });

        while (fila.moveToNext()) {
            listaSeries.add(new SerieRegistro(
                    fila.getInt(0),
                    fila.getString(1),
                    fila.getString(2),
                    fila.getString(3),
                    fila.getDouble(4),
                    fila.getDouble(5)));
        }
        fila.close();
        db.close();

        // 2. Agrupar por nombre de ejercicio (LinkedHashMap para mantener orden de
        // inserción/aparición)
        Map<String, List<SerieRegistro>> grupos = new LinkedHashMap<>();
        for (SerieRegistro serie : listaSeries) {
            String nombreNormalizado = serie.ejercicio.trim(); // Podríamos normalizar más si fuera necesario
            if (!grupos.containsKey(nombreNormalizado)) {
                grupos.put(nombreNormalizado, new ArrayList<>());
            }
            grupos.get(nombreNormalizado).add(serie);
        }

        // 3. Generar UI por cada grupo
        for (Map.Entry<String, List<SerieRegistro>> entrada : grupos.entrySet()) {
            crearTarjetaGrupoEjercicio(entrada.getKey(), entrada.getValue());
        }
    }

    /**
     * Crea una tarjeta visual (CardView) que agrupa todas las series de un mismo
     * ejercicio.
     *
     * @param nombreEjercicio Nombre del ejercicio.
     * @param series          Lista de objetos {@link SerieRegistro} pertenecientes
     *                        a este ejercicio.
     */
    private void crearTarjetaGrupoEjercicio(String nombreEjercicio, List<SerieRegistro> series) {
        // --- CARDVIEW CONTAINER DEL GRUPO ---
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.parseColor("#1E1E1E")); // Un poco mas claro que el fondo
        card.setRadius(24f);
        card.setCardElevation(4f);

        // Layout vertical principal dentro de la tarjeta
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(0, 0, 0, 16); // Padding abajo
        card.addView(mainLayout);

        // --- CABECERA (Nombre del Ejercicio) ---
        TextView tvTitulo = new TextView(this);
        tvTitulo.setText(nombreEjercicio);
        tvTitulo.setTextColor(Color.parseColor("#D0BCFF")); // Color primario
        tvTitulo.setTextSize(18f);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setPadding(24, 24, 24, 16);
        tvTitulo.setBackgroundColor(Color.parseColor("#252525")); // Fondo cabecera distinguido
        mainLayout.addView(tvTitulo);

        // --- LISTA DE SERIES (Iterar) ---
        for (SerieRegistro serie : series) {
            View rowView = crearFilaSerie(serie);
            mainLayout.addView(rowView);
        }

        llContenedorTabla.addView(card);
    }

    /**
     * Crea una vista de fila para una serie individual dentro del grupo.
     * <p>
     * Contiene CheckBox para selección, detalles de la serie y permite LongClick
     * para borrar.
     * </p>
     *
     * @param serie Objeto con los datos de la serie.
     * @return Vista (LinearLayout) configurada.
     */
    private View crearFilaSerie(final SerieRegistro serie) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(24, 12, 24, 12);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D0BCFF")));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedIds.add(serie.id);
                } else {
                    selectedIds.remove(serie.id);
                }
                actualizarBotonEliminar();
            }
        });
        row.addView(checkBox);

        // Info de la serie: "#1: 100kg x 10 reps @ RIR 2"
        TextView tvInfo = new TextView(this);
        StringBuilder sb = new StringBuilder();
        sb.append("Serie ").append(serie.numSerie).append(":  ");
        sb.append(serie.peso).append("kg  x  ").append(serie.reps).append(" reps");

        // MOSTRAR RIR SI ES MAYOR O IGUAL A 0 (Es decir, si el usuario lo introdujo
        // explícitamente o es un dato antiguo)
        if (serie.rir >= 0) {
            // Formatear para quitar el decimal si es entero (ej: "0.0" -> "0")
            String rirTexto = (serie.rir == (long) serie.rir)
                    ? String.format("%d", (long) serie.rir)
                    : String.valueOf(serie.rir);
            sb.append("  @ RIR ").append(rirTexto);
        }

        tvInfo.setText(sb.toString());
        tvInfo.setTextColor(Color.parseColor("#E6E1E5"));
        tvInfo.setTextSize(15f);
        tvInfo.setPadding(16, 0, 0, 0);

        // Hacer que el texto ocupe el espacio restante para que el long click sea facil
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvInfo.setLayoutParams(textParams);

        row.addView(tvInfo);

        // Funcionalidad Long Click para borrar individualmente
        row.setLongClickable(true);
        row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mostrarDialogoEliminar(serie.id, serie.ejercicio, serie.numSerie);
                return true;
            }
        });

        return row;
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
     * Muestra un diálogo de confirmación para eliminar todas las series
     * seleccionadas.
     * <p>
     * Al confirmar, delega la eliminación en
     * {@link AdminSQLiteOpenHelper#eliminarVariasSeries(List)} y recarga la tabla.
     * </p>
     */
    private void mostrarDialogoEliminarSeleccionados() {
        int count = selectedIds.size();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar");
        builder.setMessage("¿Borrar " + count + " serie" + (count > 1 ? "s" : "") + " seleccionada"
                + (count > 1 ? "s" : "") + "?");
        builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Integer> idsToDelete = new ArrayList<>(selectedIds);
                dbHelper.eliminarVariasSeries(idsToDelete);
                Toast.makeText(RegistroActivity.this,
                        count + " serie" + (count > 1 ? "s" : "") + " eliminada" + (count > 1 ? "s" : ""),
                        Toast.LENGTH_SHORT).show();
                cargarTabla();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    /**
     * Muestra un diálogo de confirmación para eliminar una serie individual.
     * <p>
     * Se invoca mediante pulsación larga sobre la fila de la serie.
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