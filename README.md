# Hiro's Marine — SEA EDUCATION

> **Aplikasi desktop edukasi dan konservasi ekosistem laut berbasis JavaFX, dirancang untuk memberdayakan masyarakat menjadi "Hiro" pahlawan bagi kelestarian lautan.**

---

## Daftar Isi

- [Tentang Aplikasi](#tentang-aplikasi)
- [Fitur Utama](#fitur-utama)
- [Tampilan Antarmuka](#tampilan-antarmuka)
- [Arsitektur Aplikasi](#arsitektur-aplikasi)
- [Teknologi yang Digunakan](#teknologi-yang-digunakan)
- [Struktur Proyek](#struktur-proyek)
- [Persyaratan Sistem](#persyaratan-sistem)
- [Cara Menjalankan](#cara-menjalankan)
- [Konfigurasi Database](#konfigurasi-database)
- [Peran Pengguna](#peran-pengguna)
- [Kontributor](#kontributor)

---

## Tentang Aplikasi

**Hiro's Marine** adalah aplikasi desktop edukasi lingkungan laut yang dibangun menggunakan **JavaFX** dan **Spring Boot**. Aplikasi ini menggabungkan konten edukasi kelautan dengan sistem gamifikasi (tantangan & poin) untuk mendorong masyarakat berpartisipasi aktif dalam pelestarian ekosistem laut Indonesia.

Nama "Hiro" berasal dari semangat menjadi *hero* (pahlawan) bagi lautan yang mengajak setiap pengguna untuk tidak hanya belajar tentang laut, tetapi juga bertindak nyata.

---

## Fitur Utama

### Untuk Pengguna (User)

| Fitur | Deskripsi |
|-------|-----------|
| **Autentikasi** | Login dan Registrasi akun pengguna baru |
| **Berita Kelautan** | Membaca berita dan isu terkini seputar ekosistem laut |
| **Spesies Laut** | Menjelajahi informasi spesies-spesies laut yang ada di Indonesia |
| **Rekomendasi Wisata** | Melihat rekomendasi destinasi wisata laut (Pantai Pangandaran, Anyer, Carita, dll.) beserta fitur diskusi/komentar |
| **Tantangan (Challenge)** | Mengikuti tantangan lingkungan untuk mendapatkan poin |
| **Detail Challenge** | Melihat deskripsi lengkap challenge, scan QR Code verifikasi, dan berdiskusi di kolom komentar |
| **Profil Pengguna** | Mengelola profil (nama panggilan, usia, email, Instagram, Twitter, bio) dan melihat riwayat challenge yang diselesaikan |
| **Leaderboard** | Melihat peringkat 3 besar pengguna berdasarkan poin yang dikumpulkan |

### Untuk Admin

| Panel | Deskripsi |
|-------|-----------|
| **News Admin** | Menambah, mengubah, dan menghapus berita kelautan |
| **Marine Species Admin** | Mengelola data spesies laut |
| **Recommendation Admin** | Mengelola data destinasi wisata laut yang direkomendasikan |
| **Challenge Admin** | Membuat/mengubah challenge, mengatur tanggal mulai & selesai, poin reward, dan **memverifikasi penyelesaian challenge** oleh pengguna |
| **User Admin** | Mengelola data seluruh pengguna yang terdaftar |

---

## Tampilan Antarmuka

### 1. Halaman Login

Tampilan split layout: panel kiri berisi form login (username, password, tombol Masuk, dan link ke Register), panel kanan berisi gambar laut yang imersif. Warna utama biru `#007bff` dengan efek drop shadow pada tombol.

```
[ Logo ] Hiro's Marine
[  Username Field       ]
[  Password Field       ]
[       Masuk           ]
Belum Punya Akun? [ Daftar di sini ]
                          | [  Gambar Laut  ]
```

### 2. Halaman Registrasi

Tampilan full-screen biru dengan layout vertikal center: logo, judul "Daftar Akun Baru", subtitle motivasi, form input username & password, tombol Daftar, dan link balik ke Login.

```
        [ Logo ]
   Daftar Akun Baru
"Sudah siap menjadi pahlawan bagi lautan kita?"
   "Mari bergabunglah dengan Hiro's Marine!"
   [ Nama Pengguna Baru ]
   [ Sandi Baru         ]
         [ Daftar ]
   Sudah punya akun? [ Masuk di sini ]
```

### 3. Dashboard Utama — Tab NEWS

Background laut dengan overlay gelap navy. Header "HIRO'S MARINE" besar di atas, ikon profil & username di pojok kanan atas. TabPane dengan 5 tab:

Tab **NEWS** menampilkan daftar kartu berita bergulir (scroll), masing-masing dengan gambar thumbnail + judul berita, seperti:
- "Waktu Hampir Habis untuk Selamatkan Hiu" (gambar hiu)
- "Pemutihan Terumbu Karang Akibat Panas Laut" (gambar mangrove)
- "Penembak Singa Laut Diburu, yang Menemukan Dihadiahi Rp312 Juta" (gambar singa laut)

---

### 4. Dashboard Utama — Tab MARINE SPECIES

Menampilkan tombol-tombol "Show Marine Species" yang ketika diklik akan menampilkan info spesies laut dari database.

---

### 5. Dashboard Utama — Tab RECOMMENDATION

Dua area scroll:
- **Atas (horizontal scroll):** Kartu-kartu rekomendasi destinasi wisata dengan gambar dan tombol "Diskusi Rekomendasi" — Pantai Pangandaran, Anyer, Carita.
- **Bawah (vertical scroll):** Detail rekomendasi berikut gambar, deskripsi singkat, kolom komentar, dan tombol Submit.

---

### 6. Dashboard Utama — Tab CHALLENGE

Layout dua kolom:
- **Kiri:** Header "New Challenge" dengan deskripsi singkat, diikuti daftar kartu challenge yang dapat digulir.
- **Kanan:** Panel Leaderboard bergradasi navy-biru dengan peringkat 1 (mahkota emas 👑), 2 (medali perak 🎖), dan 3 (medali perunggu 🎖), beserta nama pengguna, poin, dan label "Points to next rank".

---

### 7. Detail Challenge

Jendela terpisah dengan layout HBox dua panel:
- **Panel Kiri:** Judul challenge, badge poin berwarna biru muda, gambar challenge, deskripsi "Tentang Challenge", dan **QR Code** unik per challenge untuk verifikasi penyelesaian oleh petugas.
- **Panel Kanan:** Area diskusi "Diskusi Challenge" — scroll komentar pengguna lain, kolom teks input komentar, tombol "Kirim Komentar".

Background: gradient `#1a4e6e → #2a9d8f` (biru teal).

---

### 8. Detail Berita

Jendela popup dengan background gambar laut + overlay gelap. Menampilkan:
- Gambar berita (ukuran besar)
- Judul berita (putih tebal)
- Isi/deskripsi berita
- Label nama admin yang memposting (hijau)

---

### 9. Profil Pengguna

Popup putih vertikal dengan dua tab:
- **Tab "Profile":** Username, total poin (biru), tanggal bergabung (abu), field editable: Nama Panggilan, Usia, Email, Instagram, Twitter, Bio. Tombol "Save Changes" (hijau) dan "Logout" (merah).
- **Tab "Completed Challenges":** Daftar challenge yang sudah diselesaikan oleh pengguna.

---

### 10. Admin Panel — Challenge Management

Diakses dari tab "ADMIN PANEL" di dashboard. Terdapat dua sub-tab:
- **Verifikasi Challenge:** Tabel berisi kolom User, Current Points, Challenge, Challenge Points, dan tombol aksi verifikasi. Filter berdasarkan challenge dan pencarian user.
- **Kelola Challenge:** Split pane — tabel daftar challenge (ID, Title, Points, Tanggal Mulai/Selesai, Actions) dan form detail (Judul, Deskripsi, Poin, Date Picker Mulai/Selesai, URL Poster/Browse).

---

## Arsitektur Aplikasi

Aplikasi ini menggunakan pola arsitektur **MVC (Model-View-Controller)** yang diintegrasikan dengan lapisan **DAO (Data Access Object)** untuk akses database.

```
┌─────────────────────────────────────────────┐
│               JavaFX UI Layer               │
│         (FXML + CSS Stylesheets)            │
└────────────────────┬────────────────────────┘
                     │
┌────────────────────▼────────────────────────┐
│             Controllers Layer               │
│  LoginController  │  MainController         │
│  RegisterCtrl     │  ChallengeDetailsCtrl   │
│  UserProfileCtrl  │  ChallengeAdminCtrl     │
│  NewsAdminCtrl    │  UserAdminCtrl          │
│  RecommendationAdminCtrl │ MarineSpeciesAdminCtrl │
└────────────────────┬────────────────────────┘
                     │
┌────────────────────▼────────────────────────┐
│               DAO Layer                     │
│  UserDAO  │  ChallengeDAO  │  NewsDAO       │
│  MarineSpeciesDAO  │  RecommendationDAO     │
│  CommentDAO  │  UserProfileDAO  │  AdminDAO │
└────────────────────┬────────────────────────┘
                     │
┌────────────────────▼────────────────────────┐
│            Utilities & Services             │
│  ConnectionPool (HikariCP)                  │
│  CacheManager (Caffeine)                    │
│  QRCodeGenerator (ZXing)                    │
│  LeaderboardFetcher (RxJava async)          │
│  ChallengeDetailsFetcher                    │
│  SceneUtils │ AlertUtils                    │
└────────────────────┬────────────────────────┘
                     │
┌────────────────────▼────────────────────────┐
│           MySQL Database (Port 3300)        │
│  Database: hiros_marine                     │
└─────────────────────────────────────────────┘
```

### Model Data

| Model | Atribut Utama |
|-------|--------------|
| `User` | id, username, password, points, joinDate, role |
| `UserProfile` | userId, nickname, age, email, instagram, twitter, bio |
| `Challenge` | id, title, description, points, startDate, endDate, imageUrl |
| `News` | id, title, description, imageUrl, adminId |
| `MarineSpecies` | id, name, description, imageUrl |
| `Recommendation` | id, locationName, description, imageUrl |
| `Comment` | id, userId, challengeId, content, createdAt |
| `Admin` | id, username, password |
| `Lokasi` | id, nama, deskripsi |

---

## Teknologi yang Digunakan

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| Bahasa Pemrograman | Java | 17 / 23 |
| UI Framework | JavaFX | 17 / 22 |
| FXML | JavaFX FXML | 22 |
| Backend Framework | Spring Boot | 3.2.3 |
| Database | MySQL | 8.0 |
| JDBC Driver | MySQL Connector/J | 8.0.33 |
| Connection Pool | HikariCP | 5.0.1 |
| Caching | Caffeine Cache | 3.1.8 |
| Async Programming | RxJava | 3.1.8 |
| QR Code | Google ZXing | 3.5.0 |
| UI Components | FormsFX | 11.6.0 |
| Mobile UI | Gluon Charm Glisten | 6.2.3 |
| Logging | SLF4J | 2.0.7 |
| Build Tool | Maven (via mvnw wrapper) | — |

---

## Struktur Proyek

```
Hiros Marine Application/
├── pom.xml                          # Konfigurasi Maven & dependensi
├── mvnw / mvnw.cmd                  # Maven wrapper
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── HiroMarineApp.java           # Entry point aplikasi
    │   │   ├── AdminApp.java                # Entry point mode admin
    │   │   ├── UserApp.java                 # Entry point mode user
    │   │   ├── controllers/
    │   │   │   ├── LoginController.java
    │   │   │   ├── RegisterController.java
    │   │   │   ├── MainController.java       # Controller utama (dashboard)
    │   │   │   ├── ChallengeDetailsController.java
    │   │   │   ├── NewsDetailsController.java
    │   │   │   ├── UserProfileController.java
    │   │   │   ├── ChallengeAdminController.java
    │   │   │   ├── NewsAdminController.java
    │   │   │   ├── MarineSpeciesAdminController.java
    │   │   │   ├── RecommendationAdminController.java
    │   │   │   ├── UserAdminController.java
    │   │   │   └── AdminController.java
    │   │   ├── dao/
    │   │   │   ├── BaseDAO.java
    │   │   │   ├── UserDAO.java
    │   │   │   ├── AdminDAO.java
    │   │   │   ├── ChallengeDAO.java
    │   │   │   ├── CommentDAO.java
    │   │   │   ├── MarineSpeciesDAO.java
    │   │   │   ├── NewsDAO.java
    │   │   │   ├── RecommendationDAO.java
    │   │   │   └── UserProfileDAO.java
    │   │   ├── models/
    │   │   │   ├── User.java
    │   │   │   ├── UserProfile.java
    │   │   │   ├── Admin.java
    │   │   │   ├── Challenge.java
    │   │   │   ├── Comment.java
    │   │   │   ├── MarineSpecies.java
    │   │   │   ├── News.java
    │   │   │   ├── Recommendation.java
    │   │   │   └── Lokasi.java
    │   │   └── utils/
    │   │       ├── DatabaseConnection.java
    │   │       ├── ConnectionPool.java       # HikariCP pool
    │   │       ├── CacheManager.java         # Caffeine cache
    │   │       ├── QRCodeGenerator.java      # ZXing QR generator
    │   │       ├── LeaderboardFetcher.java   # Async leaderboard fetch
    │   │       ├── ChallengeDetailsFetcher.java
    │   │       ├── AlertUtils.java
    │   │       ├── SceneUtils.java
    │   │       └── DatabaseHelper.java
    │   └── resources/
    │       ├── fxml/
    │       │   ├── login.fxml
    │       │   ├── register.fxml
    │       │   ├── Main.fxml                 # Dashboard utama
    │       │   ├── challenge_details.fxml
    │       │   ├── news_details.fxml
    │       │   ├── user_profile.fxml
    │       │   ├── admin_challenge.fxml
    │       │   ├── admin_news.fxml
    │       │   ├── admin_marinespecies.fxml
    │       │   ├── admin_recommendation.fxml
    │       │   └── admin_user.fxml
    │       ├── images/
    │       │   ├── logo.png
    │       │   ├── background.jpg
    │       │   ├── login.jpg
    │       │   ├── profile_icon.png
    │       │   ├── hiu.jpg
    │       │   ├── mangrove.jpg
    │       │   ├── singalaut.jpg
    │       │   ├── pangandaran.jpg
    │       │   └── ...
    │       └── styles/
    │           └── scrollpane.css
    └── test/
        └── java/
```

---

## Persyaratan Sistem

| Komponen | Persyaratan |
|----------|-------------|
| Java | JDK 17 atau lebih tinggi (direkomendasikan JDK 23) |
| JavaFX | JavaFX SDK 17 atau 22+ |
| Database | MySQL Server 8.0 berjalan di port **3300** |
| RAM | Minimal 512 MB |
| OS | Windows / Linux / macOS |

---

## Cara Menjalankan

### Menggunakan IntelliJ IDEA (Disarankan)

1. Buka proyek di IntelliJ IDEA.
2. Pastikan **Project SDK** diatur ke JDK 23 (`C:\jdk-23.0.1-full` atau sesuaikan).
3. Tambahkan VM Options pada Run Configuration:
   ```
   --module-path "C:\javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.fxml
   ```
4. Jalankan kelas `HiroMarineApp` sebagai main class.

### Menggunakan Command Line (Maven Wrapper)

```bash
# Compile
./mvnw compile

# Jalankan dengan JavaFX Maven Plugin
./mvnw javafx:run
```

### Menggunakan Java Langsung (setelah compile)

```bash
java --module-path "C:\javafx-sdk-23.0.1\lib" \
     --add-modules javafx.controls,javafx.fxml \
     -cp "target\classes;<semua dependency jar>" \
     HiroMarineApp
```

---

## Konfigurasi Database

Edit file `src/main/java/utils/DatabaseConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3300/hiros_marine?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_password";
```

> **Pastikan database `hiros_marine` sudah dibuat dan MySQL berjalan di port 3300.**

### Tabel yang Dibutuhkan (DDL)

Buat tabel berikut di database `hiros_marine`:

```sql
CREATE DATABASE IF NOT EXISTS hiros_marine;
USE hiros_marine;

-- Tabel users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    points INT DEFAULT 0,
    join_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(20) DEFAULT 'user'
);

-- Tabel user_profiles
CREATE TABLE user_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE,
    nickname VARCHAR(100),
    age INT,
    email VARCHAR(255),
    instagram VARCHAR(100),
    twitter VARCHAR(100),
    bio TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tabel challenges
CREATE TABLE challenges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    points INT DEFAULT 0,
    start_date DATE,
    end_date DATE,
    image_url VARCHAR(500)
);

-- Tabel news
CREATE TABLE news (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    admin_id INT
);

-- Tabel marine_species
CREATE TABLE marine_species (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500)
);

-- Tabel recommendations
CREATE TABLE recommendations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    location_name VARCHAR(255),
    description TEXT,
    image_url VARCHAR(500)
);

-- Tabel comments
CREATE TABLE comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    challenge_id INT,
    content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (challenge_id) REFERENCES challenges(id)
);

-- Tabel admins
CREATE TABLE admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
```

---

## Peran Pengguna

Aplikasi memiliki dua jenis pengguna:

### 👤 User (Pengguna Biasa)
- Mendaftar dan login menggunakan akun user
- Membaca berita, spesies laut, dan rekomendasi wisata
- Mengikuti challenge untuk mendapatkan poin
- Berdiskusi dan berkomentar
- Memantau ranking di leaderboard
- Mengelola profil pribadi

### 🛡️ Admin
- Semua fitur user
- Akses ke **Admin Panel** di tab terpisah pada dashboard
- Mengelola konten: berita, spesies laut, rekomendasi, challenge
- Memverifikasi penyelesaian challenge pengguna (dengan bonus poin otomatis)
- Mengelola data seluruh pengguna

---

## Cara Kerja Sistem Challenge & Gamifikasi

1. Admin membuat tantangan (challenge) dengan judul, deskripsi, poin reward, dan rentang tanggal aktif.
2. Pengguna melihat challenge aktif di tab **CHALLENGE**.
3. Pengguna membuka **Detail Challenge**, membaca instruksi, lalu menyelesaikan tantangan nyata di lapangan.
4. Pengguna menunjukkan **QR Code** yang di-generate otomatis kepada petugas/admin untuk verifikasi.
5. Admin melakukan verifikasi di **Admin Panel → Challenge → Verifikasi Challenge**.
6. Poin otomatis ditambahkan ke akun pengguna.
7. **Leaderboard** diperbarui secara real-time dan menampilkan 3 pengguna teratas.

---

## Fitur Teknis Unggulan

- **HikariCP Connection Pool** — Manajemen koneksi database yang efisien dan performant.
- **Caffeine Cache** — Caching data yang sering diakses untuk mempercepat loading.
- **RxJava Async** — Pengambilan data leaderboard dilakukan secara asinkron agar UI tetap responsif.
- **ZXing QR Code Generator** — Setiap challenge menghasilkan QR code unik untuk sistem verifikasi fisik.
- **Background Loading** — Aplikasi menggunakan JavaFX `Task` untuk loading awal secara non-blocking (menampilkan progress indicator saat startup).
- 
---

<div align="center">
  <b>🌊 Bersama Kita Jaga Lautan Indonesia 🌊</b><br>
  <i>Hiro's Marine — SEA EDUCATION</i>
</div>
