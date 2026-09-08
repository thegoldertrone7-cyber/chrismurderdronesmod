package chris.murderdrones.client;

import chris.murderdrones.Constants;
import chris.murderdrones.block.ModBlocksCommon;
import chris.murderdrones.block.PlushieBaseBlockEntity;
import chris.murderdrones.block.PlushieBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class PlushieBaseBlockEntityRenderer implements BlockEntityRenderer<PlushieBaseBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    private static final ResourceLocation PLATE_MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/plushie_base");

    public PlushieBaseBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(PlushieBaseBlockEntity be, float partialTick, PoseStack poseStack,
                        MultiBufferSource buffer, int packedLight, int packedOverlay) {

        BlockState state = be.getBlockState();

        if (be.isBaseVisible()) {
            BakedModel plateModel = ClientServices.MODEL_LOOKUP.getStandaloneModel(PLATE_MODEL_ID);
            poseStack.pushPose();
            VertexConsumer plateConsumer = buffer.getBuffer(Sheets.translucentCullBlockSheet());
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(), plateConsumer, state, plateModel,
                    1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        if (!be.hasPlushie()) return;

        String character = be.getCharacter();
        int skin = be.getSkinIndex();
        float angle = be.getRotation() * 22.5F;
        // Pixel-perfect offset: 0-15 sixteenths of a block, same convention as the base plate's own grid.
        double offX = be.getOffsetX() / 16.0D;
        double offZ = be.getOffsetZ() / 16.0D;

        String modelName = character;
        if (skin > 0 && PlushieBlockEntity.hasSkins(character)) {
            modelName = character + "_alt" + skin;
        }

        String modelPath = ModBlocksCommon.getModelPath(character, modelName);
        ResourceLocation baseLocation = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID, "block/" + modelPath);
        BakedModel model = ClientServices.MODEL_LOOKUP.getStandaloneModel(baseLocation);

        // Sits on top of the plate (2px up) when the plate is visible; flush with the
        // ground when it's toggled invisible, instead of floating at plate height.
        double baseLift = be.isBaseVisible() ? 2.0D / 16.0D : 0.0D;

        PlushieBlockEntity.AnimationState anim = be.getAnimationState();

        poseStack.pushPose();
        poseStack.translate(offX, baseLift + anim.yOffset(), offZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        poseStack.scale(anim.scaleXZ(), anim.scaleY(), anim.scaleXZ());
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        VertexConsumer normal = buffer.getBuffer(Sheets.translucentCullBlockSheet());
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), normal, state, model,
                1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(PlushieBaseBlockEntity be) {
        return true;
    }
}
