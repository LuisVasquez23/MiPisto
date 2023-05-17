package sv.edu.catolica.lv.mipisto;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class Login extends AppCompatActivity {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button btnRegistro;


    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar instancias de vistas
        editTextEmail = findViewById(R.id.correo_login);
        editTextPassword = findViewById(R.id.contra_login);
        buttonLogin = findViewById(R.id.btnInicioSesion);
        btnRegistro = findViewById(R.id.btnRegistro);


        // Inicializar instancia de DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Configurar el click listener del botón de inicio de sesión
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el correo electrónico y la contraseña ingresados
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Validar si los campos están vacíos
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Por favor, ingresa todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Verificar las credenciales en la base de datos
                    if (checkCredentials(email, password)) {
                        // Iniciar sesión exitosa, redirigir a la siguiente actividad
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Cerrar la actividad de inicio de sesión
                    } else {
                        // Credenciales incorrectas, mostrar mensaje de error
                        Toast.makeText(Login.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

        //ir a registro
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registro.class);
                startActivity(intent);
                finishAffinity(); // Finaliza todas las actividades en la pila, incluyendo la de inicio de sesión
            }
        });
    }

    private boolean checkCredentials(String email, String password) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Realizar una consulta a la base de datos para verificar las credenciales
        Cursor cursor = db.rawQuery("SELECT * FROM User WHERE email = ? AND password = ?",
                new String[]{email, password});

        boolean result = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return result;
    }
}
