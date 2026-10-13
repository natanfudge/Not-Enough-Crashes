package fudge.notenoughcrashes.upload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** A local, opt-in reproduction bundle. Never uploads files or follows symbolic links. */
public final class InstanceExport {
    private InstanceExport() {}

    public static Path create(Path game, Path config, Path world, boolean includeConfig, boolean includeWorld,
                              String report, String mods, String steps) throws IOException {
        Path destination = game.resolve("crash-reports");
        Files.createDirectories(destination);
        Path zip = Files.createTempFile(destination, "instance-", ".zip");
        try (var out = new ZipOutputStream(Files.newOutputStream(zip))) {
            text(out, "crash.txt", report);
            text(out, "mods.txt", mods);
            text(out, "README.txt", "Not Enough Crashes reproduction bundle\n\n"
                    + "Install the Minecraft, loader and mod versions listed in mods.txt. Mod binaries are not included.\n"
                    + "Copy config/ and saves/ into a separate test instance if included.\n"
                    + "Review every file before sharing: reports, configs and saves can contain personal information.\n"
                    + "This archive was created locally and has not been uploaded.\n\n"
                    + "Steps to reproduce:\n" + steps + "\n");
            if (includeConfig) tree(out, config, "config/");
            if (includeWorld && world != null) tree(out, world, "saves/" + world.getFileName() + "/");
        } catch (IOException | RuntimeException error) {
            Files.deleteIfExists(zip);
            throw error;
        }
        return zip;
    }

    private static void text(ZipOutputStream out, String name, String value) throws IOException {
        out.putNextEntry(new ZipEntry(name));
        out.write(value.getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
    }

    private static void tree(ZipOutputStream out, Path root, String prefix) throws IOException {
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) return;
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return Files.isSymbolicLink(dir) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isRegularFile() && !Files.isSymbolicLink(file) && !file.getFileName().toString().equals("session.lock")) {
                    out.putNextEntry(new ZipEntry(prefix + root.relativize(file).toString().replace('\\', '/')));
                    Files.copy(file, out);
                    out.closeEntry();
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
