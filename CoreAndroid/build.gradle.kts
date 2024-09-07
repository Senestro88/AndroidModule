plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.official.senestro.core"
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
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.zxing:android-core:3.3.0")
    //noinspection KtxExtensionAvailable
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.github.bumptech.glide:glide:5.0.0-rc01")
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("com.google.zxing:core:3.5.3")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("org.nanohttpd:nanohttpd-websocket:2.3.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    implementation("com.scottyab:rootbeer-lib:0.1.0")
    implementation("commons-io:commons-io:20030203.000550")
    implementation("com.jaredrummler:apk-parser:1.0.2")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
}