plugins {
    java
    application apply(true)
}

repositories {
    mavenCentral()
}
application {
    mainClass = System.getProperty("mainClass")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22


}


dependencies {

    implementation("io.soabase.record-builder:record-builder-core:41")

    annotationProcessor("io.soabase.record-builder:record-builder-processor:41")
    implementation("dev.stratospheric:cdk-constructs:0.1.15")
}



tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.myorg.DocumentDbEcsStack" // Your stack class name
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test>().configureEach {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-preview")
}




