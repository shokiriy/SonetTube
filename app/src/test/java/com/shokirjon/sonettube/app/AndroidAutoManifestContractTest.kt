package com.shokirjon.sonettube.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class AndroidAutoManifestContractTest {
    private val androidNamespace = "http://schemas.android.com/apk/res/android"

    @Test
    fun parkedVideoActivityMustNotClaimDistractionOptimization() {
        val manifest = readManifest()
        val metadata = manifest.getElementsByTagName("meta-data")

        val hasDistractionOptimizedClaim = (0 until metadata.length).any { index ->
            val element = metadata.item(index) as Element
            element.getAttributeNS(androidNamespace, "name") == "distractionOptimized" &&
                element.getAttributeNS(androidNamespace, "value") == "true"
        }

        assertFalse(
            "Video/browser activities must not claim to be distraction-optimized",
            hasDistractionOptimizedClaim,
        )
    }

    @Test
    fun parkedCarLauncherEntryRemainsDeclared() {
        val manifest = readManifest()
        val activities = manifest.getElementsByTagName("activity")

        val mainActivity = (0 until activities.length)
            .map { activities.item(it) as Element }
            .firstOrNull {
                it.getAttributeNS(androidNamespace, "name") == ".app.MainActivity"
            }

        assertTrue("MainActivity must remain declared", mainActivity != null)
        val categories = mainActivity!!.getElementsByTagName("category")
        val hasCarLauncher = (0 until categories.length).any { index ->
            val element = categories.item(index) as Element
            element.getAttributeNS(androidNamespace, "name") ==
                "android.intent.category.CAR_LAUNCHER"
        }

        assertTrue("The parked car launcher category must be retained", hasCarLauncher)
    }

    private fun readManifest() = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()
        .parse(File("src/main/AndroidManifest.xml"))
}
