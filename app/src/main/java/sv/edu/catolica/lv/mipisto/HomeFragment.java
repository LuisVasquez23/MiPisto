package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
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

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private ImageButton btnAñadircategoria;
    private SharedPreferences sharedPreferences;

    private  TextView fondoDin;
    private LinearLayout linearLayoutCategories;
    private DatabaseHelper databaseHelper;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        // Obtener instancia de SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
        sharedPreferences = getActivity().getSharedPreferences("sesion", Context.MODE_PRIVATE);

        databaseHelper = new DatabaseHelper(getActivity());
        int id = sharedPreferences.getInt("user_id", -1);
        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();

        // Obtener el día de inicio del mes y el plazo de registros desde la base de datos
        LocalDate diaInicioMes = obtenerDiaInicioMes();
        int plazoRegistros = obtenerPlazoRegistros();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Guardar la fecha de inicio del mes en SharedPreferences
        editor.putLong("dia_inicio_mes", diaInicioMes.toEpochDay());
        editor.apply();

        // Calcular la fecha final sumando el plazo de días al día de inicio del mes
        LocalDate fechaFinal = diaInicioMes.plusDays(plazoRegistros);
      //  Toast.makeText(getContext(),""+fechaFinal.toEpochDay()+" a "+diaInicioMes.toEpochDay(), Toast.LENGTH_SHORT).show();

        // Guardar la fecha final en SharedPreferences
        editor.putLong("fecha_final", fechaFinal.toEpochDay());
        editor.apply();






        //Toast.makeText(getContext(), "guardado."+diaInicioMesAsDate.getTime()+fechaFinalAsDate.getTime(), Toast.LENGTH_SHORT).show();


        // Obtener la fecha actual
        LocalDate currentDate = LocalDate.now();


        // Verificar si la fecha actual no es un día después de la fecha final
        if (!currentDate.isBefore(fechaFinal.plusDays(1))) {
            // La fecha actual es un día después de la fecha final

            Date currentDateAsDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Toast.makeText(getContext(), "id"+id+currentDateAsDate, Toast.LENGTH_SHORT).show();

            actualizarDiaInicioMes(currentDateAsDate, id);
            Toast.makeText(getContext(), "Es la fecha. Actualizar día de inicio del mes.", Toast.LENGTH_SHORT).show();

            // Convertir LocalDate a Date
            // Actualizar el día de inicio del mes con la fecha actual
        } else {
            // La fecha actual no es un día después de la fecha final
            //Toast.makeText(getContext(), "No es la fecha. Aún quedan días.", Toast.LENGTH_SHORT).show();
        }


        // Obtener referencia al botón de añadir categorías desde la vista raíz
        btnAñadircategoria = rootView.findViewById(R.id.btnAñadircategoria);

        fondoDin = rootView.findViewById(R.id.fondosDin);
        LinearLayout fondosContainer = rootView.findViewById(R.id.accederFondos);
        fondosContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes iniciar una nueva actividad o realizar una transacción de fragmento
                // para mostrar la nueva pantalla
                Fragment fragmentSecundario = new FondosFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragmentSecundario);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });


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

    private void actualizarDiaInicioMes(Date currentDate, int userId) {
        // Abrir la conexión a la base de datos
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Formatear la fecha actual en el formato "dd/MM/yyyy"
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaActualFormateada = dateFormat.format(currentDate);

        // Construir la consulta SQL para actualizar el valor de dia_inicio_mes
        String query = "UPDATE User SET dia_inicio_mes = ? WHERE user_id = ?";
        String[] args = {fechaActualFormateada, String.valueOf(userId)};

        try {
            // Ejecutar la consulta SQL
            db.execSQL(query, args);
        } finally {
            // Cerrar la conexión a la base de datos
            db.close();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private LocalDate obtenerDiaInicioMes() {
        LocalDate diaInicioMes = LocalDate.now();  // Valor por defecto en caso de error

        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT dia_inicio_mes FROM User", null);

            if (cursor.moveToFirst()) {
                String fechaInicioMesString = cursor.getString(cursor.getColumnIndex("dia_inicio_mes"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                diaInicioMes = LocalDate.parse(fechaInicioMesString, formatter);
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Error al obtener el día de inicio del mes: " + e.getMessage();
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            Log.e("HomeFragment", "Error al obtener el día de inicio del mes: " + e.getMessage());
        }

        return diaInicioMes;
    }



    @SuppressLint("Range")
    private int obtenerPlazoRegistros() {
        int plazoRegistros = 0; // Valor predeterminado

        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT plazo_registros FROM User", null);

            if (cursor.moveToFirst()) {
                plazoRegistros = cursor.getInt(cursor.getColumnIndex("plazo_registros"));
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Error al obtener el plazo de registros: " + e.getMessage();
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            Log.e("HomeFragment", "Error al obtener el plazo de registros: " + e.getMessage());
        }

        return plazoRegistros;
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

        // Obtener el ID del usuario desde las preferencias compartidas
        int userId = getUserIdFromSharedPreferences();

        // Obtener los fondos del usuario
        double fondos = obtenerFondosUsuario(userId);

        // Mostrar los fondos en el TextView correspondiente
        fondoDin.setText(String.valueOf(fondos));
    }

    private void cargarCategorias() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        int userId = getUserIdFromSharedPreferences();

        if (userId != -1) {
           // Toast.makeText(getContext(), "id"+userId, Toast.LENGTH_SHORT).show();

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
    @SuppressLint("Range")
    private double obtenerFondosUsuario(int userId) {
        double fondos = 0.0;

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT presupuesto_mensual FROM User WHERE user_id = ?", new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            fondos = cursor.getDouble(cursor.getColumnIndex("presupuesto_mensual"));
        }

        cursor.close();
        db.close();

        return fondos;
    }

}
