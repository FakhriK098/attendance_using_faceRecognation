package com.example.absensi.ui.pegawai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.absensi.FaceRecognetion;
import com.example.absensi.MainActivity;
import com.example.absensi.R;
import com.example.absensi.core.model.DataKaryawan;
import com.example.absensi.databinding.ActivityAddPegawaiBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class AddPegawaiActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddPegawaiActivity";


    private ActivityAddPegawaiBinding binding;
    private final Calendar mCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private FirebaseAuth firebaseAuth;
    private Uri imageUri = null;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firebaseFirestore;

    private String username;
    private String resultFace = "";
    private BaseLoaderCallback baseLoaderCallback;
    private FaceRecognetion faceRecognetion;

    private com.example.absensi.core.Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPegawaiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.tittle_pegawai);

        OpenCVLoader.initDebug();
        utils = new com.example.absensi.core.Utils();
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status){
                super.onManagerConnected(status);
            }
        };

        try {
            faceRecognetion = new FaceRecognetion(getAssets(),
                    AddPegawaiActivity.this,
                    "model_efficientNetB0.tflite",
                    96);
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
        }

        setSupportActionBar(binding.toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.infoData.setVisibility(View.INVISIBLE);
        progressDialog = new ProgressDialog(this);

        dateSetListener = (view, year, month, dayOfMonth) -> {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        };

        binding.etTanggalLahir.setOnClickListener(this);
        binding.btnPilihFoto.setOnClickListener(this);
        binding.btnSimpan.setOnClickListener(this);
        binding.btnShowPass.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.et_tanggal_lahir:
                new DatePickerDialog(this,dateSetListener,
                        mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
                updateLabel();
                break;
            case R.id.btn_pilih_foto:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,1);
                break;
            case R.id.btn_simpan:
                progressDialog.setMessage("Registered, Please Wait..");
                progressDialog.show();

                String email = binding.etEmail.getText().toString();
                String password = binding.etPassword.getText().toString();

                writeNewUser(email,password);
                break;
            case R.id.btn_show_pass:
                if (binding.etPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    binding.btnShowPass.setImageResource(R.drawable.icons_hide_24);
                    binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    binding.btnShowPass.setImageResource(R.drawable.icons_show_24);
                    binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        binding.infoData.setVisibility(View.VISIBLE);
        if (requestCode == 1 && resultCode== RESULT_OK && data != null && data.getData()!=null){
            binding.infoData.setImageResource(R.drawable.icons_done);
            imageUri = data.getData();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Mat obj = new Mat();
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(), imageUri);

                obj = new Mat(bmp.getWidth(),bmp.getHeight(), CvType.CV_8UC4);
                Utils.bitmapToMat(bmp, obj);
            } catch (IOException e) {
                e.printStackTrace();
            }

            resultFace = faceRecognetion.recognizePicture(obj);

            if (resultFace.isEmpty()){
                binding.infoData.setVisibility(View.GONE);
                binding.tvFailed.setVisibility(View.VISIBLE);
            }else {
                binding.infoData.setVisibility(View.VISIBLE);
                binding.tvFailed.setVisibility(View.GONE);
            }

        }else {
            binding.infoData.setImageResource(R.drawable.icons_error);
        }
    }

    private void uploadImgae(){
        username = utils.usernameFromEmail(binding.etEmail.getText().toString());
        if (imageUri != null){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(username).child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            storageReference.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String url = uri.toString();
                        submitPost(url);
                    });
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(null,"Register failed",Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    private void submitPost(String uri) {

        String nama = binding.etNama.getText().toString();
        String email = binding.etEmail.getText().toString();
        String asal = binding.etAsal.getText().toString();
        String tanggal = binding.etTanggalLahir.getText().toString();
        String agama = binding.spAgama.getSelectedItem().toString();
        String kelamin = binding.spKelamin.getSelectedItem().toString();
        String jabatan = binding.spJabatan.getSelectedItem().toString();
        String hakAkses = String.valueOf(binding.swAdmin.isChecked());
        String nik = binding.etNik.getText().toString();

        if (!resultFace.isEmpty()){
            DataKaryawan dataKaryawan = new DataKaryawan(nik,nama,email,asal,tanggal,agama,kelamin,jabatan,uri, resultFace, hakAkses, false);
            Map<String, Object> map = dataKaryawan.toMap();
            firebaseFirestore.collection("users")
                    .document(username)
                    .set(map)
                    .addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        startActivity(new Intent(AddPegawaiActivity.this, MainActivity.class));
                    }).addOnFailureListener(e -> Log.e(TAG, e.getMessage()));
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null){
            if (OpenCVLoader.initDebug()){
                baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }else{
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this,baseLoaderCallback);
            }
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void updateLabel(){
        String format = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);

        binding.etTanggalLahir.setText(sdf.format(mCalendar.getTime()));
    }

    private boolean validateForm(){
        boolean result = true;

        if (TextUtils.isEmpty(binding.etNik.getText().toString())){
            binding.etNik.setError("Required");
            result = false;
        }else {
            binding.etNik.setError(null);
        }

        String nik = binding.etNik.getText().toString();
        Log.i("celeng", String.valueOf(nik.length()));
        if (!(nik.length() <= 16)){
            result = false;
            binding.etNik.setError("Panjang NIK harus 16");
        }else {
            binding.etNik.setError(null);
        }

        if (TextUtils.isEmpty(binding.etNama.getText().toString())){
            binding.etNama.setError("Required");
            result = false;
        }else {
            binding.etNama.setError(null);
        }

        if (TextUtils.isEmpty(binding.etEmail.getText().toString())){
            binding.etEmail.setError("Required");
            result = false;
        }else {
            binding.etEmail.setError(null);
        }

        String regexPattern = "^(.+)@(\\S+)$";
        Boolean resultEmail = Pattern.compile(regexPattern)
                .matcher(binding.etEmail.getText().toString())
                .matches();

        if (!resultEmail){
            result = false;
            binding.etEmail.setError("Format email");
        }else {
            binding.etEmail.setError(null);
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.US);
        String selectedYear = sdf.format(mCalendar.getTime());
        if (currentYear <= Integer.parseInt(selectedYear)){
            result = false;
            Toast.makeText(AddPegawaiActivity.this, "Tahun salah",Toast.LENGTH_SHORT).show();
            binding.etTanggalLahir.setError("Tahun salah");
        }else {
            binding.etTanggalLahir.setError(null);
        }

        if (TextUtils.isEmpty(binding.etPassword.getText().toString())){
            binding.etPassword.setError("Required");
            result = false;
        }else {
            binding.etPassword.setError(null);
        }

        if (TextUtils.isEmpty(binding.etAsal.getText().toString())){
            binding.etAsal.setError("Required");
            result = false;
        }else {
            binding.etAsal.setError(null);
        }

        if (TextUtils.isEmpty(binding.etTanggalLahir.getText().toString())){
            binding.etTanggalLahir.setError("Required");
            result = false;
        }else {
            binding.etTanggalLahir.setError(null);
        }

        binding.infoData.setVisibility(View.VISIBLE);
        if (imageUri == null){
            binding.infoData.setImageResource(R.drawable.icons_error);
            result = false;
        }else {
            binding.infoData.setImageResource(R.drawable.icons_done);
        }

        return result;
    }

    private void writeNewUser(String email, String password){
        String nik = binding.etNik.getText().toString();
        firebaseFirestore.collection("users").document(nik).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()){
                        progressDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(AddPegawaiActivity.this);
                        builder.setMessage("NIK Sudah Terdaftar");
                        builder.setCancelable(true);
                    }else {
                        if (validateForm()){
                            firebaseAuth.createUserWithEmailAndPassword(email,password)
                                    .addOnCompleteListener(this, task -> {
                                        if (task.isSuccessful()){
                                            uploadImgae();
                                        }else {
                                            Toast.makeText(null,"Auth failed",Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }else {
                            progressDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddPegawaiActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}