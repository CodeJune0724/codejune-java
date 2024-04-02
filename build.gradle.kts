plugins {
    java
    `java-library`
    `maven-publish`
}

subprojects {
    apply {
        plugin("java")
        plugin("java-library")
        plugin("maven-publish")
    }

    group = "com.codejune"
    version = "1.8.110-beta"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withSourcesJar()
    }

    repositories {
        mavenCentral()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    developers {
                        developer {
                            id.set("CodeJune")
                            name.set("CodeJune")
                            email.set("1476253236@qq.com")
                        }
                    }
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                setUrl("https://packages.aliyun.com/maven/repository/2137025-release-ae6xsV")
                credentials {
                    username = "613c3bc8a300e314f854bfd4"
                    password = "HNsb9RanXzmR"
                }
            }
        }
    }
}