package fr.poubone.att2.verification;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;

import fr.poubone.att2.client.hud.HUDConfig;

import net.fabricmc.loader.api.FabricLoader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;

class ConfigurationCompatibilityTest {
    @TempDir static Path configDirectory;
    private static Field configDirField;
    private static Object originalConfigDirectory;

    @BeforeAll
    static void isolateLoaderConfigDirectory() throws ReflectiveOperationException {
        // Plain JVM tests do not launch Fabric; isolate configuration from any game profile.
        Object loader = FabricLoader.getInstance();
        configDirField = loader.getClass().getDeclaredField("configDir");
        configDirField.setAccessible(true);
        originalConfigDirectory = configDirField.get(loader);
        configDirField.set(loader, configDirectory);
    }

    @AfterAll
    static void restoreLoaderConfigDirectory() throws IllegalAccessException {
        configDirField.set(FabricLoader.getInstance(), originalConfigDirectory);
    }

    @Test
    void grayBackgroundRemovalIsDisabledForNewAndExistingConfigurations() {
        assertFalse(new HUDConfig().disableGrayBackground);
        var existing =
                new Gson()
                        .fromJson(
                                "{\"orbFps\":30,\"cityTransparentBackground\":true}",
                                HUDConfig.class);
        assertFalse(existing.disableGrayBackground);
        assertEquals(30, existing.orbFps);
        assertTrue(existing.cityTransparentBackground);
    }

    @Test
    void grayBackgroundSettingRoundTripsWithoutChangingOtherSettings() {
        var config = new HUDConfig();
        config.disableGrayBackground = true;
        config.orbFps = 60;
        Gson gson = new Gson();
        var loaded = gson.fromJson(gson.toJson(config), HUDConfig.class);
        assertTrue(loaded.disableGrayBackground);
        assertEquals(60, loaded.orbFps);
    }
}
