package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private SharedPreferences sharedPreferences;

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
        // Obtener instancia de SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
        // Inicializar instancia de DatabaseHelper
        databaseHelper = new DatabaseHelper(getActivity());

        // Cargar las categorías desde la base de datos
        cargarCategorias();
    }

    private void cargarCategorias() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        int userId = getUserIdFromSharedPreferences();

        if (userId != -1) {
            Toast.makeText(getContext(), "id"+userId, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getContext(), "No se encontró el ID del usuario en SharedPreferences", Toast.LENGTH_SHORT).show();
        }
        // Realizar una consulta a la base de datos para obtener las categorías
        Cursor cursor = db.rawQuery("SELECT * FROM Categoria WHERE user_id = ?", new String[]{String.valueOf(userId)});

        // Obtener referencia al TextView para mostrar el mensaje cuando no hay categorías
        TextView textViewNoCategories = getView().findViewById(R.id.textViewNoCategories);

        // Limpiar el contenedor principal
        linearLayoutCategories.removeAllViews();

        // Variable de bandera para controlar si se encontraron categorías
        boolean categoriasEncontradas = false;

        // Verificar si hay categorías disponibles
        if (cursor.moveToFirst()) {
            do {
                // Obtener los datos de la categoría desde el cursor
                @SuppressLint("Range") int categoryId = cursor.getInt(cursor.getColumnIndex("category_id"));
                @SuppressLint("Range") String categoryName = cursor.getString(cursor.getColumnIndex("category_name"));
                @SuppressLint("Range") byte[] categoryImage = cursor.getBlob(cursor.getColumnIndex("category_image"));

                // Inflar el diseño de la categoría
                View categoriaView = getLayoutInflater().inflate(R.layout.item_categoria, linearLayoutCategories, false);

                // Configurar los elementos de la vista de la categoría según los datos obtenidos
                TextView textViewCategoryName = categoriaView.findViewById(R.id.textViewCategoryName);
                textViewCategoryName.setText(categoryName);

                // Obtener referencia al ImageButton
                ImageButton imageButtonCategory = categoriaView.findViewById(R.id.imageButtonCategory);

                // Cargar la imagen desde el byte[] almacenado en la base de datos
                Bitmap originalBitmap = BitmapFactory.decodeByteArray(categoryImage, 0, categoryImage.length);

                // Redimensionar la imagen al tamaño deseado
                int desiredWidth = 500; // Tamaño deseado en píxeles
                int desiredHeight = 250; // Tamaño deseado en píxeles
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, desiredWidth, desiredHeight, false);

                // Guardar el ID de la categoría en la etiqueta del ImageButton
                imageButtonCategory.setTag(categoryId);

                // Agregar el listener al ImageButton
                imageButtonCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Obtener el ID de la categoría desde la etiqueta del ImageButton
                        int categoryId = (int) v.getTag();


                        // Obtener una instancia del FragmentManager
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                        // Crear una instancia del fragmento que deseas mostrar y pasar el ID de la categoría
                        Fragment fragment = InicioTransacion.newInstance(categoryId,categoryName);

                        // Realizar la transacción para mostrar el fragmento
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment) // Reemplaza R.id.fragmentContainer con el ID del contenedor en tu layout
                                .addToBackStack(null) // Opcional: agrega la transacción a la pila de retroceso
                                .commit();
                    }
                });

                // Establecer la imagen redimensionada en el ImageButton
                imageButtonCategory.setImageBitmap(resizedBitmap);

                // Agregar la vista de la categoría al contenedor
                linearLayoutCategories.addView(categoriaView);

                // Establecer la bandera como verdadera, ya que se encontraron categorías
                categoriasEncontradas = true;
            } while (cursor.moveToNext());
        }

        // Mostrar u ocultar el contenedor principal y el mensaje según si se encontraron categorías
        linearLayoutCategories.setVisibility(categoriasEncontradas ? View.VISIBLE : View.GONE);
        textViewNoCategories.setVisibility(categoriasEncontradas ? View.GONE : View.VISIBLE);

        cursor.close();
        db.close();
    }
    private int getUserIdFromSharedPreferences() {
        return sharedPreferences.getInt("user_id", -1);
    }
}
