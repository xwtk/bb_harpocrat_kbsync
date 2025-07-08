package hardware.xwtk.harpocrat.kbsync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class HarpocratKBSyncService : Service() {
    private val TAG = "HarpocratKBS"
    @Volatile
    private var running = false
    private var logcatThread: Thread? = null
	
	private val availableLayouts = setOf(
        "keyboard_qwerty_en_US",
        "keyboard_qwerty_en_GB",
        "keyboard_qwerty_en_AU",
        "keyboard_qwerty_en_CA",
        "keyboard_qwerty_en_IE",
        "keyboard_qwerty_en_IN",
        "keyboard_qwerty_en_NZ",
        "keyboard_qwerty_en_SG",
        "keyboard_qwerty_en_ZA",
        "keyboard_qwerty_af",
        "keyboard_qwerty_az_AZ",
        "keyboard_qwerty_bs",
        "keyboard_qwerty_ca",
        "keyboard_qwerty_cy",
        "keyboard_qwerty_da",
        "keyboard_qwerty_es",
        "keyboard_qwerty_es_US",
        "keyboard_qwerty_es_419",
        "keyboard_qwerty_et_EE",
        "keyboard_qwerty_eu_ES",
        "keyboard_qwerty_es_MX",
        "keyboard_qwerty_fi",
        "keyboard_qwerty_fil",
        "keyboard_qwerty_ga",
        "keyboard_qwerty_gl_ES",
        "keyboard_qwerty_in",
        "keyboard_qwerty_is",
        "keyboard_qwerty_it",
        "keyboard_qwerty_nb",
        "keyboard_qwerty_nl",
        "keyboard_qwerty_pl",
        "keyboard_qwerty_pt_BR",
        "keyboard_qwerty_pt_PT",
        "keyboard_qwerty_ro",
        "keyboard_qwerty_sq",
        "keyboard_qwerty_su",
        "keyboard_qwerty_sv",
        "keyboard_qwerty_tr",
        "keyboard_qwertz_de",
        "keyboard_qwertz_at",
        "keyboard_qwertz_ch_de",
        "keyboard_qwertz_cs",
        "keyboard_qwertz_sk",
        "keyboard_qwertz_hu",
        "keyboard_qwertz_hr",
        "keyboard_qwertz_sl",
        "keyboard_qwertz_sr",
        "keyboard_azerty_fr",
        "keyboard_azerty_fr_CA",
        "keyboard_qwerty_ja",
        "keyboard_azerty_ja",
        "keyboard_qwertz_ja",
        "keyboard_qwerty_zh_CN_stroke",
        "keyboard_azerty_zh_CN_stroke",
        "keyboard_qwertz_zh_CN_stroke",
        "keyboard_qwerty_zh_TW_zhuyin",
        "keyboard_azerty_zh_TW_zhuyin",
        "keyboard_qwertz_zh_TW_zhuyin",
        "keyboard_qwerty_ar",
        "keyboard_azerty_ar",
        "keyboard_qwertz_ar",
        "keyboard_qwerty_be",
        "keyboard_azerty_be",
        "keyboard_qwertz_be",
        "keyboard_qwerty_bg",
        "keyboard_azerty_bg",
        "keyboard_qwertz_bg",
        "keyboard_qwerty_ru",
        "keyboard_azerty_ru",
        "keyboard_qwertz_ru",
        "keyboard_qwerty_ru_jcuken",
        "keyboard_azerty_ru_jcuken",
        "keyboard_qwertz_ru_jcuken",
        "keyboard_qwerty_uk",
        "keyboard_azerty_uk",
        "keyboard_qwertz_uk",
        "keyboard_qwerty_el",
        "keyboard_azerty_el",
        "keyboard_qwertz_el",
        "keyboard_qwerty_iw",
        "keyboard_azerty_iw",
        "keyboard_qwertz_iw",
        "keyboard_qwerty_hy_AM",
        "keyboard_azerty_hy_AM",
        "keyboard_qwertz_hy_AM",
        "keyboard_qwerty_bn_IN",
        "keyboard_azerty_bn_IN",
        "keyboard_qwertz_bn_IN",
        "keyboard_qwerty_hi",
        "keyboard_azerty_hi",
        "keyboard_qwertz_hi",
        "keyboard_qwerty_kn_IN",
        "keyboard_azerty_kn_IN",
        "keyboard_qwertz_kn_IN",
        "keyboard_qwerty_ml_IN",
        "keyboard_azerty_ml_IN",
        "keyboard_qwertz_ml_IN",
        "keyboard_qwerty_mr_IN",
        "keyboard_azerty_mr_IN",
        "keyboard_qwertz_mr_IN",
        "keyboard_qwerty_ta_IN",
        "keyboard_azerty_ta_IN",
        "keyboard_qwertz_ta_IN",
        "keyboard_qwerty_te_IN",
        "keyboard_azerty_te_IN",
        "keyboard_qwertz_te_IN",
        "keyboard_qwerty_km_KH",
        "keyboard_azerty_km_KH",
        "keyboard_qwertz_km_KH",
        "keyboard_qwerty_lo_LA",
        "keyboard_azerty_lo_LA",
        "keyboard_qwertz_lo_LA",
        "keyboard_qwerty_th",
        "keyboard_azerty_th",
        "keyboard_qwertz_th",
        "keyboard_qwerty_mn_MN",
        "keyboard_azerty_mn_MN",
        "keyboard_qwertz_mn_MN",
        "keyboard_qwerty_ms_MY",
        "keyboard_azerty_ms_MY",
        "keyboard_qwertz_ms_MY",
        "keyboard_qwerty_vi",
        "keyboard_qwertz_vi"
    )

    override fun onCreate() {
        super.onCreate()
        running = true
        startLogcatMonitor()
    }

    override fun onDestroy() {
        running = false
        logcatThread?.interrupt()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLogcatMonitor() {
        logcatThread = Thread {
            try {
                val proc = Runtime.getRuntime().exec(arrayOf("logcat", "-v", "brief", "LPM:I", "*:S"))
                val reader = BufferedReader(InputStreamReader(proc.inputStream))
                while (running) {
                    val line = reader.readLine() ?: break
                    if ("Changing locale to" in line) {
                        if (!isBBKeyboardActive()) {
                            continue
                        }
                        val lang = line.substringAfter("Changing locale to").trim().split(" ")[0]
                        Log.i(TAG, "Synchronizing hardware keyboard layout to $lang")
                        applyBestLayoutFor(lang)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Logcat monitor failed", e)
            }
        }.also { it.start() }
    }
    

    private fun isBBKeyboardActive(): Boolean {
        val ime = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return ime?.contains("com.blackberry.keyboard") == true
    }

    // Reflection-based method to find the keyboard InputDeviceIdentifier
    private fun findKeyboardIdentifier(): Any? {
        try {
            val inputDeviceClass = Class.forName("android.view.InputDevice")
            val getDeviceIdsMethod = inputDeviceClass.getMethod("getDeviceIds")
            val getDeviceMethod = inputDeviceClass.getMethod("getDevice", Int::class.javaPrimitiveType)

            val deviceIds = getDeviceIdsMethod.invoke(null) as IntArray
            for (id in deviceIds) {
                val device = getDeviceMethod.invoke(null, id)

                // Access vendorId, productId, name fields via reflection
                val vendorId = inputDeviceClass.getDeclaredField("mVendorId").let { f ->
                    f.isAccessible = true
                    f.getInt(device)
                }
                val productId = inputDeviceClass.getDeclaredField("mProductId").let { f ->
                    f.isAccessible = true
                    f.getInt(device)
                }
                val name = inputDeviceClass.getMethod("getName").invoke(device) as String

                if (vendorId == 1 && productId == 1 && name == "qpnp_keypad") {
                    // Get identifier property (InputDeviceIdentifier)
                    val identifier = inputDeviceClass.getMethod("getIdentifier").invoke(device)
                    return identifier
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find keyboard identifier", e)
        }
        return null
    }

    private fun applyBestLayoutFor(lang: String) {
        val base = getSystemProperty("ro.keyboardlayout", "qwerty").takeIf {
            it in setOf("qwerty", "qwertz", "azerty")
        } ?: "qwerty"

        val variants = listOf(base) + listOf("qwerty", "qwertz", "azerty").filter { it != base }

        for (variant in variants) {
            val layoutName = "keyboard_${variant}_$lang"
            if (layoutName in availableLayouts) {
                if (setKeyboardLayout("com.blackberry.keyboard/com.blackberry.inputmethod.inputdevice.InputDeviceReceiver/$layoutName")) {
                    Log.i(TAG, "Applied layout: $layoutName")
                    return
                }
            } else {
                Log.w(TAG, "Layout $layoutName not available")
            }
        }
        Log.w(TAG, "No available layout found for locale '$lang'")
    }

    private fun getSystemProperty(key: String, defaultValue: String): String {
        return try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java, String::class.java)
            getMethod.invoke(null, key, defaultValue) as String
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read system property $key", e)
            defaultValue
        }
    }

    private fun setKeyboardLayout(layoutName: String): Boolean {
        try {
            val inputManager = getSystemService(INPUT_SERVICE) ?: run {
                Log.e(TAG, "InputManager service not found")
                return false
            }

            val inputManagerClass = inputManager.javaClass

            // Find setCurrentKeyboardLayoutForInputDevice method with two parameters
            val setLayoutMethod = inputManagerClass.getMethod(
                "setCurrentKeyboardLayoutForInputDevice",
                Class.forName("android.hardware.input.InputDeviceIdentifier"),
                String::class.java
            )

            val identifier = findKeyboardIdentifier()
            if (identifier == null) {
                Log.e(TAG, "Physical keyboard device not found")
                return false
            }

            // Invoke the method reflectively
            setLayoutMethod.invoke(inputManager, identifier, layoutName)
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to set keyboard layout", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set keyboard layout", e)
        }
        return false
    }
}
