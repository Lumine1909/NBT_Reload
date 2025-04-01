import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    //id("maven-publish")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.28.0"
}

group = "io.github.lumine1909"
version = "1.6.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://libraries.minecraft.net/")
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("com.google.code.gson:gson:2.12.1")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
}

java {

}

mavenPublishing {
    coordinates(
        groupId = group as String,
        artifactId = name,
        version = version as String
    )

    pom {
        name.set("nbt")
        description.set("Flexible and intuitive library for reading and writing Minecraft's NBT format, with emphasis on custom tags.")
        url.set("https://github.com/Lumine1909/NBT_Reload")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/Lumine1909/NBT_Reload/blob/main/LICENSE.md")
            }
        }

        developers {
            developer {
                id.set("dewy")
                name.set("Dewy REDACTED")
                email.set("dewy@dewy.dev")
            }
            developer {
                id.set("Lumine1909")
                name.set("Lumine1909")
                email.set("133463833+Lumine1909@users.noreply.github.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/Lumine1909/NBT_Reload.git")
            developerConnection.set("scm:git:ssh://github.com/Lumine1909/NBT_Reload.git")
            url.set("https://github.com/Lumine1909/NBT_Reload")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
