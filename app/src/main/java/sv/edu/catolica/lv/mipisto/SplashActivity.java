package sv.edu.catolica.lv.mipisto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500; // Duración del Splash Screen en milisegundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Opcional: Si deseas ocultar la barra de acción durante el Splash Screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_splash);

        // Iniciar la siguiente actividad después del tiempo de retraso
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent para iniciar la siguiente actividad
                Intent intent = new Intent(SplashActivity.this, Login.class);
                startActivity(intent);

                // Finalizar la actividad del Splash Screen
                finish();
            }
        }, SPLASH_DELAY);
    }
}