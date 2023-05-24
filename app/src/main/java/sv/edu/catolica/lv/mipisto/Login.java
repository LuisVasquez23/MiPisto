package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private SharedPreferences sharedPreferences;

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
        // Inicializar instancia de SharedPreferences
        sharedPreferences = getSharedPreferences("session", Context.MODE_PRIVATE);

        // Verificar si hay una sesión iniciada
        if (isUserLoggedIn()) {
            redirectToHome(); // Redirigir a la actividad de inicio directamente
        }

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
                        // Iniciar sesión exitosa
                        int userId = getUserId(email); // Obtener el ID del usuario desde la base de datos
                        setLoggedIn(true); // Guardar el estado de inicio de sesión en SharedPreferences
                        saveUserId(userId); // Guardar el ID del usuario en SharedPreferences
                        redirectToHome(); // Redirigir a la actividad de inicio
                    } else {
                        // Credenciales incorrectas, mostrar mensaje de error
                        Toast.makeText(Login.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Ir a registro
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registro.class);
                startActivity(intent);
                finishAffinity(); // Finalizar todas las actividades en la pila, incluyendo la de inicio de sesión
            }
        });
    }

    @SuppressLint("Range")
    private int getUserId(String email) {
        int userId = -1; // Valor predeterminado si no se encuentra el usuario

        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT user_id FROM User WHERE email = ?", new String[]{email});

            if (cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndex("user_id"));
                Toast.makeText(Login.this, "ID del usuario: " + userId, Toast.LENGTH_SHORT).show();

            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            Toast.makeText(Login.this, "Error al obtener el ID del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return userId;
    }


    private boolean checkCredentials(String email, String password) {
        boolean result = false;

        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            // Realizar una consulta a la base de datos para verificar las credenciales
            Cursor cursor = db.rawQuery("SELECT * FROM User WHERE email = ? AND password = ?", new String[]{email, password});

            result = cursor.getCount() > 0;

            cursor.close();
            db.close();
        } catch (Exception e) {
            Toast.makeText(Login.this, "Error al verificar las credenciales: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return result;
    }

    private void setLoggedIn(boolean isLoggedIn) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", isLoggedIn);
            editor.apply();
        } catch (Exception e) {
            Toast.makeText(Login.this, "Error al guardar el estado de inicio de sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void saveUserId(int userId) {
        try {
            sharedPreferences.edit().putInt("user_id", userId).apply();

            Toast.makeText(Login.this, "ID del usuario guardado: " + userId, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(Login.this, "Error al guardar el ID del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void redirectToHome() {
        try {
            Intent intent = new Intent(Login.this, Inicio.class);
            startActivity(intent);
            finish(); // Cerrar la actividad de inicio de sesión
        } catch (Exception e) {
            Toast.makeText(Login.this, "Error al redirigir a la pantalla de inicio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}

