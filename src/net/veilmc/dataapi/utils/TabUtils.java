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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabUtils {

    private static String getCardinalDirection(final Player player) {
        double rotation = (player.getLocation().getYaw() - 90.0f) % 360.0f;
        if (rotation < 0.0) {
            rotation += 360.0;
        }
        if (0.0 <= rotation && rotation < 22.5) {
            return "W";
        }
        if (22.5 <= rotation && rotation < 67.5) {
            return "NW";
        }
        if (67.5 <= rotation && rotation < 112.5) {
            return "N";
        }
        if (112.5 <= rotation && rotation < 157.5) {
            return "NE";
        }
        if (157.5 <= rotation && rotation < 202.5) {
            return "E";
        }
        if (202.5 <= rotation && rotation < 247.5) {
            return "SE";
        }
        if (247.5 <= rotation && rotation < 292.5) {
            return "S";
        }
        if (292.5 <= rotation && rotation < 337.5) {
            return "SW";
        }
        if (337.5 <= rotation && rotation < 360.0) {
            return "W";
        }
        return null;
    }

    public static String translate(final Player player, String path) {
        FactionManager factionManager = HCF.getPlugin().getFactionManager();
        PlayerFaction playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
        
        if (path.contains("%player_kills%")) {
            path = path.replace("%player_kills%", String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS)));
        }
        if (path.contains("%player_deaths%")) {
            path = path.replace("%player_deaths%", String.valueOf(player.getStatistic(Statistic.DEATHS)));
        }
        if (path.contains("%faction_location%")) {
            final Location location = player.getLocation();
            final Faction factionAt = factionManager.getFactionAt(location);
            path = path.replace("%faction_location%", String.valueOf(factionAt.getDisplayName(player)));
        }
        if (path.contains("%player_location%")) {
            path = path.replace("%player_location%", "(" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockZ() + ") [" + getCardinalDirection(player) + "]");
        }
        if (path.contains("%online_players%")) {
            path = path.replace("%online_players%", String.valueOf(Bukkit.getServer().getOnlinePlayers().size() + "/" + Bukkit.getServer().getMaxPlayers()));
        }
        if (path.contains("%player_ping%")) {
            path = path.replace("%player_ping%", String.valueOf(((CraftPlayer)player).getHandle().ping));
        }
        if (path.contains("%player_lives%")) {
            path = path.replace("%player_lives%", String.valueOf(HCF.getPlugin().getDeathbanManager().getLives(player.getUniqueId())));
        }
        if (path.contains("%player_bal%")) {
            path = path.replace("%player_bal%", "$" + String.valueOf(HCF.getPlugin().getEconomyManager().getBalance(player.getUniqueId())));
        }
        final Map<PlayerFaction, Integer> factionOnlineMap = new HashMap<>();
        //Player[] onlinePlayers;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player == null || player.canSee(target)) {
                PlayerFaction pFac = factionManager.getPlayerFaction(target.getUniqueId());
                if (pFac != null) {
                    factionOnlineMap.put(pFac, factionOnlineMap.getOrDefault(pFac, 0) + 1);
                }
            }
        }
        final List<Map.Entry<PlayerFaction, Integer>> sortedMap = (List<Map.Entry<PlayerFaction, Integer>>) MapSorting.sortedValues(factionOnlineMap, (Comparator)Comparator.reverseOrder());
        for (int i = 0; i < 20; ++i) {
            if (i >= sortedMap.size()) {
                path = path.replace("%f_list_" + (i + 1) + "%", "");
            }
            else {
                String name = ChatColor.RED + sortedMap.get(i).getKey().getName();
                if (playerFaction != null) {
                    name = sortedMap.get(i).getKey().getDisplayName(playerFaction);
                }
                path = path.replace("%f_list_" + (i + 1) + "%", String.valueOf(name) + ChatColor.GRAY + " (" + sortedMap.get(i).getValue() + ")");
            }
        }
        if (playerFaction != null) {
            if (path.contains("%f_title%")) {
                path = path.replace("%f_title%", "Faction Info");
            }
            if (path.contains("%ftag%")) {
                path = path.replace("%ftag%", playerFaction.getDisplayName(player));
            }
            if (path.contains("%fdtr%")) {
                path = path.replace("%fdtr%", "&7DTR: &a" + String.format("%.2f", playerFaction.getDeathsUntilRaidable()));
            }
            if (path.contains("%fhome%")) {
                if (playerFaction.getHome() != null) {
                    path = path.replace("%fhome%", "&7HQ: &a" + playerFaction.getHome().getBlockX() + ", " + HCF.getInstance().getFactionManager().getPlayerFaction(player.getUniqueId()).getHome().getBlockY() + ", " + HCF.getInstance().getFactionManager().getPlayerFaction(player.getUniqueId()).getHome().getBlockZ());
                }
                else {
                    path = path.replace("%fhome%", "&7HQ: &a" + "None");
                }
            }
            if (path.contains("%fleader%")) {
                path = path.replace("%fleader%", playerFaction.getLeader().getName());
            }
            if (path.contains("%fbal%")) {
                path = path.replace("%fbal%", "&7Balance: &a" + "$" + playerFaction.getBalance());
            }
            final PlayerFaction playerFaction3 = playerFaction;
            if (path.contains("%fonline%")) {
                path = path.replace("%fonline%", "&7Online: &a" + String.valueOf(playerFaction3.getOnlinePlayers().size()) + "/" + playerFaction3.getMembers().size());
            }
            final List<Player> online = Lists.newArrayList(playerFaction3.getOnlinePlayers());
            online.sort(Comparator.comparing(HumanEntity::getName));
            online.sort(Comparator.comparingInt(o -> playerFaction3.getMember(o).getRole().ordinal()));
            for (int j = 0; j < 16; ++j) {
                if (j >= online.size()) {
                    path = path.replace("%f_member_" + j + "%", "");
                }
                else {
                    path = path.replace("%f_member_" + (j + 1) + "%", "&a" + String.valueOf(playerFaction3.getMember(online.get(j)).getRole().getAstrix()) + online.get(j).getName());
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
                path = path.replace("%f_member_" + i + "%", "");
            }
            for (int i = 1; i < 31; ++i) {
                path = path.replace("%f_list_" + i + "%", "");
            }
        }
        if (path.contains("%diamond%")) {
            path = path.replace("%diamond%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE)));
        }
        if (path.contains("%lapis%")) {
            path = path.replace("%lapis%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.LAPIS_ORE)));
        }
        if (path.contains("%iron%")) {
            path = path.replace("%iron%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.IRON_ORE)));
        }
        if (path.contains("%gold%")) {
            path = path.replace("%gold%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.GOLD_ORE)));
        }
        if (path.contains("%coal%")) {
            path = path.replace("%coal%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.COAL_ORE)));
        }
        if (path.contains("%redstone%")) {
            path = path.replace("%redstone%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.REDSTONE_ORE)));
        }
        if (path.contains("%emerald%")) {
            path = path.replace("%emerald%", String.valueOf(player.getStatistic(Statistic.MINE_BLOCK, Material.EMERALD_ORE)));
        }
        return ChatColor.translateAlternateColorCodes('&', path);
    }

}
