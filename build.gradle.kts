plugins {
    java
    id("io.freefair.lombok") version "9.1.0"
    id("com.diffplug.spotless") version "8.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.lumina"
version = "0.0.1-SNAPSHOT"


repositories {
    mavenCentral()
}


//    compileJava {
//        options.compilerArgs += ["--enable-preview"]
//    }
//    test {
//        jvmArgs(["--enable-preview"])
//    }


subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "java")

    spotless {
        java {
            googleJavaFormat("1.32.0")
            indentWithSpaces(2)
            formatAnnotations()
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
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









