package com.overheat.movement.client.screen;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import com.overheat.movement.ability.Ability;
import com.overheat.movement.ability.Action;
import com.overheat.movement.ability.ActivatorEntry;
import com.overheat.movement.network.DeleteActivatorPayload;
import com.overheat.movement.network.SaveActivatorConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class ActivatorConfigScreen extends Screen {

	private static final int CHAIN_TOP = 58;
	private static final int ROW_H = 26;
	private static final int TOGGLE_W = 92;
	private static final int BOX_W = 130;
	private static final int BOX_H = 20;
	private static final int PAL_W = 96;
	private static final int PAL_H = 20;
	private static final int PAL_GAP = 8;

	private final BlockPos pos;
	private final List<ActivatorEntry> rows = new ArrayList<>();

	private Ability dragging;
	private Action draggingAction = Action.ACTIVATE;

	public ActivatorConfigScreen(BlockPos pos, List<ActivatorEntry> initial) {
		super(Component.literal("Configure Activator"));
		this.pos = pos;
		EnumSet<Ability> seen = EnumSet.noneOf(Ability.class);
		for (ActivatorEntry entry : initial) {
			if (seen.add(entry.ability())) {
				rows.add(entry);
			}
		}
	}

	private int toggleX() {
		return this.width / 2 - 165;
	}

	private int boxX() {
		return this.width / 2 - 60;
	}

	private int paletteY() {
		return this.height - 92;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		for (int i = 0; i < rows.size(); i++) {
			final int idx = i;
			ActivatorEntry entry = rows.get(i);
			boolean activate = entry.action() == Action.ACTIVATE;
			addRenderableWidget(Button.builder(
					Component.literal(activate ? "Activate" : "Deactivate")
							.withStyle(activate ? ChatFormatting.GREEN : ChatFormatting.RED),
					b -> toggleAction(idx)).bounds(toggleX(), CHAIN_TOP + i * ROW_H, TOGGLE_W, 20).build());
		}

		int by = this.height - 34;
		addRenderableWidget(Button.builder(Component.literal("Delete Activator").withStyle(ChatFormatting.RED),
				b -> deleteActivator()).bounds(cx - 165, by, 130, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Save").withStyle(ChatFormatting.GREEN),
				b -> save()).bounds(cx - 25, by, 85, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Cancel"),
				b -> onClose()).bounds(cx + 65, by, 85, 20).build());
	}

	private void toggleAction(int idx) {
		ActivatorEntry entry = rows.get(idx);
		Action next = entry.action() == Action.ACTIVATE ? Action.DEACTIVATE : Action.ACTIVATE;
		rows.set(idx, new ActivatorEntry(next, entry.ability()));
		rebuildWidgets();
	}

	private void save() {
		ClientPlayNetworking.send(new SaveActivatorConfigPayload(pos, List.copyOf(rows)));
		onClose();
	}

	private void deleteActivator() {
		ClientPlayNetworking.send(new DeleteActivatorPayload(pos));
		onClose();
	}

	private List<Ability> palette() {
		List<Ability> list = new ArrayList<>();
		for (Ability a : Ability.values()) {
			if (a == dragging) {
				continue;
			}
			boolean placed = false;
			for (ActivatorEntry entry : rows) {
				if (entry.ability() == a) {
					placed = true;
					break;
				}
			}
			if (!placed) {
				list.add(a);
			}
		}
		return list;
	}

	private int paletteX(int index, int count) {
		int totalW = count * PAL_W + (count - 1) * PAL_GAP;
		return this.width / 2 - totalW / 2 + index * (PAL_W + PAL_GAP);
	}

	private boolean inChainDropZone(double mx, double my) {
		int cx = this.width / 2;
		int top = CHAIN_TOP - 12;
		int bottom = CHAIN_TOP + Ability.values().length * ROW_H + 12;
		return mx >= cx - 175 && mx <= cx + 85 && my >= top && my <= bottom;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if (button == 0) {
			List<Ability> pal = palette();
			if (mouseY >= paletteY() && mouseY <= paletteY() + PAL_H) {
				for (int i = 0; i < pal.size(); i++) {
					int x = paletteX(i, pal.size());
					if (mouseX >= x && mouseX <= x + PAL_W) {
						dragging = pal.get(i);
						draggingAction = Action.ACTIVATE;
						return true;
					}
				}
			}
			for (int i = 0; i < rows.size(); i++) {
				int y = CHAIN_TOP + i * ROW_H;
				if (mouseX >= boxX() && mouseX <= boxX() + BOX_W && mouseY >= y && mouseY <= y + BOX_H) {
					ActivatorEntry entry = rows.remove(i);
					dragging = entry.ability();
					draggingAction = entry.action();
					rebuildWidgets();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (dragging != null && button == 0) {
			if (inChainDropZone(mouseX, mouseY)) {
				rows.add(new ActivatorEntry(draggingAction, dragging));
			}
			dragging = null;
			rebuildWidgets();
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);
		int cx = this.width / 2;

		g.drawCenteredString(this.font, this.title, cx, 16, 0xFFFFCC33);
		box(g, cx - 70, 34, 140, 18, 0xFF3A3A3A, "When stepped on");

		for (int i = 0; i < rows.size(); i++) {
			int y = CHAIN_TOP + i * ROW_H;
			ActivatorEntry entry = rows.get(i);
			g.drawString(this.font, "→", toggleX() + TOGGLE_W + 3, y + 6, 0xFFAAAAAA, false);
			box(g, boxX(), y, BOX_W, BOX_H, abilityColor(entry.ability()), entry.ability().displayName());
		}

		if (dragging != null) {
			int top = CHAIN_TOP - 12;
			int bottom = CHAIN_TOP + Ability.values().length * ROW_H + 12;
			outline(g, cx - 175, top, cx + 85, bottom, 0xFF44DD44);
		}

		List<Ability> pal = palette();
		g.drawCenteredString(this.font, Component.literal("Drag an ability into the chain"),
				cx, paletteY() - 13, 0xFFBBBBBB);
		for (int i = 0; i < pal.size(); i++) {
			int x = paletteX(i, pal.size());
			box(g, x, paletteY(), PAL_W, PAL_H, abilityColor(pal.get(i)), pal.get(i).displayName());
		}
		if (pal.isEmpty() && dragging == null) {
			g.drawCenteredString(this.font, Component.literal("all abilities added"), cx, paletteY() + 6, 0xFF777777);
		}

		if (dragging != null) {
			box(g, mouseX - BOX_W / 2, mouseY - BOX_H / 2, BOX_W, BOX_H, abilityColor(dragging), dragging.displayName());
		}
	}

	private void box(GuiGraphics g, int x, int y, int w, int h, int color, String label) {
		g.fill(x, y, x + w, y + h, color);
		outline(g, x, y, x + w, y + h, 0xFFFFFFFF);
		g.drawCenteredString(this.font, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
	}

	private void outline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
		g.fill(x1, y1, x2, y1 + 1, color);
		g.fill(x1, y2 - 1, x2, y2, color);
		g.fill(x1, y1, x1 + 1, y2, color);
		g.fill(x2 - 1, y1, x2, y2, color);
	}

	private static int abilityColor(Ability ability) {
		return switch (ability) {
			case DASH -> 0xFFE0662E;
			case AIR_JUMP -> 0xFF3EA7E0;
			case WALL_RUN -> 0xFF4CAF50;
			case BHOP -> 0xFF9C6ADE;
			case COMBAT -> 0xFFD64545;
			case SLIDE -> 0xFFE0B62E;
		};
	}
}
