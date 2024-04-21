plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

}

android {
    namespace = "com.example.ambulanceapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ambulanceapp"
        minSdk = 21
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation ("androidx.multidex:multidex:2.0.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.google.firebase:firebase-messaging:23.4.0")
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:19.3.2")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation("com.google.firebase:firebase-database:19.3.2")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation ("com.google.maps.android:android-maps-utils:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("com.google.firebase:firebase-inappmessaging:20.4.0")
    implementation("com.google.android.libraries.places:places:2.7.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.stripe:stripe-android:16.1.1")
    implementation ("androidx.core:core-ktx:1.7.0")
    implementation("androidx.core:core:1.12.0")












    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
