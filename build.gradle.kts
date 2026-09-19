plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

if (System.getenv("CI") != "true") {
    layout.buildDirectory.set(File("C:/Users/user01/android-build/breez-notes/root-build"))
}
