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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("com.github.INDIForJava:INDIForJava-server:2.1.1")
    implementation("com.github.INDIForJava:INDIForJava-focuser:2.1.1")
    implementation("com.formdev:flatlaf:2.2")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("io.github.java-native:jssc:2.9.4")
    implementation("org.jfree:jfreechart:1.5.3")
}