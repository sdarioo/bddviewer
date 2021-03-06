package com.sdarioo.bddviewer.util;

import com.intellij.openapi.diagnostic.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class PathUtil {

    private static final Logger LOGGER = Logger.getInstance(PathUtil.class);
    public static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    private PathUtil() {}

    public static String getName(String path) {
        return getName(Paths.get(path));
    }

    public static String getName(Path path) {
        return path.getFileName().toString();
    }

    public static String getNameWithoutExtension(Path path) {
        String name = path.getFileName().toString();
        int index = name.lastIndexOf('.');
        if (index > 0) {
            name = name.substring(0, index);
        }
        return name;
    }

    public static Path findCommonRoot(List<Path> paths) {
        Set<Path> roots = paths.stream().map(Path::getRoot).collect(Collectors.toSet());
        if (roots.size() != 1) {
            LOGGER.warn("No common root found: " + roots);
            return null;
        }
        Path root = paths.stream().min(Comparator.comparing(Path::getNameCount)).get();
        if (Files.isRegularFile(root)) {
            root = root.getParent();
        }
        while (root != null) {
            boolean allStartsWith = true;
            for (Path path : paths) {
                if (!path.startsWith(root)) {
                    allStartsWith = false;
                    break;
                }
            }
            if (allStartsWith) {
                break;
            } else {
                root = root.getParent();
            }
        }
        return root;
    }

    public static String[] split(Path path) {
        String norm = path.toString().replace("\\", "/");
        return (norm.length() > 0) ?  norm.split("/") : new String[0];
    }


}
