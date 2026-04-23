plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// 加载签名配置（来自 GitHub Actions 或本地 keystore.properties）
val keystoreProperties = java.util.Properties()
val keystoreFile = rootProject.file("keystore.properties")
if (keystoreFile.exists()) {
    keystoreProperties.load(keystoreFile.inputStream())
}

android {
    namespace = "eu.ottop.yamlauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "eu.ottop.yamlauncher"
        minSdk = 24
        targetSdk = 36
        versionCode = 16
        versionName = "2.2"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    lint {
        // Disable pre-existing API compatibility warnings (BlendMode requires API 29+)
        disable += "NewApi"
        // Allow the build to continue even with lint errors to catch new issues
        abortOnError = false
    }

    signingConfigs {
        create("release") {
            val keyLoaded = keystoreProperties["signing.key.loaded"]?.toString()?.toBoolean() ?: false
            if (keyLoaded) {
                storeFile = file(keystoreProperties["storeFile"] as String? ?: "release.keystore")
                storePassword = keystoreProperties["storePassword"] as String? ?: ""
                keyAlias = keystoreProperties["keyAlias"] as String? ?: ""
                keyPassword = keystoreProperties["keyPassword"] as String? ?: ""
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "EELauncher Dev")
        }

        release {
            // 如果没有签名配置，跳过签名（Debug 构建仍可正常进行）
            val keyLoaded = keystoreProperties["signing.key.loaded"]?.toString()?.toBoolean() ?: false
            if (keyLoaded) {
                signingConfig = signingConfigs.getByName("release")
            }
            isDebuggable = false
            isShrinkResources = true
            isMinifyEnabled = true
            isProfileable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_name", "EELauncher")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.preference.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.biometric.ktx)
    implementation(libs.localbroadcastmanager)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
