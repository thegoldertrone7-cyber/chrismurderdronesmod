package chris.murderdrones;

import chris.murderdrones.block.ModBlocksCommon;
import chris.murderdrones.block.ScrewdriverItem;
import chris.murderdrones.platform.Services;
import net.minecraft.world.item.Item;

import java.util.function.Function;

/**
 * Loader-agnostic mod entry logic. Each loader's own mod entry class
 * (neoforge's ChrisMurderDronesModNeoForge / fabric's ChrisMurderDronesModFabric)
 * calls init() once during its own startup, after wiring up Services.REGISTRY,
 * passing in its own ScrewdriverItem constructor (NeoForge's needs an extra
 * override — see ScrewdriverItem's doc comment for why that can't be common code).
 */
public final class ChrisMurderDronesMod {
    private ChrisMurderDronesMod() {}

    public static void init(Function<Item.Properties, ScrewdriverItem> screwdriverFactory) {
        Constants.LOG.info("Chris' Murder Drones Mod: common init on {}", Services.PLATFORM.getPlatformName());
        ModSoundsCommon.init();
        chris.murderdrones.network.ModNetworkingCommon.init();
        // Touch ModBlocksCommon so its static registration block actually runs.
        ModBlocksCommon.init(screwdriverFactory);
        // Must run after ModBlocksCommon: the creative tab's displayItems lambda
        // reads ModBlocksCommon.itemFor(...)/SCREWDRIVER lazily (fine either order),
        // but registerCreativeTab itself needs the JUKEBOX_SONG resource keys built
        // here, and reads screwdriver's icon supplier which only exists after this.
        ModCreativeTabCommon.init();
    }
}

