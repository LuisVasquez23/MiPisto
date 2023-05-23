package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


public class AgregarCategoria extends Fragment {

    private EditText editTextNombreCategoria;

    private ImageButton btnAgregarCategoria;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_agregar_categoria, container, false);

        editTextNombreCategoria = rootView.findViewById(R.id.editTextNombreCategoria);
        btnAgregarCategoria = rootView.findViewById(R.id.btnAgregarCategoria);

        btnAgregarCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarCategoria();
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }


    private void agregarCategoria() {
        // Obtener el nombre de la categoría ingresado por el usuario
        String nombreCategoria = editTextNombreCategoria.getText().toString().trim();

        // Validar que se haya ingresado un nombre de categoría
        if (nombreCategoria.isEmpty()) {
            Toast.makeText(getActivity(), "Ingrese un nombre de categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una instancia de DatabaseHelper
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        // Obtener una referencia a la base de datos en modo escritura
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        // Crear un objeto ContentValues para almacenar los valores a insertar
        ContentValues values = new ContentValues();
        values.put("category_name", nombreCategoria);

        // Insertar los valores en la tabla de categorías
        long resultado = database.insert("Categoria", null, values);

        // Verificar si la inserción fue exitosa
        if (resultado != -1) {
            // Mostrar mensaje de éxito
            Toast.makeText(getActivity(), "Categoría agregada correctamente", Toast.LENGTH_SHORT).show();

            // Redireccionar al usuario a la pantalla de inicio
            redirectToHome();

        } else {
            // Mostrar mensaje de error
            Toast.makeText(getActivity(), "Error al agregar la categoría", Toast.LENGTH_SHORT).show();
        }

        // Cerrar la conexión a la base de datos
        database.close();
    }
    private void redirectToHome() {
        Intent intent = new Intent(getActivity(), Inicio.class);
        startActivity(intent);
        getActivity().finish(); // Cerrar la actividad de inicio de sesión
    }
}