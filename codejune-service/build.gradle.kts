dependencies {
    api(project(":codejune-common"))
    api(project(":codejune-jdbc"))
    api(project(":codejune-pool"))
    api(project(":codejune-json"))
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")
    compileOnly("org.springframework.boot:spring-boot-starter-web:3.3.0")
    compileOnly("org.springframework.boot:spring-boot-starter-websocket:3.3.0")
}