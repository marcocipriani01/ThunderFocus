plugins {
    id("java")
    id("idea")
}

group = "io.github.marcocipriani01.thunderfocus"
description = "ThunderFocus"
version = "2.6.3"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("com.github.INDIForJava:INDIForJava-server:2.1.1")
    implementation("com.github.INDIForJava:INDIForJava-focuser:2.1.1")
    implementation("com.formdev:flatlaf:3.2.5")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.github.java-native:jssc:2.9.6")
    implementation("org.jfree:jfreechart:1.5.4")
    implementation("com.github.Dansoftowner:jSystemThemeDetector:3.8")
}
