package fr.poubone.att2.verification;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

class PackagedResourcesTest {
    private static InputStream resource(String name) {
        InputStream stream = PackagedResourcesTest.class.getClassLoader().getResourceAsStream(name);
        assertNotNull(stream, name);
        return stream;
    }

    private static JsonObject json(String name) throws IOException {
        try (var reader = new InputStreamReader(resource(name), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @Test
    void fabricMetadataHasResolvedVersionAndClientEntrypoint() throws IOException {
        var metadata = json("fabric.mod.json");
        assertEquals("att2", metadata.get("id").getAsString());
        assertEquals("client", metadata.get("environment").getAsString());
        assertFalse(metadata.get("version").getAsString().contains("${"));
        assertEquals(
                "1.1.32",
                metadata.getAsJsonObject("custom")
                        .getAsJsonObject("fre")
                        .get("version")
                        .getAsString());
        assertEquals("1.21.11", metadata.getAsJsonObject("depends").get("minecraft").getAsString());
    }

    @Test
    void everyRegisteredMixinIsBuiltFromSource() throws IOException {
        for (String name : new String[] {"att2.mixins.json", "lootbeams.mixins.json"}) {
            var configuration = json(name);
            String prefix = configuration.get("package").getAsString().replace('.', '/') + "/";
            for (String side : new String[] {"mixins", "client"}) {
                if (!configuration.has(side)) continue;
                for (var mixin : configuration.getAsJsonArray(side)) {
                    try (var ignored =
                            resource(prefix + mixin.getAsString().replace('.', '/') + ".class")) {
                        assertNotNull(ignored);
                    }
                }
            }
        }
    }

    @Test
    void originalPowderAnimationHasAllSixteenFrames() throws IOException {
        try (var stream = resource("assets/att2/textures/item/fre/rune_powder.png")) {
            var image = ImageIO.read(stream);
            assertEquals(16, image.getWidth());
            assertEquals(256, image.getHeight());
            assertTrue(image.getColorModel().hasAlpha());
        }
        var animation =
                json("assets/att2/textures/item/fre/rune_powder.png.mcmeta")
                        .getAsJsonObject("animation");
        assertEquals(2, animation.get("frametime").getAsInt());
        assertNotNull(json("assets/att2/items/fre/rune_powder.json").get("model"));
        assertNotNull(json("assets/att2/models/item/fre/rune_powder.json").get("textures"));
    }

    @Test
    void grayBackgroundOptionIsTranslatedInEveryModLanguage() throws IOException {
        for (String language :
                new String[] {"ar", "de", "en", "es", "fr", "hi", "ja", "ko", "pt", "ru", "zh"}) {
            var text = json("assets/att2/lang_mod/" + language + ".json");
            assertTrue(text.has("screen.hud_config.disable_gray_background"), language);
            assertTrue(text.has("screen.hud_config.disable_gray_background.tooltip"), language);
        }
        assertEquals(
                "关闭灰色背景",
                json("assets/att2/lang_mod/zh.json")
                        .get("screen.hud_config.disable_gray_background")
                        .getAsString());
    }

    @Test
    void thirdPartyNoticesArePreserved() throws IOException {
        for (String license :
                new String[] {
                    "LICENSE_ATT2_Project",
                    "LICENSE_lootbeams",
                    "LICENSE_kenney_rpg_ui",
                    "LICENSE_pingwheel"
                }) {
            try (var stream = resource(license)) {
                assertTrue(stream.readAllBytes().length > 100, license);
            }
        }
    }
}
