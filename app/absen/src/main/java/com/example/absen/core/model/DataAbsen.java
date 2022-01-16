package com.example.absen.core.model;

import java.util.HashMap;
import java.util.Map;

public class DataAbsen {

    String tanggal, pukul, kegiatan;

    public DataAbsen(){}

    public DataAbsen(String tanggal, String pukul, String kegiatan) {
        this.tanggal = tanggal;
        this.pukul = pukul;
        this.kegiatan = kegiatan;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("tanggal",tanggal);
        result.put("pukul", pukul);
        result.put("kegiatan", kegiatan);
        return result;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getPukul() {
        return pukul;
    }

    public void setPukul(String pukul) {
        this.pukul = pukul;
    }

    public String getKegiatan() {
        return kegiatan;
    }

    public void setKegiatan(String kegiatan) {
        this.kegiatan = kegiatan;
    }
}
