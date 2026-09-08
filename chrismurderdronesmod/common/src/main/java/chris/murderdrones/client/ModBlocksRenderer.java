package chris.murderdrones.client;

import chris.murderdrones.Constants;
import chris.murderdrones.block.ModBlocksCommon;
import chris.murderdrones.block.PlushieBlockEntity;
import chris.murderdrones.client.ClientServices;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ModBlocksRenderer implements BlockEntityRenderer<PlushieBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public ModBlocksRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    /** Resolves the registry path of the block (e.g. "uzi", "n", "v", ...) */
    private static String getCharacter(PlushieBlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(state.getBlock()).getPath();
    }

    @Override
    public void render(PlushieBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        BlockState state = blockEntity.getBlockState();
        if (!state.hasProperty(ModBlocksCommon.ROTATION)) return;

        String character = getCharacter(blockEntity);
        int rotation     = state.getValue(ModBlocksCommon.ROTATION);
        float angle      = rotation * 22.5F;

        PlushieBlockEntity.AnimationState anim = blockEntity.getAnimationState();

        // For Uzi, N, and V, pick the skin-variant model; all others use the base model.
        String modelName = character;
        String textureName = character;

        int skin = blockEntity.getSkinIndex();
        if (skin > 0 && PlushieBlockEntity.hasSkins(character)) {
            modelName   = character + "_alt" + skin;
            textureName = character + "_alt" + skin;
        }

        // Model paths: assets/chrismurderdronesmod/models/block/<folder>/<modelName>.json
        String modelPath = ModBlocksCommon.getModelPath(character, modelName);
        ResourceLocation baseLocation = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID, "block/" + modelPath);
        BakedModel model = ClientServices.MODEL_LOOKUP.getStandaloneModel(baseLocation);

        // Emissive texture path: assets/chrismurderdronesmod/textures/block/<textureName>_e.png
        RenderType emissiveType = RenderType.eyes(
                ResourceLocation.fromNamespaceAndPath(
                        Constants.MOD_ID, "textures/block/" + textureName + "_e.png"));

        poseStack.pushPose();

        // 1. Move to block centre and apply Y offset (jump/anticipation)
        poseStack.translate(0.5D, anim.yOffset(), 0.5D);

        // 2. Rotate around the block's Y axis
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle));

        // 3. Squash & stretch — scale around the bottom of the model (Y=0 in block space)
        //    We translate the pivot to the bottom centre, scale, then translate back.
        poseStack.translate(0.0D, 0.0D, 0.0D); // pivot already at bottom-centre after prior translates
        poseStack.scale(anim.scaleXZ(), anim.scaleY(), anim.scaleXZ());

        // 4. Move back to block-space origin for model rendering
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        // Pass 1: normal render with world lighting
        VertexConsumer normal = buffer.getBuffer(Sheets.translucentCullBlockSheet());
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), normal, state, model,
                1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(PlushieBlockEntity blockEntity) {
        return true;
    }
}