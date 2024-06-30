plugins {
    java
    `java-library`
    `maven-publish`
}

subprojects {
    group = "com.codejune"

    version = "1.10.17"

    apply {
        plugin("java")
        plugin("java-library")
        plugin("maven-publish")
    }

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
        withSourcesJar()
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
                setUrl("https://packages.aliyun.com/613c3cec03e1c17d57a76d2b/maven/codejune")
                credentials {
                    username = "613c3bc8a300e314f854bfd4"
                    password = "HNsb9RanXzmR"
                }
            }
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}