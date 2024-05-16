plugins {
	java
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.4"
	id("io.freefair.lombok") version "8.6"
	id("com.diffplug.spotless") version "6.25.0"
}

group = "com.lumina"
version = "0.0.1-SNAPSHOT"


java {
	toolchain {

		languageVersion = JavaLanguageVersion.of(22)
	}
	sourceCompatibility = JavaVersion.VERSION_22
}

springBoot {
	mainClass = "com.lumina.MeterConfigApplication"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
	implementation("io.soabase.record-builder:record-builder-core:41")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	annotationProcessor("io.soabase.record-builder:record-builder-processor:41")
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

subprojects {
	spotless {
		java {
			googleJavaFormat("1.22.0")
			indentWithTabs(1)
			indentWithSpaces(2)
		}
	}
}

