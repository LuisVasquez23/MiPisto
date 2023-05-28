package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class AgregarFondosFragment extends Fragment {
    private EditText editTextFondos;
    private Button btnAgregar;
    private DatabaseHelper databaseHelper;
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fondos_agregar, container, false);

        editTextFondos = rootView.findViewById(R.id.editTextFondos);
        btnAgregar = rootView.findViewById(R.id.btnAgregarFondos);
        databaseHelper = new DatabaseHelper(getActivity());

        // Obtener el ID del usuario desde las preferencias compartidas
        userId = getUserIdFromSharedPreferences();

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el valor ingresado de fondos
                String fondosString = editTextFondos.getText().toString().trim();

                // Verificar si se ingresó un valor válido
                if (!fondosString.isEmpty()) {
                    double fondos = Double.parseDouble(fondosString);

                    // Actualizar los fondos mensuales en la base de datos
                    if (updateFondosMensuales(userId, fondos)) {
                        Toast.makeText(getActivity(), "Fondos agregados exitosamente "+userId+" fondos "+fondos, Toast.LENGTH_SHORT).show();
                        editTextFondos.setText(""); // Limpiar el campo de entrada

                        // Regresar a la ventana anterior
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();
                    } else {
                        Toast.makeText(getActivity(), "Error al agregar los fondos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Ingresa un valor válido de fondos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Resto del código del fragmento...

        return rootView;
    }

    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1); // Valor predeterminado si no se encuentra el ID de usuario
    }

    @SuppressLint("Range")
    private boolean updateFondosMensuales(int userId, double fondos) {
        try {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.execSQL("UPDATE User SET presupuesto_mensual = ? WHERE user_id = ?",
                    new String[]{String.valueOf(fondos), String.valueOf(userId)});
            db.close();
            return true;
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error al actualizar los fondos mensuales: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
    }




    // Resto del código del fragmento...
}
