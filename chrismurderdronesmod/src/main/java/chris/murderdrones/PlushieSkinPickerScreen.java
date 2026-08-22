package chris.murderdrones;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight per-block skin picker, opened via {@link PlushieSkinPickerOpener}'s
 * configurable hotkey while looking at a placed plushie with the screwdriver in hand.
 * Replaces the old Assembly Machine — there's no block/menu/container involved here at
 * all: clicking a skin fires {@link SetPlushieSkinPayload} straight at the block you
 * were already looking at, and since it's a real placed block the change shows up live
 * (and reverts to the block's own skin if you close without picking anything).
 * <p>
 * The grid + rotatable preview are the same visual language as the Assembly Machine's
 * Box B / Box C used to be, just drawn as a flat panel instead of over a texture, since
 * there's no vanilla container GUI to piggyback on anymore.
 */
public class PlushieSkinPickerScreen extends Screen {

    private static final int PANEL_WIDTH  = 288;
    private static final int PANEL_HEIGHT = 176;
    private static final int GRID_WIDTH   = 150;
    private static final int PREVIEW_SIZE = 108;
    private static final int CARD_SIZE = 24;
    private static final int CARD_GAP  = 4;

    private static final int COLOR_BG_DIM        = 0xB0000000;
    private static final int COLOR_PANEL         = 0xF0161616;
    private static final int COLOR_PANEL_BORDER  = 0x30FFFFFF;
    private static final int COLOR_CARD          = 0x2CFFFFFF;
    private static final int COLOR_CARD_HOVER    = 0x50FFFFFF;
    private static final int COLOR_CARD_SELECTED = 0xA04FA8FF;
    private static final int COLOR_TEXT_DIM      = 0x99CCCCCC;
    private static final int COLOR_TEXT_LABEL    = 0xFFC7C7C7;
    private static final int DEFAULT_ACCENT      = 0xFF4FA8FF;

    private static final float MIN_SCALE = 40.0F;
    private static final float MAX_SCALE = 160.0F;

    private final BlockPos pos;
    private final CharacterEntry entry;
    private final int accentColor;

    private int panelLeft, panelTop;
    private int gridLeft, gridTop, gridHeight;
    private int previewLeft, previewTop;

    private double scrollOffset = 0;
    private int contentHeight = 0;
    private final List<int[]> cardBounds = new ArrayList<>();
    private final List<Integer> cardSkin = new ArrayList<>();

    private float previewYaw = 180.0F;
    private float previewPitch = 0.0F;
    private float previewScale = 90.0F;
    private boolean draggingPreview = false;
    private double lastMouseX, lastMouseY;

    public PlushieSkinPickerScreen(BlockPos pos, CharacterEntry entry) {
        super(Component.literal(entry.displayName() + " Skins"));
        this.pos = pos;
        this.entry = entry;

        String character = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(entry.item().get()).getPath();
        PlushieCategory owner = PlushieBrowserOpener.findCategoryFor(character);
        this.accentColor = owner != null ? owner.accentColor() : DEFAULT_ACCENT;
    }

    @Override
    protected void init() {
        panelLeft = (this.width - PANEL_WIDTH) / 2;
        panelTop  = (this.height - PANEL_HEIGHT) / 2;

        gridLeft = panelLeft + 16;
        gridTop  = panelTop + 34;
        gridHeight = PANEL_HEIGHT - 34 - 16;

        previewLeft = panelLeft + PANEL_WIDTH - PREVIEW_SIZE - 16;
        previewTop  = panelTop + 34;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBlurredBackground(float partialTick) {
        // Flat dim overlay instead of the vanilla blur, matching the catalog screen.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, COLOR_BG_DIM);

        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, COLOR_PANEL);
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + 1, accentColor);
        graphics.fill(panelLeft, panelTop, panelLeft + 1, panelTop + PANEL_HEIGHT, COLOR_PANEL_BORDER);
        graphics.fill(panelLeft + PANEL_WIDTH - 1, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, COLOR_PANEL_BORDER);
        graphics.fill(panelLeft, panelTop + PANEL_HEIGHT - 1, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, COLOR_PANEL_BORDER);

        graphics.drawString(this.font, this.title, panelLeft + 16, panelTop + 12, COLOR_TEXT_LABEL, false);

        renderGrid(graphics, mouseX, mouseY);
        renderPreview(graphics);

        String hint = "Esc to close  •  Drag to rotate  •  Scroll to zoom  •  R resets";
        int hintWidth = this.font.width(hint);
        graphics.drawString(this.font, hint, panelLeft + (PANEL_WIDTH - hintWidth) / 2,
                panelTop + PANEL_HEIGHT + 6, COLOR_TEXT_DIM, false);

        super.render(graphics, mouseX, mouseY, partialTick);
        renderCardTooltip(graphics, mouseX, mouseY);
    }

    // ── Grid ─────────────────────────────────────────────────────────────────

    private void renderGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        cardBounds.clear();
        cardSkin.clear();

        int height = gridHeight;
        int selected = getCurrentSkin();

        graphics.enableScissor(gridLeft, gridTop, gridLeft + GRID_WIDTH, gridTop + height);

        int maxCols = Math.max(1, GRID_WIDTH / (CARD_SIZE + CARD_GAP));
        int cursorY = gridTop - (int) scrollOffset;
        int col = 0;

        for (int skin = 0; skin < entry.skinCount(); skin++) {
            int x = gridLeft + col * (CARD_SIZE + CARD_GAP);
            int y = cursorY;

            if (y + CARD_SIZE > gridTop && y < gridTop + height) {
                boolean isSelected = skin == selected;
                boolean isHover = mouseX >= x && mouseX < x + CARD_SIZE && mouseY >= y && mouseY < y + CARD_SIZE
                        && mouseY >= gridTop && mouseY < gridTop + height;

                graphics.fill(x, y, x + CARD_SIZE, y + CARD_SIZE,
                        isSelected ? COLOR_CARD_SELECTED : (isHover ? COLOR_CARD_HOVER : COLOR_CARD));

                ItemStack previewStack = PlushieBlockEntity.withSkin(entry.item().get().getDefaultInstance(), skin);
                renderIcon(graphics, previewStack, x + CARD_SIZE / 2, y + CARD_SIZE / 2);
            }

            cardBounds.add(new int[]{x, y, CARD_SIZE, CARD_SIZE});
            cardSkin.add(skin);

            col++;
            if (col >= maxCols) {
                col = 0;
                cursorY += CARD_SIZE + CARD_GAP;
            }
        }
        if (col != 0) cursorY += CARD_SIZE + CARD_GAP;
        contentHeight = (cursorY + (int) scrollOffset) - gridTop;

        graphics.disableScissor();

        int maxScroll = Math.max(0, contentHeight - height);
        if (maxScroll > 0) {
            int trackX = gridLeft + GRID_WIDTH - 2;
            int thumbH = Math.max(6, height * height / Math.max(1, contentHeight));
            int thumbY = gridTop + (int) ((height - thumbH) * (scrollOffset / (double) maxScroll));
            graphics.fill(trackX, gridTop, trackX + 2, gridTop + height, 0x40000000);
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0x90000000);
        }
    }

    private void renderIcon(GuiGraphics graphics, ItemStack stack, int centerX, int centerY) {
        PoseStack pose = graphics.pose();
        float scale = (CARD_SIZE - 6) / 16.0F;
        pose.pushPose();
        pose.translate(centerX - 8 * scale, centerY - 8 * scale, 0);
        pose.scale(scale, scale, 1.0F);
        graphics.renderItem(stack, 0, 0);
        pose.popPose();
    }

    // ── Preview ──────────────────────────────────────────────────────────────

    private void renderPreview(GuiGraphics graphics) {
        int centerX = previewLeft + PREVIEW_SIZE / 2;
        int centerY = previewTop + PREVIEW_SIZE / 2;

        graphics.fill(previewLeft, previewTop, previewLeft + PREVIEW_SIZE, previewTop + PREVIEW_SIZE, 0x30000000);

        ItemStack previewStack = PlushieBlockEntity.withSkin(entry.item().get().getDefaultInstance(), getCurrentSkin());

        graphics.enableScissor(previewLeft, previewTop, previewLeft + PREVIEW_SIZE, previewTop + PREVIEW_SIZE);
        renderRotatableItem(graphics, previewStack, centerX, centerY, previewScale, previewYaw, previewPitch);
        graphics.disableScissor();

        String label = entry.fullLabel(getCurrentSkin());
        int labelWidth = this.font.width(label);
        graphics.drawString(this.font, label, centerX - labelWidth / 2, previewTop + PREVIEW_SIZE + 4, COLOR_TEXT_LABEL, true);
    }

    private void renderRotatableItem(GuiGraphics graphics, ItemStack stack, int centerX, int centerY,
                                     float scale, float yaw, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getItemRenderer().getModel(stack, null, null, 0);

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 150.0);
        pose.scale(scale, -scale, scale);
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        pose.mulPose(Axis.YP.rotationDegrees(yaw));

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        mc.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose, bufferSource,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        bufferSource.endBatch();

        pose.popPose();
    }

    private void renderCardTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int i = 0; i < cardBounds.size(); i++) {
            int[] b = cardBounds.get(i);
            if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]
                    && mouseY >= gridTop && mouseY < gridTop + gridHeight) {
                graphics.renderTooltip(this.font, Component.literal(entry.skinLabel(cardSkin.get(i))), mouseX, mouseY);
                break;
            }
        }
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    /** Reads the live skin index straight off the client's copy of the block entity. */
    private int getCurrentSkin() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 0;
        if (mc.level.getBlockEntity(pos) instanceof PlushieBlockEntity plushie) {
            return plushie.getSkinIndex();
        }
        return 0;
    }

    // ── Input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < cardBounds.size(); i++) {
                int[] b = cardBounds.get(i);
                if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]
                        && mouseY >= gridTop && mouseY < gridTop + gridHeight) {
                    int skin = cardSkin.get(i);
                    PacketDistributor.sendToServer(new SetPlushieSkinPayload(pos, skin));
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }

            if (mouseX >= previewLeft && mouseX < previewLeft + PREVIEW_SIZE
                    && mouseY >= previewTop && mouseY < previewTop + PREVIEW_SIZE) {
                draggingPreview = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPreview) {
            previewYaw += (float) (mouseX - lastMouseX) * 0.9F;
            previewPitch += (float) (mouseY - lastMouseY) * 0.9F;
            previewPitch = Math.max(-89.0F, Math.min(89.0F, previewPitch));
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) draggingPreview = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= previewLeft && mouseX < previewLeft + PREVIEW_SIZE
                && mouseY >= previewTop && mouseY < previewTop + PREVIEW_SIZE) {
            previewScale += (float) scrollY * 8.0F;
            previewScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, previewScale));
            return true;
        }
        if (mouseX >= gridLeft && mouseX < gridLeft + GRID_WIDTH
                && mouseY >= gridTop && mouseY < gridTop + gridHeight) {
            scrollOffset -= scrollY * 12;
            int maxScroll = Math.max(0, contentHeight - gridHeight);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) { // R
            previewYaw = 180.0F;
            previewPitch = 0.0F;
            previewScale = 90.0F;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}