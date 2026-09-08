package chris.murderdrones.client;

import chris.murderdrones.Constants;
import chris.murderdrones.PlushieCharacters;
import chris.murderdrones.block.ModBlocksCommon;
import chris.murderdrones.block.PlushieBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (PlushieCharacters.Character character : PlushieCharacters.ALL) {
            event.registerBlockEntityRenderer(
                    ModBlocksCommon.blockEntityTypeFor(character.id()), ModBlocksRenderer::new);
        }
        event.registerBlockEntityRenderer(
                ModBlocksCommon.PLUSHIE_BASE_BLOCK_ENTITY.get(), PlushieBaseBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (PlushieCharacters.Character character : PlushieCharacters.ALL) {
            String id = character.id();

            event.register(ModClientUtil.standaloneModel(
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                            "block/" + ModBlocksCommon.getModelPath(id, id))));

            String itemPath = PlushieBlockEntity.hasSkins(id)
                    ? ModBlocksCommon.getModelPath(id, id)
                    : id;
            event.register(ModClientUtil.standaloneModel(
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/" + itemPath)));

            for (int skin = 1; skin < PlushieBlockEntity.getSkinCount(id); skin++) {
                event.register(ModClientUtil.standaloneModel(
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                                "block/" + ModBlocksCommon.getModelPath(id, id + "_alt" + skin))));
            }
        }

        event.register(ModClientUtil.standaloneModel(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/plushie_base")));
    }
}
