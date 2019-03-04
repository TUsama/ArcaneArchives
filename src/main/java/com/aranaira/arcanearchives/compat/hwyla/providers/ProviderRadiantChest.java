package com.aranaira.arcanearchives.compat.hwyla.providers;

import com.aranaira.arcanearchives.tileentities.RadiantChestTileEntity;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ProviderRadiantChest implements IWailaDataProvider
{
	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		RadiantChestTileEntity te = (RadiantChestTileEntity) accessor.getTileEntity();
		if(te != null)
		{
			String chestName = te.getChestName();
			if (!chestName.isEmpty())
				tooltip.add(TextFormatting.GOLD + "Name: " + chestName);
		}

		return tooltip;
	}
}