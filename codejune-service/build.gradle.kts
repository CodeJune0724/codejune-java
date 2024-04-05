dependencies {
    api(project(":codejune-common"))
    api(project(":codejune-jdbc"))
    api(project(":codejune-pool"))
    api(project(":codejune-json"))
    api("jakarta.persistence:jakarta.persistence-api:3.2.0-M2")
    compileOnly("org.springframework.boot:spring-boot-starter-web:3.2.4")
    compileOnly("org.springframework.boot:spring-boot-starter-websocket:3.2.4")
}