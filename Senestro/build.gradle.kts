plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1" // Use the latest version from the Gradle Plugin Portal
}

group = "com.senestro"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // JSON libraries
    implementation("org.json:json:20240303")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.konghq:unirest-java-core:4.4.5")
    implementation("com.konghq:unirest-object-mappers-gson:4.2.9")

    // Apache Commons libraries
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("org.apache.commons:commons-lang3:3.17.0")

    // Compression and file handling
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    // Barcode processing
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:android-core:3.3.0")
    implementation("com.google.zxing:javase:3.5.3")

    // HTTP clients
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    implementation("org.apache.httpcomponents.client5:httpclient5-cache:5.4.1")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.3.1")
    implementation("org.apache.httpcomponents.core5:httpcore5-h2:5.3.1")

    // OkHttp libraries
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.squareup.okio:okio:3.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:5.0.0-alpha.14")
    implementation("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.14")
}

tasks {
    shadowJar {
        archiveClassifier.set("all") // Appends "all" to the JAR name (e.g., `your-app-all.jar`)
        archiveBaseName.set("Senestro Packaged")
        manifest {
            attributes["Main-Class"] = "com.senestro.Main"
        }
    }
}