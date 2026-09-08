package chris.murderdrones;

import java.util.HashMap;
import java.util.Map;

/**
 * Real playback duration (in milliseconds) for every voiceline clip, extracted
 * directly from the .ogg files via ffprobe. Used so each voiceline's cooldown
 * matches how long that specific line actually takes to finish playing —
 * e.g. a 1s bark and a 6s monologue no longer share one flat cooldown.
 *
 * Keyed the same way getVoicelines() builds its ResourceLocations:
 * "<character>_voiceline<N>" (1-indexed, matching the sounds.json entries).
 *
 * Regenerate by re-running ffprobe over sounds/**\/*.ogg if clips are replaced.
 */
public final class VoicelineDurations {

    private static final Map<String, Integer> DURATIONS_MS = new HashMap<>();
    static {
        DURATIONS_MS.put("bubble_voiceline1", 955);
        DURATIONS_MS.put("bubble_voiceline2", 1225);
        DURATIONS_MS.put("bubble_voiceline3", 3499);
        DURATIONS_MS.put("caine_voiceline1", 4089);
        DURATIONS_MS.put("caine_voiceline2", 1750);
        DURATIONS_MS.put("caine_voiceline3", 960);
        DURATIONS_MS.put("caine_voiceline4", 960);
        DURATIONS_MS.put("caine_voiceline5", 1536);
        DURATIONS_MS.put("cyn_voiceline1", 1603);
        DURATIONS_MS.put("cyn_voiceline2", 2130);
        DURATIONS_MS.put("cyn_voiceline3", 1697);
        DURATIONS_MS.put("cyn_voiceline4", 4298);
        DURATIONS_MS.put("cyn_voiceline5", 1007);
        DURATIONS_MS.put("cynessa_voiceline1", 6022);
        DURATIONS_MS.put("cynessa_voiceline2", 1399);
        DURATIONS_MS.put("cynessa_voiceline3", 955);
        DURATIONS_MS.put("cynessa_voiceline4", 906);
        DURATIONS_MS.put("cynessa_voiceline5", 1071);
        DURATIONS_MS.put("doll_voiceline1", 2496);
        DURATIONS_MS.put("doll_voiceline2", 11012);
        DURATIONS_MS.put("doll_voiceline3", 1738);
        DURATIONS_MS.put("gangle_voiceline1", 1231);
        DURATIONS_MS.put("gangle_voiceline2", 1752);
        DURATIONS_MS.put("gangle_voiceline3", 1658);
        DURATIONS_MS.put("gangle_voiceline4", 6478);
        DURATIONS_MS.put("gangle_voiceline5", 1625);
        DURATIONS_MS.put("j_voiceline1", 5839);
        DURATIONS_MS.put("j_voiceline2", 6047);
        DURATIONS_MS.put("j_voiceline3", 1930);
        DURATIONS_MS.put("j_voiceline4", 3060);
        DURATIONS_MS.put("j_voiceline5", 3643);
        DURATIONS_MS.put("jax_voiceline1", 1483);
        DURATIONS_MS.put("jax_voiceline2", 2374);
        DURATIONS_MS.put("jax_voiceline3", 1424);
        DURATIONS_MS.put("jax_voiceline4", 3590);
        DURATIONS_MS.put("khan_voiceline1", 4063);
        DURATIONS_MS.put("khan_voiceline2", 2444);
        DURATIONS_MS.put("khan_voiceline3", 2966);
        DURATIONS_MS.put("khan_voiceline4", 1451);
        DURATIONS_MS.put("khan_voiceline5", 2417);
        DURATIONS_MS.put("kinger_voiceline1", 1963);
        DURATIONS_MS.put("kinger_voiceline2", 4574);
        DURATIONS_MS.put("kinger_voiceline3", 1045);
        DURATIONS_MS.put("kinger_voiceline4", 2427);
        DURATIONS_MS.put("kinger_voiceline5", 2177);
        DURATIONS_MS.put("lizzie_voiceline1", 1340);
        DURATIONS_MS.put("lizzie_voiceline2", 4117);
        DURATIONS_MS.put("lizzie_voiceline3", 1500);
        DURATIONS_MS.put("lizzie_voiceline4", 2600);
        DURATIONS_MS.put("lizzie_voiceline5", 1695);
        DURATIONS_MS.put("n_voiceline1", 2493);
        DURATIONS_MS.put("n_voiceline2", 1301);
        DURATIONS_MS.put("n_voiceline3", 1933);
        DURATIONS_MS.put("n_voiceline4", 2574);
        DURATIONS_MS.put("n_voiceline5", 2914);
        DURATIONS_MS.put("npc_voiceline1", 2478);
        DURATIONS_MS.put("npc_voiceline2", 4109);
        DURATIONS_MS.put("npc_voiceline3", 2246);
        DURATIONS_MS.put("npc_voiceline4", 5793);
        DURATIONS_MS.put("pomni_voiceline1", 3450);
        DURATIONS_MS.put("pomni_voiceline2", 3249);
        DURATIONS_MS.put("pomni_voiceline3", 2431);
        DURATIONS_MS.put("pomni_voiceline4", 2015);
        DURATIONS_MS.put("pomni_voiceline5", 1775);
        DURATIONS_MS.put("ragatha_voiceline1", 2343);
        DURATIONS_MS.put("ragatha_voiceline2", 1873);
        DURATIONS_MS.put("ragatha_voiceline3", 617);
        DURATIONS_MS.put("ragatha_voiceline4", 4235);
        DURATIONS_MS.put("ragatha_voiceline5", 2329);
        DURATIONS_MS.put("teacher_voiceline1", 4100);
        DURATIONS_MS.put("teacher_voiceline2", 2650);
        DURATIONS_MS.put("teacher_voiceline3", 4920);
        DURATIONS_MS.put("teacher_voiceline4", 2800);
        DURATIONS_MS.put("tessa_voiceline1", 2000);
        DURATIONS_MS.put("tessa_voiceline2", 1800);
        DURATIONS_MS.put("tessa_voiceline3", 2500);
        DURATIONS_MS.put("tessa_voiceline4", 2930);
        DURATIONS_MS.put("tessa_voiceline5", 1600);
        DURATIONS_MS.put("uzi_voiceline1", 1056);
        DURATIONS_MS.put("uzi_voiceline2", 2451);
        DURATIONS_MS.put("uzi_voiceline3", 3297);
        DURATIONS_MS.put("uzi_voiceline4", 2299);
        DURATIONS_MS.put("uzi_voiceline5", 3241);
        DURATIONS_MS.put("v_voiceline1", 2318);
        DURATIONS_MS.put("v_voiceline2", 1792);
        DURATIONS_MS.put("v_voiceline3", 3191);
        DURATIONS_MS.put("v_voiceline4", 2561);
        DURATIONS_MS.put("v_voiceline5", 2393);
        DURATIONS_MS.put("zooble_voiceline1", 2096);
        DURATIONS_MS.put("zooble_voiceline2", 2288);
        DURATIONS_MS.put("zooble_voiceline3", 1677);
    }

    private static final int DEFAULT_DURATION_MS = 2000;

    /** Duration in milliseconds for a given "<character>_voicelineN" key, or a safe default if unknown. */
    public static int getDurationMs(String key) {
        return DURATIONS_MS.getOrDefault(key, DEFAULT_DURATION_MS);
    }

    /** Convenience overload: character + 0-based line index. */
    public static int getDurationMs(String character, int lineIndex) {
        return getDurationMs(character + "_voiceline" + (lineIndex + 1));
    }
}
