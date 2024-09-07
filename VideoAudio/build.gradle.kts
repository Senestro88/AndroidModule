plugins {
    id("com.android.library")
     id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.official.senestro.video.audio.ffmpeg"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    //noinspection KtxExtensionAvailable
    implementation("androidx.core:core:1.13.1")
     implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.arthenica:mobile-ffmpeg-full:4.4")
    implementation("com.arthenica:smart-exception-common:0.2.1")
    implementation("com.arthenica:smart-exception-java:0.2.1")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(project(":CoreAndroid"))
}