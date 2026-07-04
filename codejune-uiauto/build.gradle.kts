dependencies {
    api(project(":codejune-core"))
    compileOnly(fileTree("opencv").include("*.jar"))
    implementation("net.java.dev.jna:jna-platform:5.19.1")
}