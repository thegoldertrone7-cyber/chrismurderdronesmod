package chris.murderdrones;

import chris.murderdrones.platform.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Plays plushie voicelines so they fade out smoothly with distance instead of playing
 * at a flat volume until they abruptly cut off. See ServerConfig/ICommonConfig's
 * voicelineRange doc for the mono-audio distance-attenuation explanation this relies on.
 * Only change from the original: reads the range through Services.CONFIG instead of
 * NeoForge's ServerConfig directly, since Fabric needs its own config backing.
 */
public final class ModSoundUtil {
    private ModSoundUtil() {}

    public static void playFadingSound(ServerLevel level, double x, double y, double z,
                                        SoundEvent sound, SoundSource source, float pitch) {
        double range = Services.CONFIG.voicelineRange();
        if (range <= 0) return;

        float volume = (float) (range / 16.0);

        level.playSound(null, x, y, z, sound, source, volume, pitch);
    }
}
