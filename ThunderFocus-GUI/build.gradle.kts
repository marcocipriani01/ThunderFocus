plugins {
    id("java")
    id("idea")
}

group = "io.github.marcocipriani01.thunderfocus"
description = "ThunderFocus"
version = "2.6.1"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

dependencies {
    implementation("com.github.INDIForJava:INDIForJava-server:2.1.1")
    implementation("com.github.INDIForJava:INDIForJava-focuser:2.1.1")
    implementation("com.formdev:flatlaf:1.5")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("io.github.java-native:jssc:2.9.2")
    implementation("org.jfree:jfreechart:1.5.3")
}