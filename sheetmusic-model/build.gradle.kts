import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    `maven-publish`
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    explicitApi()

    jvm {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
    android {
        namespace = "com.ryanjames.sheetmusic.score"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        withHostTest {}
    }
    iosArm64()
    iosSimulatorArm64()
    js {
        browser { testTask { enabled = false } }
        nodejs()
    }
    wasmJs {
        browser { testTask { enabled = false } }
        nodejs()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
