package chris.murderdrones;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ChrisMurderDronesMod.MODID);

    // ── Uzi ──────────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> UZI_VOICELINE1 = register("uzi_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> UZI_VOICELINE2 = register("uzi_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> UZI_VOICELINE3 = register("uzi_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> UZI_VOICELINE4 = register("uzi_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> UZI_VOICELINE5 = register("uzi_voiceline5");

    // ── N ────────────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> N_VOICELINE1 = register("n_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> N_VOICELINE2 = register("n_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> N_VOICELINE3 = register("n_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> N_VOICELINE4 = register("n_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> N_VOICELINE5 = register("n_voiceline5");

    // ── V ────────────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> V_VOICELINE1 = register("v_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> V_VOICELINE2 = register("v_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> V_VOICELINE3 = register("v_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> V_VOICELINE4 = register("v_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> V_VOICELINE5 = register("v_voiceline5");

    // ── J ────────────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> J_VOICELINE1 = register("j_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> J_VOICELINE2 = register("j_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> J_VOICELINE3 = register("j_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> J_VOICELINE4 = register("j_voiceline4");

    // ── Cyn ──────────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> CYN_VOICELINE1 = register("cyn_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYN_VOICELINE2 = register("cyn_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYN_VOICELINE3 = register("cyn_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYN_VOICELINE4 = register("cyn_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYN_VOICELINE5 = register("cyn_voiceline5");

    // ── Cynessa ───────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> CYNESSA_VOICELINE1 = register("cynessa_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYNESSA_VOICELINE2 = register("cynessa_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYNESSA_VOICELINE3 = register("cynessa_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYNESSA_VOICELINE4 = register("cynessa_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYNESSA_VOICELINE5 = register("cynessa_voiceline5");

    // ── Doll ──────────────────────────────────────────────────────────────────
    // Used to be pinned to a fixed 20-block SoundEvent range, which skipped Minecraft's
    // normal distance falloff entirely (constant volume until an abrupt cutoff). Now
    // every voiceline — Doll included — goes through ModSoundUtil's per-player distance
    // fade, so range is controlled by ServerConfig.VOICELINE_RANGE like everyone else and it
    // actually fades out with distance instead of just stopping.
    public static final DeferredHolder<SoundEvent, SoundEvent> DOLL_VOICELINE1 = register("doll_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOLL_VOICELINE2 = register("doll_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOLL_VOICELINE3 = register("doll_voiceline3");

    // ── Khan ──────────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> KHAN_VOICELINE1 = register("khan_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> KHAN_VOICELINE2 = register("khan_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> KHAN_VOICELINE3 = register("khan_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> KHAN_VOICELINE4 = register("khan_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> KHAN_VOICELINE5 = register("khan_voiceline5");

    // ── Lizzie ────────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> LIZZIE_VOICELINE1 = register("lizzie_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIZZIE_VOICELINE2 = register("lizzie_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIZZIE_VOICELINE3 = register("lizzie_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIZZIE_VOICELINE4 = register("lizzie_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIZZIE_VOICELINE5 = register("lizzie_voiceline5");

    // ── Teacher ───────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> TEACHER_VOICELINE1 = register("teacher_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> TEACHER_VOICELINE2 = register("teacher_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> TEACHER_VOICELINE3 = register("teacher_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> TEACHER_VOICELINE4 = register("teacher_voiceline4");

    // ── Tessa ─────────────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> TESSA_VOICELINE1 = register("tessa_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> TESSA_VOICELINE2 = register("tessa_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> TESSA_VOICELINE3 = register("tessa_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> TESSA_VOICELINE4 = register("tessa_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> TESSA_VOICELINE5 = register("tessa_voiceline5");

    // ── Pomni (TADC) ─────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> POMNI_VOICELINE1 = register("pomni_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> POMNI_VOICELINE2 = register("pomni_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> POMNI_VOICELINE3 = register("pomni_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> POMNI_VOICELINE4 = register("pomni_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> POMNI_VOICELINE5 = register("pomni_voiceline5");

    // ── Jax (TADC) ───────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> JAX_VOICELINE1 = register("jax_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> JAX_VOICELINE2 = register("jax_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> JAX_VOICELINE3 = register("jax_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> JAX_VOICELINE4 = register("jax_voiceline4");

    // ── Ragatha (TADC) ───────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> RAGATHA_VOICELINE1 = register("ragatha_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RAGATHA_VOICELINE2 = register("ragatha_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RAGATHA_VOICELINE3 = register("ragatha_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> RAGATHA_VOICELINE4 = register("ragatha_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> RAGATHA_VOICELINE5 = register("ragatha_voiceline5");

    // ── Gangle (TADC) ────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> GANGLE_VOICELINE1 = register("gangle_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GANGLE_VOICELINE2 = register("gangle_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GANGLE_VOICELINE3 = register("gangle_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> GANGLE_VOICELINE4 = register("gangle_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> GANGLE_VOICELINE5 = register("gangle_voiceline5");

    // ── Zooble (TADC) ────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> ZOOBLE_VOICELINE1 = register("zooble_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZOOBLE_VOICELINE2 = register("zooble_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZOOBLE_VOICELINE3 = register("zooble_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZOOBLE_VOICELINE4 = register("zooble_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZOOBLE_VOICELINE5 = register("zooble_voiceline5");

    // ── Kinger (TADC) ────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> KINGER_VOICELINE1 = register("kinger_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> KINGER_VOICELINE2 = register("kinger_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> KINGER_VOICELINE3 = register("kinger_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> KINGER_VOICELINE4 = register("kinger_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> KINGER_VOICELINE5 = register("kinger_voiceline5");

    // ── Caine (TADC) ─────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> CAINE_VOICELINE1 = register("caine_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAINE_VOICELINE2 = register("caine_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAINE_VOICELINE3 = register("caine_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAINE_VOICELINE4 = register("caine_voiceline4");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAINE_VOICELINE5 = register("caine_voiceline5");

    // ── Bubble (TADC) ────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE_VOICELINE1 = register("bubble_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE_VOICELINE2 = register("bubble_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE_VOICELINE3 = register("bubble_voiceline3");

    // ── NPC (TADC) ───────────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_VOICELINE1 = register("npc_voiceline1");
    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_VOICELINE2 = register("npc_voiceline2");
    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_VOICELINE3 = register("npc_voiceline3");
    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_VOICELINE4 = register("npc_voiceline4");

    // ── Music Discs ──────────────────────────────────────────────────────────
    // Registered here like any other SoundEvent; the JukeboxSong datapack entries
    // (data/chrismurderdronesmod/jukebox_song/*.json) are what point at these ids
    // and turn them into playable discs — see those files for length/comparator output.
    // Drop the actual audio at:
    //   src/main/resources/assets/chrismurderdronesmod/sounds/records/ftige_music_disc.ogg
    //   src/main/resources/assets/chrismurderdronesmod/sounds/records/bm_music_disc.ogg
    //   src/main/resources/assets/chrismurderdronesmod/sounds/records/rts_music_disc.ogg
    // and reference them from sounds.json (snippet provided alongside this file).
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_FTIGE = register("music_disc_ftige");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BM    = register("music_disc_bm");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_RTS   = register("music_disc_rts");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_FOREVER  = register("music_disc_forever");

    // ── Helper ────────────────────────────────────────────────────────────────
    // All voicelines are plain variable-range registrations now — the actual audible
    // range and distance fade for voicelines is handled manually per-player by
    // ModSoundUtil (see ServerConfig.VOICELINE_RANGE), so this baked-in SoundEvent range is
    // only relevant to the music discs, where vanilla's default handling is fine.
    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () ->
                SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(ChrisMurderDronesMod.MODID, name)));
    }
}