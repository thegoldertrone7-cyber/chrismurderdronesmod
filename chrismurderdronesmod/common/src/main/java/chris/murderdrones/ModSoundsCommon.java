package chris.murderdrones;

import chris.murderdrones.platform.Services;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Replaces the original ModSounds.java, which had 98 hand-written
 * `register("x_voicelineN")` calls (one per character per voiceline, plus 4 music
 * discs). Every voiceline name is fully determined by PlushieCharacters.ALL's
 * voicelineCount, so this just loops over it instead.
 * <p>
 * This has to exist and run BEFORE any code calls ModBlocksCommon.getVoicelines() —
 * without it, BuiltInRegistries.SOUND_EVENT.get(id) there returns null for every
 * voiceline, since assets/sounds.json alone only maps an *already-registered* sound
 * event name to audio files; it doesn't create the registry entry itself.
 */
public final class ModSoundsCommon {
    private ModSoundsCommon() {}

    public static final String[] MUSIC_DISCS = {
            "music_disc_ftige", "music_disc_bm", "music_disc_rts", "music_disc_forever"
    };

    private static final Map<String, Supplier<SoundEvent>> SOUND_EVENTS = new LinkedHashMap<>();

    public static Supplier<SoundEvent> soundFor(String name) {
        return SOUND_EVENTS.get(name);
    }

    public static void init() {
        for (PlushieCharacters.Character character : PlushieCharacters.ALL) {
            for (int i = 1; i <= character.voicelineCount(); i++) {
                register(character.id() + "_voiceline" + i);
            }
        }
        for (String disc : MUSIC_DISCS) {
            register(disc);
        }
    }

    private static void register(String name) {
        SOUND_EVENTS.put(name, Services.REGISTRY.registerSoundEvent(name,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name))));
    }
}
