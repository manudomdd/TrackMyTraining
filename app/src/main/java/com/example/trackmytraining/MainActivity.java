package com.example.trackmytraining;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;

/**
 * Actividad principal de la aplicación TrackMyTraining.
 * <p>
 * Presenta un {@link CalendarView} que permite al usuario seleccionar una fecha
 * para registrar o consultar sus entrenamientos. Al seleccionar una fecha,
 * se navega automáticamente a {@link RegistroActivity} enviando la fecha
 * seleccionada como extra del Intent.
 * </p>
 *
 * @author Manuel Dominguez
 * @version 1.0
 * @see RegistroActivity
 */
public class MainActivity extends AppCompatActivity {

    /** Vista de calendario para la selección de fecha del entrenamiento. */
    CalendarView calendarView;

    /**
     * Inicializa la actividad, configura el layout y establece el listener
     * del calendario para detectar cambios de fecha.
     * <p>
     * Cuando el usuario selecciona una fecha, se formatea como "dd/M/yyyy"
     * y se envía a {@link RegistroActivity} mediante un Intent con la clave "FECHA".
     * </p>
     *
     * @param savedInstanceState Estado guardado de la instancia anterior, o null si es nueva.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Formateamos la fecha (Nota: month empieza en 0)
                String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;

                // Abrimos la segunda actividad enviando la fecha
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                intent.putExtra("FECHA", fechaSeleccionada);
                startActivity(intent);
            }
        });
    }
}