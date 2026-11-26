package com.ds.calc

import android.app.Activity
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {

    private var jamMasuk = 8
    private var menitMasuk = 0
    private var jamKeluar = 17
    private var menitKeluar = 0
    private var totalBiayaSaatIni = 0

    private lateinit var btnJamMasuk: Button
    private lateinit var btnJamKeluar: Button
    private lateinit var tvHasilDurasi: TextView
    private lateinit var tvHasilBiaya: TextView
    private lateinit var tvKeteranganTarif: TextView
    private lateinit var etUangBayar: EditText
    private lateinit var tvKembalian: TextView
    private lateinit var rgKendaraan: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val now = Calendar.getInstance()
        jamKeluar = now.get(Calendar.HOUR_OF_DAY)
        menitKeluar = now.get(Calendar.MINUTE)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F0F4F8"))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 36, 36, 36)
        }

        // Header Aplikasi
        val tvTitle = TextView(this).apply {
            text = "Ds Calc — Tarif Parkir"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#102A43"))
        }
        val tvSubtitle = TextView(this).apply {
            text = "Kalkulator Durasi & Biaya Petugas Parkir"
            textSize = 13f
            setTextColor(Color.parseColor("#627D98"))
            setPadding(0, 4, 0, 24)
        }
        root.addView(tvTitle)
        root.addView(tvSubtitle)

        // 1. Pilihan Jenis Kendaraan
        val tvLabelKendaraan = TextView(this).apply {
            text = "1. Jenis Kendaraan"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 8, 0, 12)
        }
        root.addView(tvLabelKendaraan)

        rgKendaraan = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(20, 16, 20, 16)
        }

        val rbMotor = RadioButton(this).apply {
            id = 1
            text = "Motor   "
            textSize = 16f
            isChecked = true
        }
        val rbMobil = RadioButton(this).apply {
            id = 2
            text = "Mobil   "
            textSize = 16f
        }
        val rbTruk = RadioButton(this).apply {
            id = 3
            text = "Truk"
            textSize = 16f
        }

        rgKendaraan.addView(rbMotor)
        rgKendaraan.addView(rbMobil)
        rgKendaraan.addView(rbTruk)
        root.addView(rgKendaraan)

        // 2. Waktu Masuk
        val tvLabelMasuk = TextView(this).apply {
            text = "2. Jam Masuk (Karcis)"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 24, 0, 8)
        }
        root.addView(tvLabelMasuk)

        btnJamMasuk = Button(this).apply {
            text = formatWaktu(jamMasuk, menitMasuk)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setOnClickListener {
                TimePickerDialog(this@MainActivity, { _, hour, minute ->
                    jamMasuk = hour
                    menitMasuk = minute
                    text = formatWaktu(jamMasuk, menitMasuk)
                    hitungParkir()
                }, jamMasuk, menitMasuk, true).show()
            }
        }
        root.addView(btnJamMasuk)

        // 3. Waktu Keluar
        val tvLabelKeluar = TextView(this).apply {
            text = "3. Jam Keluar"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 20, 0, 8)
        }
        root.addView(tvLabelKeluar)

        val rowKeluar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        btnJamKeluar = Button(this).apply {
            text = formatWaktu(jamKeluar, menitKeluar)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                TimePickerDialog(this@MainActivity, { _, hour, minute ->
                    jamKeluar = hour
                    menitKeluar = minute
                    text = formatWaktu(jamKeluar, menitKeluar)
                    hitungParkir()
                }, jamKeluar, menitKeluar, true).show()
            }
        }

        val btnSekarang = Button(this).apply {
            text = "Set Sekarang"
            textSize = 14f
            setOnClickListener {
                val cal = Calendar.getInstance()
                jamKeluar = cal.get(Calendar.HOUR_OF_DAY)
                menitKeluar = cal.get(Calendar.MINUTE)
                btnJamKeluar.text = formatWaktu(jamKeluar, menitKeluar)
                hitungParkir()
            }
        }

        rowKeluar.addView(btnJamKeluar)
        rowKeluar.addView(btnSekarang)
        root.addView(rowKeluar)

        // Kartu Hasil Tagihan
        val cardHasil = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(32, 32, 32, 32)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 28, 0, 20)
            }
            layoutParams = params
        }

        tvHasilDurasi = TextView(this).apply {
            text = "Durasi: 0 jam 0 menit"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#334E68"))
            setPadding(0, 0, 0, 8)
        }

        tvHasilBiaya = TextView(this).apply {
            text = "Rp 0"
            textSize = 32f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1B7F5C"))
            setPadding(0, 0, 0, 12)
        }

        tvKeteranganTarif = TextView(this).apply {
            text = "-"
            textSize = 13f
            setTextColor(Color.parseColor("#627D98"))
        }

        cardHasil.addView(tvHasilDurasi)
        cardHasil.addView(tvHasilBiaya)
        cardHasil.addView(tvKeteranganTarif)
        root.addView(cardHasil)

        // Kalkulator Kembalian Tunai
        val cardKembalian = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(32, 28, 32, 28)
        }

        val tvLabelBayar = TextView(this).apply {
            text = "Hitung Kembalian (Opsional)"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 0, 0, 8)
        }

        etUangBayar = EditText(this).apply {
            hint = "Masukkan uang diterima (misal: 10000)"
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 16f
        }

        tvKembalian = TextView(this).apply {
            text = "Kembalian: Rp 0"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#D97706"))
            setPadding(0, 12, 0, 0)
        }

        cardKembalian.addView(tvLabelBayar)
        cardKembalian.addView(etUangBayar)
        cardKembalian.addView(tvKembalian)
        root.addView(cardKembalian)

        scrollView.addView(root)
        setContentView(scrollView)

        rgKendaraan.setOnCheckedChangeListener { _, _ -> hitungParkir() }

        etUangBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hitungKembalian()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        hitungParkir()
    }

    private fun formatWaktu(jam: Int, menit: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", jam, menit)
    }

    private fun hitungParkir() {
        val totalMenitMasuk = jamMasuk * 60 + menitMasuk
        var totalMenitKeluar = jamKeluar * 60 + menitKeluar

        // Dukungan lintas tengah malam (contoh masuk 22:00 keluar 04:00)
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
            selisihMenit <= 60 -> tarifJamPertama to "Tarif Jam Pertama (≤ 1 jam)"
            selisihMenit <= 120 -> tarifLebih1Jam to "Tarif > 1 Jam (1–2 jam)"
            else -> tarifMaksimal to "Tarif Maksimal (> 2 jam)"
        }

        totalBiayaSaatIni = biayaAkhir

        val rupiahFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        tvHasilDurasi.text = "Durasi Parkir: $durasiJam jam $sisaMenit menit"
        tvHasilBiaya.text = "Rp ${rupiahFormat.format(biayaAkhir)}"
        tvKeteranganTarif.text = "$namaKendaraan • $statusTier\n" +
                "Skema: Jam ke-1 Rp ${rupiahFormat.format(tarifJamPertama)} | >1 Jam Rp ${rupiahFormat.format(tarifLebih1Jam)} | Maks Rp ${rupiahFormat.format(tarifMaksimal)}"

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

    data class TarifParkir(
        val nama: String,
        val jamPertama: Int,
        val lebihSatuJam: Int,
        val maksimal: Int
    )
}
