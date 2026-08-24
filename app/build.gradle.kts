plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.juntoscontradengue"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.juntoscontradengue"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Firebase BoM (Gerencia as versões automaticamente para evitar conflitos)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("androidx.activity:activity-ktx:1.8.0") // Versão estável 2026

    // Firebase (Sem versão manual, o BoM resolve)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-functions")

    // AndroidX e UI
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.core:core-ktx:1.12.0")

    // DATASTORE - CORREÇÃO DO ERRO (Use apenas uma versão, a 1.0.0 é a mais compatível com Java)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-core:1.0.0")

    // Navigation e Lifecycle
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Firebase UI
    implementation("com.firebaseui:firebase-ui-database:8.0.2")

    // Credential Manager e Identity
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.picasso:picasso:2.8")

    // Utils & Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Adicionado para o envio do Push
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // RxJava & Coroutines
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Media3 ExoPlayer - todas na mesma versão 1.3.1
    implementation ("androidx.media3:media3-exoplayer:1.3.1")
    implementation ("androidx.media3:media3-ui:1.3.1")
    implementation ("androidx.media3:media3-session:1.3.1")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("androidx.browser:browser:1.8.0")

}
