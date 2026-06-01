dependencies {
    api(project(":codejune-core"))
    compileOnly(fileTree("javafx/lib").include("*.jar"))
    compileOnly("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    compileOnly("org.kordamp.ikonli:ikonli-fontawesome-pack:12.4.0")
    implementation("org.controlsfx:controlsfx:11.2.3")
}