import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.4.5"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
    jacoco
}

group = "com.invitae.conductor"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.15.2")
    implementation("com.netflix.conductor:conductor-client:2.30.4")
    testImplementation("io.mockk:mockk:1.10.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Copy>("installGitHooks") {
    from("etc/pre-commit", "etc/pre-push", "etc/prepare-commit-msg")
    into(".git/hooks/")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

jacoco {
    toolVersion = "0.8.6"
}
