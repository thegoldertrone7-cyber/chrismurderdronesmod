package chris.murderdrones.platform;

import chris.murderdrones.ServerConfig;
import chris.murderdrones.config.ICommonConfig;

/** Thin adapter from the common ICommonConfig interface onto the original ServerConfig (ModConfigSpec). */
public class NeoForgeConfigHelper implements ICommonConfig {
    @Override
    public boolean enableHandVoicelines() {
        return ServerConfig.ENABLE_HAND_VOICELINES.getAsBoolean();
    }

    @Override
    public double voicelineRange() {
        return ServerConfig.VOICELINE_RANGE.get();
    }

    @Override
    public int voicelineCooldownTicks() {
        return ServerConfig.VOICELINE_COOLDOWN_TICKS.get();
    }

    @Override
    public boolean enableSkinChangeFeedback() {
        return ServerConfig.ENABLE_SKIN_CHANGE_FEEDBACK.getAsBoolean();
    }

    @Override
    public boolean enableCreativeQuickGive() {
        return ServerConfig.ENABLE_CREATIVE_QUICK_GIVE.getAsBoolean();
    }
}
