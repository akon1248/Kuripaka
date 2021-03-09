package com.akon.kuripaka;

import com.akon.kuripaka.reflection.ClassUtil;
import com.akon.kuripaka.reflection.FieldAccessor;
import com.google.common.collect.MapMaker;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.scoreboard.CraftScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientSideTeam implements Team {

	private static final DecimalFormat TEN_DIGITS = new DecimalFormat("0000000000");

	@SuppressWarnings("unchecked")
	private static final Collection<Scoreboard> SCOREBOARDS = new FieldAccessor(CraftScoreboardManager.class, "scoreboards").get(Bukkit.getScoreboardManager(), Collection.class).onCatch(Throwable::printStackTrace).result();
	private static final FieldAccessor NMS_TEAM = new FieldAccessor(ClassUtil.getCBClass("scoreboard.CraftTeam").result(), "team");

	private static final ConcurrentMap<String, ClientSideTeam> TEAMS = new MapMaker().weakValues().makeMap();
	private static int teamCount = 0;

	private final Team team;
	private final Set<Player> players = Collections.newSetFromMap(new WeakHashMap<>());
	@Getter
	private final Collection<Player> visiblePlayers = Collections.unmodifiableCollection(this.players); //返されるのはビューなので定数で問題ない

	@Nullable
	public static ClientSideTeam fromName(String name) {
		return TEAMS.get(name);
	}

	public static ClientSideTeam createNew() {
		String name = "CSTeam" + TEN_DIGITS.format(teamCount);
		teamCount++;
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		SCOREBOARDS.remove(scoreboard);
		Team team = scoreboard.registerNewTeam(name);
		ClientSideTeam csTeam = new ClientSideTeam(team);
		TEAMS.put(name, csTeam);
		return csTeam;
	}

	public ScoreboardTeam getHandle() {
		return NMS_TEAM.get(this.team, ScoreboardTeam.class).result();
	}

	private void checkUnregistered() {
		Validate.validState(fromName(this.getName()) != null, "Unregistered client-side team");
	}

	private PacketPlayOutScoreboardTeam newTeamPacket(TeamUpdateAction action) {
		Validate.isTrue(action != TeamUpdateAction.ADD_PLAYERS && action != TeamUpdateAction.REMOVE_PLAYERS, "Illegal update action: " + action);
		return new PacketPlayOutScoreboardTeam(this.getHandle(), action.getActionId());
	}

	private PacketPlayOutScoreboardTeam newTeamPacket(TeamUpdateAction action, Collection<String> players) {
		Validate.isTrue(action == TeamUpdateAction.ADD_PLAYERS || action == TeamUpdateAction.REMOVE_PLAYERS, "Illegal update action: " + action);
		Validate.isTrue(!players.isEmpty(), "Players cannot be empty.");
		return new PacketPlayOutScoreboardTeam(this.getHandle(), players, action.getActionId());
	}

	private void update(TeamUpdateAction action) {
		PacketPlayOutScoreboardTeam packet = this.newTeamPacket(action);
		this.players.forEach(player -> ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet));
	}

	private void update(TeamUpdateAction action, Collection<String> players) {
		PacketPlayOutScoreboardTeam packet = this.newTeamPacket(action, players);
		this.players.forEach(player -> ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet));
	}

	public boolean addVisiblePlayer(Player player) {
		this.checkUnregistered();
		if (this.players.add(player)) {
			((CraftPlayer)player).getHandle().playerConnection.sendPacket(this.newTeamPacket(TeamUpdateAction.ADD));
			return true;
		}
		return false;
	}

	public boolean removeVisiblePlayer(Player player) {
		this.checkUnregistered();
		if (this.players.remove(player)) {
			((CraftPlayer)player).getHandle().playerConnection.sendPacket(this.newTeamPacket(TeamUpdateAction.REMOVE));
			return true;
		}
		return false;
	}

	@Override
	public @NotNull String getName() throws IllegalStateException {
		return this.team.getName();
	}

	@Override
	public @NotNull String getDisplayName() throws IllegalStateException {
		return this.team.getDisplayName();
	}

	@Override
	public void setDisplayName(@NotNull String s) throws IllegalStateException, IllegalArgumentException {
		this.checkUnregistered();
		this.team.setDisplayName(s);
		this.update(TeamUpdateAction.CHANGE);
	}

	@Override
	public @NotNull String getPrefix() throws IllegalStateException {
		return this.team.getPrefix();
	}

	@Override
	public void setPrefix(@NotNull String s) throws IllegalStateException, IllegalArgumentException {
		this.checkUnregistered();
		this.team.setPrefix(s);
		this.update(TeamUpdateAction.CHANGE);
	}

	@Override
	public @NotNull String getSuffix() throws IllegalStateException {
		return this.team.getSuffix();
	}

	@Override
	public void setSuffix(@NotNull String s) throws IllegalStateException, IllegalArgumentException {
		this.checkUnregistered();
		this.team.setSuffix(s);
		this.update(TeamUpdateAction.CHANGE);
	}

	@Override
	public @NotNull ChatColor getColor() throws IllegalStateException {
		return this.team.getColor();
	}

	@Override
	public void setColor(@NotNull ChatColor chatColor) {
		this.checkUnregistered();
		this.team.setColor(chatColor);
		this.update(TeamUpdateAction.CHANGE);
	}

	@Override
	public boolean allowFriendlyFire() throws IllegalStateException {
		return this.team.allowFriendlyFire();
	}

	@Override
	public void setAllowFriendlyFire(boolean b) throws IllegalStateException {
		this.checkUnregistered();
		this.team.setAllowFriendlyFire(b);
		this.update(TeamUpdateAction.CHANGE);
	}

	@Override
	public boolean canSeeFriendlyInvisibles() throws IllegalStateException {
		return this.team.canSeeFriendlyInvisibles();
	}

	@Override
	public void setCanSeeFriendlyInvisibles(boolean b) throws IllegalStateException {
		this.checkUnregistered();
		this.team.setCanSeeFriendlyInvisibles(b);
		this.update(TeamUpdateAction.CHANGE);
	}

	@Override
	public @NotNull NameTagVisibility getNameTagVisibility() throws IllegalArgumentException {
		return this.team.getNameTagVisibility();
	}

	@Override
	public void setNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) throws IllegalArgumentException {
		this.checkUnregistered();
		this.team.setNameTagVisibility(nameTagVisibility);
		this.update(TeamUpdateAction.CHANGE);
	}

	@Override
	public @NotNull Set<OfflinePlayer> getPlayers() throws IllegalStateException {
		return this.team.getPlayers();
	}

	@Override
	public @NotNull Set<String> getEntries() throws IllegalStateException {
		return this.team.getEntries();
	}

	@Override
	public int getSize() throws IllegalStateException {
		return this.team.getSize();
	}

	@Override
	public @Nullable Scoreboard getScoreboard() {
		throw new UnsupportedOperationException("The method \"getScoreboard\" is not supported.");
	}

	@Override
	public void addPlayer(@NotNull OfflinePlayer offlinePlayer) throws IllegalStateException, IllegalArgumentException {
		//addEntryが呼ばれるのでupdateを呼ぶ必要はない
		this.team.addPlayer(offlinePlayer);
	}

	@Override
	public void addEntry(@NotNull String s) throws IllegalStateException, IllegalArgumentException {
		this.checkUnregistered();
		this.team.addEntry(s);
		this.update(TeamUpdateAction.ADD_PLAYERS, Collections.singleton(s));
	}

	@Override
	public boolean removePlayer(@NotNull OfflinePlayer offlinePlayer) throws IllegalStateException, IllegalArgumentException {
		//removeEntryが呼ばれるのでupdateを呼ぶ必要はない
		return this.team.removePlayer(offlinePlayer);
	}

	@Override
	public boolean removeEntry(@NotNull String s) throws IllegalStateException, IllegalArgumentException {
		this.checkUnregistered();
		boolean result = this.team.removeEntry(s);
		if (result) {
			this.update(TeamUpdateAction.REMOVE_PLAYERS, Collections.singleton(s));
		}
		return result;
	}

	@Override
	public void unregister() throws IllegalStateException {
		this.checkUnregistered();
		this.team.unregister();
		this.update(TeamUpdateAction.REMOVE);
		TEAMS.remove(this.getName());
		this.players.clear();
	}

	@Override
	public boolean hasPlayer(@NotNull OfflinePlayer offlinePlayer) throws IllegalArgumentException, IllegalStateException {
		return this.team.hasPlayer(offlinePlayer);
	}

	@Override
	public boolean hasEntry(@NotNull String s) throws IllegalArgumentException, IllegalStateException {
		return this.team.hasEntry(s);
	}

	@Override
	public @NotNull OptionStatus getOption(@NotNull Option option) throws IllegalStateException {
		return this.team.getOption(option);
	}

	@Override
	public void setOption(@NotNull Option option, @NotNull OptionStatus optionStatus) throws IllegalStateException {
		this.checkUnregistered();
		this.team.setOption(option, optionStatus);
		this.update(TeamUpdateAction.CHANGE);
	}

}
