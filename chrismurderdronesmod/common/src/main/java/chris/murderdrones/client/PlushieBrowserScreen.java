package chris.murderdrones.client;

import chris.murderdrones.CharacterEntry;
import chris.murderdrones.PlushieCategory;
import chris.murderdrones.PlushieFavorites;
import chris.murderdrones.network.GivePlushiePayload;
import chris.murderdrones.platform.Services;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Immersive, gallery-style catalog of every plushie skin.
 *
 * Layout: a slim category-tab rail on the far left, a scrollable filterable icon
 * grid in the middle, and a "spotlight" preview stage on the right that slowly
 * turns the selected plushie on its own when you're not dragging it, with quick
 * prev/next arrows to flip through a character's skins without going back to the grid.
 *
 * Skin display names are NOT edited here in-game — they're defined in code via
 * {@link CharacterEntry#skinNames()}. See PlushieCharacters for where each
 * CharacterEntry is built; pass a List.of("Name1", "Name2", ...) there to rename skins.
 */
public class PlushieBrowserScreen extends Screen {

    // ── Layout constants ───────────────────────────────────────────────────────
    private static final int MARGIN            = 16;
    private static final int TOP_BAR_HEIGHT     = 28;
    private static final int CATEGORY_HEADER_H  = 20;
    private static final int CHARACTER_LABEL_H  = 13;
    private static final int CARD_SIZE          = 60;
    private static final int CARD_ICON_SIZE     = 28;
    private static final int CARD_GAP           = 6;
    private static final int CHARACTER_GAP      = 8;
    private static final int CATEGORY_GAP       = 10;
    private static final int PREVIEW_SIZE       = 188;
    private static final int SIDEBAR_WIDTH      = 92;
    private static final int TAB_HEIGHT         = 34;

    private static final int COLOR_BG_DIM        = 0xD0000000;
    private static final int COLOR_PANEL         = 0xF0161616;
    private static final int COLOR_PANEL_BORDER  = 0x30FFFFFF;
    private static final int COLOR_CARD          = 0x2CFFFFFF;
    private static final int COLOR_CARD_HOVER    = 0x50FFFFFF;
    private static final int COLOR_TEXT_DIM      = 0x99CCCCCC;
    private static final int COLOR_TEXT_LABEL    = 0xFFC7C7C7;
    private static final int COLOR_ACCENT        = 0xFF4FA8FF;

    private static final long FADE_IN_MS   = 260L;
    private static final long IDLE_SPIN_DELAY_MS = 1200L;
    private static final float IDLE_SPIN_DEG_PER_SEC = 12.0F;

    private final List<PlushieCategory> categories;

    // Left rail (category tabs) geometry
    private int railLeft, railTop, railHeight;
    private final List<int[]> tabBounds = new ArrayList<>();          // x, y, w, h
    private final List<Integer> tabTargetScroll = new ArrayList<>();  // scroll offset each tab jumps to

    // Middle panel (grid) geometry
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private double scrollOffset = 0;
    private int contentHeight;

    // Right panel (preview) geometry
    private int previewLeft, previewTop;
    private int[] prevArrowBounds;
    private int[] nextArrowBounds;
    private int[] giveButtonBounds;

    private final List<int[]> cardBounds = new ArrayList<>();       // x, y, w, h
    private final List<ItemStack> cardStacks = new ArrayList<>();
    private final List<String> cardTooltips = new ArrayList<>();    // full "Character – Skin" label
    private final List<int[]> cardFavoriteBounds = new ArrayList<>(); // star hit-box, aligned with cardBounds
    private final List<String> cardFavoriteKeys = new ArrayList<>();  // PlushieFavorites key, aligned with cardBounds

    // "Favorites" rail tab, toggled instead of jumping to a scroll offset
    private boolean showFavoritesOnly = false;

    // Recipe popup state — set when "View Recipe" is clicked for the current selection
    private boolean showRecipePopup = false;
    private boolean recipeLookupAttempted = false;
    private RecipeHolder<CraftingRecipe> selectedRecipe = null;
    private int[] favoriteStarBounds; // star toggle next to the preview label
    private int[] recipeButtonBounds;

    // Set during the grid pass when the hovered card's label got truncated; rendered
    // as a real tooltip after everything else so it isn't drawn under later cards.
    private String hoveredCardTooltip = null;

    // Selection + preview state
    private ItemStack selectedStack = null;
    private String selectedLabel = null;
    private CharacterEntry selectedEntry = null;
    private int selectedSkinIndex = 0;
    private int selectedAccentColor = COLOR_ACCENT;

    private float previewYaw = 0.0F; // Default rotation: faces north
    private float previewPitch = 0.0F;
    private float previewScale = 116.0F;
    private static final float MIN_SCALE = 40.0F;
    private static final float MAX_SCALE = 260.0F;

    private boolean draggingPreview = false;
    private double lastMouseX, lastMouseY;
    private long lastInteractionTime = 0L;
    private long lastFrameNanos = 0L;

    private long openTimeMillis = 0L;

    private EditBox filterBox;

    public PlushieBrowserScreen(List<PlushieCategory> categories) {
        super(Component.literal("Plushie Catalog"));
        this.categories = categories;
    }

    // ── Init / layout ───────────────────────────────────────────────────────────

    @Override
    protected void init() {
        openTimeMillis = System.currentTimeMillis();
        lastFrameNanos = System.nanoTime();
        lastInteractionTime = System.currentTimeMillis();

        railLeft = MARGIN;
        railTop = MARGIN + TOP_BAR_HEIGHT + 6;

        panelLeft = railLeft + SIDEBAR_WIDTH + 10;
        panelTop = railTop;
        panelWidth = Math.max(260, Math.min(320, (int) (this.width * 0.36)));
        panelHeight = this.height - panelTop - MARGIN;
        railHeight = panelHeight;

        previewLeft = panelLeft + panelWidth + 20;
        previewTop = panelTop;

        filterBox = new EditBox(this.font, panelLeft, MARGIN + 4, panelWidth, 18, Component.literal("Filter"));
        filterBox.setHint(Component.literal("Filter plushies..."));
        filterBox.setResponder(s -> scrollOffset = 0);
        addRenderableWidget(filterBox);
        this.setInitialFocus(filterBox); // so you can start typing to filter the moment the catalog opens
    }

    @Override
    public void renderBlurredBackground(float partialTick) {
        // Skip the vanilla blur pass for a flat, fast overlay
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = System.nanoTime();
        float deltaSeconds = Math.min(0.1F, (now - lastFrameNanos) / 1_000_000_000.0F);
        lastFrameNanos = now;

        long elapsedOpenMs = System.currentTimeMillis() - openTimeMillis;
        float fadeT = Math.min(1.0F, elapsedOpenMs / (float) FADE_IN_MS);
        float easedFade = fadeT * fadeT * (3f - 2f * fadeT);
        int slideOffset = (int) ((1.0F - easedFade) * 14);

        renderThemedBackground(graphics, partialTick);

        graphics.pose().pushPose();
        graphics.pose().translate(0, slideOffset, 0);

        drawGlowingTitle(graphics);

        hoveredCardTooltip = null;
        renderCategoryRail(graphics, mouseX, mouseY - slideOffset);
        renderLeftPanel(graphics, mouseX, mouseY - slideOffset);
        renderPreviewPanel(graphics, mouseX, mouseY - slideOffset, deltaSeconds);

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.pose().popPose();

        // Drawn after the pose pop (and after every card) so it sits on top and isn't
        // offset by the fade-in slide.
        if (hoveredCardTooltip != null) {
            graphics.renderTooltip(this.font, Component.literal(hoveredCardTooltip), mouseX, mouseY);
        }

        // Fade-in veil, drawn last so it doesn't affect widget hit-testing
        if (fadeT < 1.0F) {
            int alpha = (int) ((1.0F - easedFade) * 255) << 24;
            graphics.fill(0, 0, this.width, this.height, alpha);
        }
    }

    private void renderThemedBackground(GuiGraphics graphics, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, COLOR_BG_DIM);

        // Soft themed wash pulled from whatever plushie is currently selected,
        // otherwise a gentle neutral glow — keeps the whole screen feeling alive
        // instead of a flat grey overlay.
        int accent = selectedAccentColor;
        int washTop = (accent & 0x00FFFFFF) | 0x22000000;
        graphics.fillGradient(0, 0, this.width, this.height / 2, washTop, 0x00000000);

        // Vignette
        int vig = 0x66000000;
        graphics.fillGradient(0, 0, this.width, 40, 0x99000000, 0x00000000);
        graphics.fillGradient(0, this.height - 40, this.width, this.height, 0x00000000, 0x99000000);
        graphics.fillGradient(0, 0, 40, this.height, vig, 0x00000000);
        graphics.fillGradient(this.width - 40, 0, this.width, this.height, 0x00000000, vig);
    }

    private void drawGlowingTitle(GuiGraphics graphics) {
        String titleText = "Plushie Catalog";
        int tx = railLeft;
        int ty = MARGIN - 10;
        // Faux glow: a dim wide copy behind the crisp one
        graphics.drawString(this.font, titleText, tx, ty, (selectedAccentColor & 0x00FFFFFF) | 0x55000000, false);
        graphics.drawString(this.font, titleText, tx, ty, 0xFFFFFFFF, true);

        int totalSkins = categories.stream()
                .flatMap(c -> c.characters().stream())
                .mapToInt(CharacterEntry::skinCount)
                .sum();
        String subtitle = categories.size() + " categories  •  " + totalSkins + " skins";
        graphics.drawString(this.font, subtitle, tx + this.font.width(titleText) + 10, ty + 1, COLOR_TEXT_DIM, false);
    }

    // ── Category rail ───────────────────────────────────────────────────────────

    private void renderCategoryRail(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(railLeft, railTop, railLeft + SIDEBAR_WIDTH, railTop + railHeight, COLOR_PANEL);
        graphics.fill(railLeft, railTop, railLeft + SIDEBAR_WIDTH, railTop + 1, COLOR_PANEL_BORDER);
        graphics.fill(railLeft, railTop + railHeight - 1, railLeft + SIDEBAR_WIDTH, railTop + railHeight, COLOR_PANEL_BORDER);

        tabBounds.clear();
        tabTargetScroll.clear();

        int cursorY = railTop + 4;

        // "All" tab resets the filter and jumps to the top
        boolean allHovered = mouseX >= railLeft && mouseX < railLeft + SIDEBAR_WIDTH
                && mouseY >= cursorY && mouseY < cursorY + TAB_HEIGHT;
        boolean allActive = !showFavoritesOnly && scrollOffset < 4 && (filterBox == null || filterBox.getValue().isBlank());
        drawTab(graphics, railLeft + 3, cursorY, SIDEBAR_WIDTH - 6, TAB_HEIGHT - 4, 0xFF808080, "All", allHovered, allActive);
        tabBounds.add(new int[]{railLeft + 3, cursorY, SIDEBAR_WIDTH - 6, TAB_HEIGHT - 4});
        tabTargetScroll.add(-1); // special: -1 means "reset filter + scroll to top"
        cursorY += TAB_HEIGHT;

        // "Favorites" tab filters the grid down to only starred skins, across all categories
        boolean favHovered = mouseX >= railLeft && mouseX < railLeft + SIDEBAR_WIDTH
                && mouseY >= cursorY && mouseY < cursorY + TAB_HEIGHT;
        drawTab(graphics, railLeft + 3, cursorY, SIDEBAR_WIDTH - 6, TAB_HEIGHT - 4, 0xFFFFD700, "★ Favorites", favHovered, showFavoritesOnly);
        tabBounds.add(new int[]{railLeft + 3, cursorY, SIDEBAR_WIDTH - 6, TAB_HEIGHT - 4});
        tabTargetScroll.add(-2); // special: -2 means "toggle favorites-only view"
        cursorY += TAB_HEIGHT;

        int activeCatByScroll = -1;
        int[] catStartOffsets = new int[categories.size()];
        int running = 0;
        String filter = filterBox != null ? filterBox.getValue().trim().toLowerCase() : "";
        for (int i = 0; i < categories.size(); i++) {
            PlushieCategory category = categories.get(i);
            catStartOffsets[i] = running;
            running += measureCategoryHeight(category, filter);
        }
        for (int i = 0; i < categories.size(); i++) {
            if (catStartOffsets[i] <= scrollOffset + 4) activeCatByScroll = i;
        }

        for (int i = 0; i < categories.size(); i++) {
            PlushieCategory category = categories.get(i);
            boolean hasMatch = category.characters().stream().anyMatch(e -> characterMatches(category, e, filter));
            if (!hasMatch) continue;

            boolean hovered = mouseX >= railLeft && mouseX < railLeft + SIDEBAR_WIDTH
                    && mouseY >= cursorY && mouseY < cursorY + TAB_HEIGHT;
            boolean active = !showFavoritesOnly && i == activeCatByScroll;
            drawTab(graphics, railLeft + 3, cursorY, SIDEBAR_WIDTH - 6, TAB_HEIGHT - 4,
                    category.accentColor(), category.title(), hovered, active);

            tabBounds.add(new int[]{railLeft + 3, cursorY, SIDEBAR_WIDTH - 6, TAB_HEIGHT - 4});
            tabTargetScroll.add(catStartOffsets[i]);
            cursorY += TAB_HEIGHT;
        }
    }

    private void drawTab(GuiGraphics graphics, int x, int y, int w, int h, int accent, String label, boolean hovered, boolean active) {
        int bg = active ? (accent & 0x00FFFFFF) | 0x60000000 : (hovered ? 0x30FFFFFF : 0x18FFFFFF);
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.fill(x, y, x + 3, y + h, active ? (accent | 0xFF000000) : ((accent & 0x00FFFFFF) | 0x60000000));

        List<String> lines = wrapLabel(label, w - 8);
        int textY = y + (h - lines.size() * 9) / 2;
        for (String line : lines) {
            graphics.drawString(this.font, line, x + 8, textY, active ? 0xFFFFFFFF : COLOR_TEXT_LABEL, false);
            textY += 9;
        }
    }

    private List<String> wrapLabel(String label, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (this.font.width(label) <= maxWidth) {
            lines.add(label);
            return lines;
        }
        String[] words = label.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (this.font.width(candidate) > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    /** Same walk as renderLeftPanel's layout pass, but height-only (no drawing) — used for tab jump targets. */
    private int measureCategoryHeight(PlushieCategory category, String filter) {
        boolean categoryHasMatch = category.characters().stream().anyMatch(e -> characterMatches(category, e, filter));
        if (!categoryHasMatch) return 0; // mirrors renderLeftPanel, which skips the whole category (no header drawn)

        int innerWidth = panelWidth - 16;
        int maxCols = Math.max(1, innerWidth / (CARD_SIZE + CARD_GAP));
        int h = CATEGORY_HEADER_H + 6;
        for (CharacterEntry entry : category.characters()) {
            if (!characterMatches(category, entry, filter)) continue;
            h += CHARACTER_LABEL_H + 3;
            int matchingSkins = 0;
            for (int skin = 0; skin < entry.skinCount(); skin++) {
                if (skinMatches(category, entry, skin, filter)) matchingSkins++;
            }
            int rows = (int) Math.ceil(matchingSkins / (double) maxCols);
            h += rows * (CARD_SIZE + CARD_GAP);
            h += CHARACTER_GAP;
        }
        h += CATEGORY_GAP - CHARACTER_GAP;
        return h;
    }

    // ── Middle grid panel ───────────────────────────────────────────────────────

    private void renderLeftPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, COLOR_PANEL);
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 1, COLOR_PANEL_BORDER);
        graphics.fill(panelLeft, panelTop + panelHeight - 1, panelLeft + panelWidth, panelTop + panelHeight, COLOR_PANEL_BORDER);
        graphics.fill(panelLeft, panelTop, panelLeft + 1, panelTop + panelHeight, COLOR_PANEL_BORDER);
        graphics.fill(panelLeft + panelWidth - 1, panelTop, panelLeft + panelWidth, panelTop + panelHeight, COLOR_PANEL_BORDER);

        graphics.enableScissor(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight);

        cardBounds.clear();
        cardStacks.clear();
        cardTooltips.clear();
        cardFavoriteBounds.clear();
        cardFavoriteKeys.clear();

        String filter = filterBox != null ? filterBox.getValue().trim().toLowerCase() : "";
        int innerLeft = panelLeft + 8;
        int innerWidth = panelWidth - 16;
        int maxCols = Math.max(1, innerWidth / (CARD_SIZE + CARD_GAP));

        int cursorY = panelTop + 6 - (int) scrollOffset;
        long pulseNow = System.currentTimeMillis();

        boolean anyMatch = false;
        for (PlushieCategory category : categories) {
            boolean categoryHasMatch = category.characters().stream().anyMatch(e -> characterMatches(category, e, filter));
            if (!categoryHasMatch) continue;
            anyMatch = true;

            if (cursorY + CATEGORY_HEADER_H > panelTop && cursorY < panelTop + panelHeight) {
                graphics.fillGradient(innerLeft, cursorY, innerLeft + innerWidth, cursorY + CATEGORY_HEADER_H,
                        category.accentColor(), (category.accentColor() & 0x00FFFFFF) | 0x60000000);
                graphics.drawString(this.font, category.title(), innerLeft + 6, cursorY + 6, 0xFFFFFFFF, true);
            }
            cursorY += CATEGORY_HEADER_H + 6;

            for (CharacterEntry entry : category.characters()) {
                if (!characterMatches(category, entry, filter)) continue;

                if (cursorY + CHARACTER_LABEL_H > panelTop && cursorY < panelTop + panelHeight) {
                    graphics.drawString(this.font, entry.displayName(), innerLeft + 2, cursorY, COLOR_TEXT_LABEL, false);
                }
                cursorY += CHARACTER_LABEL_H + 3;

                int col = 0;
                int rowY = cursorY;
                for (int skin = 0; skin < entry.skinCount(); skin++) {
                    if (!skinMatches(category, entry, skin, filter)) continue;

                    String fullLabel = entry.fullLabel(skin);
                    ItemStack stack = buildStack(entry, skin);

                    int x = innerLeft + col * (CARD_SIZE + CARD_GAP);
                    int y = rowY;

                    if (y + CARD_SIZE > panelTop && y < panelTop + panelHeight) {
                        boolean selected = fullLabel.equals(selectedLabel);
                        boolean hovered = mouseX >= x && mouseX < x + CARD_SIZE && mouseY >= y && mouseY < y + CARD_SIZE
                                && mouseY >= panelTop && mouseY < panelTop + panelHeight;

                        int drawX = x, drawY = y, drawSize = CARD_SIZE;
                        if (hovered && !selected) {
                            drawY -= 2; // gentle lift on hover
                        }

                        int bg = selected ? ((category.accentColor() & 0x00FFFFFF) | 0x55000000) : (hovered ? COLOR_CARD_HOVER : COLOR_CARD);
                        graphics.fill(drawX, drawY, drawX + drawSize, drawY + drawSize, bg);

                        if (selected) {
                            // Animated pulsing glow ring around the selected card
                            float pulse = (float) (0.5 + 0.5 * Math.sin(pulseNow / 220.0));
                            int glowAlpha = (int) (120 + pulse * 100);
                            int glow = (glowAlpha << 24) | (category.accentColor() & 0x00FFFFFF);
                            graphics.fill(drawX, drawY, drawX + drawSize, drawY + 2, glow);
                            graphics.fill(drawX, drawY + drawSize - 2, drawX + drawSize, drawY + drawSize, glow);
                            graphics.fill(drawX, drawY, drawX + 2, drawY + drawSize, glow);
                            graphics.fill(drawX + drawSize - 2, drawY, drawX + drawSize, drawY + drawSize, glow);
                        } else if (hovered) {
                            graphics.fill(drawX, drawY, drawX + drawSize, drawY + 1, (category.accentColor() & 0x00FFFFFF) | 0x90000000);
                        }

                        renderScaledIcon(graphics, stack, drawX + drawSize / 2, drawY + 8 + CARD_ICON_SIZE / 2, CARD_ICON_SIZE);

                        String shortLabel = entry.skinLabel(skin);
                        String trimmed = this.font.plainSubstrByWidth(shortLabel, drawSize - 4);
                        int textX = drawX + (drawSize - this.font.width(trimmed)) / 2;
                        graphics.drawString(this.font, trimmed, textX, drawY + drawSize - 12, selected ? 0xFFFFFFFF : 0xCCCCCC, false);

                        if (hovered) {
                            hoveredCardTooltip = fullLabel;
                        }

                        // Favorite star, top-right corner of the card
                        String favKey = PlushieFavorites.keyFor(entry, skin);
                        boolean isFav = PlushieFavorites.isFavorite(favKey);
                        int starSize = 9;
                        int starX = drawX + drawSize - starSize - 1;
                        int starY = drawY + 1;
                        boolean starHovered = mouseX >= starX && mouseX < starX + starSize
                                && mouseY >= starY && mouseY < starY + starSize
                                && mouseY >= panelTop && mouseY < panelTop + panelHeight;
                        int starColor = isFav ? 0xFFFFD700 : (starHovered ? 0xFFFFFFFF : 0x80AAAAAA);
                        graphics.drawString(this.font, isFav ? "★" : "☆", starX, starY, starColor, false);
                        cardFavoriteBounds.add(new int[]{starX, starY, starSize, starSize});
                        cardFavoriteKeys.add(favKey);
                    } else {
                        // Not on-screen this frame — push a placeholder so the favorite-bounds
                        // list stays index-aligned with cardBounds/cardTooltips below.
                        cardFavoriteBounds.add(null);
                        cardFavoriteKeys.add(PlushieFavorites.keyFor(entry, skin));
                    }

                    cardBounds.add(new int[]{x, y, CARD_SIZE, CARD_SIZE});
                    cardStacks.add(stack);
                    cardTooltips.add(fullLabel);

                    col++;
                    if (col >= maxCols) {
                        col = 0;
                        rowY += CARD_SIZE + CARD_GAP;
                    }
                }
                if (col != 0) rowY += CARD_SIZE + CARD_GAP;
                cursorY = rowY + CHARACTER_GAP;
            }
            cursorY += CATEGORY_GAP - CHARACTER_GAP;
        }

        if (!anyMatch) {
            String msg = showFavoritesOnly
                    ? Component.translatable("chrismurderdronesmod.catalog.no_favorites").getString()
                    : "No plushies match \"" + filter + "\"";
            graphics.drawString(this.font, msg, innerLeft, panelTop + 10, COLOR_TEXT_DIM, false);
        }

        contentHeight = cursorY - (panelTop + 6) + (int) scrollOffset;
        graphics.disableScissor();

        int maxScroll = Math.max(0, contentHeight - panelHeight);
        if (maxScroll > 0) {
            int trackX = panelLeft + panelWidth - 4;
            int thumbH = Math.max(16, panelHeight * panelHeight / contentHeight);
            int thumbY = panelTop + (int) ((panelHeight - thumbH) * (scrollOffset / (double) maxScroll));
            graphics.fill(trackX, panelTop, trackX + 3, panelTop + panelHeight, 0x30FFFFFF);
            graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, (selectedAccentColor & 0x00FFFFFF) | 0xB0000000);
        }
    }

    private ItemStack buildStack(CharacterEntry entry, int skin) {
        ItemStack stack = new ItemStack(entry.item().get());
        if (skin > 0) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(skin));
        }
        return stack;
    }

    private boolean skinMatches(PlushieCategory category, CharacterEntry entry, int skin, String filter) {
        if (showFavoritesOnly && !PlushieFavorites.isFavorite(entry, skin)) return false;
        if (filter.isEmpty()) return true;
        return entry.fullLabel(skin).toLowerCase().contains(filter)
                || entry.displayName().toLowerCase().contains(filter)
                || category.title().toLowerCase().contains(filter);
    }

    private boolean characterMatches(PlushieCategory category, CharacterEntry entry, String filter) {
        for (int skin = 0; skin < entry.skinCount(); skin++) {
            if (skinMatches(category, entry, skin, filter)) return true;
        }
        return false;
    }

    private void renderScaledIcon(GuiGraphics graphics, ItemStack stack, int centerX, int centerY, int renderSize) {
        float s = renderSize / 16.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0);
        graphics.pose().scale(s, s, 1.0F);
        graphics.renderItem(stack, -8, -8);
        graphics.pose().popPose();
    }

    // ── Preview spotlight stage ─────────────────────────────────────────────────

    private void renderPreviewPanel(GuiGraphics graphics, int mouseX, int mouseY, float deltaSeconds) {
        int boxRight = previewLeft + PREVIEW_SIZE;
        int boxBottom = previewTop + PREVIEW_SIZE;

        graphics.fill(previewLeft, previewTop, boxRight, boxBottom, COLOR_PANEL);

        // Spotlight glow behind the plushie, tinted to the character's category —
        // now spans the full preview box top-to-bottom instead of just the lower portion.
        if (selectedStack != null) {
            int cx = previewLeft + PREVIEW_SIZE / 2;
            int cy = previewTop + PREVIEW_SIZE / 2;
            int glowColor = (selectedAccentColor & 0x00FFFFFF) | 0x50000000;
            graphics.fillGradient(previewLeft, previewTop, boxRight, boxBottom,
                    glowColor, 0x00000000);
            // Pedestal glow strip beneath the item
            int floorY = cy + 44;
            graphics.fillGradient(cx - 60, floorY, cx + 60, floorY + 3, (selectedAccentColor | 0xFF000000), 0x00000000);
        }

        graphics.fill(previewLeft, previewTop, boxRight, previewTop + 2, selectedAccentColor);
        graphics.fill(previewLeft, previewTop, previewLeft + 1, boxBottom, COLOR_PANEL_BORDER);
        graphics.fill(boxRight - 1, previewTop, boxRight, boxBottom, COLOR_PANEL_BORDER);
        graphics.fill(previewLeft, boxBottom - 1, boxRight, boxBottom, COLOR_PANEL_BORDER);

        graphics.enableScissor(previewLeft, previewTop, boxRight, boxBottom);
        if (selectedStack != null) {
            if (showRecipePopup) {
                renderRecipePopup(graphics, previewLeft, previewTop, PREVIEW_SIZE, PREVIEW_SIZE);
            } else {
                // Idle auto-rotate: only spins once the person's left it alone for a moment
                if (!draggingPreview && System.currentTimeMillis() - lastInteractionTime > IDLE_SPIN_DELAY_MS) {
                    previewYaw += IDLE_SPIN_DEG_PER_SEC * deltaSeconds;
                }
                renderRotatableItem(graphics, selectedStack, previewLeft + PREVIEW_SIZE / 2, previewTop + PREVIEW_SIZE / 2 - 6,
                        previewScale, previewYaw, previewPitch);
            }
        } else {
            String hint = "Select a plushie to preview";
            int tx = previewLeft + (PREVIEW_SIZE - this.font.width(hint)) / 2;
            int ty = previewTop + PREVIEW_SIZE / 2 - 4;
            graphics.drawString(this.font, hint, tx, ty, COLOR_TEXT_DIM, false);
        }
        graphics.disableScissor();

        prevArrowBounds = null;
        nextArrowBounds = null;
        giveButtonBounds = null;
        favoriteStarBounds = null;
        recipeButtonBounds = null;

        if (selectedStack != null) {
            boolean isFav = selectedEntry != null && PlushieFavorites.isFavorite(selectedEntry, selectedSkinIndex);
            String starGlyph = isFav ? "★" : "☆";
            int starW = this.font.width(starGlyph);
            graphics.drawString(this.font, selectedLabel, previewLeft, boxBottom + 6, 0xFFFFFFFF, true);
            boolean starHovered = mouseX >= previewLeft + this.font.width(selectedLabel) + 5
                    && mouseX < previewLeft + this.font.width(selectedLabel) + 5 + starW + 2
                    && mouseY >= boxBottom + 5 && mouseY < boxBottom + 15;
            int starColor = isFav ? 0xFFFFD700 : (starHovered ? 0xFFFFFFFF : 0x80AAAAAA);
            int starX = previewLeft + this.font.width(selectedLabel) + 5;
            graphics.drawString(this.font, starGlyph, starX, boxBottom + 6, starColor, false);
            favoriteStarBounds = new int[]{starX, boxBottom + 5, starW + 2, 10};

            graphics.drawString(this.font, "Drag to rotate  •  Scroll to zoom  •  R to reset",
                    previewLeft, boxBottom + 18, COLOR_TEXT_DIM, false);

            int cursorY = boxBottom + 30;

            if (selectedEntry != null && selectedEntry.skinCount() > 1) {
                int arrowY = cursorY;
                int arrowSize = 14;

                boolean prevHovered = mouseX >= previewLeft && mouseX < previewLeft + arrowSize
                        && mouseY >= arrowY && mouseY < arrowY + arrowSize;
                boolean nextHovered = mouseX >= previewLeft + 90 && mouseX < previewLeft + 90 + arrowSize
                        && mouseY >= arrowY && mouseY < arrowY + arrowSize;

                graphics.fill(previewLeft, arrowY, previewLeft + arrowSize, arrowY + arrowSize, prevHovered ? 0x50FFFFFF : 0x28FFFFFF);
                graphics.drawString(this.font, "<", previewLeft + 5, arrowY + 3, 0xFFFFFFFF, false);
                prevArrowBounds = new int[]{previewLeft, arrowY, arrowSize, arrowSize};

                graphics.fill(previewLeft + 90, arrowY, previewLeft + 90 + arrowSize, arrowY + arrowSize, nextHovered ? 0x50FFFFFF : 0x28FFFFFF);
                graphics.drawString(this.font, ">", previewLeft + 90 + 5, arrowY + 3, 0xFFFFFFFF, false);
                nextArrowBounds = new int[]{previewLeft + 90, arrowY, arrowSize, arrowSize};

                String counter = (selectedSkinIndex + 1) + " / " + selectedEntry.skinCount();
                int counterX = previewLeft + arrowSize + (90 - arrowSize - this.font.width(counter)) / 2;
                graphics.drawString(this.font, counter, counterX, arrowY + 3, COLOR_TEXT_DIM, false);

                cursorY += 18;
            }

            var mcPlayer = Minecraft.getInstance().player;
            int btnW = 116, btnH = 16;
            if (mcPlayer != null && mcPlayer.isCreative()) {
                int btnY = cursorY;
                boolean hovered = mouseX >= previewLeft && mouseX < previewLeft + btnW
                        && mouseY >= btnY && mouseY < btnY + btnH;

                int bg = hovered ? ((selectedAccentColor & 0x00FFFFFF) | 0xB0000000) : ((selectedAccentColor & 0x00FFFFFF) | 0x70000000);
                graphics.fill(previewLeft, btnY, previewLeft + btnW, btnY + btnH, bg);
                graphics.fill(previewLeft, btnY, previewLeft + btnW, btnY + 1, selectedAccentColor);

                String btnLabel = "Give (Creative)";
                int btnTx = previewLeft + (btnW - this.font.width(btnLabel)) / 2;
                graphics.drawString(this.font, btnLabel, btnTx, btnY + 4, 0xFFFFFFFF, false);

                giveButtonBounds = new int[]{previewLeft, btnY, btnW, btnH};
                cursorY += btnH + 4;
            }

            // "View Recipe" toggles an in-place popup over the item preview above
            // (looks up a crafting recipe producing this exact skin, falling back to
            // any recipe for the base item, the first time it's clicked).
            int recipeBtnY = cursorY;
            boolean recipeHovered = mouseX >= previewLeft && mouseX < previewLeft + btnW
                    && mouseY >= recipeBtnY && mouseY < recipeBtnY + btnH;
            int recipeBg = showRecipePopup ? ((selectedAccentColor & 0x00FFFFFF) | 0xB0000000)
                    : (recipeHovered ? 0x40FFFFFF : 0x20FFFFFF);
            graphics.fill(previewLeft, recipeBtnY, previewLeft + btnW, recipeBtnY + btnH, recipeBg);
            graphics.fill(previewLeft, recipeBtnY, previewLeft + btnW, recipeBtnY + 1, selectedAccentColor);
            String recipeLabel = Component.translatable(showRecipePopup
                    ? "chrismurderdronesmod.catalog.hide_recipe"
                    : "chrismurderdronesmod.catalog.view_recipe").getString();
            int recipeTx = previewLeft + (btnW - this.font.width(recipeLabel)) / 2;
            graphics.drawString(this.font, recipeLabel, recipeTx, recipeBtnY + 4, 0xFFFFFFFF, false);
            recipeButtonBounds = new int[]{previewLeft, recipeBtnY, btnW, btnH};
        }
    }

    // ── Recipe popup ─────────────────────────────────────────────────────────────

    private void ensureRecipeLookup() {
        if (recipeLookupAttempted || selectedStack == null) return;
        recipeLookupAttempted = true;
        selectedRecipe = findRecipeFor(selectedStack);
    }

    private RecipeHolder<CraftingRecipe> findRecipeFor(ItemStack stack) {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        var registries = level.registryAccess();
        RecipeHolder<CraftingRecipe> fallback = null;
        CustomModelData wantCmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        for (RecipeHolder<CraftingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            ItemStack result = holder.value().getResultItem(registries);
            if (result.getItem() != stack.getItem()) continue;
            if (fallback == null) fallback = holder;
            CustomModelData gotCmd = result.get(DataComponents.CUSTOM_MODEL_DATA);
            boolean skinMatch = (wantCmd == null && gotCmd == null) || (wantCmd != null && wantCmd.equals(gotCmd));
            if (skinMatch) return holder;
        }
        return fallback;
    }

    /** Renders the recipe (or a "no recipe" message) centered inside the given box — used to
     *  replace the rotating item preview in-place, so it's always fully on-screen regardless
     *  of window size (nothing about this screen scrolls). */
    private void renderRecipePopup(GuiGraphics graphics, int boxX, int boxY, int boxW, int boxH) {
        ensureRecipeLookup();

        if (selectedRecipe == null) {
            String msg = Component.translatable("chrismurderdronesmod.catalog.no_recipe").getString();
            List<String> lines = wrapLabel(msg, boxW - 16);
            int ty = boxY + (boxH - lines.size() * 9) / 2;
            for (String line : lines) {
                int tx = boxX + (boxW - this.font.width(line)) / 2;
                graphics.drawString(this.font, line, tx, ty, COLOR_TEXT_DIM, false);
                ty += 9;
            }
            return;
        }

        CraftingRecipe recipe = selectedRecipe.value();
        List<Ingredient> ingredients = recipe.getIngredients();
        int width = 3, height = 3;
        if (recipe instanceof ShapedRecipe shaped) {
            width = shaped.getWidth();
            height = shaped.getHeight();
        }

        int cellSize = 20, gap = 3;
        int gridW = width * cellSize + (width + 1) * gap;
        int gridH = height * cellSize + (height + 1) * gap;
        int arrowGap = 14;
        int resultSize = cellSize + 6;

        int clusterW = gridW + arrowGap + resultSize;
        int clusterH = Math.max(gridH, resultSize);

        int gridLeft = boxX + (boxW - clusterW) / 2;
        int gridTop = boxY + (boxH - clusterH) / 2 + (clusterH - gridH) / 2;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int index = row * width + col;
                int cx = gridLeft + gap + col * (cellSize + gap);
                int cy = gridTop + gap + row * (cellSize + gap);
                graphics.fill(cx, cy, cx + cellSize, cy + cellSize, 0x30FFFFFF);

                if (index < ingredients.size()) {
                    Ingredient ingredient = ingredients.get(index);
                    if (!ingredient.isEmpty()) {
                        ItemStack[] options = ingredient.getItems();
                        if (options.length > 0) {
                            long cycle = (System.currentTimeMillis() / 1000) % options.length;
                            ItemStack shown = options[(int) cycle];
                            renderScaledIcon(graphics, shown, cx + cellSize / 2, cy + cellSize / 2, cellSize - 4);
                        }
                    }
                }
            }
        }

        // Arrow + result icon, vertically centered against the grid
        int arrowX = gridLeft + gridW + 2;
        int arrowY = boxY + boxH / 2 - 4;
        graphics.drawString(this.font, "→", arrowX, arrowY, COLOR_TEXT_DIM, false);
        int resultCx = arrowX + arrowGap + resultSize / 2;
        int resultCy = boxY + boxH / 2;
        renderScaledIcon(graphics, selectedStack, resultCx, resultCy, resultSize);
    }

    private void selectSkin(PlushieCategory category, CharacterEntry entry, int skin) {
        this.selectedEntry = entry;
        this.selectedSkinIndex = skin;
        this.selectedStack = buildStack(entry, skin);
        this.selectedLabel = entry.fullLabel(skin);
        this.selectedAccentColor = category != null ? category.accentColor() : COLOR_ACCENT;
        resetRecipeState();
        playClickSound();
    }

    /** New skin selected — the old recipe lookup no longer applies. */
    private void resetRecipeState() {
        showRecipePopup = false;
        recipeLookupAttempted = false;
        selectedRecipe = null;
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /** Sends a GivePlushiePayload for the currently-selected skin. Server re-validates creative mode. */
    private void sendGivePacket() {
        if (selectedStack == null || selectedStack.isEmpty()) return;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(selectedStack.getItem());
        String label = selectedLabel != null ? selectedLabel : selectedStack.getHoverName().getString();
        Services.NETWORK.sendToServer(new GivePlushiePayload(itemId, selectedSkinIndex, label));
        playClickSound();
    }

    private void cycleSelectedSkin(int direction) {
        if (selectedEntry == null || selectedEntry.skinCount() <= 1) return;
        int count = selectedEntry.skinCount();
        int next = ((selectedSkinIndex + direction) % count + count) % count;
        selectedSkinIndex = next;
        selectedStack = buildStack(selectedEntry, next);
        selectedLabel = selectedEntry.fullLabel(next);
        resetRecipeState();
        lastInteractionTime = System.currentTimeMillis();
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

    // ── Input handling ───────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < tabBounds.size(); i++) {
                int[] b = tabBounds.get(i);
                if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                    int target = tabTargetScroll.get(i);
                    if (target == -1) {
                        if (filterBox != null) filterBox.setValue("");
                        showFavoritesOnly = false;
                        scrollOffset = 0;
                    } else if (target == -2) {
                        showFavoritesOnly = true;
                        scrollOffset = 0;
                    } else {
                        showFavoritesOnly = false;
                        scrollOffset = target;
                    }
                    lastInteractionTime = System.currentTimeMillis();
                    return true;
                }
            }

            for (int i = 0; i < cardFavoriteBounds.size(); i++) {
                int[] b = cardFavoriteBounds.get(i);
                if (b != null && withinBounds(mouseX, mouseY, b)
                        && mouseY >= panelTop && mouseY < panelTop + panelHeight) {
                    CharacterEntry owningEntry = findEntryForLabel(cardTooltips.get(i));
                    if (owningEntry != null) {
                        int skinIndex = findSkinIndexForLabel(owningEntry, cardTooltips.get(i));
                        PlushieFavorites.toggle(owningEntry, skinIndex);
                        playClickSound();
                    }
                    lastInteractionTime = System.currentTimeMillis();
                    return true;
                }
            }

            for (int i = 0; i < cardBounds.size(); i++) {
                int[] b = cardBounds.get(i);
                if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]
                        && mouseY >= panelTop && mouseY < panelTop + panelHeight) {
                    PlushieCategory owningCategory = findCategoryForLabel(cardTooltips.get(i));
                    CharacterEntry owningEntry = findEntryForLabel(cardTooltips.get(i));
                    int skinIndex = findSkinIndexForLabel(owningEntry, cardTooltips.get(i));
                    if (owningEntry != null) {
                        selectSkin(owningCategory, owningEntry, skinIndex);
                    } else {
                        selectedStack = cardStacks.get(i);
                        selectedLabel = cardTooltips.get(i);
                        resetRecipeState();
                        playClickSound();
                    }
                    lastInteractionTime = System.currentTimeMillis();
                    return true;
                }
            }

            if (prevArrowBounds != null && withinBounds(mouseX, mouseY, prevArrowBounds)) {
                cycleSelectedSkin(-1);
                return true;
            }
            if (nextArrowBounds != null && withinBounds(mouseX, mouseY, nextArrowBounds)) {
                cycleSelectedSkin(1);
                return true;
            }

            if (giveButtonBounds != null && withinBounds(mouseX, mouseY, giveButtonBounds)) {
                sendGivePacket();
                lastInteractionTime = System.currentTimeMillis();
                return true;
            }

            if (favoriteStarBounds != null && withinBounds(mouseX, mouseY, favoriteStarBounds) && selectedEntry != null) {
                PlushieFavorites.toggle(selectedEntry, selectedSkinIndex);
                playClickSound();
                lastInteractionTime = System.currentTimeMillis();
                return true;
            }

            if (recipeButtonBounds != null && withinBounds(mouseX, mouseY, recipeButtonBounds)) {
                showRecipePopup = !showRecipePopup;
                playClickSound();
                lastInteractionTime = System.currentTimeMillis();
                return true;
            }

            if (mouseX >= previewLeft && mouseX < previewLeft + PREVIEW_SIZE
                    && mouseY >= previewTop && mouseY < previewTop + PREVIEW_SIZE
                    && selectedStack != null && !showRecipePopup) {
                draggingPreview = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                lastInteractionTime = System.currentTimeMillis();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean withinBounds(double mouseX, double mouseY, int[] b) {
        return mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3];
    }

    private PlushieCategory findCategoryForLabel(String fullLabel) {
        for (PlushieCategory category : categories) {
            for (CharacterEntry entry : category.characters()) {
                for (int skin = 0; skin < entry.skinCount(); skin++) {
                    if (entry.fullLabel(skin).equals(fullLabel)) return category;
                }
            }
        }
        return null;
    }

    private CharacterEntry findEntryForLabel(String fullLabel) {
        for (PlushieCategory category : categories) {
            for (CharacterEntry entry : category.characters()) {
                for (int skin = 0; skin < entry.skinCount(); skin++) {
                    if (entry.fullLabel(skin).equals(fullLabel)) return entry;
                }
            }
        }
        return null;
    }

    private int findSkinIndexForLabel(CharacterEntry entry, String fullLabel) {
        if (entry == null) return 0;
        for (int skin = 0; skin < entry.skinCount(); skin++) {
            if (entry.fullLabel(skin).equals(fullLabel)) return skin;
        }
        return 0;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPreview) {
            previewYaw += (float) (mouseX - lastMouseX) * 0.75F;
            previewPitch += (float) (mouseY - lastMouseY) * 0.75F;
            previewPitch = Math.max(-89.0F, Math.min(89.0F, previewPitch));
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            lastInteractionTime = System.currentTimeMillis();
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
                && mouseY >= previewTop && mouseY < previewTop + PREVIEW_SIZE
                && selectedStack != null && !showRecipePopup) {
            previewScale += (float) scrollY * 12.0F;
            previewScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, previewScale));
            lastInteractionTime = System.currentTimeMillis();
            return true;
        }

        if (mouseX >= panelLeft && mouseX < panelLeft + panelWidth
                && mouseY >= panelTop && mouseY < panelTop + panelHeight) {
            scrollOffset -= scrollY * 16;
            int maxScroll = Math.max(0, contentHeight - panelHeight);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82 && getFocused() != filterBox) {
            previewYaw = 0.0F;
            previewPitch = 0.0F;
            previewScale = 116.0F;
            lastInteractionTime = System.currentTimeMillis();
            return true;
        }
        if (getFocused() != filterBox) {
            if (keyCode == 263) { // left arrow
                cycleSelectedSkin(-1);
                return true;
            }
            if (keyCode == 262) { // right arrow
                cycleSelectedSkin(1);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}