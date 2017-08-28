package net.veilmc.dataapi.utils;

import com.customhcf.hcf.HCF;
import com.customhcf.hcf.faction.FactionManager;
import com.customhcf.hcf.faction.type.Faction;
import com.customhcf.hcf.faction.type.PlayerFaction;
import com.customhcf.util.MapSorting;
import net.minecraft.util.com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class TabUtils {

    public static String getCardinalDirection(final Player player) {
        double rotation = (player.getLocation().getYaw() - 90.0f) % 360.0f;
        if (rotation < 0.0) {
            rotation += 360.0;
        }
        if (0.0 <= rotation && rotation < 22.5) {
            return "N";
        }
        if (22.5 <= rotation && rotation < 67.5) {
            return "NE";
        }
        if (67.5 <= rotation && rotation < 112.5) {
            return "E";
        }
        if (112.5 <= rotation && rotation < 157.5) {
            return "SE";
        }
        if (157.5 <= rotation && rotation < 202.5) {
            return "S";
        }
        if (202.5 <= rotation && rotation < 247.5) {
            return "SW";
        }
        if (247.5 <= rotation && rotation < 292.5) {
            return "W";
        }
        if (292.5 <= rotation && rotation < 337.5) {
            return "NW";
        }
        if (337.5 <= rotation && rotation < 360.0) {
            return "N";
        }
        return null;
    }

    public static String translate(final Player player, String path) {
        FactionManager factionManager = HCF.getPlugin().getFactionManager();
        if (path.contains("%player_kills%")) {
            return path.replace("%player_kills%", String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS)));
        }
        if (path.contains("%player_deaths%")) {
            return path.replace("%player_deaths%", String.valueOf(player.getStatistic(Statistic.DEATHS)));
        }
        if (path.contains("%faction_location%")) {
            final Location location = player.getLocation();
            final Faction factionAt = factionManager.getFactionAt(location);
            return path.replace("%faction_location%", String.valueOf(factionAt.getDisplayName(player)));
        }
        if (path.contains("%player_location%")) {
            return path.replace("%player_location%", String.valueOf(getCardinalDirection(player)) + " (" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockZ() + ")");
        }
        if (path.contains("%online_players%")) {
            return path.replace("%online_players%", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()));
        }
        if (path.contains("%player_ping%")) {
            return path.replace("%player_ping%", String.valueOf(((CraftPlayer)player).getHandle().ping));
        }
        final Map<PlayerFaction, Integer> factionOnlineMap = new HashMap<>();
        //Player[] onlinePlayers;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player == null || player.canSee(target)) {
                PlayerFaction playerFaction = factionManager.getPlayerFaction(target.getUniqueId());
                if (playerFaction != null) {
                    factionOnlineMap.put(playerFaction, factionOnlineMap.getOrDefault(playerFaction, 0) + 1);
                }
            }
        }
        final List<Map.Entry<PlayerFaction, Integer>> sortedMap = (List<Map.Entry<PlayerFaction, Integer>>) MapSorting.sortedValues(factionOnlineMap, (Comparator)Comparator.reverseOrder());
        for (int i = 0; i < 20; ++i) {
            if (i >= sortedMap.size()) {
                return path.replace("%f_list_" + (i + 1) + "%", "");
            }
            else {
                String name = ChatColor.RED + sortedMap.get(i).getKey().getName();
                if (factionManager.getPlayerFaction(player.getUniqueId()) != null) {
                    name = sortedMap.get(i).getKey().getDisplayName(factionManager.getPlayerFaction(player.getUniqueId()));
                }
                return path.replace("%f_list_" + (i + 1) + "%", String.valueOf(name) + ChatColor.GRAY + " (" + sortedMap.get(i).getValue() + ")");
            }
        }
        if (factionManager.getPlayerFaction(player.getUniqueId()) != null) {
            if (path.contains("%f_title%")) {
                return path.replace("%f_title%", "Faction Info");
            }
            if (path.contains("%ftag%")) {
                return path.replace("%ftag%", factionManager.getPlayerFaction(player.getUniqueId()).getName());
            }
            if (path.contains("%fdtr%")) {
                return path.replace("%fdtr%", String.format("%.2f", factionManager.getPlayerFaction(player.getUniqueId()).getDeathsUntilRaidable()));
            }
            if (path.contains("%fhome%")) {
                if (factionManager.getPlayerFaction(player.getUniqueId()).getHome() != null) {
                    return path.replace("%fhome%", String.valueOf(ChatColor.WHITE.toString()) + factionManager.getPlayerFaction(player.getUniqueId()).getHome().getBlockX() + ", " + HCF.getInstance().getFactionManager().getPlayerFaction(player.getUniqueId()).getHome().getBlockY() + ", " + HCF.getInstance().getFactionManager().getPlayerFaction(player.getUniqueId()).getHome().getBlockZ());
                }
                else {
                    return path.replace("%fhome%", ChatColor.WHITE + "None");
                }
            }
            if (path.contains("%fleader%")) {
                return path.replace("%fleader%", factionManager.getPlayerFaction(player.getUniqueId()).getLeader().getName());
            }
            if (path.contains("%fbal%")) {
                return path.replace("%fbal%", "$" + factionManager.getPlayerFaction(player.getUniqueId()).getBalance());
            }
            final PlayerFaction playerFaction3 = factionManager.getPlayerFaction(player.getUniqueId());
            if (path.contains("%fonline%")) {
                return path.replace("%fonline%", String.valueOf(playerFaction3.getMembers().size()) + "/" + playerFaction3.getOnlinePlayers().size());
            }
            final List<Player> online = Lists.newArrayList(playerFaction3.getOnlinePlayers());
            online.sort(Comparator.comparing(HumanEntity::getName));
            online.sort(Comparator.comparingInt(o -> playerFaction3.getMember(o).getRole().ordinal()));
            for (int j = 0; j < 16; ++j) {
                if (j >= online.size()) {
                    return path.replace("%f_member_" + j + "%", "");
                }
                else {
                    return path.replace("%f_member_" + (j + 1) + "%", String.valueOf(playerFaction3.getMember(online.get(j)).getRole().getAstrix()) + online.get(j).getName());
                }
            }
        }
        else {
            if (path.contains("%f_title%")) {
                return "";
            }
            if (path.contains("%ftag%")) {
                return "";
            }
            if (path.contains("%fdtr%")) {
                return "";
            }
            if (path.contains("%fhome%")) {
                return "";
            }
            if (path.contains("%fleader%")) {
                return "";
            }
            if (path.contains("%fbal%")) {
                return "";
            }
            if (path.contains("%fonline%")) {
                return "";
            }
            for (int i = 1; i < 31; ++i) {
                return path.replace("%f_member_" + i + "%", "");
            }
            for (int i = 1; i < 31; ++i) {
                return path.replace("%f_list_" + i + "%", "");
            }
        }
        if (path.contains("%diamond%")) {
            return path.replace("%diamond%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE)));
        }
        if (path.contains("%lapis%")) {
            return path.replace("%lapis%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.LAPIS_ORE)));
        }
        if (path.contains("%iron%")) {
            return path.replace("%iron%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.IRON_ORE)));
        }
        if (path.contains("%gold%")) {
            return path.replace("%gold%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.GOLD_ORE)));
        }
        if (path.contains("%coal%")) {
            return path.replace("%coal%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.COAL_ORE)));
        }
        if (path.contains("%redstone%")) {
            return path.replace("%redstone%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.REDSTONE_ORE)));
        }
        if (path.contains("%emerald%")) {
            return path.replace("%emerald%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.EMERALD_ORE)));
        }
        return path;
    }

}
