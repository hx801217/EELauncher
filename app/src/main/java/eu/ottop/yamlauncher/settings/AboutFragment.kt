package eu.ottop.yamlauncher.settings

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import eu.ottop.yamlauncher.R

/**
 * About fragment displaying app information.
 * Shows app name, credits, and version.
 */
class AboutFragment : Fragment(), TitleProvider {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display app version
        val currentVersion = "v" + requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName
        requireActivity().findViewById<TextView>(R.id.version).text = currentVersion
    }

    override fun getTitle(): String {
        return getString(R.string.about_title)
    }
}
