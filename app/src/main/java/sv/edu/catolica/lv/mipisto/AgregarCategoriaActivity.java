package sv.edu.catolica.lv.mipisto;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AgregarCategoriaActivity extends AppCompatActivity {

    private EditText editTextNombreCategoria;
    private Button btnAgregarCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_categoria);

        // Obtener referencia a los elementos de la interfaz
        editTextNombreCategoria = findViewById(R.id.editTextNombreCategoria);
        btnAgregarCategoria = findViewById(R.id.btnAgregarCategoria);

        // Configurar OnClickListener para el botón de agregar categoría
        btnAgregarCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarCategoria();
            }
        });
    }

    private void agregarCategoria() {
        // Obtener el nombre de la categoría ingresado por el usuario
        String nombreCategoria = editTextNombreCategoria.getText().toString().trim();

        // Validar que se haya ingresado un nombre de categoría
        if (nombreCategoria.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre de categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una instancia de DatabaseHelper
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

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
            Toast.makeText(this, "Categoría agregada correctamente", Toast.LENGTH_SHORT).show();

            // Redireccionar al usuario a la pantalla de inicio
            redirectToHome();

        } else {
            // Mostrar mensaje de error
            Toast.makeText(this, "Error al agregar la categoría", Toast.LENGTH_SHORT).show();
        }

        // Cerrar la conexión a la base de datos
        database.close();
    }
    private void redirectToHome() {
        Intent intent = new Intent(this, Inicio.class);
        startActivity(intent);
        finish(); // Cerrar la actividad de inicio de sesión
    }

}
