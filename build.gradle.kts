plugins {
    alias(libs.plugins.android.application) apply false
    // AGP 9 bringt Kotlin selbst mit; dieser Eintrag legt nur die Kotlin-Version fest.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
