package sv.edu.catolica.lv.mipisto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

public class Inicio extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        sharedPreferences = getSharedPreferences("session", MODE_PRIVATE);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_nav,R.string.close_nav);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_home){
            selectedFragment = new HomeFragment();
        } else if (item.getItemId() == R.id.nav_perfil) {
            selectedFragment = new PerfilFragment();
        } else if (item.getItemId() == R.id.nav_fondos) {
            selectedFragment = new FondosFragment();
        } else if (item.getItemId() == R.id.nav_config) {
            selectedFragment = new SettingsFragment();
        } else if (item.getItemId() == R.id.nav_cesesion) {
            logout();
        }

        if (selectedFragment != null) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (currentFragment != null && currentFragment.getClass().equals(selectedFragment.getClass())) {
                // El fragmento seleccionado es el mismo que el fragmento actualmente mostrado
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    private void logout() {
        // Realizar aquí las tareas de cierre de sesión, como borrar datos de sesión, etc.
        setLoggedIn(false); // Establecer el estado de inicio de sesión como falso o cerrado
        clearUserId(); // Borrar el ID del usuario guardado en SharedPreferences

        // Redirigir a la pantalla de inicio de sesión (Login)
        Intent intent = new Intent(Inicio.this, Login.class);
        startActivity(intent);
        finish(); // Cerrar la actividad actual (Inicio)
    }

    private void setLoggedIn(boolean isLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void clearUserId() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.apply();
    }
}
