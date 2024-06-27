plugins {
    java
    id("io.freefair.lombok") version "8.6"
    id("com.diffplug.spotless") version "7.0.0.BETA1"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.lumina"
version = "0.0.1-SNAPSHOT"


repositories {
    mavenCentral()
}




java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
}
subprojects {
    apply(plugin = "com.diffplug.spotless")
    spotless {
        java {
            googleJavaFormat("1.22.0")
            indentWithSpaces(2)
            formatAnnotations()
        }
    }

    compileJava {
        options.compilerArgs += ["--enable-preview"]
    }
    test {
        jvmArgs([""--enable-preview"])
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
}









