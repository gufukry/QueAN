import java.util.Properties

val weiYan = Properties().apply {
    val config = file(System.getProperty("user.home") + "/.config/sky/weiyan.properties")
    if (config.exists()) config.inputStream().use { load(it) }
}
fun configString(name: String, fallback: String = "") = "\"" + weiYan.getProperty(name, fallback).replace("\\", "\\\\").replace("\"", "\\\"") + "\""

plugins {
    id("com.android.application") version "9.4.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
}
android {
    namespace = "com.sky.ui"
    compileSdk { version = release(37) { minorApiLevel = 0 } }
    buildToolsVersion = "37.0.0"
    defaultConfig {
        applicationId = "com.sky.ui"
        minSdk = 33
        targetSdk = 35
        versionCode = 24
        versionName = "2.2.1"
        buildConfigField("String", "WEIYAN_APP_ID", configString("appId", "28506"))
        buildConfigField("String", "WEIYAN_RC4_KEY", configString("rc4Key"))
        buildConfigField("String", "WEIYAN_APP_KEY", configString("appKey"))
        buildConfigField("String", "WEIYAN_LOGIN_CODE", configString("loginCode", "520"))
        buildConfigField("String", "WEIYAN_HEARTBEAT_ACTION", configString("heartbeatAction"))
        buildConfigField("String", "WEIYAN_HEARTBEAT_CODE", configString("heartbeatCode"))
        buildConfigField("String", "WEIYAN_VARIABLE_ACTION", configString("variableAction"))
        buildConfigField("String", "WEIYAN_VARIABLE_FIELD", configString("variableField"))
        buildConfigField("String", "WEIYAN_VARIABLE_NAME", configString("variableName"))
        buildConfigField("String", "WEIYAN_VARIABLE_CODE", configString("variableCode"))

    }
    sourceSets["main"].apply {
        manifest.srcFile("AndroidManifest.xml")
        java.setSrcDirs(emptyList<String>())
        kotlin.setSrcDirs(listOf("compose-src"))
        assets.setSrcDirs(listOf("assets"))
    }
    testOptions { unitTests.isIncludeAndroidResources = false }
    buildFeatures { compose = true; buildConfig = true }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    signingConfigs.getByName("debug").apply {
        storeFile = file("build/debug.p12")
        storePassword = "android"
        keyAlias = "debug"
        keyPassword = "android"
        storeType = "PKCS12"
    }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.11.0")
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-preference-android:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-blur-android:0.9.4")
}
