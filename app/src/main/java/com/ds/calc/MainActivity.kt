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

    // State Tab 1: Kalkulator Satuan
    private var jamMasuk = 8
    private var menitMasuk = 0
    private var jamKeluar = 17
    private var menitKeluar = 0
    private var totalBiayaSaatIni = 0
    private var durasiTeksSaatIni = ""
    private var kendaraanSaatIni = "Motor"

    // Navigasi 3 Tab
    private lateinit var btnTabHitung: Button
    private lateinit var btnTabRekapan: Button
    private lateinit var btnTabLaporan: Button
    private lateinit var layoutHitung: LinearLayout
    private lateinit var layoutRekapan: LinearLayout
    private lateinit var layoutLaporan: LinearLayout

    // UI Tab 1: Hitung Satuan
    private lateinit var btnJamMasuk: Button
    private lateinit var btnJamKeluar: Button
    private lateinit var etPlatNomor: EditText
    private lateinit var tvHasilDurasi: TextView
    private lateinit var tvHasilBiaya: TextView
    private lateinit var tvKeteranganTarif: TextView
    private lateinit var etUangBayar: EditText
    private lateinit var tvKembalian: TextView
    private lateinit var rgKendaraan: RadioGroup

    // UI Tab 2: Kalkulator Rekapan
    private lateinit var etMotor3k: EditText
    private lateinit var etMotor5k: EditText
    private lateinit var etMotor7k: EditText
    private lateinit var etMobil5k: EditText
    private lateinit var etMobil8k: EditText
    private lateinit var etMobil10k: EditText
    private lateinit var etTruk6k: EditText
    private lateinit var etTruk9k: EditText
    private lateinit var etTruk15k: EditText
    private lateinit var etMember0k: EditText
    private lateinit var etFisikRekapan: EditText
    private lateinit var tvPreviewRekapan: TextView

    // UI Tab 3: Laporan Riwayat Satuan
    private lateinit var tvRingkasanLaporan: TextView
    private lateinit var etUangFisikLaporan: EditText
    private lateinit var tvSelisihLaporan: TextView
    private lateinit var containerRiwayat: LinearLayout
    private var totalRekapLaporanSaatIni = 0

    private val PREF_NAME = "ds_calc_laporan_pref"
    private val KEY_DATA = "riwayat_parkir_json"
    private val KEY_REKAPAN_DRAFT = "rekapan_draft_json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val now = Calendar.getInstance()
        jamKeluar = now.get(Calendar.HOUR_OF_DAY)
        menitKeluar = now.get(Calendar.MINUTE)

        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F0F4F8"))
        }

        // --- TAB BAR (3 TAB) ---
        val tabBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#102A43"))
            setPadding(12, 14, 12, 14)
        }

        btnTabHitung = Button(this).apply {
            text = "HITUNG"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 6, 0)
            }
            setOnClickListener { tampilkanTab(0) }
        }

        btnTabRekapan = Button(this).apply {
            text = "REKAPAN"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(6, 0, 6, 0)
            }
            setOnClickListener { tampilkanTab(1) }
        }

        btnTabLaporan = Button(this).apply {
            text = "LAPORAN"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(6, 0, 0, 0)
            }
            setOnClickListener { tampilkanTab(2) }
        }

        tabBar.addView(btnTabHitung)
        tabBar.addView(btnTabRekapan)
        tabBar.addView(btnTabLaporan)
        mainContainer.addView(tabBar)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }

        val contentWrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 36)
        }

        buildLayoutHitung()
        buildLayoutRekapan()
        buildLayoutLaporan()

        contentWrapper.addView(layoutHitung)
        contentWrapper.addView(layoutRekapan)
        contentWrapper.addView(layoutLaporan)
        scrollView.addView(contentWrapper)
        mainContainer.addView(scrollView)
        setContentView(mainContainer)

        muatDraftRekapan()
        hitungRekapanCepat()
        tampilkanTab(0)
        hitungParkir()
    }

    private fun tampilkanTab(index: Int) {
        layoutHitung.visibility = if (index == 0) View.VISIBLE else View.GONE
        layoutRekapan.visibility = if (index == 1) View.VISIBLE else View.GONE
        layoutLaporan.visibility = if (index == 2) View.VISIBLE else View.GONE

        val aktifBg = Color.parseColor("#2B6CB0")
        val nonAktifBg = Color.parseColor("#334E68")
        val aktifTxt = Color.WHITE
        val nonAktifTxt = Color.parseColor("#BCCCDC")

        btnTabHitung.setBackgroundColor(if (index == 0) aktifBg else nonAktifBg)
        btnTabHitung.setTextColor(if (index == 0) aktifTxt else nonAktifTxt)

        btnTabRekapan.setBackgroundColor(if (index == 1) aktifBg else nonAktifBg)
        btnTabRekapan.setTextColor(if (index == 1) aktifTxt else nonAktifTxt)

        btnTabLaporan.setBackgroundColor(if (index == 2) aktifBg else nonAktifBg)
        btnTabLaporan.setTextColor(if (index == 2) aktifTxt else nonAktifTxt)

        if (index == 1) hitungRekapanCepat()
        if (index == 2) muatDataLaporan()
    }

    // =========================================================================
    // TAB 1: KALKULATOR SATUAN (JAM MASUK - KELUAR)
    // =========================================================================
    private fun buildLayoutHitung() {
        layoutHitung = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val tvTitle = TextView(this).apply {
            text = "Ds Calc — Hitung Satuan"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#102A43"))
            setPadding(0, 0, 0, 14)
        }
        layoutHitung.addView(tvTitle)

        val tvLabelKendaraan = TextView(this).apply {
            text = "1. Jenis Kendaraan / Karcis"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 4, 0, 8)
        }
        layoutHitung.addView(tvLabelKendaraan)

        rgKendaraan = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(20, 12, 20, 12)
        }

        val rbMotor = RadioButton(this).apply { id = 1; text = "Motor"; textSize = 15f; isChecked = true }
        val rbMobil = RadioButton(this).apply { id = 2; text = "Mobil"; textSize = 15f }
        val rbTruk = RadioButton(this).apply { id = 3; text = "Truk"; textSize = 15f }
        val rbMember = RadioButton(this).apply { id = 4; text = "Member (Rp 0)"; textSize = 15f }

        rgKendaraan.addView(rbMotor)
        rgKendaraan.addView(rbMobil)
        rgKendaraan.addView(rbTruk)
        rgKendaraan.addView(rbMember)
        layoutHitung.addView(rgKendaraan)

        val tvLabelMasuk = TextView(this).apply {
            text = "2. Jam Masuk & Jam Keluar"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 18, 0, 8)
        }
        layoutHitung.addView(tvLabelMasuk)

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
        layoutHitung.addView(rowJam)

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
        layoutHitung.addView(btnSekarang)

        val cardHasil = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 22, 28, 22)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 18, 0, 14) }
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
            setPadding(0, 0, 0, 6)
        }

        tvKeteranganTarif = TextView(this).apply {
            text = "-"
            textSize = 12f
            setTextColor(Color.parseColor("#627D98"))
        }

        cardHasil.addView(tvHasilDurasi)
        cardHasil.addView(tvHasilBiaya)
        cardHasil.addView(tvKeteranganTarif)
        layoutHitung.addView(cardHasil)

        val cardSimpan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 18, 28, 22)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 14) }
        }

        etPlatNomor = EditText(this).apply {
            hint = "Plat Nomor / Catatan (Opsional)"
            textSize = 14f
        }

        val btnSimpanLaporan = Button(this).apply {
            text = "+ SIMPAN KE TAB LAPORAN"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#1B7F5C"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 10, 0, 0) }
            setOnClickListener { simpanTransaksiKeLaporan() }
        }

        cardSimpan.addView(etPlatNomor)
        cardSimpan.addView(btnSimpanLaporan)
        layoutHitung.addView(cardSimpan)

        val cardKembalian = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 18, 28, 18)
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
        layoutHitung.addView(cardKembalian)

        rgKendaraan.setOnCheckedChangeListener { _, _ -> hitungParkir() }
        etUangBayar.addTextChangedListener(buatTextWatcher { hitungKembalian() })
    }

    // =========================================================================
    // TAB 2: KALKULATOR REKAPAN CEPAT (INPUT LEMBAR KARCIS PER TARIF)
    // =========================================================================
    private fun buildLayoutRekapan() {
        layoutRekapan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val tvTitle = TextView(this).apply {
            text = "Kalkulator Rekapan Karcis"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#102A43"))
        }
        val tvSub = TextView(this).apply {
            text = "Isi jumlah lembar karcis per tarif untuk menghitung setoran:"
            textSize = 13f
            setTextColor(Color.parseColor("#627D98"))
            setPadding(0, 4, 0, 16)
        }
        layoutRekapan.addView(tvTitle)
        layoutRekapan.addView(tvSub)

        val cardMotor = buatKartuKategoriRekap("MOTOR")
        etMotor3k = tambahBarisInputTarif(cardMotor, "Tarif 3rb (Jam ke-1)")
        etMotor5k = tambahBarisInputTarif(cardMotor, "Tarif 5rb (> 1 Jam)")
        etMotor7k = tambahBarisInputTarif(cardMotor, "Tarif 7rb (Maksimal)")
        layoutRekapan.addView(cardMotor)

        val cardMobil = buatKartuKategoriRekap("MOBIL")
        etMobil5k = tambahBarisInputTarif(cardMobil, "Tarif 5rb (Jam ke-1)")
        etMobil8k = tambahBarisInputTarif(cardMobil, "Tarif 8rb (> 1 Jam)")
        etMobil10k = tambahBarisInputTarif(cardMobil, "Tarif 10rb (Maksimal)")
        layoutRekapan.addView(cardMobil)

        val cardTruk = buatKartuKategoriRekap("TRUK")
        etTruk6k = tambahBarisInputTarif(cardTruk, "Tarif 6rb (Jam ke-1)")
        etTruk9k = tambahBarisInputTarif(cardTruk, "Tarif 9rb (> 1 Jam)")
        etTruk15k = tambahBarisInputTarif(cardTruk, "Tarif 15rb (Maksimal)")
        layoutRekapan.addView(cardTruk)

        val cardMember = buatKartuKategoriRekap("MEMBER (Tarif Rp 0)")
        etMember0k = tambahBarisInputTarif(cardMember, "Lembar Karcis Member")
        layoutRekapan.addView(cardMember)

        val cardFisik = buatKartuKategoriRekap("UANG FISIK DI TANGAN")
        etFisikRekapan = EditText(this).apply {
            hint = "Masukkan total uang tunai fisik (misal: 250000)"
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 16f
        }
        cardFisik.addView(etFisikRekapan)
        layoutRekapan.addView(cardFisik)

        val cardPreview = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 24, 28, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 16) }
        }

        tvPreviewRekapan = TextView(this).apply {
            text = "-"
            textSize = 13.5f
            typeface = Typeface.MONOSPACE
            setTextColor(Color.parseColor("#102A43"))
        }
        cardPreview.addView(tvPreviewRekapan)
        layoutRekapan.addView(cardPreview)

        val rowKirim = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 10) }
        }

        val btnKirimJpg = Button(this).apply {
            text = "Kirim Gambar (JPG)"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#1B7F5C"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener {
                val teks = hitungRekapanCepat()
                kirimTeksSebagaiGambarJpg(teks, "Rekap_Karcis_DsCalc.jpg")
            }
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
            setOnClickListener {
                val teks = hitungRekapanCepat()
                salinDanBagikanTeks(teks)
            }
        }

        rowKirim.addView(btnKirimJpg)
        rowKirim.addView(btnKirimTeks)
        layoutRekapan.addView(rowKirim)

        val btnResetRekapan = Button(this).apply {
            text = "Reset / Kosongkan Angka Rekapan"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#C53030"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { konfirmasiResetRekapan() }
        }
        layoutRekapan.addView(btnResetRekapan)

        val semuaInput = listOf(
            etMotor3k, etMotor5k, etMotor7k,
            etMobil5k, etMobil8k, etMobil10k,
            etTruk6k, etTruk9k, etTruk15k,
            etMember0k, etFisikRekapan
        )
        val watcher = buatTextWatcher {
            hitungRekapanCepat()
            simpanDraftRekapan()
        }
        for (et in semuaInput) {
            et.addTextChangedListener(watcher)
        }
    }

    private fun buatKartuKategoriRekap(judul: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(24, 18, 24, 18)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 14) }

            val tvHeader = TextView(this@MainActivity).apply {
                text = judul
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#2B6CB0"))
                setPadding(0, 0, 0, 8)
            }
            addView(tvHeader)
        }
    }

    private fun tambahBarisInputTarif(parent: LinearLayout, label: String): EditText {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 4, 0, 4)
        }

        val tvLabel = TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.parseColor("#243B53"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f)
        }

        val etJumlah = EditText(this).apply {
            hint = "0 Lbr"
            inputType = InputType.TYPE_CLASS_NUMBER
            gravity = Gravity.END
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        row.addView(tvLabel)
        row.addView(etJumlah)
        parent.addView(row)
        return etJumlah
    }

    private fun bacaInt(et: EditText): Int {
        return et.text.toString().trim().toIntOrNull() ?: 0
    }

    private fun formatStatusSetoran(uangFisik: Int, totalRekap: Int): String {
        val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))
        val selisih = uangFisik - totalRekap
        return when {
            uangFisik == 0 && totalRekap == 0 -> "Rp 0"
            uangFisik == 0 -> "-Rp ${nf.format(totalRekap)} (Uang fisik belum diisi)"
            selisih == 0 -> "PAS (Rp 0)"
            selisih > 0 -> "PLUS +Rp ${nf.format(selisih)}"
            else -> "MINUS -Rp ${nf.format(-selisih)}"
        }
    }

    private fun hitungRekapanCepat(): String {
        val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))

        val m3 = bacaInt(etMotor3k); val m5 = bacaInt(etMotor5k); val m7 = bacaInt(etMotor7k)
        val totLbrMotor = m3 + m5 + m7
        val totRpMotor = (m3 * 3000) + (m5 * 5000) + (m7 * 7000)

        val mb5 = bacaInt(etMobil5k); val mb8 = bacaInt(etMobil8k); val mb10 = bacaInt(etMobil10k)
        val totLbrMobil = mb5 + mb8 + mb10
        val totRpMobil = (mb5 * 5000) + (mb8 * 8000) + (mb10 * 10000)

        val t6 = bacaInt(etTruk6k); val t9 = bacaInt(etTruk9k); val t15 = bacaInt(etTruk15k)
        val totLbrTruk = t6 + t9 + t15
        val totRpTruk = (t6 * 6000) + (t9 * 9000) + (t15 * 15000)

        val mem0 = bacaInt(etMember0k)

        val totalLembarSemua = totLbrMotor + totLbrMobil + totLbrTruk + mem0
        val totalRekapRp = totRpMotor + totRpMobil + totRpTruk
        val uangFisik = bacaInt(etFisikRekapan)

        val sb = StringBuilder()
        val tglCetak = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        sb.append("REKAPAN KARCIS PARKIR - DS CALC\n")
        sb.append("Waktu: $tglCetak\n")
        sb.append("================================\n\n")

        if (totLbrMotor > 0) {
            sb.append("Motor Total $totLbrMotor Lembar karcis\n")
            if (m3 > 0) sb.append("  Tarif 3k  = ${m3}L = ${formatSingkatRibuan(m3 * 3000)}\n")
            if (m5 > 0) sb.append("  Tarif 5k  = ${m5}L = ${formatSingkatRibuan(m5 * 5000)}\n")
            if (m7 > 0) sb.append("  Tarif 7k  = ${m7}L = ${formatSingkatRibuan(m7 * 7000)}\n")
            sb.append("  Subtotal Motor = Rp ${nf.format(totRpMotor)}\n\n")
        }

        if (totLbrMobil > 0) {
            sb.append("Mobil Total $totLbrMobil Lembar karcis\n")
            if (mb5 > 0) sb.append("  Tarif 5k  = ${mb5}L = ${formatSingkatRibuan(mb5 * 5000)}\n")
            if (mb8 > 0) sb.append("  Tarif 8k  = ${mb8}L = ${formatSingkatRibuan(mb8 * 8000)}\n")
            if (mb10 > 0) sb.append("  Tarif 10k = ${mb10}L = ${formatSingkatRibuan(mb10 * 10000)}\n")
            sb.append("  Subtotal Mobil = Rp ${nf.format(totRpMobil)}\n\n")
        }

        if (totLbrTruk > 0) {
            sb.append("Truk Total $totLbrTruk Lembar karcis\n")
            if (t6 > 0) sb.append("  Tarif 6k  = ${t6}L = ${formatSingkatRibuan(t6 * 6000)}\n")
            if (t9 > 0) sb.append("  Tarif 9k  = ${t9}L = ${formatSingkatRibuan(t9 * 9000)}\n")
            if (t15 > 0) sb.append("  Tarif 15k = ${t15}L = ${formatSingkatRibuan(t15 * 15000)}\n")
            sb.append("  Subtotal Truk = Rp ${nf.format(totRpTruk)}\n\n")
        }

        if (mem0 > 0) {
            sb.append("Member Total $mem0 Lembar\n")
            sb.append("  Tarif 0 karena member\n\n")
        }

        if (totalLembarSemua == 0) {
            sb.append("(Belum ada jumlah lembar diisi)\n\n")
        }

        val infoSelisih = formatStatusSetoran(uangFisik, totalRekapRp)

        sb.append("================================\n")
        sb.append("Total Karcis     = $totalLembarSemua Lembar\n")
        sb.append("Total Rekap      = Rp ${nf.format(totalRekapRp)}\n")
        sb.append("Total Uang Fisik = Rp ${nf.format(uangFisik)}\n")
        sb.append("Selisih Setoran  = $infoSelisih")

        val hasil = sb.toString()
        tvPreviewRekapan.text = hasil
        return hasil
    }

    private fun simpanDraftRekapan() {
        val obj = JSONObject().apply {
            put("m3", etMotor3k.text.toString())
            put("m5", etMotor5k.text.toString())
            put("m7", etMotor7k.text.toString())
            put("mb5", etMobil5k.text.toString())
            put("mb8", etMobil8k.text.toString())
            put("mb10", etMobil10k.text.toString())
            put("t6", etTruk6k.text.toString())
            put("t9", etTruk9k.text.toString())
            put("t15", etTruk15k.text.toString())
            put("mem0", etMember0k.text.toString())
            put("fisik", etFisikRekapan.text.toString())
        }
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_REKAPAN_DRAFT, obj.toString())
            .apply()
    }

    private fun muatDraftRekapan() {
        val str = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REKAPAN_DRAFT, null) ?: return
        try {
            val obj = JSONObject(str)
            etMotor3k.setText(obj.optString("m3", ""))
            etMotor5k.setText(obj.optString("m5", ""))
            etMotor7k.setText(obj.optString("m7", ""))
            etMobil5k.setText(obj.optString("mb5", ""))
            etMobil8k.setText(obj.optString("mb8", ""))
            etMobil10k.setText(obj.optString("mb10", ""))
            etTruk6k.setText(obj.optString("t6", ""))
            etTruk9k.setText(obj.optString("t9", ""))
            etTruk15k.setText(obj.optString("t15", ""))
            etMember0k.setText(obj.optString("mem0", ""))
            etFisikRekapan.setText(obj.optString("fisik", ""))
        } catch (_: Exception) {
        }
    }

    private fun konfirmasiResetRekapan() {
        AlertDialog.Builder(this)
            .setTitle("Kosongkan Rekapan?")
            .setMessage("Semua angka lembar karcis dan uang fisik di tab Rekapan akan direset ke 0.")
            .setPositiveButton("Reset") { _, _ ->
                listOf(
                    etMotor3k, etMotor5k, etMotor7k,
                    etMobil5k, etMobil8k, etMobil10k,
                    etTruk6k, etTruk9k, etTruk15k,
                    etMember0k, etFisikRekapan
                ).forEach { it.setText("") }
                simpanDraftRekapan()
                hitungRekapanCepat()
                Toast.makeText(this, "Angka rekapan direset", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // =========================================================================
    // TAB 3: LAPORAN RIWAYAT KARCIS SATUAN
    // =========================================================================
    private fun buildLayoutLaporan() {
        layoutLaporan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val tvJudulLaporan = TextView(this).apply {
            text = "Laporan Riwayat Satuan"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#102A43"))
            setPadding(0, 0, 0, 14)
        }
        layoutLaporan.addView(tvJudulLaporan)

        val cardRingkasan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(28, 22, 28, 22)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 14) }
        }

        tvRingkasanLaporan = TextView(this).apply {
            text = "Memuat laporan..."
            textSize = 13.5f
            typeface = Typeface.MONOSPACE
            setTextColor(Color.parseColor("#102A43"))
        }
        cardRingkasan.addView(tvRingkasanLaporan)

        val divider = View(this).apply {
            setBackgroundColor(Color.parseColor("#D9E2EC"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply {
                setMargins(0, 18, 0, 14)
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

        etUangFisikLaporan = EditText(this).apply {
            hint = "Masukkan total uang fisik"
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 16f
        }
        cardRingkasan.addView(etUangFisikLaporan)

        tvSelisihLaporan = TextView(this).apply {
            text = "Selisih Setoran: Rp 0"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1B7F5C"))
            setPadding(0, 8, 0, 0)
        }
        cardRingkasan.addView(tvSelisihLaporan)
        layoutLaporan.addView(cardRingkasan)

        val rowKirim = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 10) }
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
            setOnClickListener {
                val teks = susunTeksRekapLaporan()
                kirimTeksSebagaiGambarJpg(teks, "Laporan_Satuan_DsCalc.jpg")
            }
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
            setOnClickListener {
                val teks = susunTeksRekapLaporan()
                salinDanBagikanTeks(teks)
            }
        }

        rowKirim.addView(btnKirimGambar)
        rowKirim.addView(btnKirimTeks)
        layoutLaporan.addView(rowKirim)

        val btnResetLaporan = Button(this).apply {
            text = "Hapus Semua Riwayat Satuan"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#C53030"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 18) }
            setOnClickListener { konfirmasiHapusSemuaLaporan() }
        }
        layoutLaporan.addView(btnResetLaporan)

        val tvHeaderRiwayat = TextView(this).apply {
            text = "Rincian Lembar Karcis Tersimpan:"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#243B53"))
            setPadding(0, 0, 0, 10)
        }
        layoutLaporan.addView(tvHeaderRiwayat)

        containerRiwayat = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layoutLaporan.addView(containerRiwayat)

        etUangFisikLaporan.addTextChangedListener(buatTextWatcher { updateSelisihLaporan() })
    }

    // =========================================================================
    // LOGIKA PERHITUNGAN & HELPER
    // =========================================================================
    private fun buatTextWatcher(aksi: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { aksi() }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun formatWaktu(jam: Int, menit: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", jam, menit)
    }

    private fun formatSingkatRibuan(nominal: Int): String {
        return if (nominal >= 1000 && nominal % 1000 == 0) {
            "${nominal / 1000}rb"
        } else {
            val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))
            "Rp ${nf.format(nominal)}"
        }
    }

    private fun hitungParkir() {
        val totalMenitMasuk = jamMasuk * 60 + menitMasuk
        var totalMenitKeluar = jamKeluar * 60 + menitKeluar

        if (totalMenitKeluar < totalMenitMasuk) {
            totalMenitKeluar += 24 * 60
        }

        var selisihMenit = totalMenitKeluar - totalMenitMasuk
        if (selisihMenit == 0) selisihMenit = 1

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

    private fun susunTeksRekapLaporan(): String {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))
        val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))

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

        totalRekapLaporanSaatIni = grandRekap

        val sb = StringBuilder()
        val tglCetak = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        sb.append("LAPORAN SATUAN PARKIR - DS CALC\n")
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

        val uangFisik = bacaInt(etUangFisikLaporan)
        val infoSelisih = formatStatusSetoran(uangFisik, grandRekap)

        sb.append("================================\n")
        sb.append("Total Karcis     = $totalLembarSemua Lembar\n")
        sb.append("Total Rekap      = Rp ${nf.format(grandRekap)}\n")
        sb.append("Total Uang Fisik = Rp ${nf.format(uangFisik)}\n")
        sb.append("Selisih Setoran  = $infoSelisih")

        return sb.toString()
    }

    private fun updateSelisihLaporan() {
        val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))
        val uangFisik = bacaInt(etUangFisikLaporan)
        val selisih = uangFisik - totalRekapLaporanSaatIni

        when {
            uangFisik == 0 -> {
                tvSelisihLaporan.text = "Selisih Setoran: Rp 0"
                tvSelisihLaporan.setTextColor(Color.parseColor("#627D98"))
            }
            selisih == 0 -> {
                tvSelisihLaporan.text = "Selisih Setoran: PAS (Rp 0)"
                tvSelisihLaporan.setTextColor(Color.parseColor("#1B7F5C"))
            }
            selisih > 0 -> {
                tvSelisihLaporan.text = "Selisih Setoran: PLUS +Rp ${nf.format(selisih)}"
                tvSelisihLaporan.setTextColor(Color.parseColor("#2B6CB0"))
            }
            else -> {
                tvSelisihLaporan.text = "Selisih Setoran: MINUS -Rp ${nf.format(-selisih)}"
                tvSelisihLaporan.setTextColor(Color.parseColor("#C53030"))
            }
        }
        tvRingkasanLaporan.text = susunTeksRekapLaporan()
    }

    private fun muatDataLaporan() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_DATA, "[]"))
        val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))

        containerRiwayat.removeAllViews()

        if (jsonArray.length() == 0) {
            totalRekapLaporanSaatIni = 0
            tvRingkasanLaporan.text = "Belum ada karcis satuan tersimpan.\nGunakan tab HITUNG untuk menyimpan karcis satu per satu."
            updateSelisihLaporan()
            return
        }

        tvRingkasanLaporan.text = susunTeksRekapLaporan()
        updateSelisihLaporan()

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
                setPadding(24, 18, 24, 18)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 10) }
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
            if (obj.getLong("id") != targetId) baru.put(obj)
        }
        prefs.edit().putString(KEY_DATA, baru.toString()).apply()
        muatDataLaporan()
        Toast.makeText(this, "Karcis dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun konfirmasiHapusSemuaLaporan() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Riwayat Satuan?")
            .setMessage("Semua data karcis satuan yang tersimpan akan dihapus permanen.")
            .setPositiveButton("Hapus") { _, _ ->
                getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_DATA, "[]")
                    .apply()
                etUangFisikLaporan.setText("")
                muatDataLaporan()
                Toast.makeText(this, "Riwayat dibersihkan", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun salinDanBagikanTeks(teks: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Rekap Parkir", teks))
        Toast.makeText(this, "Teks rekap disalin ke clipboard!", Toast.LENGTH_SHORT).show()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, teks)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Kirim Teks Laporan via"))
    }

    private fun kirimTeksSebagaiGambarJpg(teks: String, namaFile: String) {
        val barisList = teks.split("\n")

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#102A43")
            textSize = 34f
            typeface = Typeface.MONOSPACE
        }

        val padding = 60
        val lineHeight = 48
        val width = 1220
        val height = (barisList.size * lineHeight) + (padding * 2) + 40

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint().apply {
            color = Color.parseColor("#BCCCDC")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(24f, 24f, (width - 24).toFloat(), (height - 24).toFloat(), borderPaint)

        var yPos = padding + 36f
        for ((index, baris) in barisList.withIndex()) {
            if (index == 0 ||
                baris.startsWith("Total") ||
                baris.startsWith("Selisih Setoran") ||
                baris.contains("Total ")
            ) {
                paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            } else {
                paint.typeface = Typeface.MONOSPACE
            }

            if (baris.contains("MINUS")) {
                paint.color = Color.parseColor("#C53030")
            } else if (baris.contains("PLUS") || baris.contains("PAS")) {
                paint.color = Color.parseColor("#1B7F5C")
            } else {
                paint.color = Color.parseColor("#102A43")
            }

            canvas.drawText(baris, padding.toFloat(), yPos, paint)
            yPos += lineHeight
        }

        try {
            val cacheFolder = File(cacheDir, "laporan_images")
            if (!cacheFolder.exists()) cacheFolder.mkdirs()

            val fileJpg = File(cacheFolder, namaFile)
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
