dependencies {
    api(project(":codejune-core"))
    compileOnly("net.sf.ucanaccess:ucanaccess:5.0.1")
    compileOnly("org.mongodb:mongodb-driver-sync:5.2.0")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa:3.3.4")
    compileOnly("com.oracle.database.jdbc:ojdbc11:23.5.0.24.07")
    compileOnly("com.mysql:mysql-connector-j:9.0.0")
    compileOnly("org.xerial:sqlite-jdbc:3.46.1.3")
}