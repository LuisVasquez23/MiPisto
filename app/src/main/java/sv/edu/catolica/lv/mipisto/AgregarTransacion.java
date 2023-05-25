package sv.edu.catolica.lv.mipisto;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AgregarTransacion extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 3;
    private static final int PERMISSION_REQUEST_STORAGE = 4;

    private EditText editTextDescription;
    private EditText editTextAmount;
    private EditText editTextTransactionType;
    private EditText editTextCategoryId;
    private EditText editTextUserId;
    private ImageButton btnAgregarTransaccion;
    private ImageButton btnCancelar;
    private SharedPreferences sharedPreferences;
    private int categoryId; // Variable para almacenar categoryId

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_agregar_transacion, container, false);

        editTextDescription = rootView.findViewById(R.id.editTextDescription);
        editTextAmount = rootView.findViewById(R.id.editTextAmount);
        editTextTransactionType = rootView.findViewById(R.id.editTextTransactionType);
        btnAgregarTransaccion = rootView.findViewById(R.id.btnAgregarTransaccion);
        btnCancelar = rootView.findViewById(R.id.btnCancelar);
        sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);


        btnAgregarTransaccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarTransaccion();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToHome();
            }
        });


        Bundle args = getArguments();
        if (args != null) {
            categoryId = args.getInt("categoryId"); // Asignar el valor de categoryId
            // Resto del código...
        }


        return rootView;
    }

    private void agregarTransaccion() {
        // Obtener los valores ingresados por el usuario
        String description = editTextDescription.getText().toString();
        double amount = Double.parseDouble(editTextAmount.getText().toString());
        String transactionType = editTextTransactionType.getText().toString();
        int userId = getUserIdFromSharedPreferences(); // Obtener el ID de usuario desde SharedPreferences

        // Validar que se hayan ingresado todos los campos requeridos
        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(transactionType)) {
            Toast.makeText(getActivity(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí puedes realizar cualquier validación adicional necesaria
        // Obtener la fecha actual
        Calendar calendar = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            calendar = Calendar.getInstance();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            date = dateFormat.format(calendar.getTime());
        }
        // Obtener una instancia de SQLiteDatabase
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        try {
            // Iniciar una transacción
            database.beginTransaction();

            Toast.makeText(getActivity(), "El id que estamos guarado es"+categoryId, Toast.LENGTH_SHORT).show();

            // Crear un objeto ContentValues para almacenar los valores de la transacción
            ContentValues values = new ContentValues();
            values.put("description", description);
            values.put("amount", amount);
            values.put("transaction_type", transactionType);
            values.put("category_id", categoryId);
            values.put("user_id", userId);
            values.put("Data_registred", date); // Agregar la fecha a los valores


            // Insertar la transacción en la tabla correspondiente
            long result = database.insert("Transacciones", null, values);

            // Verificar si la inserción fue exitosa
            if (result != -1) {
                Toast.makeText(getActivity(), "Transacción agregada correctamente", Toast.LENGTH_SHORT).show();

                // Establecer el marcador de transacción exitosa
                database.setTransactionSuccessful();

                // Realizar alguna acción adicional, como regresar al fragmento anterior
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    fragmentManager.popBackStack();
                }
            } else {
                Toast.makeText(getActivity(), "Error al agregar la transacción", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al agregar la transacción: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Finalizar la transacción y cerrar la conexión con la base de datos
            database.endTransaction();
            database.close();
        }
    }



    private int getUserIdFromSharedPreferences() {
        return sharedPreferences.getInt("user_id", -1);
    }




    private void redirectToHome() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStack(); // Retrocede una transacción en la pila de retroceso
        }
    }










}
