plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.20"
    id("com.android.library")
    id("org.jetbrains.compose") version "1.5.11"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                
                // Ktor Client (HTTP)
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                implementation("io.ktor:ktor-client-logging:2.3.7")
                implementation("io.ktor:ktor-client-auth:2.3.7")
                
                // Kotlinx Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                
                // Kotlinx Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                
                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                
                // Navigation
                implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
                implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")
                
                // ViewModel
                implementation("cafe.adriel.voyager:voyager-screenmodel:1.0.0")
            }
        }
        
        val androidMain by getting {
            dependencies {
                // Ktor Android Engine
                implementation("io.ktor:ktor-client-okhttp:2.3.7")
                
                // AndroidX
                implementation("androidx.activity:activity-compose:1.8.2")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
            }
        }
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            
            dependencies {
                // Ktor iOS Engine
                implementation("io.ktor:ktor-client-darwin:2.3.7")
            }
        }
    }
}

android {
    namespace = "com.eventos.app.shared"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

