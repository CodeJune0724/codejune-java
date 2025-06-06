dependencies {
    api(project(":codejune-core"))
    compileOnly("net.sf.ucanaccess:ucanaccess:5.0.1")
    compileOnly("org.mongodb:mongodb-driver-sync:5.5.0")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa:3.5.0")
    compileOnly("com.oracle.database.jdbc:ojdbc11:23.8.0.25.04")
    compileOnly("com.mysql:mysql-connector-j:9.3.0")
    compileOnly("org.xerial:sqlite-jdbc:3.49.1.0")
}