package com.ds.calc

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private var jamMasuk = 8
    private var menitMasuk = 0
    private var jamKeluar = 17
    private var menitKeluar = 0
    private var totalBiayaSaatIni = 0
    private var durasiTeksSaatIni = ""
    private var kendaraanSaatIni = "Motor"

    private lateinit var btnTabKalkulator: Button
    private lateinit var btnTabLaporan: Button
    private lateinit var layoutKalkulator: LinearLayout
    private lateinit var layoutLaporan: LinearLayout

    private lateinit var btnJamMasuk: Button
    private lateinit var btnJamKeluar: Button
    private lateinit var etPlatNomor: EditText
    private lateinit var tvHasilDurasi: TextView
    private lateinit var tvHasilBiaya: TextView
    private lateinit var tvKeteranganTarif: TextView
    private lateinit var etUangBayar: EditText
    private lateinit var tvKembalian: TextView
    private lateinit var rgKendaraan: RadioGroup

    private lateinit var tvRingkasanLaporan: TextView
    private lateinit var etUangFisik: EditText
    private lateinit var tvGrandTotal: TextView
    private lateinit var containerRiwayat: LinearLayout

    private var totalRekapSaatIni = 0
    private var teksRekapUntukGambar = ""

    private val PREF_NAME = "ds_calc_laporan_pref"
    private val KEY_DATA = "riwayat_parkir_json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val now = Calendar.getInstance()
        jamKeluar = now.get(Calendar.HOUR_OF_DAY)
        menitKeluar = now.get(Calendar.MINUTE)

        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F0F4F8"))
        }

        // --- TAB BAR ---
        val tabBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#102A43"))
            setPadding(16, 16, 16, 16)
        }

        btnTabKalkulator = Button(this).apply {
            text = "KALKULATOR"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener { tampilkanTab(isKalkulator = true) }
        }

        btnTabLaporan = Button(this).apply {
            text = "LAPORAN"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 0, 0, 0)
            }
            setOnClickListener { tampilkanTab(isKalkulator = false) }
        }

        tabBar.addView(btnTabKalkulator)
        tabBar.addView(btnTabLaporan)
        mainContainer.addView(tabBar)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }

        val contentWrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 32, 36, 36)
        }

        // ==========================================
        // HALAMAN 1: KALKULATOR
        // ==========================================
        layoutKalkulator = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvTitle = TextView(this).apply {
            text = "Ds Calc — Tarif Parkir"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#102A43"))
            setPadding(0, 0, 0, 16)
        }
        layoutKalkulator.addView(tvTitle)

        val tvLabelKendaraan = TextView(this).apply {
            text = "1. Jenis Kendaraan / Karcis"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 4, 0, 8)
        }
        layoutKalkulator.addView(tvLabelKendaraan)

        rgKendaraan = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(20, 12, 20, 12)
        }

        val rowRadio1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val rbMotor = RadioButton(this).apply { id = 1; text = "Motor      "; textSize = 15f; isChecked = true }
        val rbMobil = RadioButton(this).apply { id = 2; text = "Mobil"; textSize = 15f }

        val rowRadio2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val rbTruk = RadioButton(this).apply { id = 3; text = "Truk        "; textSize = 15f }
        val rbMember = RadioButton(this).apply { id = 4; text = "Member (Rp 0)"; textSize = 15f }

        // RadioGroup langsung menampung semua RadioButton
        rgKendaraan.addView(rbMotor)
        rgKendaraan.addView(rbMobil)
        rgKendaraan.addView(rbTruk)
        rgKendaraan.addView(rbMember)
        layoutKalkulator.addView(rgKendaraan)

        val tvLabelMasuk = TextView(this).apply {
            text = "2. Jam Masuk & Jam Keluar"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 20, 0, 8)
        }
        layoutKalkulator.addView(tvLabelMasuk)

        val rowJam = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        btnJamMasuk = Button(this).apply {
            text = "Masuk: ${formatWaktu(jamMasuk, menitMasuk)}"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener {
                TimePickerDialog(this@MainActivity, { _, hour, minute ->
                    jamMasuk = hour
                    menitMasuk = minute
                    text = "Masuk: ${formatWaktu(jamMasuk, menitMasuk)}"
                    hitungParkir()
                }, jamMasuk, menitMasuk, true).show()
            }
        }

        btnJamKeluar = Button(this).apply {
            text = "Keluar: ${formatWaktu(jamKeluar, menitKeluar)}"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 0, 0, 0)
            }
            setOnClickListener {
                TimePickerDialog(this@MainActivity, { _, hour, minute ->
                    jamKeluar = hour
                    menitKeluar = minute
                    text = "Keluar: ${formatWaktu(jamKeluar, menitKeluar)}"
                    hitungParkir()
                }, jamKeluar, menitKeluar, true).show()
            }
        }

        rowJam.addView(btnJamMasuk)
        rowJam.addView(btnJamKeluar)
        layoutKalkulator.addView(rowJam)

        val btnSekarang = Button(this).apply {
            text = "Set Jam Keluar = Sekarang"
            textSize = 13f
            setOnClickListener {
                val cal = Calendar.getInstance()
                jamKeluar = cal.get(Calendar.HOUR_OF_DAY)
                menitKeluar = cal.get(Calendar.MINUTE)
                btnJamKeluar.text = "Keluar: ${formatWaktu(jamKeluar, menitKeluar)}"
                hitungParkir()
            }
        }
        layoutKalkulator.addView(btnSekarang)

        // Kartu Hasil Tagihan
        val cardHasil = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 24, 28, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 20, 0, 16) }
            layoutParams = params
        }

        tvHasilDurasi = TextView(this).apply {
            text = "Durasi: 0 jam 0 menit"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#334E68"))
            setPadding(0, 0, 0, 6)
        }

        tvHasilBiaya = TextView(this).apply {
            text = "Rp 0"
            textSize = 30f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1B7F5C"))
            setPadding(0, 0, 0, 8)
        }

        tvKeteranganTarif = TextView(this).apply {
            text = "-"
            textSize = 12f
            setTextColor(Color.parseColor("#627D98"))
        }

        cardHasil.addView(tvHasilDurasi)
        cardHasil.addView(tvHasilBiaya)
        cardHasil.addView(tvKeteranganTarif)
        layoutKalkulator.addView(cardHasil)

        // Simpan ke Laporan
        val cardSimpan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 20, 28, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            layoutParams = params
        }

        etPlatNomor = EditText(this).apply {
            hint = "Plat Nomor / Catatan (Opsional)"
            textSize = 14f
        }

        val btnSimpanLaporan = Button(this).apply {
            text = "+ SIMPAN KARCIS KE LAPORAN"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#1B7F5C"))
            setTextColor(Color.WHITE)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 12, 0, 0) }
            layoutParams = params
            setOnClickListener { simpanTransaksiKeLaporan() }
        }

        cardSimpan.addView(etPlatNomor)
        cardSimpan.addView(btnSimpanLaporan)
        layoutKalkulator.addView(cardSimpan)

        // Kembalian Tunai
        val cardKembalian = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 20, 28, 20)
        }

        val tvLabelBayar = TextView(this).apply {
            text = "Hitung Kembalian Tunai (Opsional)"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 0, 0, 6)
        }

        etUangBayar = EditText(this).apply {
            hint = "Uang diterima (misal: 10000)"
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 15f
        }

        tvKembalian = TextView(this).apply {
            text = "Kembalian: Rp 0"
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#D97706"))
            setPadding(0, 8, 0, 0)
        }

        cardKembalian.addView(tvLabelBayar)
        cardKembalian.addView(etUangBayar)
        cardKembalian.addView(tvKembalian)
        layoutKalkulator.addView(cardKembalian)

        // ==========================================
        // HALAMAN 2: LAPORAN REKAPITULASI
        // ==========================================
        layoutLaporan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val tvJudulLaporan = TextView(this).apply {
            text = "Rekap Laporan Setoran"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#102A43"))
            setPadding(0, 0, 0, 16)
        }
        layoutLaporan.addView(tvJudulLaporan)

        // Kartu Rekap Dikelompokkan
        val cardRingkasan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 24, 28, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            layoutParams = params
        }

        tvRingkasanLaporan = TextView(this).apply {
            text = "Memuat laporan..."
            textSize = 14f
            typeface = Typeface.MONOSPACE
            setTextColor(Color.parseColor("#102A43"))
        }
        cardRingkasan.addView(tvRingkasanLaporan)

        // Input Uang Fisik & Grand Total Selisih
        val divider = View(this).apply {
            setBackgroundColor(Color.parseColor("#D9E2EC"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply {
                setMargins(0, 20, 0, 16)
            }
        }
        cardRingkasan.addView(divider)

        val tvLabelFisik = TextView(this).apply {
            text = "Input Total Uang Fisik (Tunai di Tangan):"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
        }
        cardRingkasan.addView(tvLabelFisik)

        etUangFisik = EditText(this).apply {
            hint = "Masukkan total uang fisik (misal: 150000)"
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 16f
        }
        cardRingkasan.addView(etUangFisik)

        tvGrandTotal = TextView(this).apply {
            text = "Grand Total (Selisih): Rp 0"
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1B7F5C"))
            setPadding(0, 10, 0, 0)
        }
        cardRingkasan.addView(tvGrandTotal)
        layoutLaporan.addView(cardRingkasan)

        // Tombol Kirim Gambar JPG & Kirim Teks
        val rowKirim = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 10) }
            layoutParams = params
        }

        val btnKirimGambar = Button(this).apply {
            text = "Kirim Gambar (JPG)"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#1B7F5C"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener { kirimLaporanGambarJpg() }
        }

        val btnKirimTeks = Button(this).apply {
            text = "Salin / Kirim Teks"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#2B6CB0"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 0, 0, 0)
            }
            setOnClickListener { bagikanAtauSalinTeks() }
        }

        rowKirim.addView(btnKirimGambar)
        rowKirim.addView(btnKirimTeks)
        layoutLaporan.addView(rowKirim)

        val btnResetLaporan = Button(this).apply {
            text = "Hapus Semua Data Laporan"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#C53030"))
            setTextColor(Color.WHITE)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 20) }
            layoutParams = params
            setOnClickListener { konfirmasiHapusSemua() }
        }
        layoutLaporan.addView(btnResetLaporan)

        val tvHeaderRiwayat = TextView(this).apply {
            text = "Rincian Lembar Karcis Tersimpan:"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 0, 0, 12)
        }
        layoutLaporan.addView(tvHeaderRiwayat)

        containerRiwayat = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        layoutLaporan.addView(containerRiwayat)

        contentWrapper.addView(layoutKalkulator)
        contentWrapper.addView(layoutLaporan)
        scrollView.addView(contentWrapper)
        mainContainer.addView(scrollView)
        setContentView(mainContainer)

        rgKendaraan.setOnCheckedChangeListener { _, _ -> hitungParkir() }

        etUangBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hitungKembalian()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etUangFisik.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateGrandTotal()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tampilkanTab(isKalkulator = true)
        hitungParkir()
    }

    private fun tampilkanTab(isKalkulator: Boolean) {
        if (isKalkulator) {
            layoutKalkulator.visibility = View.VISIBLE
            layoutLaporan.visibility = View.GONE
            btnTabKalkulator.setBackgroundColor(Color.parseColor("#2B6CB0"))
            btnTabKalkulator.setTextColor(Color.WHITE)
            btnTabLaporan.setBackgroundColor(Color.parseColor("#334E68"))
            btnTabLaporan.setTextColor(Color.parseColor("#BCCCDC"))
        } else {
            layoutKalkulator.visibility = View.GONE
            layoutLaporan.visibility = View.VISIBLE
            btnTabLaporan.setBackgroundColor(Color.parseColor("#2B6CB0"))
            btnTabLaporan.setTextColor(Color.WHITE)
            btnTabKalkulator.setBackgroundColor(Color.parseColor("#334E68"))
            btnTabKalkulator.setTextColor(Color.parseColor("#BCCCDC"))
            muatDataLaporan()
        }
    }

    private fun formatWaktu(jam: Int, menit: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", jam, menit)
    }

    private fun hitungParkir() {
        val totalMenitMasuk = jamMasuk * 60 + menitMasuk
        var totalMenitKeluar = jamKeluar * 60 + menitKeluar

        if (totalMenitKeluar < totalMenitMasuk) {
            totalMenitKeluar += 24 * 60
        }

        var selisihMenit = totalMenitKeluar - totalMenitMasuk
        if (selisihMenit == 0) {
            selisihMenit = 1
        }

        val durasiJam = selisihMenit / 60
        val sisaMenit = selisihMenit % 60
        durasiTeksSaatIni = "$durasiJam jam $sisaMenit menit"

        val terpilih = rgKendaraan.checkedRadioButtonId
        if (terpilih == 4) {
            kendaraanSaatIni = "Member"
            totalBiayaSaatIni = 0
            tvHasilDurasi.text = "Durasi: $durasiTeksSaatIni"
            tvHasilBiaya.text = "Rp 0"
            tvKeteranganTarif.text = "Member • Tarif Rp 0 (Bebas Biaya)"
            hitungKembalian()
            return
        }

        val (namaKendaraan, tarifJamPertama, tarifLebih1Jam, tarifMaksimal) = when (terpilih) {
            1 -> TarifParkir("Motor", 3000, 5000, 7000)
            2 -> TarifParkir("Mobil", 5000, 8000, 10000)
            else -> TarifParkir("Truk", 6000, 9000, 15000)
        }

        val (biayaAkhir, statusTier) = when {
            selisihMenit <= 60 -> tarifJamPertama to "Jam Pertama (≤ 1 jam)"
            selisihMenit <= 120 -> tarifLebih1Jam to "> 1 Jam (1–2 jam)"
            else -> tarifMaksimal to "Tarif Maksimal (> 2 jam)"
        }

        kendaraanSaatIni = namaKendaraan
        totalBiayaSaatIni = biayaAkhir

        val rupiahFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        tvHasilDurasi.text = "Durasi: $durasiTeksSaatIni"
        tvHasilBiaya.text = "Rp ${rupiahFormat.format(biayaAkhir)}"
        tvKeteranganTarif.text = "$namaKendaraan • $statusTier"

        hitungKembalian()
    }

    private fun hitungKembalian() {
        val rupiahFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        val inputStr = etUangBayar.text.toString().trim()
        val uangDiterima = inputStr.toLongOrNull() ?: 0L

        if (uangDiterima == 0L) {
            tvKembalian.text = "Kembalian: Rp 0"
            tvKembalian.setTextColor(Color.parseColor("#627D98"))
            return
        }

        val selisih = uangDiterima - totalBiayaSaatIni
        if (selisih >= 0) {
            tvKembalian.text = "Kembalian: Rp ${rupiahFormat.format(selisih)}"
            tvKembalian.setTextColor(Color.parseColor("#1B7F5C"))
        } else {
            tvKembalian.text = "Kurang: Rp ${rupiahFormat.format(-selisih)}"
            tvKembalian.setTextColor(Color.parseColor("#D64545"))
        }
    }

    private fun simpanTransaksiKeLaporan() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))

        val tanggalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sekarang = Date()

        val plat = etPlatNomor.text.toString().trim().uppercase(Locale.getDefault())
        val item = JSONObject().apply {
            put("id", System.currentTimeMillis())
            put("tanggal", tanggalFormat.format(sekarang))
            put("kendaraan", kendaraanSaatIni)
            put("jam_masuk", formatWaktu(jamMasuk, menitMasuk))
            put("jam_keluar", formatWaktu(jamKeluar, menitKeluar))
            put("durasi", durasiTeksSaatIni)
            put("biaya", totalBiayaSaatIni)
            put("plat", plat)
        }

        jsonArray.put(item)
        prefs.edit().putString(KEY_DATA, jsonArray.toString()).apply()

        etPlatNomor.setText("")
        etUangBayar.setText("")

        val rupiahFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        Toast.makeText(
            this,
            "Tersimpan: $kendaraanSaatIni (Rp ${rupiahFormat.format(totalBiayaSaatIni)})",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun formatSingkatRibuan(nominal: Int): String {
        return if (nominal >= 1000 && nominal % 1000 == 0) {
            "${nominal / 1000}rb"
        } else {
            val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))
            "Rp ${nf.format(nominal)}"
        }
    }

    private fun susunTeksRekap(): String {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))
        val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))

        // Kelompokkan per jenis kendaraan -> map tarif -> jumlah lembar
        val kategoriUrutan = listOf("Motor", "Mobil", "Truk", "Member")
        val dataKelompok = mutableMapOf<String, MutableMap<Int, Int>>()
        for (kat in kategoriUrutan) {
            dataKelompok[kat] = sortedMapOf()
        }

        var grandRekap = 0
        var totalLembarSemua = 0

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val kendaraan = obj.getString("kendaraan")
            val biaya = obj.getInt("biaya")

            grandRekap += biaya
            totalLembarSemua++

            val mapTarif = dataKelompok.getOrPut(kendaraan) { sortedMapOf() }
            mapTarif[biaya] = (mapTarif[biaya] ?: 0) + 1
        }

        totalRekapSaatIni = grandRekap

        val sb = StringBuilder()
        val tglCetak = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        sb.append("REKAP LAPORAN PARKIR - DS CALC\n")
        sb.append("Waktu: $tglCetak\n")
        sb.append("================================\n\n")

        for (kat in kategoriUrutan) {
            val mapTarif = dataKelompok[kat] ?: continue
            val totalLembarKat = mapTarif.values.sum()
            if (totalLembarKat == 0) continue

            sb.append("$kat Total $totalLembarKat Lembar karcis\n")
            if (kat == "Member") {
                sb.append("  Tarif 0 karena member\n\n")
            } else {
                var subTotalKat = 0
                for ((tarif, lembar) in mapTarif) {
                    val hasilKali = tarif * lembar
                    subTotalKat += hasilKali
                    val labelTarif = "${tarif / 1000}k"
                    sb.append("  Tarif $labelTarif = ${lembar}L = ${formatSingkatRibuan(hasilKali)}\n")
                }
                sb.append("  Subtotal $kat = Rp ${nf.format(subTotalKat)}\n\n")
            }
        }

        val uangFisik = etUangFisik.text.toString().trim().toIntOrNull() ?: 0
        val selisih = uangFisik - grandRekap
        val statusSelisih = when {
            uangFisik == 0 -> "(Belum diisi)"
            selisih == 0 -> "(PAS)"
            selisih > 0 -> "(LEBIH +Rp ${nf.format(selisih)})"
            else -> "(KURANG -Rp ${nf.format(-selisih)})"
        }

        sb.append("================================\n")
        sb.append("Total Karcis : $totalLembarSemua Lembar\n")
        sb.append("Total Rekap  : Rp ${nf.format(grandRekap)}\n")
        sb.append("Uang Fisik   : Rp ${nf.format(uangFisik)}\n")
        sb.append("Grand Total  : Rp ${nf.format(selisih)} $statusSelisih")

        teksRekapUntukGambar = sb.toString()
        return teksRekapUntukGambar
    }

    private fun updateGrandTotal() {
        val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))
        val uangFisik = etUangFisik.text.toString().trim().toIntOrNull() ?: 0
        val selisih = uangFisik - totalRekapSaatIni

        when {
            uangFisik == 0 -> {
                tvGrandTotal.text = "Grand Total (Selisih): Rp 0"
                tvGrandTotal.setTextColor(Color.parseColor("#627D98"))
            }
            selisih == 0 -> {
                tvGrandTotal.text = "Grand Total: Rp 0 (PAS)"
                tvGrandTotal.setTextColor(Color.parseColor("#1B7F5C"))
            }
            selisih > 0 -> {
                tvGrandTotal.text = "Grand Total: +Rp ${nf.format(selisih)} (LEBIH)"
                tvGrandTotal.setTextColor(Color.parseColor("#2B6CB0"))
            }
            else -> {
                tvGrandTotal.text = "Grand Total: -Rp ${nf.format(-selisih)} (KURANG)"
                tvGrandTotal.setTextColor(Color.parseColor("#C53030"))
            }
        }

        // Perbarui teks rekap
        tvRingkasanLaporan.text = susunTeksRekap()
    }

    private fun muatDataLaporan() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))
        val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))

        containerRiwayat.removeAllViews()

        if (jsonArray.length() == 0) {
            totalRekapSaatIni = 0
            tvRingkasanLaporan.text = "Belum ada lembar karcis tersimpan.\nGunakan tombol '+ SIMPAN KARCIS KE LAPORAN' di tab Kalkulator."
            updateGrandTotal()
            return
        }

        tvRingkasanLaporan.text = susunTeksRekap()
        updateGrandTotal()

        for (i in jsonArray.length() - 1 downTo 0) {
            val obj = jsonArray.getJSONObject(i)
            val idItem = obj.getLong("id")
            val tanggal = obj.optString("tanggal", "-")
            val kendaraan = obj.getString("kendaraan")
            val jMasuk = obj.getString("jam_masuk")
            val jKeluar = obj.getString("jam_keluar")
            val durasi = obj.getString("durasi")
            val biaya = obj.getInt("biaya")
            val plat = obj.optString("plat", "")

            val itemCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundColor(Color.WHITE)
                setPadding(24, 20, 24, 20)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 12) }
                layoutParams = params
            }

            val infoCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val judulBaris = if (plat.isNotEmpty()) "$kendaraan ($plat)" else kendaraan
            val tvItemTitle = TextView(this).apply {
                text = "$judulBaris — Rp ${nf.format(biaya)}"
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#102A43"))
            }

            val tvItemDetail = TextView(this).apply {
                text = "$tanggal • $jMasuk - $jKeluar ($durasi)"
                textSize = 12f
                setTextColor(Color.parseColor("#627D98"))
                setPadding(0, 4, 0, 0)
            }

            infoCol.addView(tvItemTitle)
            infoCol.addView(tvItemDetail)

            val btnHapusItem = Button(this).apply {
                text = "Hapus"
                textSize = 11f
                setTextColor(Color.parseColor("#C53030"))
                setOnClickListener { hapusSatuTransaksi(idItem) }
            }

            itemCard.addView(infoCol)
            itemCard.addView(btnHapusItem)
            containerRiwayat.addView(itemCard)
        }
    }

    private fun hapusSatuTransaksi(targetId: Long) {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))
        val baru = JSONArray()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getLong("id") != targetId) {
                baru.put(obj)
            }
        }
        prefs.edit().putString(KEY_DATA, baru.toString()).apply()
        muatDataLaporan()
        Toast.makeText(this, "Karcis dihapus dari laporan", Toast.LENGTH_SHORT).show()
    }

    private fun konfirmasiHapusSemua() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Laporan?")
            .setMessage("Semua data rekap dan karcis tersimpan akan dihapus permanen.")
            .setPositiveButton("Hapus") { _, _ ->
                getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_DATA, "[]")
                    .apply()
                etUangFisik.setText("")
                muatDataLaporan()
                Toast.makeText(this, "Laporan dibersihkan", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun bagikanAtauSalinTeks() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))
        if (jsonArray.length() == 0) {
            Toast.makeText(this, "Belum ada data laporan", Toast.LENGTH_SHORT).show()
            return
        }

        val teks = susunTeksRekap()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Rekap Parkir", teks))
        Toast.makeText(this, "Teks rekap disalin!", Toast.LENGTH_SHORT).show()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, teks)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Kirim Teks Laporan via"))
    }

    private fun kirimLaporanGambarJpg() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))
        if (jsonArray.length() == 0) {
            Toast.makeText(this, "Belum ada data laporan untuk dijadikan gambar", Toast.LENGTH_SHORT).show()
            return
        }

        val teks = susunTeksRekap()
        val barisList = teks.split("\n")

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#102A43")
            textSize = 34f
            typeface = Typeface.MONOSPACE
        }

        val padding = 60
        val lineHeight = 48
        val width = 1080
        val height = (barisList.size * lineHeight) + (padding * 2) + 40

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Latar belakang putih bersih seperti kertas struk
        canvas.drawColor(Color.WHITE)

        // Bingkai tepi tipis
        val borderPaint = Paint().apply {
            color = Color.parseColor("#BCCCDC")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(24f, 24f, (width - 24).toFloat(), (height - 24).toFloat(), borderPaint)

        var yPos = padding + 36f
        for ((index, baris) in barisList.withIndex()) {
            if (index == 0 || baris.startsWith("Total") || baris.startsWith("Uang Fisik") || baris.startsWith("Grand Total")) {
                paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            } else {
                paint.typeface = Typeface.MONOSPACE
            }
            canvas.drawText(baris, padding.toFloat(), yPos, paint)
            yPos += lineHeight
        }

        try {
            val cacheFolder = File(cacheDir, "laporan_images")
            if (!cacheFolder.exists()) cacheFolder.mkdirs()

            val fileJpg = File(cacheFolder, "Rekap_Parkir_DsCalc.jpg")
            FileOutputStream(fileJpg).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            val uri = FileProvider.getUriForFile(this, "com.ds.calc.fileprovider", fileJpg)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Rekap Laporan Parkir - Ds Calc")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Kirim Gambar Laporan (JPG) ke WA"))
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuat gambar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    data class TarifParkir(
        val nama: String,
        val jamPertama: Int,
        val lebihSatuJam: Int,
        val maksimal: Int
    )
}
