package com.example.absensi.ui;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.absensi.FaceRecognetion;
import com.example.absensi.R;
import com.example.absensi.core.model.DataKaryawan;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class DialogEdit extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "DialogEdit";

    private EditText mNama, mEmail, mAsal, mTanggalLahir;
    private Spinner mAgama, mKelamin, mJabatan;
    private TextView mFailed;
    private ImageView mInfo;
    private Switch mAdmin;
    private String imageUri;
    private Uri imageUriNew = null;
    private String userId;
    private String readFace;

    private final Calendar mCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private ProgressDialog progressDialog;

    private FirebaseFirestore firebaseFirestore;

    private FaceRecognetion faceRecognetion;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_pegawai,container,false);

        firebaseFirestore = FirebaseFirestore.getInstance();

        mNama = view.findViewById(R.id.et_nama);
        mEmail = view.findViewById(R.id.et_email);
        mAsal = view.findViewById(R.id.et_asal);
        mTanggalLahir = view.findViewById(R.id.et_tanggal_lahir);

        mAgama = view.findViewById(R.id.sp_agama);
        mKelamin = view.findViewById(R.id.sp_kelamin);
        mJabatan = view.findViewById(R.id.sp_jabatan);

        mAdmin = view.findViewById(R.id.sw_admin);

        mFailed = view.findViewById(R.id.tv_failed);

        Button mPilihPoto = view.findViewById(R.id.btn_pilih_foto);
        Button mUbah = view.findViewById(R.id.btn_ubah);

        ImageView mCancel = view.findViewById(R.id.btn_cancel);
        mInfo = view.findViewById(R.id.info_data);

        Bundle bundle = getArguments();
        if (bundle != null){
            showKaryawan(bundle);
        }
        
        progressDialog = new ProgressDialog(getActivity());

        mCancel.setOnClickListener(v -> getDialog().dismiss());

        dateSetListener = (view1, year, month, dayOfMonth) -> {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        };

        mTanggalLahir.setOnClickListener(this);
        mUbah.setOnClickListener(this);
        mPilihPoto.setOnClickListener(this);

        setFaceRecognation();

        return view;
    }

    private void setFaceRecognation() {
        OpenCVLoader.initDebug();

        BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(getActivity()) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
            }
        };

        try {
            faceRecognetion = new FaceRecognetion(getActivity().getAssets(),
                    getActivity(),
                    "model_efficientNetB0.tflite",
                    96);
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
        }
    }

    private void showKaryawan(Bundle bundle) {
        userId = bundle.getString("userId","");
        mNama.setText(bundle.getString("nama",""));
        mEmail.setText(bundle.getString("email",""));
        mAsal.setText(bundle.getString("asal",""));
        mTanggalLahir.setText(bundle.getString("ttl",""));
        imageUri = bundle.getString("imageUri","");
        readFace = bundle.getString("readFace","");

        String hakAkses = bundle.getString("hakAkses","");
        if (hakAkses.equals("true")){
            mAdmin.setChecked(true);
        }

        String agama = bundle.getString("agama","");
        String[] arrayA = getResources().getStringArray(R.array.agama);
        for (int i = 0;i<arrayA.length;i++){
            if (arrayA[i].equals(agama)){
                mAgama.setSelection(i);
            }
        }

        String kelamin = bundle.getString("kelamin","");
        String[] arrayK = getResources().getStringArray(R.array.kelamin);
        for (int i = 0;i < arrayK.length; i++){
            if (arrayK[i].equals(kelamin)){
                mKelamin.setSelection(i);
            }
        }

        String jabatan = bundle.getString("jabatan","");
        String[] arrayJ = getResources().getStringArray(R.array.jabatan);
        for (int i = 0; i< arrayJ.length; i++){
            if (arrayJ[i].equals(jabatan)){
                mJabatan.setSelection(i);
            }
        }

        imageUri = bundle.getString("imageUri","");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.et_tanggal_lahir:
                new DatePickerDialog(getActivity(),dateSetListener,
                        mCalendar.get(Calendar.YEAR),mCalendar.get(Calendar.MONTH),mCalendar.get(Calendar.DAY_OF_MONTH)).show();
                updateLabel();
                break;

            case R.id.btn_ubah:
                progressDialog.setMessage("Update, Please wait...");
                progressDialog.show();
                checkImage();
                break;

            case R.id.btn_pilih_foto:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,1);
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode== RESULT_OK && data != null && data.getData()!=null){
            imageUriNew = data.getData();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Mat obj = new Mat();
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(
                        getActivity().getContentResolver(), imageUriNew);

                obj = new Mat(bmp.getWidth(),bmp.getHeight(), CvType.CV_8UC4);
                Utils.bitmapToMat(bmp, obj);
            } catch (IOException e) {
                e.printStackTrace();
            }

            readFace = faceRecognetion.recognizePicture(obj);

            if (readFace.isEmpty()){
                mInfo.setVisibility(View.GONE);
                mFailed.setVisibility(View.VISIBLE);
            }
        }
    }

    private void deleteImage(Uri imageUriNew){
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUri);

        reference.delete().addOnSuccessListener(unused -> {
            Log.d(TAG,"delete Sukses");
            uploadImgae(userId, imageUriNew);
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getActivity().getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImgae(String userId, Uri imageUri_){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(userId).child(System.currentTimeMillis()+"."+getFileExtension(imageUri_));
        storageReference.putFile(imageUri_).addOnCompleteListener(task ->
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            final String image = uri.toString();
            editKaryawan(image);
        }));
    }

    private void checkImage(){
        if (validateForm()){
            if (imageUriNew != null){
                deleteImage(imageUriNew);
            }else {
                editKaryawan(imageUri);
            }
        }
    }

    private void editKaryawan(String image) {
        String nama = mNama.getText().toString();
        String email = mEmail.getText().toString();
        String asal = mAsal.getText().toString();
        String ttl = mTanggalLahir.getText().toString();
        String agama = mAgama.getSelectedItem().toString();
        String kelamin = mKelamin.getSelectedItem().toString();
        String jabatan = mJabatan.getSelectedItem().toString();
        String hakAkses = String.valueOf(mAdmin.isChecked());

        DataKaryawan dataKaryawan = new DataKaryawan(userId,nama,email,asal,ttl,agama,kelamin,jabatan,image,readFace, hakAkses, false);
        Map<String,Object> map = dataKaryawan.toMap();
        firebaseFirestore.collection("users").document(userId)
                .update(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        getDialog().dismiss();
                    }
                });
                
    }
    private boolean validateForm(){
        boolean result = true;
        if (TextUtils.isEmpty(mNama.getText().toString())){
            mNama.setError("Required");
            result = false;
        }else {
            mNama.setError(null);
        }

        if (TextUtils.isEmpty(mEmail.getText().toString())){
            mEmail.setError("Required");
            result = false;
        }else {
            mEmail.setError(null);
        }

        if (TextUtils.isEmpty(mAsal.getText().toString())){
            mAsal.setError("Required");
            result = false;
        }else {
            mAsal.setError(null);
        }

        if (TextUtils.isEmpty(mTanggalLahir.getText().toString())){
            mTanggalLahir.setError("Required");
            result = false;
        }else {
            mTanggalLahir.setError(null);
        }

        if (imageUri == null){
            mInfo.setImageResource(R.drawable.icons_error);
            result = false;
        }else {
            mInfo.setImageResource(R.drawable.icons_done);
        }

        return result;
    }
    private void updateLabel() {
        String format = "dd-MM-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);

        mTanggalLahir.setText(dateFormat.format(mCalendar.getTime()));
    }
}
