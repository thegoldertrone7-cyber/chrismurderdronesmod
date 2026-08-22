package chris.murderdrones;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Configurable hotkey (default 'K', rebindable like any other keybind under Controls)
 * that replaces the old Assembly Machine block. Look at a placed plushie, hold the
 * screwdriver, press the hotkey: a small picker opens showing every skin for that one
 * character, and clicking one re-skins the block you were looking at directly — no
 * crafting station, no menu, no separate item slot involved.
 */
@EventBusSubscriber(modid = ChrisMurderDronesMod.MODID, value = Dist.CLIENT)
public class PlushieSkinPickerOpener {

    private static final int COLOR_WARN = 0xFF8888;

    public static final KeyMapping OPEN_SKIN_PICKER_KEY = new KeyMapping(
            "key.chrismurderdronesmod.open_skin_picker",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            "key.categories.chrismurderdronesmod");

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SKIN_PICKER_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_SKIN_PICKER_KEY.consumeClick()) {
            if (mc.screen == null && mc.player != null && mc.level != null) {
                tryOpen(mc);
            }
        }
    }

    private static void tryOpen(Minecraft mc) {
        boolean holdingScrewdriver = mc.player.getMainHandItem().is(ChrisMurderDronesMod.SCREWDRIVER.get())
                || mc.player.getOffhandItem().is(ChrisMurderDronesMod.SCREWDRIVER.get());
        if (!holdingScrewdriver) {
            warn(mc, "Hold the screwdriver to pick a skin");
            return;
        }

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult blockHit)) {
            warn(mc, "Look at a plushie first");
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof ModBlocks.PlushieBlock plushieBlock)) {
            warn(mc, "Look at a plushie first");
            return;
        }

        String character = plushieBlock.getCharacter();
        if (!PlushieBlockEntity.hasSkins(character)) {
            warn(mc, "This plushie has no alternate skins");
            return;
        }

        CharacterEntry entry = PlushieBrowserOpener.findEntry(character);
        if (entry == null) return;

        mc.setScreen(new PlushieSkinPickerScreen(pos, entry));
    }

    private static void warn(Minecraft mc, String message) {
        mc.player.displayClientMessage(
                Component.literal(message).withStyle(Style.EMPTY.withColor(COLOR_WARN)), true);
    }
}