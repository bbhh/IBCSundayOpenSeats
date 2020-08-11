plugins {
    kotlin("jvm") version "1.3.72"
    application
}

group = "com.biblefoundry.ibcsundayopenseats"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "com.biblefoundry.ibcsundayopenseats.AppKt"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.apache.logging.log4j:log4j-api:2.13.3")
    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.0.0")
    implementation("org.fusesource.jansi:jansi:1.18")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")

    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.6")

    implementation("com.github.ajalt:clikt:2.7.1")

    implementation("com.uchuhimo:konf:0.22.1")

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.7.3")

    implementation("com.github.PhilJay:JWT:1.1.5")

    implementation("org.http4k:http4k-core:3.248.0")
    implementation("org.http4k:http4k-format-jackson:3.248.0")
    implementation("org.http4k:http4k-server-jetty:3.248.0")
    implementation("org.http4k:http4k-template-handlebars:3.248.0")

    implementation("org.quartz-scheduler:quartz:2.3.2")

    implementation("org.jetbrains.exposed:exposed-core:0.25.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.25.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.25.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.25.1")
    implementation("org.xerial:sqlite-jdbc:3.31.1")

    implementation("io.jsonwebtoken:jjwt-api:0.11.1")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.1")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.1")

    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.795"))
    implementation("com.amazonaws:aws-java-sdk-ses")
}

distributions {
    main {
        contents {
            from("src/main/resources") {
                into("src/main/resources")
            }
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.register<JavaExec>("seatsCli") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    main = "com.biblefoundry.ibcsundayopenseats.cli.SeatsCliKt"
}

val cliStartScripts = tasks.register<CreateStartScripts>("cliStartScripts") {
    mainClassName = "com.biblefoundry.ibcsundayopenseats.cli.SeatsCliKt"
    applicationName = "seats-cli"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
    doLast {
        windowsScript.writeText(windowsScript.readText().replace(Regex("set CLASSPATH=.*"), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*"))
    }
}

tasks.named<CreateStartScripts>("startScripts") {
    dependsOn(cliStartScripts)
    doLast {
        windowsScript.writeText(windowsScript.readText().replace(Regex("set CLASSPATH=.*"), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*"))
    }
}