package chris.murderdrones;

import net.neoforged.neoforge.common.ModConfigSpec;

// Server-side gameplay configuration. Registered as ModConfig.Type.SERVER (see
// ChrisMurderDronesMod's constructor), not the old Type.COMMON.
//
// Every value here gates something a *server* decides — whether a voiceline plays at
// all, how far it carries, how long its cooldown is, whether the creative quick-give
// button works, whether skin-change feedback is broadcast — and every read of these
// values happens on the logical server (see ModBlocks/ModNetworking/ModSoundUtil).
// SERVER-type config lives in the world save under serverconfig/ and NeoForge
// automatically pushes it to each client on login, so everyone connected to a given
// server is guaranteed to see the same values. Type.COMMON would NOT do that — each
// side would silently read its own local file instead, which for something like
// VOICELINE_COOLDOWN_TICKS (read on both sides so the client-side item-cooldown
// overlay and the server's actual cooldown gate agree) could drift out of sync.
//
// See ClientConfig for the purely client-side counterpart — currently empty, since
// nothing in this mod today is cosmetic-only, but that's where a future
// rendering/UI-only toggle (that a server has no business enforcing) would go.
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // BUILDER.push/.pop group the options below into named sections in the
    // generated .toml (each becomes a "[section]" header with its own
    // comment block) instead of one long flat list of settings. Purely
    // cosmetic — doesn't change any config key names or values — but makes
    // the file much easier to scan by hand or in a config-editing mod.

    // ── Plushie voiceline settings ──────────────────────────────────────────────
    static {
        BUILDER.comment("Voiceline settings — how plushie voicelines are triggered and heard")
                .push("voicelines");
    }

    public static final ModConfigSpec.BooleanValue ENABLE_HAND_VOICELINES = BUILDER
            .comment("Whether right-clicking a plushie item while holding it in your hand (not placed) plays a voiceline")
            .define("enableHandVoicelines", true);

    public static final ModConfigSpec.DoubleValue VOICELINE_RANGE = BUILDER
            .comment("How far away (in blocks) placed and in-hand plushie voicelines can be heard.",
                    "Volume fades linearly from full at the source down to silent at this distance,",
                    "same as any other vanilla positional sound tapering off as you walk away (see ModSoundUtil).")
            .defineInRange("voicelineRange", 32.0, 1.0, 64.0);

    public static final ModConfigSpec.IntValue VOICELINE_COOLDOWN_TICKS = BUILDER
            .comment("How many ticks (20 = 1 second) must pass before a plushie can trigger another voiceline,",
                    "whether placed or held in hand. Prevents spam-clicking while a line is still playing.",
                    "Default of 221 ticks (~11.01s) matches the longest voiceline file, doll_stairs.ogg,",
                    "so no line can be interrupted by spam-clicking a shorter one first.")
            .defineInRange("voicelineCooldownTicks", 130, 0, 1200);

    static { BUILDER.pop(); }

    // ── QoL settings ─────────────────────────────────────────────────────────────
    static {
        BUILDER.comment("Quality-of-life settings — optional feedback/shortcuts, safe to disable")
                .push("qualityOfLife");
    }

    public static final ModConfigSpec.BooleanValue ENABLE_CREATIVE_QUICK_GIVE = BUILDER
            .comment("Whether the 'Give (Creative)' button in the Plushie Catalog (P) works.",
                    "Only ever usable by players already in Creative mode; this just lets server",
                    "owners disable the shortcut entirely if they'd rather it wasn't there.")
            .define("enableCreativeQuickGive", true);

    public static final ModConfigSpec.BooleanValue ENABLE_SKIN_CHANGE_FEEDBACK = BUILDER
            .comment("Whether cycling a placed plushie's skin with the screwdriver shows an action-bar",
                    "message and plays a click sound. Purely cosmetic — turn off for a silent swap.")
            .define("enableSkinChangeFeedback", true);

    static { BUILDER.pop(); }

    static final ModConfigSpec SPEC = BUILDER.build();
}