package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sv.edu.catolica.lv.mipisto.ViewModels.HistorialItem;

public class PerfilFragment extends Fragment {

    private TableLayout historyTable;
    private TextView userNameTextView;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el diseño del fragmento
        View rootView = inflater.inflate(R.layout.fragment_perfil, container, false);
        sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(getActivity());
        int id = sharedPreferences.getInt("user_id", -1);

        historyTable = rootView.findViewById(R.id.historyTable);
        userNameTextView = rootView.findViewById(R.id.userNameTextView);

        // Obtener el nombre de usuario de la base de datos
        String userName = getUserNameFromDatabase();
        userNameTextView.setText("Nombre de usuario: "+userName);
        // Agregar los encabezados a la tabla
        TableRow headerRow = new TableRow(getContext());
        TextView dateHeader = createTableHeader("Fecha");
        TextView data1Header = createTableHeader("Fondos Iniciales");
        TextView data2Header = createTableHeader("Fondos finales ");
        headerRow.addView(dateHeader);
        headerRow.addView(data1Header);
        headerRow.addView(data2Header);
        historyTable.addView(headerRow);
        // Obtener los datos del historial de la base de datos
        List<HistorialItem> historialItems = getHistorialItemsFromDatabase();

        // Agregar las filas a la tabla
        for (HistorialItem item : historialItems) {
            TableRow row = new TableRow(getContext());

            // Crear celdas y establecer los valores correspondientes
            TextView dateTextView = createTableCell(item.getDate());
            TextView data1TextView = createTableCell(item.getData1());
            TextView data2TextView = createTableCell(item.getData2());

            // Agregar las celdas a la fila
            row.addView(dateTextView);
            row.addView(data1TextView);
            row.addView(data2TextView);

            // Agregar la fila a la tabla
            historyTable.addView(row);
        }

        return rootView;
    }

    // Método auxiliar para crear una celda de tabla
    private TextView createTableCell(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        return textView;
    }
    // Método auxiliar para crear un encabezado de tabla
    private TextView createTableHeader(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }


    // Método auxiliar para obtener los datos del historial de la base de datos
// Método auxiliar para obtener los datos del historial de la base de datos
    // Método auxiliar para obtener el nombre de usuario de la base de datos
    @SuppressLint("Range")
    private String getUserNameFromDatabase() {
        int id = sharedPreferences.getInt("user_id", -1);

        // Obtener una instancia de lectura de la base de datos
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Consulta para obtener el nombre de usuario
        String query = "SELECT user_name FROM User WHERE user_id = ?";
        String[] selectionArgs = {String.valueOf(id)};

        // Ejecutar la consulta
        Cursor cursor = db.rawQuery(query, selectionArgs);

        // Obtener el nombre de usuario del cursor
        String userName = "";
        if (cursor.moveToFirst()) {
            userName = cursor.getString(cursor.getColumnIndex("user_name"));
        }

        // Cerrar el cursor y la conexión a la base de datos
        cursor.close();
        db.close();

        return userName;
    }

    // Método auxiliar para obtener los datos del historial de la base de datos
    private List<HistorialItem> getHistorialItemsFromDatabase() {
        List<HistorialItem> historialItems = new ArrayList<>();
        int id = sharedPreferences.getInt("user_id", -1);

        // Obtener una instancia de lectura de la base de datos
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Consulta para obtener los registros de historial
        String query = "SELECT fecha_final, fondos_iniciales, fondos_finales FROM Historial WHERE user_id = ?";
        String[] selectionArgs = {String.valueOf(id)};

        // Ejecutar la consulta
        Cursor cursor = db.rawQuery(query, selectionArgs);

        // Recorrer el cursor y agregar los registros a la lista de historialItems
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String fechaFinal = cursor.getString(cursor.getColumnIndex("fecha_final"));
                @SuppressLint("Range") String fondosIniciales = cursor.getString(cursor.getColumnIndex("fondos_iniciales"));
                @SuppressLint("Range") String fondosFinales = cursor.getString(cursor.getColumnIndex("fondos_finales"));

                HistorialItem historialItem = new HistorialItem(fechaFinal, fondosIniciales, fondosFinales);
                historialItems.add(historialItem);
            } while (cursor.moveToNext());
        }

        // Cerrar el cursor y la conexión a la base de datos
        cursor.close();
        db.close();

        return historialItems;
    }


}
