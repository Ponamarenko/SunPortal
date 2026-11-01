// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
<<<<<<< HEAD
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
=======
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

// Apply the Hilt plugin to the project
apply(plugin = "com.google.dagger.hilt.android")
>>>>>>> 026e7bf (Task: Create SunPortal app with Svitgrid theme — Base structure with solar monitoring UI)
