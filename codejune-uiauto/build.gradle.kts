dependencies {
    api(project(":codejune-core"))
    api(project(":codejune-shell"))
    api(project(":codejune-system"))
    compileOnly(fileTree("opencv").include("*.jar"))
    implementation("net.java.dev.jna:jna-platform:5.19.1")
}