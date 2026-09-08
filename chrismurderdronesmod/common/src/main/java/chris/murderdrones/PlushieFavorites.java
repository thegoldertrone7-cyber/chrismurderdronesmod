package chris.murderdrones;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tracks which plushie skins the player has starred in the Plushie Catalog (P).
 * Purely a client-side convenience — favorites are stored per-install in
 * config/chrismurderdronesmod-favorites.txt (one "itemId#skinIndex" key per line),
 * not per-world and not synced, so this class is safe to touch from the catalog
 * screen without any networking.
 */
public final class PlushieFavorites {

    private static final Path FILE = chris.murderdrones.platform.Services.PLATFORM.getConfigDir().resolve("chrismurderdronesmod-favorites.txt");

    // Loaded lazily on first use rather than at class-init, so a broken/missing
    // config directory at classload time can't crash anything.
    private static Set<String> favorites = null;

    private PlushieFavorites() {}

    /** Stable key for a given character's skin, e.g. "chrismurderdronesmod:uzi#1". */
    public static String keyFor(CharacterEntry entry, int skin) {
        net.minecraft.resources.ResourceLocation id =
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(entry.item().get());
        return id + "#" + skin;
    }

    public static boolean isFavorite(CharacterEntry entry, int skin) {
        return isFavorite(keyFor(entry, skin));
    }

    public static boolean isFavorite(String key) {
        return load().contains(key);
    }

    /** Toggles the given skin's favorite status and persists the change. Returns the new state. */
    public static boolean toggle(CharacterEntry entry, int skin) {
        String key = keyFor(entry, skin);
        Set<String> set = load();
        boolean nowFavorite;
        if (set.remove(key)) {
            nowFavorite = false;
        } else {
            set.add(key);
            nowFavorite = true;
        }
        save();
        return nowFavorite;
    }

    public static boolean hasAnyFavorites() {
        return !load().isEmpty();
    }

    private static Set<String> load() {
        if (favorites != null) return favorites;
        favorites = new LinkedHashSet<>();
        try {
            if (Files.exists(FILE)) {
                for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) favorites.add(trimmed);
                }
            }
        } catch (IOException e) {
            Constants.LOG.warn("Failed to load plushie favorites, starting empty", e);
        }
        return favorites;
    }

    private static void save() {
        if (favorites == null) return;
        try {
            Files.createDirectories(FILE.getParent());
            Files.write(FILE, favorites, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Constants.LOG.warn("Failed to save plushie favorites", e);
        }
    }
}