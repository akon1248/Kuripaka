package com.akon.kuripaka;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public final class Kuripaka extends JavaPlugin implements Listener {

	private static PlayerDisguise kuripakaDisguise;
	private static ClientSideTeam invisibleNameTag;

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		WrappedGameProfile profile = new WrappedGameProfile(UUID.fromString("b3a0f721-b23d-4e34-aca7-243f0469a685"), ChatColor.GREEN + "Kuripaka");
		profile.getProperties().put("textures", new WrappedSignedProperty("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYxMTU2NTE2MzQ2MSwKICAicHJvZmlsZUlkIiA6ICI0ZDY0YmM5ODkwZTQ0NDQ5OTY4NGExOTE0Njk3ZDJmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJLdXJpcGFrYWNoYWFuIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlkMmJkOGU1YWRhZDY4ODJlODZlMDNmNTc5YTVkMDYwNTE3MWZkOWI5MDMwODYzNmJmYjg1Njg0YWJmYTIyMzciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", "rWwf2ZQ9cgoVjqzygM9k2yEPsv2w4nnUQIQU7HsYI+hkGzydgxWPMD/13+iHeblIfhVMInyThj+uv0jEiwmVWg079Mz6B8dKT3T/RRmLQ9xaw7qJDeh3He/RyBHBlCFf/Uty4q4SdkAoYafGYsWEcDAr5Wg+eaQCVHLSlA8NPeJNSL+lyfbiLEuvFLYS/kWMwH+soC0Lnf+1SMhvA7PMHAfqkNLsAewpY/veU/jHXcMxUW2mhDhzqKUxbpjOHsyYxBSCKWRElIehldfSHmMf44D23/Wc5PjKpBO0IXlPCNATanPbGh1hOuK+7Op5N6ENG81C31wmBDTrrtGxpgwoWDaHC6A5qQVX3FPYicCwxbhfwjB26ggtaIz37SWbt/nUA6VY4tD0GMViSZsO1cImmOJRWAS6M+ptFDQl4ReK5HbL9Nwq0FG9MgKymw3rOUpLulv1RqDZnLlqNE5bVnzQE/YnwCtstULZ3Kfd4LIKCHKHOYN045ECh7YvBJaXmpNp4R/8IDR+rUzW5/yaGeRDoF+z1tinij+JxeBCfDZkgGslpnGpIDI7wg1zUFgXc0Tlu1MIiVvAUxNOvhn3Kau7n3xRmm11OVug0MLHn0bhlqxHnxnofQu7F6Q+Or5xWKGhj9MysNzCxsW9s/pLZBL61xBK/NnsEIqY3l2/u1OPUKA="));
		kuripakaDisguise = new PlayerDisguise(profile);
		kuripakaDisguise.setReplaceSounds(false);
		invisibleNameTag = ClientSideTeam.createNew();
		invisibleNameTag.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
		invisibleNameTag.addEntry(ChatColor.GREEN + "Kuripaka");
		Bukkit.getWorlds()
			.stream()
			.map(World::getEntities)
			.flatMap(Collection::stream)
			.filter(Creeper.class::isInstance)
			.forEach(this::disguiseCreeper);
		Bukkit.getOnlinePlayers().forEach(invisibleNameTag::addVisiblePlayer);
	}

	private void disguiseCreeper(Entity creeper) {
		DisguiseAPI.disguiseEntity(creeper, kuripakaDisguise);
	}

	@Override
	public void onDisable() {
		Bukkit.getWorlds()
			.stream()
			.map(World::getEntities)
			.flatMap(Collection::stream)
			.filter(Creeper.class::isInstance)
			.forEach(DisguiseAPI::undisguiseToAll);
	}

	@EventHandler
	public void onSpawnCreeper(CreatureSpawnEvent e) {
		if (e.getEntityType() == EntityType.CREEPER) {
			this.disguiseCreeper(e.getEntity());
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		Arrays.stream(e.getChunk().getEntities())
			.filter(Creeper.class::isInstance)
			.forEach(this::disguiseCreeper);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		invisibleNameTag.addVisiblePlayer(e.getPlayer());
	}

}
