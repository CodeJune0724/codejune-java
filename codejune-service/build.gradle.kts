dependencies {
    api(project(":codejune-core"))
    api(project(":codejune-jdbc"))
    api(project(":codejune-pool"))
    api(project(":codejune-json"))
    api("jakarta.persistence:jakarta.persistence-api:4.0.0-M4")
    compileOnly("org.springframework.boot:spring-boot-starter-web:4.1.0-RC1")
    compileOnly("org.springframework.boot:spring-boot-starter-websocket:4.1.0-RC1")
}