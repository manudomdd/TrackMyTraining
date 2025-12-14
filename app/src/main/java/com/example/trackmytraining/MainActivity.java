package com.example.trackmytraining;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    CalendarView calendarView;

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