package chris.murderdrones;

import chris.murderdrones.block.ModBlocksCommon;
import chris.murderdrones.block.PlushieBlockEntity;
import chris.murderdrones.client.ModBlocksRenderer;
import chris.murderdrones.client.ModKeyBindings;
import chris.murderdrones.client.PlushieBaseBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric client entry point — registers block entity renderers and standalone
 * alt-skin models, data-driven off PlushieCharacters.ALL.
 * <p>
 * Note: Fabric's ModelLoadingPlugin.Context#addModels takes a plain ResourceLocation,
 * not a ModelResourceLocation like NeoForge's ModelEvent.RegisterAdditional#register
 * does — a genuine API shape difference between the two loaders, not a mistake to
 * "fix" into matching; see ClientModEvents (neoforge) for the NeoForge side.
 */
public class ChrisMurderDronesModFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ModKeyBindings.OPEN_BROWSER_KEY);
        KeyBindingHelper.registerKeyBinding(ModKeyBindings.OPEN_SKIN_PICKER_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(client -> ModKeyBindings.onClientTick());
        chris.murderdrones.client.FabricCreativeTabHeaderEvents.register();

        for (PlushieCharacters.Character character : PlushieCharacters.ALL) {
            BlockEntityRendererRegistry.register(
                    ModBlocksCommon.blockEntityTypeFor(character.id()), ModBlocksRenderer::new);
        }
        BlockEntityRendererRegistry.register(
                ModBlocksCommon.PLUSHIE_BASE_BLOCK_ENTITY.get(), PlushieBaseBlockEntityRenderer::new);

        ModelLoadingPlugin.register(context -> {
            for (PlushieCharacters.Character character : PlushieCharacters.ALL) {
                String id = character.id();

                context.addModels(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                        "block/" + ModBlocksCommon.getModelPath(id, id)));

                String itemPath = PlushieBlockEntity.hasSkins(id)
                        ? ModBlocksCommon.getModelPath(id, id)
                        : id;
                context.addModels(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/" + itemPath));

                for (int skin = 1; skin < PlushieBlockEntity.getSkinCount(id); skin++) {
                    context.addModels(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                            "block/" + ModBlocksCommon.getModelPath(id, id + "_alt" + skin)));
                }
            }

            context.addModels(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/plushie_base"));
        });
    }
}
