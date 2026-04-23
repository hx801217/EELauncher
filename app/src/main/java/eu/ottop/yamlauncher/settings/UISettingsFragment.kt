package eu.ottop.yamlauncher.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import eu.ottop.yamlauncher.R
import eu.ottop.yamlauncher.utils.Logger
import java.io.File

/**
 * UI settings fragment.
 * Contains preferences for text color, fonts, sizing, and appearance.
 */
class UISettingsFragment : PreferenceFragmentCompat(), TitleProvider {

    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private lateinit var logger: Logger
    private lateinit var openFontFileLauncher: ActivityResultLauncher<Intent>
    private var customFontPreference: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.ui_preferences, rootKey)

        sharedPreferenceManager = SharedPreferenceManager(requireContext())
        logger = Logger.getInstance(requireContext())

        customFontPreference = findPreference("customFontFile")

        // Register file picker launcher
        openFontFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    copyCustomFont(uri)
                }
            }
        }

        // Set up custom font file picker
        customFontPreference?.setOnPreferenceClickListener {
            openFontFileChooser()
            true
        }

        // Update custom font preference visibility based on current font selection
        updateCustomFontVisibility()
        updateCustomFontSummary()

        // Listen for font preference changes to toggle custom font visibility
        findPreference<Preference>("textFont")?.setOnPreferenceChangeListener { _, newValue ->
            // Update visibility immediately with the new value
            customFontPreference?.isVisible = newValue == "custom"
            true
        }
    }

    override fun onResume() {
        super.onResume()
        updateCustomFontVisibility()
        updateCustomFontSummary()
    }

    /**
     * Shows or hides the custom font preference based on the current font selection.
     */
    private fun updateCustomFontVisibility() {
        val currentFont = sharedPreferenceManager.getTextFont()
        customFontPreference?.isVisible = currentFont == "custom"
    }

    /**
     * Updates the custom font preference summary to show the current font file name.
     */
    private fun updateCustomFontSummary() {
        val path = sharedPreferenceManager.getCustomFontPath()
        if (path != null) {
            val fileName = File(path).name
            customFontPreference?.summary = fileName
        } else {
            customFontPreference?.summary = getString(R.string.custom_font_not_set)
        }
    }

    /**
     * Opens the system file picker for .ttf and .otf files.
     */
    private fun openFontFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "font/ttf",
                "font/otf",
                "application/x-font-ttf",
                "application/x-font-otf",
                "application/octet-stream"
            ))
        }
        try {
            openFontFileLauncher.launch(intent)
        } catch (e: Exception) {
            logger.e("UISettingsFragment", "Failed to open font file picker", e)
            Toast.makeText(requireContext(), getString(R.string.custom_font_error), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies the selected font file to app private storage and saves the path.
     */
    private fun copyCustomFont(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return

            // Determine file extension from URI
            val displayName = getFileName(uri)
            val extension = when {
                displayName.endsWith(".otf", ignoreCase = true) -> ".otf"
                displayName.endsWith(".ttf", ignoreCase = true) -> ".ttf"
                else -> ".ttf"
            }

            // Create fonts directory in app private storage
            val fontsDir = File(requireContext().filesDir, "custom_fonts")
            if (!fontsDir.exists()) {
                fontsDir.mkdirs()
            }

            // Clean up old custom font files
            fontsDir.listFiles()?.forEach { it.delete() }

            // Copy new font file
            val destFile = File(fontsDir, "custom_font$extension")
            destFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            // Save path to preferences
            sharedPreferenceManager.setCustomFontPath(destFile.absolutePath)

            // Update summary
            updateCustomFontSummary()

            logger.i("UISettingsFragment", "Custom font copied: ${destFile.absolutePath}")

        } catch (e: Exception) {
            logger.e("UISettingsFragment", "Failed to copy custom font", e)
            Toast.makeText(requireContext(), getString(R.string.custom_font_error), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Extracts the display name from a URI.
     */
    private fun getFileName(uri: Uri): String {
        var name = "font.ttf"
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    override fun getTitle(): String {
        return getString(R.string.ui_settings_title)
    }
}
