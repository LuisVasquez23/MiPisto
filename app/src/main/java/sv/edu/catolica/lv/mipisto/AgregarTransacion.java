package sv.edu.catolica.lv.mipisto;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

    private DatabaseHelper databaseHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_agregar_transacion, container, false);

        editTextDescription = rootView.findViewById(R.id.editTextDescription);
        editTextAmount = rootView.findViewById(R.id.editTextAmount);
        editTextTransactionType = rootView.findViewById(R.id.editTextTransactionType);
        btnAgregarTransaccion = rootView.findViewById(R.id.btnAgregarTransaccion);
        btnCancelar = rootView.findViewById(R.id.btnCancelar);
        sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);

        databaseHelper = new DatabaseHelper(getActivity());

        btnAgregarTransaccion.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
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

        // Obtener los fondos actuales del usuario
        double fondosActuales = getFondosActuales(userId);

        // Obtener los gastos totales del usuario
        double gastosTotales = getGastosMesAnterior();

        // Calcular los gastos totales después de agregar la transacción
        double nuevosGastosTotales = gastosTotales + amount;

        // Verificar si los nuevos gastos totales son mayores que los fondos actuales
        if (nuevosGastosTotales > fondosActuales) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Confirmación");
            builder.setMessage("Has excedido tus fondos actuales. ¿Deseas continuar?");

            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Agregar la transacción y mostrar la notificación
                    agregarTransaccionBD(description, amount, transactionType, userId);
                    mostrarNotificacion("Atención", "¡Has excedido tus fondos actuales!");
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Acciones a realizar si el usuario no desea continuar con la transacción
                }
            });

            // Mostrar el cuadro de diálogo de confirmación
            builder.create().show();
        } else {
            // Calcular el nuevo saldo después de agregar la transacción
            double nuevoSaldo = fondosActuales - nuevosGastosTotales;

            // Verificar si el nuevo saldo está dentro del 10% de los fondos actuales
            double limiteFondos = fondosActuales * 0.1; // 10% de los fondos actuales

            if (nuevoSaldo < limiteFondos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmación");
                builder.setMessage("Estás cerca de alcanzar tus fondos actuales. ¿Deseas continuar?");

                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Agregar la transacción y mostrar la notificación
                        agregarTransaccionBD(description, amount, transactionType, userId);
                        mostrarNotificacion("Advertencia", "¡Estás cerca de alcanzar tus fondos actuales!");
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Acciones a realizar si el usuario no desea continuar con la transacción
                    }
                });

                // Mostrar el cuadro de diálogo de confirmación
                builder.create().show();
            } else {
                // Agregar la transacción
                agregarTransaccionBD(description, amount, transactionType, userId);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void agregarTransaccionBD(String description, double amount, String transactionType, int userId) {
        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());

        // Obtener una instancia de SQLiteDatabase
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        try {
            // Iniciar una transacción
            database.beginTransaction();

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

    @SuppressLint("Range")
    private double getFondosActuales(int userId) {
        double fondosActuales = 0.0;

        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT presupuesto_mensual FROM User WHERE user_id = ?", new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                fondosActuales = cursor.getDouble(cursor.getColumnIndex("presupuesto_mensual"));
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error al obtener los fondos actuales: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return fondosActuales;
    }

    private void mostrarNotificacion(String titulo, String mensaje) {
        // Crear un ID único para la notificación
        int notificationId = 1;

        // Crear un intent para abrir la actividad principal al hacer clic en la notificación
        Intent intent = new Intent(getActivity(), FondosFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Crear el canal de notificación para Android 8.0 y versiones posteriores
        String channelId = "mi_canal";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Mi Canal";
            String channelDescription = "Descripción del canal";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            // Registrar el canal en el sistema
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), channelId)
                .setSmallIcon(R.drawable.login1)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Mostrar la notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.notify(notificationId, builder.build());
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    private double getGastosMesAnterior() {
        double gastosMesAnterior = 0.0;

        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            // Obtener el ID de usuario desde SharedPreferences
            int userId = getUserIdFromSharedPreferences();

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
            long diaInicioMesTime = sharedPreferences.getLong("dia_inicio_mes", 0);
            long fechaFinalTime = sharedPreferences.getLong("fecha_final", 0);

            LocalDate diaInicioMes = LocalDate.ofEpochDay(diaInicioMesTime);
            LocalDate fechaFinal = LocalDate.ofEpochDay(fechaFinalTime);

            // Convertir las fechas a formato String
            String fechaInicioMesAnteriorStr = diaInicioMes.minusMonths(1).toString();
            String fechaFinMesAnteriorStr = fechaFinal.minusMonths(1).toString();

            // Consultar la suma de los gastos del mes anterior para el usuario especificado
            Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM Transacciones WHERE user_id = ? " +
                    "AND Data_registred >= ? AND Data_registred <= ?", new String[]{
                    String.valueOf(userId), fechaInicioMesAnteriorStr, fechaFinMesAnteriorStr});

            if (cursor.moveToFirst()) {
                gastosMesAnterior = cursor.getDouble(0);
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error al obtener los gastos del mes anterior: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return gastosMesAnterior;
    }






}
