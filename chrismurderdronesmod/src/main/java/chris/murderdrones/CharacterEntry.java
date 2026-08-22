package chris.murderdrones;

import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Supplier;

/**
 * @param displayName the character's name shown as the section label in the catalog
 * @param item        supplier for the character's default plushie item
 * @param skinCount   total number of skins (including the default at index 0)
 * @param skinNames   OPTIONAL custom names for each skin index, e.g. List.of("Classic", "Battle Damaged").
 *                    Leave an entry blank/null (or the whole list shorter than skinCount) to fall back
 *                    to the default "Default" / "Skin N" label. This is the place to rename skins —
 *                    just edit the list where each CharacterEntry is built in PlushieBrowserOpener.
 */
public record CharacterEntry(String displayName, Supplier<Item> item, int skinCount, List<String> skinNames) {

    /** Convenience constructor for characters that don't need custom skin names. */
    public CharacterEntry(String displayName, Supplier<Item> item, int skinCount) {
        this(displayName, item, skinCount, List.of());
    }

    /** Short label for a single skin slot (used under its thumbnail): "Default", "Skin 2", or a custom name. */
    public String skinLabel(int skin) {
        String custom = (skinNames != null && skin < skinNames.size()) ? skinNames.get(skin) : null;
        if (custom != null && !custom.isBlank()) return custom;
        return skin == 0 ? "Default" : "Skin " + skin;
    }

    /** Full label combining the character name with the skin label, e.g. "Uzi – Battle Damaged". */
    public String fullLabel(int skin) {
        String custom = (skinNames != null && skin < skinNames.size()) ? skinNames.get(skin) : null;
        if (custom != null && !custom.isBlank()) return displayName + " – " + custom;
        return skin == 0 ? displayName : displayName + " – Skin " + skin;
    }
}

