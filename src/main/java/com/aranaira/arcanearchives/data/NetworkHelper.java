package com.aranaira.arcanearchives.data;

import com.aranaira.arcanearchives.ArcaneArchives;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkHelper
{
	// TODO: This needs to be cleared whenever the player enters a new world
	private static Map<UUID, ArcaneArchivesClientNetwork> CLIENT_MAP = new HashMap<>();
	private static AAWorldSavedData savedData = null;

	public static void clearClientCache()
	{
		CLIENT_MAP.clear();
	}

	public static void clearServerCache()
	{
		savedData = null;
	}

	public static ArcaneArchivesNetwork getArcaneArchivesNetwork(UUID uuid)
	{
		if(savedData == null)
		{
			World world = DimensionManager.getWorld(0);
			if(world == null || world.getMapStorage() == null)
			{
				ArcaneArchives.logger.error(String.format("Attempted to load a network for %s, but the world is currently null!", uuid.toString()));
				return null;
			}

			AAWorldSavedData saveData = (AAWorldSavedData) world.getMapStorage().getOrLoadData(AAWorldSavedData.class, AAWorldSavedData.ID);

			if(saveData == null)
			{
				saveData = new AAWorldSavedData();
				world.getMapStorage().setData(AAWorldSavedData.ID, saveData);
			}

			savedData = saveData;
		}

		return savedData.getNetwork(uuid);
	}

	public static ArcaneArchivesNetwork getArcaneArchivesNetwork(String uuid)
	{
		return getArcaneArchivesNetwork(UUID.fromString(uuid));
	}

	public static ArcaneArchivesNetwork getArcaneArchivesNetwork(EntityPlayer player)
	{
		return getArcaneArchivesNetwork(player.getUniqueID());
	}

	public static ArcaneArchivesClientNetwork getArcaneArchivesClientNetwork(UUID uuid)
	{
		if(CLIENT_MAP.containsKey(uuid))
		{
			return CLIENT_MAP.get(uuid);
		} else
		{
			ArcaneArchivesClientNetwork net = new ArcaneArchivesClientNetwork(uuid);
			CLIENT_MAP.put(uuid, net);
			return net;
		}
	}

	public static ArcaneArchivesClientNetwork getArcaneArchivesClientNetwork(String uuid)
	{
		return getArcaneArchivesClientNetwork(UUID.fromString(uuid));
	}

	public static ArcaneArchivesClientNetwork getArcaneArchivesClientNetwork(EntityPlayer player)
	{
		return getArcaneArchivesClientNetwork(player.getUniqueID());
	}
}
