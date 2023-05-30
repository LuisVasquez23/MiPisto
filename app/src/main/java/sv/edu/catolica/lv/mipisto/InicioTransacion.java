package sv.edu.catolica.lv.mipisto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.time.LocalDate;
import java.util.Date;

public class InicioTransacion extends Fragment {

    // DECLARACION DE VARIABLES
    private ImageButton btnAñadirTransacion;
    private LinearLayout linearLayoutCategories;
    private DatabaseHelper databaseHelper;
    private int categoryId;
    private SharedPreferences sharedPreferences;
    private String categoryName;
    private TextView textViewCategory,totalCat;


    public static InicioTransacion newInstance(int categoryId, String categoryName) {
        InicioTransacion fragment = new InicioTransacion();
        Bundle args = new Bundle();
        args.putInt("categoryId", categoryId);
        args.putString("categoryName", categoryName);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_inicio_trasancion, container, false);

        // Obtener referencia al botón de añadir categorías desde la vista raíz
        btnAñadirTransacion = rootView.findViewById(R.id.btnAñadirTransacion);


        // Obtener el ID de la categoría desde los argumentos pasados al fragmento
        Bundle args = getArguments();

        if (args != null) {
            categoryId = args.getInt("categoryId");
            categoryName = args.getString("categoryName");
        }

        textViewCategory = rootView.findViewById(R.id.tvCategoriaName);

        Log.i("InicioTransacion", "Valor text view: " + textViewCategory); // Log de información

        textViewCategory.setText("Categoria: " + categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1));

        btnAñadirTransacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Llamar al fragmento secundario a abrir y mostrarlo
                    Fragment fragmentSecundario = new AgregarTransacion();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragmentSecundario);
                    // Crear un Bundle para pasar los argumentos
                    Bundle args = new Bundle();
                    args.putInt("categoryId", categoryId);
                    args.putString("categoryName", categoryName);
                    fragmentSecundario.setArguments(args);
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

                    // Cerrar el fragmento actual
                    FragmentTransaction fragmentTransactionInicio = fragmentManager.beginTransaction();
                    fragmentTransactionInicio.remove(InicioTransacion.this);
                    fragmentTransactionInicio.commit();


                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "Error al iniciar AgregarCategoria: " + e.getMessage();
                    Log.e("InicioTransacion", "Error al iniciar AgregarCategoria: " + e.getMessage());
                }
            }
        });


        return rootView;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener referencia al contenedor de categorías
        linearLayoutCategories = view.findViewById(R.id.linearLayoutCategories);

        // Inicializar instancia de DatabaseHelper
        databaseHelper = new DatabaseHelper(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);

        // Obtener el ID de la categoría enviado como argumento
        categoryId = getArguments().getInt("categoryId");

        // Cargar las transacciones desde la base de datos
        cargarTransaccion();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void cargarTransaccion() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Obtener el ID de usuario desde SharedPreferences
        int userId = getUserIdFromSharedPreferences();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
        long diaInicioMesTime = sharedPreferences.getLong("dia_inicio_mes", 0);
        long fechaFinalTime = sharedPreferences.getLong("fecha_final", 0);

        // Variable para almacenar la suma de los amounts
        double totalAmount = 0.0;
       // Toast.makeText(getContext(), "id en categoria" + userId + " en la categoria con id: " + diaInicioMesTime + "fin" + fechaFinalTime, Toast.LENGTH_SHORT).show();

        LocalDate diaInicioMes = LocalDate.ofEpochDay(diaInicioMesTime);
        LocalDate fechaFinal = LocalDate.ofEpochDay(fechaFinalTime);

        // Realizar una consulta a la base de datos para obtener las transacciones de la categoría y usuario específicos
        // comprendidas entre el día de inicio del mes y la fecha final
        Cursor cursor = db.rawQuery("SELECT * FROM Transacciones WHERE category_id = ? AND user_id = ? " +
                "AND Data_registred >= ? AND Data_registred <= ?", new String[]{
                String.valueOf(categoryId), String.valueOf(userId), diaInicioMes.toString(), fechaFinal.toString()});



        // Obtener referencia al TextView para mostrar el mensaje cuando no hay transacciones
        TextView textViewNoTransacion = getView().findViewById(R.id.textViewNoTransacion);

        // Obtener referencia al TableLayout
        TableLayout transaccionesTable = getView().findViewById(R.id.transaccionesTable);

        // Limpiar el TableLayout
        transaccionesTable.removeAllViews();

        // Variable de bandera para controlar si se encontraron transacciones
        boolean transaccionesEncontradas = false;

        // Crear una nueva fila para los encabezados
        TableRow encabezadosRow = new TableRow(getContext());

        // Configurar los elementos de los encabezados

        /*
        * ==========================================
        *   ENCABEZADO - DESCRIPCION
        *  ===========================================
        * */
        TextView textViewHeaderDescription = new TextView(getContext());
        textViewHeaderDescription.setText("Descripción");
        textViewHeaderDescription.setGravity(Gravity.CENTER);
        textViewHeaderDescription.setTypeface(Typeface.DEFAULT_BOLD);


        /*
         * ==========================================
         *   ENCABEZADO - FECHA
         *  ===========================================
         * */
        TextView textViewHeaderDate = new TextView(getContext());
        textViewHeaderDate.setText("Fecha");
        textViewHeaderDate.setGravity(Gravity.CENTER);
        textViewHeaderDate.setTypeface(Typeface.DEFAULT_BOLD);


        /*
         * ==========================================
         *   ENCABEZADO - CANTIDAD
         *  ===========================================
         * */
        TextView textViewHeaderAmount = new TextView(getContext());
        textViewHeaderAmount.setText("Cantidad");
        textViewHeaderAmount.setGravity(Gravity.CENTER);
        textViewHeaderAmount.setTypeface(Typeface.DEFAULT_BOLD);

        // Agregar los elementos de los encabezados a la fila
        encabezadosRow.addView(textViewHeaderDescription);
        encabezadosRow.addView(textViewHeaderDate);
        encabezadosRow.addView(textViewHeaderAmount);

        // Agregar la fila de los encabezados a la tabla
        transaccionesTable.addView(encabezadosRow);

        /*
         * ==========================================
         *   CUERPO - RENDERIZAR LOS DATOS
         *  ===========================================
         * */

        // Verificar si hay transacciones disponibles
        if (cursor.moveToFirst()) {
            do {
                // Obtener el amount de la transacción desde el cursor


                // Obtener los datos de la transacción desde el cursor
                @SuppressLint("Range") int transactionId = cursor.getInt(cursor.getColumnIndex("Transaction_Id"));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("Description"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("Data_registred"));
                @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex("Amount"));
                // Sumar el amount al totalAmount
                totalAmount += amount;
                // Crear una nueva fila para la transacción
                TableRow row = new TableRow(getContext());


                // Configurar los elementos de la fila de la transacción según los datos obtenidos
                TextView textViewDescription = new TextView(getContext());
                textViewDescription.setText(description);
                textViewDescription.setGravity(Gravity.CENTER);

                TextView textViewDate = new TextView(getContext());
                textViewDate.setText(date);
                textViewDate.setGravity(Gravity.CENTER);

                TextView textViewAmount = new TextView(getContext());
                textViewAmount.setText(String.valueOf(amount));
                textViewAmount.setGravity(Gravity.CENTER);


                // Agregar los elementos a la fila
                row.addView(textViewDescription);
                row.addView(textViewDate);
                row.addView(textViewAmount);

                // Agregar la fila a la tabla
                transaccionesTable.addView(row);

                // Establecer la bandera como verdadera, ya que se encontraron transacciones
                transaccionesEncontradas = true;
            } while (cursor.moveToNext());
        }
        // Obtener referencia al TextView totalCat
        TextView textViewTotalCat = getView().findViewById(R.id.totalCat);

        // Establecer el valor del TextView con la suma total
        textViewTotalCat.setText(String.valueOf("$"+totalAmount));
        // Mostrar u ocultar el TableLayout y el mensaje según si se encontraron transacciones
        transaccionesTable.setVisibility(transaccionesEncontradas ? View.VISIBLE : View.GONE);
        textViewNoTransacion.setVisibility(transaccionesEncontradas ? View.GONE : View.VISIBLE);

        cursor.close();
        db.close();
    }


    private int getUserIdFromSharedPreferences() {
        return sharedPreferences.getInt("user_id", -1);
    }


}
