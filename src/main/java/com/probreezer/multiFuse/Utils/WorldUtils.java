package com.probreezer.multiFuse.Utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class WorldUtils {

    private final JavaPlugin plugin;

    public WorldUtils(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void replaceWorld() throws IOException {
        File serverDir = plugin.getServer().getWorldContainer();

        deleteWorld(serverDir, "world");
        copyWorld(serverDir, "world");
        plugin.getLogger().info("World replaced successfully");
    }

    private void deleteWorld(File serverDir, String worldName) {
        String[] worldFolders = {worldName, worldName + "_nether", worldName + "_the_end"};

        for (String folder : worldFolders) {
            File worldFolder = new File(serverDir, folder);
            if (worldFolder.exists()) {
                try {
                    FileUtils.deleteDirectory(worldFolder);
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to delete world folder: " + folder);
                    plugin.getLogger().warning(e.getMessage());
                }
            }
        }
    }

    private void copyWorld(File serverDir, String worldName) throws IOException {
        var multiFuseWorld = new File(serverDir, "MultiFuse");
        var newWorld = new File(serverDir, worldName);

        if (!multiFuseWorld.exists()) {
            throw new IOException("MultiFuse world directory does not exist");
        }

        var multiFuseWorldPath = multiFuseWorld.toPath();
        var newWorldPath = newWorld.toPath();

        Files.walkFileTree(multiFuseWorldPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = newWorldPath.resolve(multiFuseWorldPath.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, newWorldPath.resolve(multiFuseWorldPath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
