plugins {
    id("java")
}

group = "me.thealgorithm476"
version = "3.0"

repositories {
    mavenCentral()

    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}