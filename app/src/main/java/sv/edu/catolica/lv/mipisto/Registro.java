package sv.edu.catolica.lv.mipisto;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import sv.edu.catolica.lv.mipisto.R;

public class Registro extends AppCompatActivity {

    private EditText nombreEditText, apellidoEditText, telefonoEditText, correoEditText, contraEditText;
    private Button registroButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        nombreEditText = findViewById(R.id.nombre);
        apellidoEditText = findViewById(R.id.apellido);
        telefonoEditText = findViewById(R.id.telefono);
        correoEditText = findViewById(R.id.correo);
        contraEditText = findViewById(R.id.contra);
        registroButton = findViewById(R.id.btnRegistro);

        registroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores ingresados por el usuario
                String nombre = nombreEditText.getText().toString();
                String apellido = apellidoEditText.getText().toString();
                String telefono = telefonoEditText.getText().toString();
                String correo = correoEditText.getText().toString();
                String contra = contraEditText.getText().toString();

                // Validar los campos (por ejemplo, verificar que no estén vacíos)
                if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || correo.isEmpty() || contra.isEmpty()) {
                    Toast.makeText(Registro.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Realizar el registro en la base de datos (aquí debes implementar tu lógica de base de datos)
                boolean exitoRegistro = registrarUsuario(nombre, apellido, telefono, correo, contra);

                if (exitoRegistro) {
                    Toast.makeText(Registro.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    // Aquí puedes redirigir al usuario a otra actividad o realizar alguna acción adicional
                    Intent intent = new Intent(Registro.this, Login.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Registro.this, "Error en el registro. Inténtalo de nuevo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean registrarUsuario(String nombre, String apellido, String telefono, String correo, String contra) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_name", nombre);
        values.put("password", contra);
        values.put("email", correo);

        long resultado = db.insert("User", null, values);

        db.close();

        return resultado != -1;
    }

}
