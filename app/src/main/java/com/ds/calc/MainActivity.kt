package com.ds.calc

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import org.json.JSONArray
import org.json.JSONObject
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
    private lateinit var containerRiwayat: LinearLayout

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

        // --- BARIS TAB NAVIGASI ATAS ---
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
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
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

        // 1. Jenis Kendaraan
        val tvLabelKendaraan = TextView(this).apply {
            text = "1. Jenis Kendaraan"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 4, 0, 8)
        }
        layoutKalkulator.addView(tvLabelKendaraan)

        rgKendaraan = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(20, 14, 20, 14)
        }

        val rbMotor = RadioButton(this).apply { id = 1; text = "Motor   "; textSize = 16f; isChecked = true }
        val rbMobil = RadioButton(this).apply { id = 2; text = "Mobil   "; textSize = 16f }
        val rbTruk = RadioButton(this).apply { id = 3; text = "Truk"; textSize = 16f }

        rgKendaraan.addView(rbMotor)
        rgKendaraan.addView(rbMobil)
        rgKendaraan.addView(rbTruk)
        layoutKalkulator.addView(rgKendaraan)

        // 2. Jam Masuk & Keluar
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
            ).apply {
                setMargins(0, 20, 0, 16)
            }
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

        // Catatan Plat Nomor + Tombol Simpan ke Laporan
        val cardSimpan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 20, 28, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            layoutParams = params
        }

        etPlatNomor = EditText(this).apply {
            hint = "Plat Nomor / Catatan (Opsional, misal: B 1234 XYZ)"
            textSize = 14f
        }

        val btnSimpanLaporan = Button(this).apply {
            text = "+ SIMPAN KE LAPORAN"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#1B7F5C"))
            setTextColor(Color.WHITE)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 12, 0, 0)
            }
            layoutParams = params
            setOnClickListener { simpanTransaksiKeLaporan() }
        }

        cardSimpan.addView(etPlatNomor)
        cardSimpan.addView(btnSimpanLaporan)
        layoutKalkulator.addView(cardSimpan)

        // Kalkulator Kembalian Tunai
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
        // HALAMAN 2: LAPORAN TRANSAKSI
        // ==========================================
        layoutLaporan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val tvJudulLaporan = TextView(this).apply {
            text = "Laporan Pendapatan Parkir"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#102A43"))
            setPadding(0, 0, 0, 16)
        }
        layoutLaporan.addView(tvJudulLaporan)

        // Kartu Ringkasan Total
        val cardRingkasan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 24, 28, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            layoutParams = params
        }

        tvRingkasanLaporan = TextView(this).apply {
            text = "Memuat laporan..."
            textSize = 15f
            setTextColor(Color.parseColor("#102A43"))
        }
        cardRingkasan.addView(tvRingkasanLaporan)
        layoutLaporan.addView(cardRingkasan)

        // Tombol Aksi Laporan (Bagikan WA & Hapus Semua)
        val rowAksiLaporan = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 20)
            }
            layoutParams = params
        }

        val btnBagikanLaporan = Button(this).apply {
            text = "Kirim / Salin Laporan"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#2B6CB0"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener { bagikanAtauSalinLaporan() }
        }

        val btnResetLaporan = Button(this).apply {
            text = "Hapus Semua"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#C53030"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 0, 0, 0)
            }
            setOnClickListener { konfirmasiHapusSemua() }
        }

        rowAksiLaporan.addView(btnBagikanLaporan)
        rowAksiLaporan.addView(btnResetLaporan)
        layoutLaporan.addView(rowAksiLaporan)

        val tvHeaderRiwayat = TextView(this).apply {
            text = "Rincian Transaksi Tersimpan:"
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

        val (namaKendaraan, tarifJamPertama, tarifLebih1Jam, tarifMaksimal) = when (rgKendaraan.checkedRadioButtonId) {
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
        durasiTeksSaatIni = "$durasiJam jam $sisaMenit menit"

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
        val jsonStr = prefs.getString(KEY_DATA, "[]")
        val jsonArray = JSONArray(jsonStr)

        val tanggalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val waktuSimpanFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sekarang = Date()

        val plat = etPlatNomor.text.toString().trim().uppercase(Locale.getDefault())
        val item = JSONObject().apply {
            put("id", System.currentTimeMillis())
            put("tanggal", tanggalFormat.format(sekarang))
            put("waktu_catat", waktuSimpanFormat.format(sekarang))
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
            "Tersimpan: $kendaraanSaatIni Rp ${rupiahFormat.format(totalBiayaSaatIni)}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun muatDataLaporan() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))
        val rupiahFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))

        var totalSemua = 0
        var countMotor = 0
        var totalMotor = 0
        var countMobil = 0
        var totalMobil = 0
        var countTruk = 0
        var totalTruk = 0

        containerRiwayat.removeAllViews()

        if (jsonArray.length() == 0) {
            tvRingkasanLaporan.text = "Belum ada transaksi tersimpan.\nGunakan tombol '+ SIMPAN KE LAPORAN' di tab Kalkulator."
            return
        }

        // Loop dari yang paling baru di atas
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

            totalSemua += biaya
            when (kendaraan) {
                "Motor" -> { countMotor++; totalMotor += biaya }
                "Mobil" -> { countMobil++; totalMobil += biaya }
                "Truk" -> { countTruk++; totalTruk += biaya }
            }

            // Kartu Item Transaksi
            val itemCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundColor(Color.WHITE)
                setPadding(24, 20, 24, 20)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
                layoutParams = params
            }

            val infoCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val judulBaris = if (plat.isNotEmpty()) "$kendaraan ($plat)" else kendaraan
            val tvItemTitle = TextView(this).apply {
                text = "$judulBaris — Rp ${rupiahFormat.format(biaya)}"
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

        tvRingkasanLaporan.text =
            "TOTAL PENDAPATAN: Rp ${rupiahFormat.format(totalSemua)}\n" +
            "Total Kendaraan: ${jsonArray.length()} unit\n\n" +
            "• Motor : $countMotor unit (Rp ${rupiahFormat.format(totalMotor)})\n" +
            "• Mobil : $countMobil unit (Rp ${rupiahFormat.format(totalMobil)})\n" +
            "• Truk  : $countTruk unit (Rp ${rupiahFormat.format(totalTruk)})"
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
        Toast.makeText(this, "Transaksi dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun konfirmasiHapusSemua() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Laporan?")
            .setMessage("Semua catatan transaksi parkir yang tersimpan akan dihapus permanen.")
            .setPositiveButton("Hapus") { _, _ ->
                getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_DATA, "[]")
                    .apply()
                muatDataLaporan()
                Toast.makeText(this, "Semua laporan telah dibersihkan", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun bagikanAtauSalinLaporan() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))
        if (jsonArray.length() == 0) {
            Toast.makeText(this, "Belum ada data laporan untuk dikirim", Toast.LENGTH_SHORT).show()
            return
        }

        val rupiahFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        var totalSemua = 0
        var countMotor = 0; var totalMotor = 0
        var countMobil = 0; var totalMobil = 0
        var countTruk = 0; var totalTruk = 0

        val detailBuilder = StringBuilder()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val kendaraan = obj.getString("kendaraan")
            val jMasuk = obj.getString("jam_masuk")
            val jKeluar = obj.getString("jam_keluar")
            val biaya = obj.getInt("biaya")
            val plat = obj.optString("plat", "")

            totalSemua += biaya
            when (kendaraan) {
                "Motor" -> { countMotor++; totalMotor += biaya }
                "Mobil" -> { countMobil++; totalMobil += biaya }
                "Truk" -> { countTruk++; totalTruk += biaya }
            }

            val labelPlat = if (plat.isNotEmpty()) " [$plat]" else ""
            detailBuilder.append("${i + 1}. $kendaraan$labelPlat ($jMasuk-$jKeluar): Rp ${rupiahFormat.format(biaya)}\n")
        }

        val tglCetak = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val teksLaporan = """
            *LAPORAN PARKIR - DS CALC*
            Dicetak: $tglCetak
            
            *RINGKASAN:*
            • Motor: $countMotor unit = Rp ${rupiahFormat.format(totalMotor)}
            • Mobil: $countMobil unit = Rp ${rupiahFormat.format(totalMobil)}
            • Truk: $countTruk unit = Rp ${rupiahFormat.format(totalTruk)}
            *TOTAL (${jsonArray.length()} unit): Rp ${rupiahFormat.format(totalSemua)}*
            
            *RINCIAN TRANSAKSI:*
            $detailBuilder
        """.trimIndent()

        // Salin otomatis ke clipboard
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Laporan Parkir", teksLaporan))
        Toast.makeText(this, "Laporan disalin ke clipboard!", Toast.LENGTH_SHORT).show()

        // Buka menu Share Android (bisa langsung kirim ke WhatsApp)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, teksLaporan)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Kirim Laporan Parkir via"))
    }

    data class TarifParkir(
        val nama: String,
        val jamPertama: Int,
        val lebihSatuJam: Int,
        val maksimal: Int
    )
}
