package chris.murderdrones.platform;

import chris.murderdrones.config.ICommonConfig;

/**
 * TODO (follow-up phase, not part of this port): back this with a real, editable
 * config file — e.g. a simple .properties file under config/chrismurderdronesmod/,
 * mirroring NeoForge's ServerConfig. For now these are just the same default values
 * ServerConfig.java ships with, so Fabric behaves identically out of the box; players
 * on Fabric just can't change them yet without editing this file and rebuilding.
 */
public class FabricConfigHelper implements ICommonConfig {
    @Override
    public boolean enableHandVoicelines() {
        return true;
    }

    @Override
    public double voicelineRange() {
        return 32.0;
    }

    @Override
    public int voicelineCooldownTicks() {
        return 130;
    }

    @Override
    public boolean enableSkinChangeFeedback() {
        return true;
    }

    @Override
    public boolean enableCreativeQuickGive() {
        return true;
    }
}
