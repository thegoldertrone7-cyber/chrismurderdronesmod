package chris.murderdrones;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = ChrisMurderDronesMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register the renderer for EVERY plushie block entity
        event.registerBlockEntityRenderer(ModBlocks.UZI_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.N_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.V_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.J_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CYN_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CYNESSA_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.DOLL_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.KHAN_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.LIZZIE_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.TEACHER_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.TESSA_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.POMNI_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.JAX_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.RAGATHA_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.GANGLE_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.ZOOBLE_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.KINGER_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CAINE_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.NPC_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.BUBBLE_BLOCK_ENTITY.get(), ModBlocksRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.PLUSHIE_BASE_BLOCK_ENTITY.get(), PlushieBaseBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        String[] characters = {"uzi", "n", "v", "j", "cyn", "cynessa", "doll", "khan", "lizzie", "teacher", "tessa",
                "pomni", "jax", "ragatha", "gangle", "zooble", "kinger", "caine", "npc", "bubble"};

        for (String character : characters) {
            event.register(ModelResourceLocation.standalone(
                    ResourceLocation.fromNamespaceAndPath(ChrisMurderDronesMod.MODID,
                            "block/" + ModBlocks.getModelPath(character, character))
            ));

            String itemPath = PlushieBlockEntity.hasSkins(character)
                    ? ModBlocks.getModelPath(character, character)
                    : character;
            event.register(ModelResourceLocation.standalone(
                    ResourceLocation.fromNamespaceAndPath(ChrisMurderDronesMod.MODID, "item/" + itemPath)
            ));

            for (int skin = 1; skin < PlushieBlockEntity.getSkinCount(character); skin++) {
                event.register(ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(ChrisMurderDronesMod.MODID,
                                "block/" + ModBlocks.getModelPath(character, character + "_alt" + skin))
                ));
            }
        }

        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(ChrisMurderDronesMod.MODID, "block/plushie_base")));
    }
}