package chris.murderdrones;

import chris.murderdrones.platform.NeoForgeNetworkHelper;
import chris.murderdrones.platform.NeoForgeRegistryHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Constants.MOD_ID)
public class ChrisMurderDronesModNeoForge {

    public ChrisMurderDronesModNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Flush the DeferredRegisters onto the mod event bus BEFORE common init queues
        // anything into them — see NeoForgeRegistryHelper's doc comment for why the
        // relative order between this and ChrisMurderDronesMod.init() doesn't actually
        // matter (both just need to happen before FML fires RegisterEvent).
        NeoForgeRegistryHelper.registerAll(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(NeoForgeNetworkHelper::flush);

        // Triggers ModBlocksCommon's static registration block — this must run on
        // both the client and the dedicated server, so it belongs in the shared
        // constructor rather than a client-only or server-only event.
        ChrisMurderDronesMod.init(chris.murderdrones.block.NeoForgeScrewdriverItem::new);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Constants.LOG.info("HELLO FROM COMMON SETUP");
    }
}

