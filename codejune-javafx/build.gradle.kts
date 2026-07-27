dependencies {
    api(project(":codejune-core"))
    compileOnly(fileTree("javafx/lib").include("*.jar"))
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-fontawesome-pack:12.4.0")
    implementation("org.controlsfx:controlsfx:11.2.3")
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("--module-path=javafx/lib", "--add-modules=javafx.controls,javafx.fxml")
}