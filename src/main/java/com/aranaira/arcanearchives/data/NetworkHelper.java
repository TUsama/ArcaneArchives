package com.aranaira.arcanearchives.data;

import com.aranaira.arcanearchives.ArcaneArchives;
import com.aranaira.arcanearchives.data.AAWorldSavedData;
import com.aranaira.arcanearchives.data.ArcaneArchivesNetwork;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class NetworkHelper
{
	// TODO: This needs to be cleared whenever the player enters a new world
	private static Map<UUID, ArcaneArchivesClientNetwork> CLIENT_MAP = new HashMap<>();

	public static ArcaneArchivesNetwork getArcaneArchivesNetwork(UUID uuid)
	{
		FMLCommonHandler handler = FMLCommonHandler.instance();
		Side effective = handler.getEffectiveSide();
		Side delegate = handler.getSide();

		ArcaneArchives.logger.info(String.format("getArcaneArchivesNetwork(%s) was called. Effective side |%s|, delegate side |%s|", uuid.toString(), (effective.isServer()) ? "SERVER" : "CLIENT", (delegate.isServer()) ? "SERVER" : "CLIENT"));

		World world = DimensionManager.getWorld(0);
		if(world == null || world.getMapStorage() == null)
		{
			return new AAWorldSavedData().getNetwork(uuid);
		}

		AAWorldSavedData saveData = (AAWorldSavedData) world.getMapStorage().getOrLoadData(AAWorldSavedData.class, AAWorldSavedData.ID);

		if(saveData == null)
		{
			saveData = new AAWorldSavedData();
			world.getMapStorage().setData(AAWorldSavedData.ID, saveData);
		}

		return saveData.getNetwork(uuid);
	}

	public static ArcaneArchivesNetwork getArcaneArchivesNetwork(String uuid)
	{
		return getArcaneArchivesNetwork(UUID.fromString(uuid));
	}

	@SideOnly(Side.CLIENT)
	public static ArcaneArchivesClientNetwork getArcaneArchivesClientNetwork (UUID uuid) {
		if (CLIENT_MAP.containsKey(uuid)) {
			return CLIENT_MAP.get(uuid);
		}
		else {
			ArcaneArchivesClientNetwork net = new ArcaneArchivesClientNetwork(uuid);
			CLIENT_MAP.put(uuid, net);
			return net;
		}
	}

	@SideOnly(Side.CLIENT)
	public static ArcaneArchivesClientNetwork getArcaneArchivesClientNetwork (String uuid) {
		return getArcaneArchivesClientNetwork(UUID.fromString(uuid));
	}
}
