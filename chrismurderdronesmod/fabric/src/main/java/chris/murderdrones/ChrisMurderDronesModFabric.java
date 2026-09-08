package chris.murderdrones;

import net.fabricmc.api.ModInitializer;

public class ChrisMurderDronesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Unlike NeoForge, Fabric registers blocks/items immediately (no deferred
        // registry step to flush first) — so this is the only call needed here.
        ChrisMurderDronesMod.init(chris.murderdrones.block.ScrewdriverItem::new);
    }
}
