package com.codejune.javafx.component;

import javafx.scene.Scene;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;

public final class StyleSheets {

    private static final String DIRECTORY = "javafx/style/";

    private static final String BASE = DIRECTORY + "base.css";

    private StyleSheets() {}

    private static class Holder {
        private static final List<String> URLS = scan();
    }

    public static void install(Scene scene) {
        List<String> missing = Holder.URLS.stream()
                .filter(url -> !scene.getStylesheets().contains(url))
                .toList();
        if (!missing.isEmpty()) {
            scene.getStylesheets().addAll(missing);
        }
    }

    private static List<String> scan() {
        URL anchor = StyleSheets.class.getResource("/" + BASE);
        if (anchor == null) {
            throw new IllegalStateException("Missing resource: " + BASE);
        }
        List<String> names = new ArrayList<>();
        try {
            switch (anchor.getProtocol()) {
                case "file" -> {
                    Path directory = Path.of(anchor.toURI()).getParent();
                    try (var files = Files.walk(directory)) {
                        files.filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".css"))
                                .forEach(path -> names.add(DIRECTORY +
                                        directory.relativize(path).toString().replace(java.io.File.separatorChar, '/')));
                    }
                }
                case "jar" -> {
                    JarURLConnection connection = (JarURLConnection) anchor.openConnection();
                    connection.setUseCaches(false);
                    try (var jar = connection.getJarFile()) {
                        jar.stream()
                                .filter(entry -> !entry.isDirectory())
                                .map(ZipEntry::getName)
                                .filter(name -> name.startsWith(DIRECTORY) && name.endsWith(".css"))
                                .forEach(names::add);
                    }
                }
                default -> throw new IllegalStateException("Unsupported resource URL: " + anchor);
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Failed to scan stylesheets", e);
        }
        return names.stream().distinct()
                .sorted(Comparator.<String>comparingInt(name -> name.equals(BASE) ? 0 : 1)
                        .thenComparing(Comparator.naturalOrder()))
                .map(name -> {
                    URL url = StyleSheets.class.getResource("/" + name);
                    if (url == null) throw new IllegalStateException("Missing resource: " + name);
                    return url.toExternalForm();
                })
                .toList();
    }

}