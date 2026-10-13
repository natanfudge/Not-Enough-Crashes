package fudge.notenoughcrashes.upload;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.nio.file.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import static org.junit.Assert.*;

public class InstanceExportTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    @Test public void includesOnlyExplicitlySelectedContext() throws Exception {
        Path game = temp.newFolder("game").toPath(), config = Files.createDirectories(game.resolve("config"));
        Path world = Files.createDirectories(game.resolve("saves/Test"));
        Files.writeString(config.resolve("mod.json"), "config");
        Files.writeString(world.resolve("level.dat"), "world");
        Files.writeString(world.resolve("session.lock"), "lock");
        Files.writeString(game.resolve("accounts.json"), "secret");
        Path minimal = InstanceExport.create(game, config, world, false, false, "crash", "mod 1.0", "steps");
        try (var zip = new ZipFile(minimal.toFile())) {
            assertEquals(Set.of("README.txt", "mods.txt", "crash.txt"), zip.stream().map(e -> e.getName()).collect(Collectors.toSet()));
        }
        Path full = InstanceExport.create(game, config, world, true, true, "crash", "mod 1.0", "reproduce é");
        try (var zip = new ZipFile(full.toFile())) {
            assertNotNull(zip.getEntry("config/mod.json"));
            assertNotNull(zip.getEntry("saves/Test/level.dat"));
            assertNull(zip.getEntry("saves/Test/session.lock"));
            assertNull(zip.getEntry("accounts.json"));
            assertTrue(new String(zip.getInputStream(zip.getEntry("README.txt")).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).contains("reproduce é"));
        }
    }
    @Test public void doesNotFollowLinks() throws Exception {
        Path game = temp.newFolder("game").toPath(), config = Files.createDirectories(game.resolve("config"));
        Path secret = temp.newFile("secret").toPath();
        try { Files.createSymbolicLink(config.resolve("outside"), secret); }
        catch (UnsupportedOperationException | FileSystemException unsupported) { org.junit.Assume.assumeNoException(unsupported); }
        Path archive = InstanceExport.create(game, config, null, true, false, "report", "mods", "steps");
        try (var zip = new ZipFile(archive.toFile())) { assertNull(zip.getEntry("config/outside")); }
    }
}
