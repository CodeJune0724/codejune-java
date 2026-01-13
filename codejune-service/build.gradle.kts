dependencies {
    api(project(":codejune-core"))
    api(project(":codejune-jdbc"))
    api(project(":codejune-pool"))
    api(project(":codejune-json"))
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")
    compileOnly("org.springframework.boot:spring-boot-starter-web:4.0.1")
    compileOnly("org.springframework.boot:spring-boot-starter-websocket:4.0.1")
}