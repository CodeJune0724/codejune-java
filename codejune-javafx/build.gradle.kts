dependencies {
    api(project(":codejune-core"))
    compileOnly(fileTree("javafx/lib").include("*.jar"))
    compileOnly("org.kordamp.ikonli:ikonli-javafx:+")
    compileOnly("org.kordamp.ikonli:ikonli-fontawesome-pack:+")
}