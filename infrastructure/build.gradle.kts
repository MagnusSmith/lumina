plugins {
    java
    application apply(true)


    //id("software.amazon.awscdk") version "2.58.1"
}

repositories {
    mavenCentral()
}
application {
    mainClass = System.getProperty("mainClass")
}


//dependencyManagement {
//    imports {
//        mavenBom("software.amazon.awscdk:bom:2.58.1") // or your preferred version
//    }
//}

dependencies {
  //  implementation(platform("software.amazon.awssdk:bom:2.58.1"))
    implementation("io.soabase.record-builder:record-builder-core:41")

    annotationProcessor("io.soabase.record-builder:record-builder-processor:41")
//    implementation("software.amazon.awscdk:aws-ec2")
//    implementation("software.amazon.awscdk:aws-ecs")
//    implementation("software.amazon.awssdk:aws-ecs-patterns")
//    implementation("software.amazon.awscdk:aws-docdb")
    implementation("dev.stratospheric:cdk-constructs:0.1.15")
}



tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.myorg.DocumentDbEcsStack" // Your stack class name
    }
}





