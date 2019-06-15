package com.aranaira.arcanearchives.items.gems;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.aranaira.arcanearchives.ArcaneArchives;
import com.aranaira.arcanearchives.config.ConfigHandler;
import com.aranaira.arcanearchives.inventory.handlers.GemSocketHandler;
import com.aranaira.arcanearchives.items.BaubleGemSocket;
import com.aranaira.arcanearchives.items.templates.ItemTemplate;
import com.aranaira.arcanearchives.util.NBTUtils;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.model.ModelLoader;

import java.awt.*;
import java.util.ArrayList;

public abstract class ArcaneGemItem extends ItemTemplate {
	public GemCut cut;
	public GemColor color;
	public int maxChargeNormal, maxChargeUpgraded;

	public static final byte UPGRADE_MATTER = 1, UPGRADE_POWER = 2, UPGRADE_SPACE = 4, UPGRADE_TIME = 8;

	public ArcaneGemItem (String name, GemCut cut, GemColor color, int maxChargeNormal, int maxChargeUpgraded) {
		super(name);
		this.cut = cut;
		this.color = color;
		this.maxChargeNormal = maxChargeNormal;
		this.maxChargeUpgraded = maxChargeUpgraded;
		setMaxStackSize(1);

		if (!ConfigHandler.ArsenalConfig.EnableArsenal) {
			setCreativeTab(null);
		}

		addPropertyOverride(new ResourceLocation(ArcaneArchives.MODID, "colourblind"), (stack, worldIn, entityIn) -> ConfigHandler.ArsenalConfig.ColourblindMode ? 1 : 0);
	}

	public GemCut getGemCut () {
		return cut;
	}

	public GemColor getGemColor () {
		return color;
	}

	public int getMaxChargeNormal () {
		return maxChargeNormal;
	}

	public int getMaxChargeUpgraded () {
		return maxChargeUpgraded;
	}

	protected String getTooltipData (ItemStack stack) {
		String str = "";

		if (hasToggleMode()) {
			if (GemUtil.isToggledOn(stack)) {
				str += "[On] ";
			} else {
				str += "[Off] ";
			}
		}

		if (GemUtil.hasUnlimitedCharge(stack)) {
			str += "[Unlimited]";
		} else {
			str += "[" + GemUtil.getCharge(stack) + " / " + GemUtil.getMaxCharge(stack) + "]";
		}

		byte upgrades = GemUtil.getUpgrades(stack);
		if ((upgrades & UPGRADE_MATTER) == UPGRADE_MATTER) {
			str += "   " + TextFormatting.GREEN + I18n.format("arcanearchives.tooltip.gemupgrade.matter");
		}
		if ((upgrades & UPGRADE_POWER) == UPGRADE_POWER) {
			str += "   " + TextFormatting.RED + I18n.format("arcanearchives.tooltip.gemupgrade.power");
		}
		if ((upgrades & UPGRADE_SPACE) == UPGRADE_SPACE) {
			str += "   " + TextFormatting.BLUE + I18n.format("arcanearchives.tooltip.gemupgrade.space");
		}
		if ((upgrades & UPGRADE_TIME) == UPGRADE_TIME) {
			str += "   " + TextFormatting.RED + I18n.format("arcanearchives.tooltip.gemupgrade.time");
		}
		return str;
	}

	/**
	 * Used by the HUD element to determine whether to use the bar or the bar with toggle indicator
	 *
	 * @return true if toggle indicator should be present
	 */
	public boolean hasToggleMode () {
		return false;
	}

	/**
	 * Retrieves the resource location for the gem's dun texture
	 *
	 * @param cut The gem's cut
	 * @return
	 */
	protected ModelResourceLocation getDunGemResourceLocation (GemCut cut) {
		String loc = "arcanearchives:gems/";
		loc += cut.toString().toLowerCase() + "/dun";
		return new ModelResourceLocation(loc, "inventory");
	}

	/**
	 * Retrieves the resource location for the gem's conflicted static texture
	 *
	 * @param cut The gem's cut
	 * @return
	 */
	protected ModelResourceLocation getConflictGemResourceLocation (GemCut cut) {
		String loc = "arcanearchives:gems/";
		loc += cut.toString().toLowerCase() + "/static";
		return new ModelResourceLocation(loc, "inventory");
	}

	/**
	 * Retrieves the resource location for the gem's textures
	 *
	 * @param cut   The gem's cut
	 * @param color The gem's color spectrum
	 * @return
	 */
	protected ModelResourceLocation getChargedGemResourceLocation (GemCut cut, GemColor color) {
		String loc = "arcanearchives:gems/";
		loc += cut.toString().toLowerCase() + "/";
		loc += color.toString().toLowerCase();
		return new ModelResourceLocation(loc, "inventory");
	}

	/**
	 * Sets up the models for both charged and dun states
	 */
	@Override
	public void registerModels () {
		ModelResourceLocation charged = getChargedGemResourceLocation(cut, color);
		ModelResourceLocation conflict = getConflictGemResourceLocation(cut);
		ModelResourceLocation dun = getDunGemResourceLocation(cut);

		ModelBakery.registerItemVariants(this, charged, dun);

		ModelLoader.setCustomMeshDefinition(this, stack -> {
			if (GemUtil.isChargeEmpty(stack)) {
				return dun;
			} else if (false) {//TODO: Check for dupes in inventory
				return conflict;
			} else {
				return charged;
			}
		});
	}

	/**
	 * Convenience method to convert BlockPos into a Vec3d
	 *
	 * @param pos           The BlockPos to convert
	 * @param shiftToCenter Whether to leave the BlockPos as is or shift it to the center of the block
	 * @return
	 */
	protected Vec3d blockPosToVector (BlockPos pos, boolean shiftToCenter) {
		if (shiftToCenter) {
			return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		} else {
			return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		}
	}

	public enum GemCut {
		NOCUT, ASSCHER, OVAL, PAMPEL, PENDELOQUE, TRILLION;

		/**
		 * Converts a gem cut to a specific value. Used in packets.
		 *
		 * @param cut The gem's cut
		 */
		public static byte ToByte (GemCut cut) {
			if (cut == ASSCHER) {
				return 1;
			}
			if (cut == OVAL) {
				return 2;
			}
			if (cut == PAMPEL) {
				return 3;
			}
			if (cut == PENDELOQUE) {
				return 4;
			}
			if (cut == TRILLION) {
				return 5;
			}
			return 0;
		}

		/**
		 * Converts a byte value into a specific gem cut value. Used in packets.
		 *
		 * @param query The byte value to check
		 * @return The gem's cut
		 */
		public static GemCut fromByte (byte query) {
			if (query == 1) {
				return ASSCHER;
			}
			if (query == 2) {
				return OVAL;
			}
			if (query == 3) {
				return PAMPEL;
			}
			if (query == 4) {
				return PENDELOQUE;
			}
			if (query == 5) {
				return TRILLION;
			}
			return NOCUT;
		}
	}

	public class GemWrapper {
		public ItemStack gem;
		public boolean inSocket;

		public GemWrapper (ItemStack gem, boolean inSocket) {
			this.gem = gem;
			this.inSocket = inSocket;
		}
	}

	public enum GemColor {
		NOCOLOR, RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, PURPLE, PINK, BLACK, WHITE;

		/**
		 * Converts a gem cut to a specific value. Used in packets.
		 *
		 * @param color The gem's color
		 */
		public static byte ToByte (GemColor color) {
			if (color == RED) {
				return 1;
			} else if (color == ORANGE) {
				return 2;
			} else if (color == YELLOW) {
				return 3;
			} else if (color == GREEN) {
				return 4;
			} else if (color == CYAN) {
				return 5;
			} else if (color == BLUE) {
				return 6;
			} else if (color == PURPLE) {
				return 7;
			} else if (color == PINK) {
				return 8;
			} else if (color == BLACK) {
				return 9;
			} else if (color == WHITE) {
				return 10;
			}
			return 0;
		}

		/**
		 * Converts a byte value into a specific color. Used in packets.
		 *
		 * @param query The byte value to check
		 * @return The color value
		 */
		public static GemColor fromByte (byte query) {
			if (query == 1) {
				return RED;
			} else if (query == 2) {
				return ORANGE;
			} else if (query == 3) {
				return YELLOW;
			} else if (query == 4) {
				return GREEN;
			} else if (query == 5) {
				return CYAN;
			} else if (query == 6) {
				return BLUE;
			} else if (query == 7) {
				return PURPLE;
			} else if (query == 8) {
				return PINK;
			} else if (query == 9) {
				return BLACK;
			} else if (query == 10) {
				return WHITE;
			}
			return NOCOLOR;
		}

		public static Color getColor (GemColor color) {
			if (color == GemColor.RED) {
				return new Color(1.00f, 0.50f, 0.50f, 1.0f);
			} else if (color == GemColor.ORANGE) {
				return new Color(1.00f, 0.75f, 0.50f, 1.0f);
			} else if (color == GemColor.YELLOW) {
				return new Color(1.00f, 1.00f, 0.50f, 1.0f);
			} else if (color == GemColor.GREEN) {
				return new Color(0.50f, 1.00f, 0.60f, 1.0f);
			} else if (color == GemColor.CYAN) {
				return new Color(0.50f, 1.00f, 1.00f, 1.0f);
			} else if (color == GemColor.BLUE) {
				return new Color(0.50f, 0.65f, 1.00f, 1.0f);
			} else if (color == GemColor.PURPLE) {
				return new Color(0.80f, 0.50f, 1.00f, 1.0f);
			} else if (color == GemColor.PINK) {
				return new Color(1.00f, 0.55f, 1.00f, 1.0f);
			} else if (color == GemColor.BLACK) {
				return new Color(0.00f, 0.00f, 0.00f, 1.0f);
			}
			return new Color(1, 1, 1, 1);
		}
	}

	public enum GemUpgrades {
		MATTER, POWER, SPACE, TIME
	}
}
