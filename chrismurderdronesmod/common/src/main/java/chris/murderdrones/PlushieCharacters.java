package chris.murderdrones;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for every plushie character's metadata.
 *
 * Before this file existed, adding/renaming a skin meant touching four separate
 * places: a *_SKIN_COUNT constant in PlushieBlockEntity, a switch case in the same
 * file, a switch case in ModBlocks#getVoicelines, and a skin-name List in
 * PlushieBrowserOpener — and those lists could silently drift out of sync with the
 * skin count (that's exactly what happened with Uzi's 4th "Hologram" skin, which
 * had no name entry and fell back to the generic "Skin 3" label).
 *
 * TO ADD A NEW PLUSHIE CHARACTER:
 *   1. Register its block/item in ModBlocks.java (registerPlushie/registerPlushieItem)
 *      and its BlockEntityType, same pattern as every other character.
 *   2. Register its voicelines in ModSounds.java following the naming convention
 *      "<id>_voicelineN" (e.g. uzi_voiceline1, uzi_voiceline2, ...) — that naming
 *      convention is what lets ModBlocks#getVoicelines look them up automatically.
 *   3. Add ONE entry below with its id, display name, category, voiceline count,
 *      tooltip flavour text, and skin names.
 * That's it — skin count, the catalog listing, tooltip text, and voiceline lookup
 * all derive from this one entry. No other file needs to change.
 *
 * TO ADD OR RENAME A SKIN:
 *   Just edit the skin-name list for that character below. The first entry ("")
 *   is always the default skin. Skin count is simply the list's length — there is
 *   no separate count to keep in sync.
 */
public final class PlushieCharacters {

    public static final String MURDER_DRONES = "Murder Drones";
    public static final String DIGITAL_CIRCUS = "Digital Circus";

    public record Character(
            String id,
            String displayName,
            String category,
            int voicelineCount,
            String flavourText,
            List<String> skinNames
    ) {
        /** Total skin count, default skin included. */
        public int skinCount() {
            return skinNames.isEmpty() ? 1 : skinNames.size();
        }

        /** Short label for a single skin slot: "Default", "Skin N", or a custom name. */
        public String skinLabel(int skin) {
            String custom = skin < skinNames.size() ? skinNames.get(skin) : null;
            if (custom != null && !custom.isBlank()) return custom;
            return skin == 0 ? "Default" : "Skin " + skin;
        }

        /** Full label combining the character name with the skin label, e.g. "Uzi – Hologram". */
        public String fullLabel(int skin) {
            String custom = skin < skinNames.size() ? skinNames.get(skin) : null;
            if (custom != null && !custom.isBlank()) return displayName + " – " + custom;
            return skin == 0 ? displayName : displayName + " – Skin " + skin;
        }
    }

    private static Character c(String id, String name, String category, int voicelines, String flavour, String... skins) {
        return new Character(id, name, category, voicelines, flavour, List.of(skins));
    }

    public static final List<Character> ALL = List.of(
            // ── Murder Drones ───────────────────────────────────────────────
            c("uzi", "Uzi", MURDER_DRONES, 5, "BITE ME!",
                    "", "Prom", "PJ", "Hologram","Maid","Manor"),
            c("n", "N", MURDER_DRONES, 5, "He's really trying his best, okay?",
                    "", "Camper", "Dapper", "Manor", "PJ"),
            c("v", "V", MURDER_DRONES, 5, "I still feel nothing.",
                    "", "Camper", "Prom", "Manor", "PJ"),
            c("j", "J", MURDER_DRONES, 4, "Efficiency rating: 97.3%.",
                    "", "Manor", "Emotion Spilled Out", "Camper", "Prom", "PJ"),
            c("cyn", "Cyn", MURDER_DRONES, 5, "Big brother N.",
                    "", "Disassembly Drone", "Freddy FazSuit"),
            c("cynessa", "Cynessa", MURDER_DRONES, 5, "Lick.",
                    "", "T-Rex"),
            c("doll", "Doll", MURDER_DRONES, 3, "Orphan.",
                    "", "Prom", "Bandage", "Broken Eye"),
            c("khan", "Khan", MURDER_DRONES, 5, "Best dad on Copper-9.",
                    "", "BALD", "Bowtie"),
            c("lizzie", "Lizzie", MURDER_DRONES, 5, "Uzi is SO embarrassing.",
                    "", "Prom", "Camper"),
            c("teacher", "Teacher", MURDER_DRONES, 4, "Please read chapter four.",
                    "", "Camper"),
            c("tessa", "Tessa", MURDER_DRONES, 5, "Righty-O'.",
                    "", "Manor"),

            // ── Digital Circus ──────────────────────────────────────────────
            c("pomni", "Pomni", DIGITAL_CIRCUS, 5, "I need to get out of here!",
                    "", "Spudsy's", "Noir", "Running the Show", "Replacement Code", "Big Tops", "Big Tops Finale",
                    "Possessed", "President", "Swimsuit", "Evil", "Anime", "Abstracted"),
            c("jax", "Jax", DIGITAL_CIRCUS, 4, "Get your Jax Toy",
                    "", "Spudsy's", "Noir", "Maid", "Running the Show", "Shadow", "Trans", "Peeled", "Evil",
                    "Pacher's Paradise", "Spring", "Anime", "Abstracted", "Blank", "Casual 1", "Casual 2",
                    "Casual Hoodie", "Casual Jacket", "Swimsuit", "Military", "Static", "Bow"),
            c("ragatha", "Ragatha", DIGITAL_CIRCUS, 5, "It's gonna be okay, I promise.",
                    "", "Spudsy's", "Noir", "Running the Show", "Shadow", "Kitty Cat", "Full Bigtops", "Evil",
                    "Bigtops", "Anime", "Beach", "Cowgirl", "Abstracted"),
            c("gangle", "Gangle", DIGITAL_CIRCUS, 5, "WE NEED TO FIND THE TOMMY GUN",
                    "", "Spudsy's", "Noir", "Running the Show", "Happy Mask", "New Zeland Extremist", "Anime",
                    "Evil", "Beach", "EP 4 Mask", "Bigtops", "Kitty Cat", "Abstracted"),
            c("zooble", "Zooble", DIGITAL_CIRCUS, 5, "Are you afriad of corn?",
                    "", "Spudsy's", "Noir", "Running the Show", "Bodyguard", "Anime", "Evil", "YIKES!", "Abstracted"),
            c("kinger", "Kinger", DIGITAL_CIRCUS, 5, "I'm right behind you aren't I?",
                    "", "Noir", "Bucket Hat", "Running the Show", "Coach Dictatorer", "Bigtops", "Beach", "Anime",
                    "Queenie", "Kitty Cat", "Abstracted"),
            c("caine", "Caine", DIGITAL_CIRCUS, 5, "Welcome to the Amazing Digital Circus!",
                    "", "Running the Show", "Shadow", "Eniac", "Christmas", "Fisher"),
            c("npc", "NPC", DIGITAL_CIRCUS, 4, "Just a regular NPC.",
                    "", "Abel", "Red", "Orange", "Yellow", "Lime", "Green", "Cyan", "Light Blue", "Blue",
                    "Magenta", "Purple", "Pink", "Disapearing Guy", "Ming"),
            c("bubble", "Bubble", DIGITAL_CIRCUS, 3, "Pop!",
                    "", "Floor", "Chef", "Video Cam")
    );

    private static final Map<String, Character> BY_ID = new LinkedHashMap<>();
    static {
        for (Character character : ALL) {
            BY_ID.put(character.id(), character);
        }
    }

    /** Look up a character by its block/item registry id (e.g. "uzi"). Returns null if unknown. */
    public static Character get(String id) {
        return BY_ID.get(id);
    }

    /** All characters belonging to a given category, in declaration order. */
    public static List<Character> inCategory(String category) {
        List<Character> result = new ArrayList<>();
        for (Character character : ALL) {
            if (character.category().equals(category)) result.add(character);
        }
        return result;
    }
}
