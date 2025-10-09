plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.yogarena_protoype"
    compileSdk = 36


    aaptOptions {
        noCompress("tflite") // Change to this
    }

    defaultConfig {
        applicationId = "com.example.yogarena_protoype"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        mlModelBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("org.tensorflow:tensorflow-lite:2.17.0") {
        exclude(group = "com.google.ai.edge.litert", module = "litert-api")
    }
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0") {
        exclude(group = "com.google.ai.edge.litert", module = "litert-api")
    }
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4") {
        exclude(group = "com.google.ai.edge.litert", module = "litert-api")
    }
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4") {
        exclude(group = "com.google.ai.edge.litert", module = "litert-api")
    }
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4") {
        exclude(group = "com.google.ai.edge.litert", module = "litert-api")
    }






    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //Image Blur Library
    implementation("jp.wasabeef:blurry:4.0.1")


    // CameraX dependencies
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")


}