package chris.murderdrones.config;

/**
 * Loader-agnostic view of the gameplay config values ServerConfig used to expose
 * directly via NeoForge's ModConfigSpec. NeoForge's implementation is backed by a
 * real ModConfigSpec (unchanged from the original mod); Fabric's implementation
 * currently just returns the same defaults as plain fields — wiring up a real,
 * editable Fabric config file (e.g. via a simple .properties/.json loader) is a
 * follow-up step, not part of this port pass.
 */
public interface ICommonConfig {
    boolean enableHandVoicelines();
    double voicelineRange();
    int voicelineCooldownTicks();
    boolean enableSkinChangeFeedback();
    boolean enableCreativeQuickGive();
}
