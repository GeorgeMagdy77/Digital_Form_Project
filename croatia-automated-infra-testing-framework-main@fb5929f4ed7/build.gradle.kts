plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
    java
    id("io.qameta.allure") version "2.8.1"
}

group "com.croatia.infra.testing"
version "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.0-M1")
    testImplementation("org.junit.platform:junit-platform-launcher:1.8.0-M1")
    testImplementation ("io.rest-assured:rest-assured:4.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    implementation ("com.deliveredtechnologies:tf-maven-plugin:0.12")
    implementation ("com.deliveredtechnologies:tf-test-groovy:0.12")
    implementation ("com.deliveredtechnologies:tf-cmd-api:0.12")
    implementation("com.sksamuel.hoplite:hoplite-core:1.4.1")
    implementation("com.sksamuel.hoplite:hoplite-yaml:1.4.1")
    implementation (platform("software.amazon.awssdk:bom:2.16.74"))
    implementation ("software.amazon.awssdk:ec2")
    implementation ("software.amazon.awssdk:eks")
    implementation ("software.amazon.awssdk:autoscaling")
    implementation (platform("com.oracle.oci.sdk:oci-java-sdk-bom:2.4.1"))
    implementation("com.oracle.oci.sdk:oci-java-sdk-core")
    implementation("com.oracle.oci.sdk:oci-java-sdk-identity")
    implementation("com.oracle.oci.sdk:oci-java-sdk-bastion")
    implementation("com.oracle.oci.sdk:oci-java-sdk-events")
    implementation("com.oracle.oci.sdk:oci-java-sdk-filestorage")
    implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage")
    implementation("com.oracle.oci.sdk:oci-java-sdk-containerengine")
    implementation("com.oracle.oci.sdk:oci-java-sdk-vault")
    implementation("com.oracle.oci.sdk:oci-java-sdk-keymanagement")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
}
tasks.test {
    useJUnitPlatform()
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
}

val allureVersion = "2.13.6"
configure<io.qameta.allure.gradle.AllureExtension> {
    autoconfigure = true
    aspectjweaver = true
    version = allureVersion

    clean = true

    useJUnit5 {
        version = allureVersion
    }
}

tasks.register<Zip>("packageDistribution") {
    archiveFileName.set("test-results.zip")
    destinationDirectory.set(layout.buildDirectory.dir("dist"))

    from(layout.buildDirectory.dir("reports"))
}

