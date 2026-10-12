package com.camdaloon.simpleflags;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.data.dialog.action.ClickCallback;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class NativeFlagEditor {
    private final SimpleFlags plugin;
    private final FlagService service;
    private final Player player;
    private final ItemStack original;
    private final int[] pixels;
    private String name;

    public NativeFlagEditor(SimpleFlags plugin, FlagService service, Player player, ItemStack flag) {
        this.plugin = plugin;
        this.service = service;
        this.player = player;
        this.original = flag.clone();
        this.pixels = service.decode(service.getDesign(flag));
        this.name = service.getName(flag);
    }

    public void open() {
        player.showDialog(buildDialog());
    }

    private Dialog buildDialog() {
        List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.plainMessage(Component.text("Flag Painter  •  9 × 5 canvas")));
        body.add(DialogBody.plainMessage(renderCanvas()));
        body.add(DialogBody.plainMessage(Component.text("Choose a canvas coordinate, enter a hex color, then select Paint Pixel. Coordinates start at 0.")));
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Simple Flags — Flag Painter"))
                .body(body)
                .inputs(List.of(
                    DialogInput.text("flag_name", Component.text("Flag name"))
                        .initial(name)
                        .maxLength(48)
                        .width(300)
                        .build(),
                    DialogInput.numberRange("pixel_x", Component.text("Canvas X (0–8)"), 0.0F, 8.0F)
                        .initial(4.0F)
                        .step(1.0F)
                        .width(300)
                        .build(),
                    DialogInput.numberRange("pixel_y", Component.text("Canvas Y (0–4)"), 0.0F, 4.0F)
                        .initial(2.0F)
                        .step(1.0F)
                        .width(300)
                        .build(),
                    DialogInput.text("hex_color", Component.text("Paint color (hex, e.g. #246BFD)"))
                        .initial("#246BFD")
                        .maxLength(7)
                        .width(300)
                        .build()
                ))
                .canCloseWithEscape(true)
                .build())
            .type(DialogType.multiAction(List.of(
                ActionButton.builder(Component.text("Paint Pixel", TextColor.color(0x8CC8FF)))
                    .tooltip(Component.text("Paint the selected canvas coordinate"))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (!(audience instanceof Player target) || !target.getUniqueId().equals(player.getUniqueId())) return;
                        String requestedName = view.getText("flag_name");
                        if (requestedName != null && !requestedName.isBlank()) name = requestedName.substring(0, Math.min(48, requestedName.length()));
                        int x = Math.max(0, Math.min(8, Math.round(view.getFloat("pixel_x"))));
                        int y = Math.max(0, Math.min(4, Math.round(view.getFloat("pixel_y"))));
                        int color = parseColor(view.getText("hex_color"));
                        pixels[y * FlagService.WIDTH + x] = color;
                        open();
                    }, ClickCallback.Options.builder().uses(1).build()))
                    .build(),
                ActionButton.builder(Component.text("Fill Canvas", TextColor.color(0xF5D76E)))
                    .tooltip(Component.text("Fill all canvas pixels with the entered color"))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (!(audience instanceof Player target) || !target.getUniqueId().equals(player.getUniqueId())) return;
                        String requestedName = view.getText("flag_name");
                        if (requestedName != null && !requestedName.isBlank()) name = requestedName.substring(0, Math.min(48, requestedName.length()));
                        int color = parseColor(view.getText("hex_color"));
                        for (int i = 0; i < pixels.length; i++) pixels[i] = color;
                        open();
                    }, ClickCallback.Options.builder().uses(1).build()))
                    .build(),
                ActionButton.builder(Component.text("Save", TextColor.color(0x85E89D)))
                    .tooltip(Component.text("Save this design to the flag"))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (!(audience instanceof Player target) || !target.getUniqueId().equals(player.getUniqueId())) return;
                        String requestedName = view.getText("flag_name");
                        if (requestedName != null && !requestedName.isBlank()) name = requestedName.substring(0, Math.min(48, requestedName.length()));
                        save();
                        target.closeDialog();
                        target.sendMessage(Component.text("Flag saved.", TextColor.color(0x85E89D)));
                    }, ClickCallback.Options.builder().uses(1).build()))
                    .build(),
                ActionButton.builder(Component.text("Cancel", TextColor.color(0xFF8585)))
                    .tooltip(Component.text("Discard changes"))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player target) target.closeDialog();
                    }, ClickCallback.Options.builder().uses(1).build()))
                    .build()
            ))).build());
    }

    private Component renderCanvas() {
        Component result = Component.empty();
        for (int y = 0; y < FlagService.HEIGHT; y++) {
            for (int x = 0; x < FlagService.WIDTH; x++) {
                int rgb = pixels[y * FlagService.WIDTH + x] & 0xFFFFFF;
                result = result.append(Component.text("■", TextColor.color(rgb)));
                if (x < FlagService.WIDTH - 1) result = result.append(Component.text(" "));
            }
            if (y < FlagService.HEIGHT - 1) result = result.append(Component.newline());
        }
        return result;
    }

    private int parseColor(String input) {
        if (input == null) return 0x246BFD;
        String value = input.trim().replace("#", "");
        if (value.length() != 6) return 0x246BFD;
        try {
            return Integer.parseInt(value, 16) & 0xFFFFFF;
        } catch (NumberFormatException exception) {
            return 0x246BFD;
        }
    }

    private void save() {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!service.isFlag(held) || !service.getId(held).equals(service.getId(original))) {
            player.sendMessage(Component.text("Hold the same flag in your main hand to save changes.", TextColor.color(0xFF8585)));
            return;
        }
        service.setDesign(held, service.encode(pixels));
        ItemMeta meta = held.getItemMeta();
        meta.displayName(Component.text(name));
        meta.getPersistentDataContainer().set(SimpleFlags.nameKey, PersistentDataType.STRING, name);
        meta.getPersistentDataContainer().set(SimpleFlags.idKey, PersistentDataType.STRING, service.buildId(service.getCreator(held), name));
        held.setItemMeta(meta);
    }
}
