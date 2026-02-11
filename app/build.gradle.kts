import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply { load(rootProject.file("local.properties").inputStream()) }
val signProperties = Properties().apply { load(File(localProperties.getProperty("sign.props.path")).inputStream()) }

android {
    namespace = "com.noobexon.xposedfakelocation"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.wizos.fakelocation"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(signProperties["storeFile"] as String)
            storePassword = signProperties["storePassword"] as String
            keyAlias = signProperties["keyAlias"] as String
            keyPassword = signProperties["keyPassword"] as String
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    applicationVariants.all {
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            outputImpl.outputFileName =
                "fakelocation-${buildType.name}-v${versionName}(${versionCode}).apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
tasks.register<Copy>("exportReleaseApk") {
    dependsOn("assembleRelease")
    from("${layout.buildDirectory}/outputs/apk/release")
    include("*.apk")
    into("${rootDir}/dist")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.osmdroid.android)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.line.awesome.android)
    implementation(libs.font.awesome)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.gson)
    implementation(libs.hiddenapibypass)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    compileOnly("de.robv.android.xposed:api:82:sources")
    compileOnly("de.robv.android.xposed:api:82")
}