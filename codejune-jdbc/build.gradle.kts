dependencies {
    api(project(":codejune-core"))
    compileOnly("net.sf.ucanaccess:ucanaccess:5.0.1")
    compileOnly("org.mongodb:mongodb-driver-sync:5.3.0-beta0")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa:3.4.0")
    compileOnly("com.oracle.database.jdbc:ojdbc11:23.6.0.24.10")
    compileOnly("com.mysql:mysql-connector-j:9.1.0")
    compileOnly("org.xerial:sqlite-jdbc:3.47.1.0")
}