plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

import java.util.Properties

// Đọc local.properties để lấy API keys
val localProperties by lazy {
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { this.load(it) }
        }
    }
}

android {
    namespace = "com.example.kidapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kidapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Thêm API keys vào BuildConfig
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("gemini.api.key", "")}\"")
        buildConfigField("String", "FAL_AI_API_KEY", "\"${localProperties.getProperty("fal.ai.api.key", "")}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY_SECRET", "\"${localProperties.getProperty("cloudinary.api.key.secret", "")}\"")
        buildConfigField("String", "CLOUDINARY_NAME", "\"${localProperties.getProperty("cloudinary.name", "")}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY_PUBLIC", "\"${localProperties.getProperty("cloudinary.api.key.public", "")}\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true  // Kích hoạt BuildConfig
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.core.ktx)
    implementation(libs.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation ("androidx.gridlayout:gridlayout:1.0.0")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.tbuonomo:dotsindicator:4.3")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("me.relex:circleindicator:2.1.6")
    implementation("com.squareup.okio:okio:3.6.0")
    implementation ("com.airbnb.android:lottie:6.6.4")
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-firestore:24.9.1")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation ("io.github.sceneview:arsceneview:0.10.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.json:json:20211205")
    // Glide để tải ảnh từ URL
    implementation("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240205")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    
    // Thêm các dependencies mới
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
}