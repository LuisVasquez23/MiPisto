package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private TextInputEditText editTextStartDate;
    private RadioGroup radioGroup;
    TextView textViewInicioMes;
    TextView textViewPlazoDias;
    private Calendar calendar;
    private int userId;
    private DatabaseHelper databaseHelper;

    public SettingsFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el diseño del fragmento
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Obtener referencia al TextInputEditText de la fecha de inicio y al ImageButton del calendario
        editTextStartDate = rootView.findViewById(R.id.editTextStartDate);
         textViewInicioMes = rootView.findViewById(R.id.textViewInicioMes);
         textViewPlazoDias = rootView.findViewById(R.id.textViewPlazoDias);
        ImageButton imageButtonCalendar = rootView.findViewById(R.id.imageButtonCalendar);

        // Obtener referencia al RadioGroup
        radioGroup = rootView.findViewById(R.id.radioGroup);

        // Obtener el ID del usuario desde SharedPreferences
        userId = getUserId();

        // Inicializar instancia de DatabaseHelper
        databaseHelper = new DatabaseHelper(getActivity());

        // Crear un objeto Calendar para almacenar la fecha seleccionada
        calendar = Calendar.getInstance();

        // Establecer el OnClickListener para el ImageButton del calendario
        imageButtonCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Configurar el click listener del botón "Guardar preferencias"
        Button buttonGuardarPreferencias = rootView.findViewById(R.id.buttonGuardarPreferencias);
        buttonGuardarPreferencias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
            }
        });
        // Obtener los valores de inicio de mes y plazo de días desde la base de datos
        getPreferencesFromDatabase();
        return rootView;
    }

    private void showDatePickerDialog() {
        // Crear el DatePickerDialog con la fecha actual como fecha inicial
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Mostrar el DatePickerDialog
        datePickerDialog.show();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // Actualizar la fecha seleccionada en el objeto Calendar
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Formatear la fecha seleccionada en el formato deseado
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editTextStartDate.setText(dateFormat.format(calendar.getTime()));
        }
    };

    private int getUserId() {
        // Obtener el ID del usuario guardado en SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1); // Valor predeterminado si no se encuentra el ID
    }

    private void savePreferences() {
        // Obtener los valores seleccionados por el usuario
        int plazoRegistros = -1;
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton15) {
            plazoRegistros = 15;
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton31) {
            plazoRegistros = 31;
        }

        String diaInicioMes = editTextStartDate.getText().toString();

        // Validar si se seleccionaron valores válidos
        if (plazoRegistros != -1 && !diaInicioMes.isEmpty()) {
            // Obtener la fecha actual
            Calendar currentDate = Calendar.getInstance();
            currentDate.set(Calendar.HOUR_OF_DAY, 0);
            currentDate.set(Calendar.MINUTE, 0);
            currentDate.set(Calendar.SECOND, 0);
            currentDate.set(Calendar.MILLISECOND, 0);

            // Obtener la fecha seleccionada
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date selectedDate;
            try {
                selectedDate = dateFormat.parse(diaInicioMes);
            } catch (ParseException e) {
                selectedDate = null;
            }

            // Comparar las fechas
            if (selectedDate != null && !selectedDate.before(currentDate.getTime())) {
                // Guardar los valores en la base de datos
                savePreferencesInDatabase(plazoRegistros, diaInicioMes);
                Toast.makeText(getActivity(), "Preferencias guardadas correctamente", Toast.LENGTH_SHORT).show();

                // Actualizar los TextView con los valores seleccionados
                textViewInicioMes.setText("Día de inicio: " + diaInicioMes);
                textViewPlazoDias.setText("Plazo de días: " + plazoRegistros);
                textViewInicioMes.setVisibility(View.VISIBLE);
                textViewPlazoDias.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getActivity(), "La fecha seleccionada debe ser igual o posterior a la fecha actual", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Por favor, selecciona valores válidos", Toast.LENGTH_SHORT).show();
        }
    }



    private void savePreferencesInDatabase(int plazoRegistros, String diaInicioMes) {
        try {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            // Crear un objeto ContentValues para guardar los valores a insertar/actualizar
            ContentValues values = new ContentValues();
            values.put("plazo_registros", plazoRegistros);
            values.put("dia_inicio_mes", diaInicioMes);

            // Actualizar la fila correspondiente al usuario en la tabla "User"
            String whereClause = "user_id = ?";
            String[] whereArgs = {String.valueOf(userId)};
            db.update("User", values, whereClause, whereArgs);

            db.close();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error al guardar las preferencias: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void getPreferencesFromDatabase() {
        // Obtener los valores de inicio de mes y plazo de días desde la base de datos
        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            String[] columns = {"plazo_registros", "dia_inicio_mes"};
            String selection = "user_id = ?";
            String[] selectionArgs = {String.valueOf(userId)};
            Cursor cursor = db.query("User", columns, selection, selectionArgs, null, null, null);

            if (cursor.moveToFirst()) {
                @SuppressLint("Range") int plazoRegistros = cursor.getInt(cursor.getColumnIndex("plazo_registros"));
                @SuppressLint("Range") String diaInicioMes = cursor.getString(cursor.getColumnIndex("dia_inicio_mes"));

                // Mostrar los valores en los TextView correspondientes
                textViewInicioMes.setText("Día de inicio: " + diaInicioMes);
                textViewPlazoDias.setText("Plazo de días: " + plazoRegistros);
                textViewInicioMes.setVisibility(View.VISIBLE);
                textViewPlazoDias.setVisibility(View.VISIBLE);
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error al obtener las preferencias: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
