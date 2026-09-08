package chris.murderdrones;

import net.neoforged.neoforge.common.ModConfigSpec;

// Client-side configuration. Registered as ModConfig.Type.CLIENT (see
// ChrisMurderDronesMod's constructor) — unlike ServerConfig, values in here are
// NEVER synced to or enforced by the server; each player's own local file is
// authoritative for their own client only. That makes this the right home for
// purely cosmetic/UI preferences a server has no legitimate reason to override —
// but NOT for anything that changes what other nearby players see or hear (a
// broadcast sound, a message sent to a specific player, a gameplay toggle), since
// the server can't read another player's CLIENT config to decide whether to send
// those in the first place.
//
// Empty for now: every current setting (voiceline behavior/range/cooldown,
// creative quick-give, skin-change feedback) gates something the *server*
// decides and broadcasts — see ServerConfig for all of them and why. This file
// exists so the CLIENT/SERVER split is already wired up (registration below)
// the moment this mod actually needs a client-only toggle, e.g. a purely local
// "don't render the creative-tab section banners for me" preference.
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static final ModConfigSpec SPEC = BUILDER.build();
}
