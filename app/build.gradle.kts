import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Signatur für Release-Builds: Pfad und Passwörter stehen in keystore.properties im Projektordner
// (git-ignoriert, Vorlage: keystore.properties.example). Fehlt die Datei, entsteht eine unsignierte
// Release-APK. Ist sie da, aber unvollständig, bricht nur der Release-Build mit einer Meldung ab.
val keystoreFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystoreFile.exists()) {
    try {
        // removePrefix: Windows-Editoren setzen gern ein unsichtbares BOM an den Dateianfang.
        keystoreProperties.load(keystoreFile.readText(Charsets.UTF_8).removePrefix("\uFEFF").reader())
    } catch (e: IllegalArgumentException) {
        logger.error(
            "keystore.properties konnte nicht gelesen werden (${e.message}). " +
                "Unter Windows / statt \\ im Pfad verwenden, ein \\ im Passwort als \\\\ schreiben.",
        )
    }
}
fun keystoreValue(key: String): String? = keystoreProperties.getProperty(key)?.trim()?.ifEmpty { null }
val keystoreKeys = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
val missingKeystoreValues = if (keystoreFile.exists()) keystoreKeys.filter { keystoreValue(it) == null } else emptyList()
val hasReleaseKeystore = keystoreFile.exists() && missingKeystoreValues.isEmpty()

android {
    namespace = "de.motivationcode.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "de.motivationcode.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreValue("storeFile")!!)
                storePassword = keystoreValue("storePassword")
                keyAlias = keystoreValue("keyAlias")
                keyPassword = keystoreValue("keyPassword")
                // v3 erlaubt später einen Schlüsselwechsel, ohne dass Nutzer neu installieren müssen.
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        debug {
            // Eigene Paket-ID: Test- und Release-Version passen nebeneinander aufs Handy.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Unvollständige keystore.properties: lieber eine klare Meldung als still eine unsignierte APK.
// Betrifft nur Release-Builds, Debug-Builds und Tests laufen weiter.
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    val missing = missingKeystoreValues
    doFirst {
        if (missing.isNotEmpty()) {
            throw GradleException(
                "keystore.properties ist unvollständig, es fehlt: ${missing.joinToString()}. " +
                    "Vorlage: keystore.properties.example (bei PKCS12-Schlüsseln ist keyPassword = storePassword).",
            )
        }
    }
}
