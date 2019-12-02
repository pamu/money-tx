plugins {
    application
    kotlin("jvm") version "1.3.21"
}

application {
    applicationName = "moneytx"
    mainClassName = "com.moneytx.MainKt"
}

object Versions {
    const val javalin = "3.6.0"
    const val slf4j = "1.7.28"
    const val jacksonDatabind = "2.10.0"
    const val JacksonKt = "2.10.0"
    const val akkaActor = "2.6.0"
    const val kotlinTest = "3.3.0"
    const val fuel = "2.2.0"
    const val fuelJackson = "2.2.0"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.javalin:javalin:${Versions.javalin}")
    implementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.JacksonKt}")
    implementation("com.typesafe.akka:akka-actor_2.13:${Versions.akkaActor}")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
    testImplementation("com.github.kittinunf.fuel:fuel:${Versions.fuel}")
    testImplementation("com.github.kittinunf.fuel:fuel-jackson:${Versions.fuelJackson}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.1")
}

repositories {
    jcenter()
}