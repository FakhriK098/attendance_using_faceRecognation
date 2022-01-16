package com.example.absensi.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataKaryawan {
    private String nama, email, asal, tanggal_lahir, agama, jenis_kelamin, jabatan,imageUri, readFace, hakAkses;

    public DataKaryawan(){}

    public DataKaryawan(String nama, String email, String asal, String tanggal_lahir, String agama, String jenis_kelamin, String jabatan, String imageUri, String readFace, String hakAkses) {
        this.nama = nama;
        this.email = email;
        this.asal = asal;
        this.tanggal_lahir = tanggal_lahir;
        this.agama = agama;
        this.jenis_kelamin = jenis_kelamin;
        this.jabatan = jabatan;
        this.imageUri = imageUri;
        this.readFace = readFace;
        this.hakAkses = hakAkses;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("nama",nama);
        result.put("email", email);
        result.put("asal", asal);
        result.put("tanggal_lahir", tanggal_lahir);
        result.put("agama", agama);
        result.put("jenis_kelamin", jenis_kelamin);
        result.put("jabatan", jabatan);
        result.put("imageUri", imageUri);
        result.put("readFace", readFace);
        result.put("hakAkses", hakAkses);
        return result;
    }

    public String getHakAkses() {
        return hakAkses;
    }

    public void setHakAkses(String hakAkses) {
        this.hakAkses = hakAkses;
    }

    public String getReadFace() {
        return readFace;
    }

    public void setReadFace(String readFace) {
        this.readFace = readFace;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAsal() {
        return asal;
    }

    public void setAsal(String asal) {
        this.asal = asal;
    }

    public String getTanggal_lahir() {
        return tanggal_lahir;
    }

    public void setTanggal_lahir(String tanggal_lahir) {
        this.tanggal_lahir = tanggal_lahir;
    }

    public String getAgama() {
        return agama;
    }

    public void setAgama(String agama) {
        this.agama = agama;
    }

    public String getJenis_kelamin() {
        return jenis_kelamin;
    }

    public void setJenis_kelamin(String jenis_kelamin) {
        this.jenis_kelamin = jenis_kelamin;
    }

    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

}
