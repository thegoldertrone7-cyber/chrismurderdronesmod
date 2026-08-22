package chris.murderdrones;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Plays plushie voicelines so they fade out smoothly with distance instead of playing at a
 * flat volume until they abruptly cut off.
 * <p>
 * Minecraft's sound engine (OpenAL, via LWJGL) only applies real-time 3D distance attenuation
 * to <b>mono</b> sound files — stereo files always play at a constant volume no matter how far
 * the listener is. All of this mod's voiceline .ogg files are now mono for exactly this reason.
 * <p>
 * With mono files, the engine's own distance falloff kicks in automatically, and the
 * {@code volume} passed to {@link net.minecraft.world.level.Level#playSound} directly scales
 * the audible radius: {@code volume = 1.0} fades out at 16 blocks, and larger volumes stretch
 * that radius linearly (fade-out radius = {@code volume * 16} blocks), with a gradual falloff
 * baked in by the engine the whole way out. So to get a smooth fade across
 * {@link ServerConfig#VOICELINE_RANGE} blocks, all we have to do is scale volume by
 * {@code range / 16.0} and let vanilla do the rest — no manual per-player packet math needed.
 */
public final class ModSoundUtil {
    private ModSoundUtil() {}

    public static void playFadingSound(ServerLevel level, double x, double y, double z,
                                       SoundEvent sound, SoundSource source, float pitch) {
        double range = ServerConfig.VOICELINE_RANGE.get();
        if (range <= 0) return;

        float volume = (float) (range / 16.0);

        level.playSound(null, x, y, z, sound, source, volume, pitch);
    }
}