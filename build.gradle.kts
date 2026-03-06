import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
    id("org.springframework.boot") version "4.0.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects{
    repositories {
        mavenCentral()
    }

    group = "com.example"
    version = "0.0.1-SNAPSHOT"
    description = "tracing-lab"
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }

        configure<KotlinJvmProjectExtension> {
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-Xjsr305=strict",
                    "-Xannotation-default-target=param-property"
                )
            }
        }

        dependencies {

        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}