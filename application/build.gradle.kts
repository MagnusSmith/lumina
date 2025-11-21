import com.google.cloud.tools.jib.api.Jib
import com.google.cloud.tools.jib.gradle.JibTask

plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.1.0"
    id("com.google.cloud.tools.jib") version "3.5.1"

}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25


}



springBoot {
    mainClass = "com.lumina.MeterConfigApplication"
}


jib {


    val ghUsername = System.getenv("GITHUB_ACTOR")


    from {
        image = "public.ecr.aws/amazoncorretto/amazoncorretto:25"
    }

    to {
		val projectName = project.name
		tags = setOf("latest") // Add "latest" tag as well
        if (ghUsername != null) {
        	image = "ghcr.io/lumina360/${projectName}:latest"
			auth {
				username = ghUsername
				password = System.getenv("LUMINA_GITHUB_TOKEN")
			}
            println("image -> ${image}")
            println("username -> ${auth.username}")
            println("pass -> ${auth.password}")

        } else {
        	image = projectName
      	}

    }

        container {
            jvmFlags = listOf("-Xms512m", "-Xmx1024m", "--enable-preview")
            mainClass = "com.lumina.MeterConfigApplication"
            ports = listOf("8080")
            environment = mapOf(
                //	"SPRING_PROFILES_ACTIVE" to "prod",
                "SPRING_DOCKER_COMPOSE_ENABLED" to "false"
            )
            //args = listOf("arg1", "arg2")
            creationTime = "USE_CURRENT_TIMESTAMP"
        }
    }


    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
        implementation("io.soabase.record-builder:record-builder-core:51")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        developmentOnly("org.springframework.boot:spring-boot-devtools")
        developmentOnly("org.springframework.boot:spring-boot-docker-compose")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        annotationProcessor("io.soabase.record-builder:record-builder-processor:51")
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




