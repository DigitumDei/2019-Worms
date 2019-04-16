import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget

plugins {
    kotlin("multiplatform") version "1.3.21"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("jacoco")
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://dl.bintray.com/entelect-challenge/game-engine")
}

kotlin {
    jvm()
    js()

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        commonTest {

            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("com.google.code.gson:gson:2.8.5")
                api("za.co.entelect.challenge:game-engine-interface:2019.1.0+")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }

    kotlin.js().compilation("main").kotlinOptions {
        moduleKind = "commonjs"
    }
}

group = "za.co.entelect.challenge"
version = "2019.1.0-RC0"

task<JacocoReport>("testCoverageReport") {
    group = "report"
    description = "Generates a test coverage report"

    executionData("$buildDir/jacoco/jvmTest.exec")

    val compilation = kotlin.jvm().compilation("main")

    additionalClassDirs(compilation.output.classesDirs)
    compilation.sourceDirectories().forEach { additionalSourceDirs(it) }

    reports {
        html.isEnabled = true
        xml.isEnabled = false
        csv.isEnabled = false
    }

    mustRunAfter("jvmTest")
}

task<JacocoCoverageVerification>("testCoverageVerification") {
    group = "verification"
    description = "Enforces test coverage on all classes"

    executionData("$buildDir/jacoco/jvmTest.exec")
    val compilation = kotlin.jvm().compilation("main")

    additionalClassDirs(compilation.output.classesDirs)
    compilation.sourceDirectories().forEach { additionalSourceDirs(it) }

    violationRules {
        rule {
            element = "CLASS"
            excludes = listOf(
                    "za.co.entelect.challenge.game.delegate.*",
                    "za.co.entelect.challenge.game.engine.simplexNoise.*",
                    "za.co.entelect.challenge.game.engine.config.*",
                    "za.co.entelect.challenge.game.engine.player.Weapon",
                    "za.co.entelect.challenge.game.engine.processor.GameError",
                    "za.co.entelect.challenge.game.engine.renderer.WormsRenderer.Companion"
            )
            limit {
                minimum = "0.75".toBigDecimal()
            }
        }
    }

    mustRunAfter("jvmTest")
    shouldRunAfter("testCoverageReport")
}

task<ShadowJar>("shadowJarJvm") {
    group = "shadow"
    description = "Creates a fat jar with all jvm classes and dependencies"

    val compilation = kotlin.jvm().compilation("main")

    from(compilation.output)

    configurations = listOf(compilation.runtimeDependencyFiles as Configuration)
    archiveAppendix.set("jvm-full")

    shouldRunAfter("check")
}

task<ShadowJar>("shadowJarJs") {
    group = "shadow"
    description = "Creates a fat jar with all js classes and dependencies"

    val compilation = kotlin.js().compilation("main")

    from(compilation.output)

    configurations = listOf(compilation.runtimeDependencyFiles as Configuration)
    archiveAppendix.set("js-full")

    shouldRunAfter("check")
}

task<Sync>("jsPackage") {
    group = "build"
    val output = kotlin.js().compilation("main").output

    //node_modules is preserved for local npm folder installs
    preserve {
        include("node_modules")
    }

    output.classesDirs.forEach {
        from(it) {
            include("*.js")
            exclude("*.meta.js")
        }
    }
    from(output.resourcesDir)

    into("$buildDir/package")

    mustRunAfter("jsMainClasses")
}

fun <T : KotlinCompilation<KotlinCommonOptions>> KotlinOnlyTarget<T>.compilation(name: String): T {
    return compilations.getByName(name)
}

fun KotlinCompilation<*>.sourceDirectories(): Iterable<FileCollection> {
    return allKotlinSourceSets.map {
        it.kotlin.sourceDirectories
    }
}

tasks.named<AbstractTestTask>("jvmTest") {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.named("assemble") {
    dependsOn("shadowJarJvm", "shadowJarJs")
}

tasks.named("check") {
    dependsOn("testCoverageReport", "testCoverageVerification")
}

tasks.named("build") {
    dependsOn("jsPackage")
}
