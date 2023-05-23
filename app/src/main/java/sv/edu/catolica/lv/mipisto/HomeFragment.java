package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {
    private ImageButton btnAñadircategoria;

    private LinearLayout linearLayoutCategories;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Obtener referencia al botón de añadir categorías desde la vista raíz
        btnAñadircategoria = rootView.findViewById(R.id.btnAñadircategoria);

        // Configurar OnClickListener para el botón de añadir categorías
        btnAñadircategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //llamar al fragment secundario a abrir y mostrarlo
                    Fragment fragmentSecundario = new AgregarCategoria();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragmentSecundario);
                    // Opcionalmente, puedes agregar una transición animada
                    // fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    FragmentTransaction fragmentTransac = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(
                            R.anim.fade_in, // ID del recurso de animación para la entrada del Fragment
                            R.anim.fade_out, // ID del recurso de animación para la salida del Fragment
                            R.anim.fade_in, // ID del recurso de animación para la entrada del Fragment secundario
                            R.anim.fade_out // ID del recurso de animación para la salida del Fragment secundario
                    );
                    fragmentTransaction.replace(R.id.fragment_container, fragmentSecundario);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();



                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "Error al iniciar AgregarCategoria: " + e.getMessage();
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("HomeFragment", "Error al iniciar AgregarCategoria: " + e.getMessage());
                }
            }
        });

        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener referencia al contenedor de categorías
        linearLayoutCategories = view.findViewById(R.id.linearLayoutCategories);

        // Inicializar instancia de DatabaseHelper
        databaseHelper = new DatabaseHelper(getActivity());

        // Cargar las categorías desde la base de datos
        cargarCategorias();
    }

    private void cargarCategorias() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Realizar una consulta a la base de datos para obtener las categorías
        Cursor cursor = db.rawQuery("SELECT * FROM Categoria", null);

        // Verificar si hay categorías disponibles
        if (cursor.moveToFirst()) {
            do {
                // Obtener los datos de la categoría desde el cursor
                @SuppressLint("Range") int categoryId = cursor.getInt(cursor.getColumnIndex("category_id"));
                @SuppressLint("Range") String categoryName = cursor.getString(cursor.getColumnIndex("category_name"));

                // Inflar el diseño de la categoría
                View categoriaView = getLayoutInflater().inflate(R.layout.item_categoria, linearLayoutCategories, false);

                // Configurar los elementos de la vista de la categoría según los datos obtenidos
                TextView textViewCategoryName = categoriaView.findViewById(R.id.textViewCategoryName);
                textViewCategoryName.setText(categoryName);

                // Agregar la vista de la categoría al contenedor
                linearLayoutCategories.addView(categoriaView);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }
}
