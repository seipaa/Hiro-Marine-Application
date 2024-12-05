package models;

public class Lokasi {
    private String namaLokasi;
    private String deskripsi;
    private String gambar;

    public Lokasi(String namaLokasi, String deskripsi, String gambar) {
        this.namaLokasi = namaLokasi;
        this.deskripsi = deskripsi;
        this.gambar = gambar;
    }

    // Getters dan Setters
    public String getNamaLokasi() {
        return namaLokasi;
    }

    public void setNamaLokasi(String namaLokasi) {
        this.namaLokasi = namaLokasi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }
} 