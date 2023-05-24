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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;

public class AgregarCategoria extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 3;
    private static final int PERMISSION_REQUEST_STORAGE = 4;

    private EditText editTextNombreCategoria;
    private ImageView imageView;
    private ImageButton btnAgregarCategoria, btnCancelAR, btnSelect;
    private SharedPreferences sharedPreferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_agregar_categoria, container, false);
        sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);

        editTextNombreCategoria = rootView.findViewById(R.id.editTextNombreCategoria);
        imageView = rootView.findViewById(R.id.imageView);
        btnAgregarCategoria = rootView.findViewById(R.id.btnAgregarCategoria);
        btnCancelAR = rootView.findViewById(R.id.btnCancelar);
        btnSelect = rootView.findViewById(R.id.selectButton);
        btnAgregarCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarCategoria();
            }
        });

        btnCancelAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToHome();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSelectionOptions();
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSelectionOptions();
            }
        });


        return rootView;
    }

    private void agregarCategoria() {
        String nombreCategoria = editTextNombreCategoria.getText().toString().trim();

        if (nombreCategoria.isEmpty()) {
            Toast.makeText(getActivity(), "Ingrese un nombre de categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        byte[] imagenCategoria = obtenerBytesDeImagen();
        int userId = getUserIdFromSharedPreferences();

        ContentValues values = new ContentValues();
        values.put("category_name", nombreCategoria);
        values.put("category_image", imagenCategoria);
        values.put("user_id", userId);


        long resultado = database.insert("Categoria", null, values);

        if (resultado != -1) {
            Toast.makeText(getActivity(), "Categoría agregada correctamente", Toast.LENGTH_SHORT).show();
            redirectToHome();
        } else {
            Toast.makeText(getActivity(), "Error al agregar la categoría", Toast.LENGTH_SHORT).show();
        }

        database.close();
    }

    private void redirectToHome() {
        Intent intent = new Intent(getActivity(), Inicio.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void showImageSelectionOptions() {
        String[] options = {"Tomar fotografía", "Seleccionar de la galería"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Seleccionar imagen");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    checkCameraPermission();
                } else if (which == 1) {
                    checkStoragePermission();
                }
            }
        });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        } else {
            dispatchPickImageIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchPickImageIntent() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/*");
        startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            showSelectedImage(imageBitmap);
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                showSelectedImage(selectedImageUri);
            }
        }
    }

    private void showSelectedImage(Bitmap imageBitmap) {
        imageView.setImageBitmap(imageBitmap);
    }

    private void showSelectedImage(Uri selectedImageUri) {
        Glide.with(this)
                .load(selectedImageUri)
                .into(imageView);
    }

    private byte[] obtenerBytesDeImagen() {
        Bitmap bitmap = null;
        try {
            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }

        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getActivity(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchPickImageIntent();
            } else {
                Toast.makeText(getActivity(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private int getUserIdFromSharedPreferences() {
        return sharedPreferences.getInt("user_id", -1);
    }
}
