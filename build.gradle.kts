plugins {
    kotlin("multiplatform") version "2.1.20"
    kotlin("plugin.allopen") version "2.1.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.13"
}

group = "iris.collections"
version = "0.1.0"

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    jvm {
        compilations.create("benchmark") {
            associateWith(this@jvm.compilations.getByName("main"))
        }
    }

    sourceSets {
        commonMain.dependencies {}
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        val jvmBenchmark by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.13")
            }
        }
    }
}

benchmark {
    targets {
        register("jvmBenchmark")
    }
    configurations {
        register("fast") {
            warmups = 3
            iterations = 5
            iterationTime = 1
            iterationTimeUnit = "s"
        }
        register("short30") {
            warmups = 3
            iterations = 5
            iterationTime = 1
            iterationTimeUnit = "s"
            include(".*LongArrayListBenchmark.*")
        }
    }
}
