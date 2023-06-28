dependencies {
    api(project(":codejune-common"))
    api(project(":codejune-jdbc"))
    api("jakarta.persistence:jakarta.persistence-api:3.1.0")
    compileOnly("org.springframework.boot:spring-boot-starter-web:3.1.5")
    compileOnly("org.springframework.boot:spring-boot-starter-websocket:3.1.5")
}