package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Locale;

public class FondosFragment extends Fragment {
    private TextView textViewFondosActuales;
    private TextView textViewGastos;

    private TextView textViewAhorros;


    private DatabaseHelper databaseHelper;
    private int userId;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fondos, container, false);

        textViewFondosActuales = rootView.findViewById(R.id.textViewFondosActuales);
        textViewGastos = rootView.findViewById(R.id.textViewGastos);
        textViewAhorros = rootView.findViewById(R.id.textViewAhorros);


        databaseHelper = new DatabaseHelper(getActivity());

        // Obtener el ID del usuario desde las preferencias compartidas
        userId = getUserIdFromSharedPreferences();

        // Obtener los fondos actuales del usuario y mostrarlos en el TextView
        double fondosActuales = getFondosActuales(userId);
        textViewFondosActuales.setText("Fondos Actuales: $" + fondosActuales);

        // Obtener los gastos del usuario y mostrarlos en el TextView
        double  gastos = getGastosMesAnterior();
        textViewGastos.setText(String.format(Locale.getDefault(), "Gastos: $%.2f", gastos));

        // Calcular y mostrar los ahorros del usuario
        double ahorros = fondosActuales - gastos;
        textViewAhorros.setText(String.format(Locale.getDefault(), "Ahorros: $%.2f", ahorros));

        // Cambiar el color del texto de los ahorros según si son negativos o positivos
        if (ahorros < 0) {
            textViewAhorros.setTextColor(Color.RED);
        } else {
            textViewAhorros.setTextColor(Color.GREEN);
        }
        // Resto del código del fragmento...

        Button btnAgregarFondos = rootView.findViewById(R.id.btnAgregarFondos);
        btnAgregarFondos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar al fragmento AgregarFondosFragment
                Fragment agregarFondosFragment = new AgregarFondosFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, agregarFondosFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return rootView;
    }

    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1); // Valor predeterminado si no se encuentra el ID de usuario
    }

    @SuppressLint("Range")
    private double getFondosActuales(int userId) {
        double fondosActuales = 0.0;

        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT presupuesto_mensual FROM User WHERE user_id = ?", new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                fondosActuales = cursor.getDouble(cursor.getColumnIndex("presupuesto_mensual"));
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error al obtener los fondos actuales: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return fondosActuales;
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private double getGastosMesAnterior() {
        double gastosMesAnterior = 0.0;

        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            // Obtener el ID de usuario desde SharedPreferences
            int userId = getUserIdFromSharedPreferences();

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
            long diaInicioMesTime = sharedPreferences.getLong("dia_inicio_mes", 0);
            long fechaFinalTime = sharedPreferences.getLong("fecha_final", 0);

            LocalDate diaInicioMes = LocalDate.ofEpochDay(diaInicioMesTime);
            LocalDate fechaFinal = LocalDate.ofEpochDay(fechaFinalTime);

            // Convertir las fechas a formato String
            String fechaInicioMesAnteriorStr = diaInicioMes.minusMonths(1).toString();
            String fechaFinMesAnteriorStr = fechaFinal.minusMonths(1).toString();

            // Consultar la suma de los gastos del mes anterior para el usuario especificado
            Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM Transacciones WHERE user_id = ? " +
                    "AND Data_registred >= ? AND Data_registred <= ?", new String[]{
                    String.valueOf(userId), fechaInicioMesAnteriorStr, fechaFinMesAnteriorStr});

            if (cursor.moveToFirst()) {
                gastosMesAnterior = cursor.getDouble(0);
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error al obtener los gastos del mes anterior: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return gastosMesAnterior;
    }




    // Resto del código del fragmento...
}

