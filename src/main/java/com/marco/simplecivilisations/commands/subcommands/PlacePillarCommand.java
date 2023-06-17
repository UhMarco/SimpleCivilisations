package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.Pillar;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.DaylightDetector;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PlacePillarCommand extends SubCommand {
    public PlacePillarCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("placepillar");
    }

    @Override
    public String getDescription() {
        return "Place a pillar on the block you are looking at.";
    }

    @Override
    public String getUsage() {
        return "/cv placepillar";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            User user = plugin.users.get(player.getUniqueId());
            Civilisation civilisation = plugin.civilisations.get(user.getCivilisationId());

            if (civilisation == null) {
                player.sendMessage(SimpleCivilisations.color + "You must be in a civilisation to run this command.");
                return;
            } else if (!civilisation.getLeader().toString().equals(player.getUniqueId().toString())) {
                player.sendMessage(SimpleCivilisations.color + "Only the leader may rename the civilisation.");
                return;
            } else if (civilisation.getPillarsAvailable() < 1) {
                player.sendMessage(SimpleCivilisations.color + "Your civilisation has no available pillars.");
                return;
            }

            Block block = player.getTargetBlockExact(5);
            if (block == null) {
                player.sendMessage(SimpleCivilisations.color + "The block you are looking at is too far away.");
                return;
            }

            World world = block.getWorld();
            if (block.isLiquid() || block.isPassable()) {
                player.sendMessage(SimpleCivilisations.color + "You can't place a pillar on this block.");
                return;
            } else if (List.of(
                    Material.AIR,
                    Material.CAVE_AIR,
                    Material.VOID_AIR
            ).contains(world.getBlockAt(block.getX(), block.getY() - 1, block.getZ()).getType())) {
                player.sendMessage(SimpleCivilisations.color + "The area underneath this block is not suitable for a pillar.");
                return;
            }

            if (overlapsTerritory(block.getLocation(), civilisation.getUniqueId())) {
                player.sendMessage(SimpleCivilisations.color + "This is too close to another pillar.");
                return;
            }

            Block above = world.getBlockAt(block.getX(), block.getY() + 1, block.getZ());
            if (above.isLiquid()) {
                player.sendMessage(SimpleCivilisations.color + "Pillars cannot be submerged.");
                return;
            }

            if (!world.getHighestBlockAt(block.getLocation()).equals(block)) {
                player.sendMessage(SimpleCivilisations.color + "Pillars cannot have any blocks above them.");
                return;
            }

            int levels = 0, total = 0;
            Chunk chunk = block.getChunk();
            for (int x = chunk.getX() * 16; x < chunk.getX() * 16 + 16; x++) {
                for (int z = chunk.getZ() * 16; z < chunk.getZ() * 16 + 16; z++) {
                    Block b = world.getHighestBlockAt(x, z);
                    if (b.getType().toString().endsWith("LEAVES")) continue;
                    levels += world.getHighestBlockYAt(x, z);
                    total += 1;
                }
            }
            int average = levels / total;

            if (Math.abs(block.getY() - average) > 2) {
                player.sendMessage(SimpleCivilisations.color + "Pillars can only be placed on an even, above ground surface.");
                return;
            }

            int x = above.getX(), y = above.getY(), z = above.getZ();

            Location location = new Location(world, x, y, z);
            if (player.getLocation().distance(location) < 1) {
                player.teleport(new Location(world, x - 0.5, world.getHighestBlockYAt(x - 1, z - 1) + 1, z - 0.5, -45, 0));
            }
            player.playEffect(EntityEffect.TELEPORT_ENDER);

            Block b1 = world.getBlockAt(x, y, z);
            b1.setType(Material.SPRUCE_LOG);
            surroundWithStairs(b1, false);
            world.getBlockAt(x, y + 1, z).setType(Material.OBSIDIAN);
            world.getBlockAt(x, y + 2, z).setType(Material.STONE_BRICK_WALL);
            world.getBlockAt(x, y + 3, z).setType(Material.STONE_BRICK_WALL);
            Block b2 = world.getBlockAt(x, y + 4, z);
            b2.setType(Material.REDSTONE_LAMP);
            surroundWithStairs(b2, true);
            Block b3 = world.getBlockAt(x, y + 5, z);
            b3.setType(Material.DAYLIGHT_DETECTOR);
            DaylightDetector sensorData = (DaylightDetector) b3.getBlockData();
            sensorData.setInverted(true);
            b3.setBlockData(sensorData, false);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                civilisation.addPillar(new Location(world, x, y + 1, z));
                civilisation.usePillar();
                player.sendMessage(SimpleCivilisations.color + "Pillar placed.");
                plugin.update(civilisation);
            });
            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players may run this command.");
    }

    public void surroundWithStairs(Block centerBlock, boolean upsidedown) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        for (BlockFace face : faces) {
            Block block = centerBlock.getRelative(face);
            block.setType(Material.SPRUCE_STAIRS);

            Stairs stairData = (Stairs) block.getBlockData();
            if (upsidedown) stairData.setHalf(Bisected.Half.TOP);
            stairData.setFacing(face.getOppositeFace());
            block.setBlockData(stairData, false);
        }
    }

    public boolean overlapsTerritory(Location location, UUID uuid) {
        World world = location.getWorld();
        double halfLength = (4 * 16 + 7.5);
        double x = location.getChunk().getX() * 16 + 7.5;
        double z = location.getChunk().getZ() * 16 + 7.5;

        for (Civilisation civilisation : plugin.civilisations.values()) {
            for (Pillar pillar : civilisation.getPillars()) {
                if (pillar.getCivilisationId() == uuid) {
                    if (location.getChunk() == pillar.getLocation().getChunk()) {
                        return true;
                    }
                } else {
                    if (
                            SimpleCivilisations.inRangeOfPillar(new Location(world, x + halfLength, location.getBlockY(), z + halfLength), pillar) ||
                            SimpleCivilisations.inRangeOfPillar(new Location(world, x + halfLength, location.getBlockY(), z - halfLength), pillar) ||
                            SimpleCivilisations.inRangeOfPillar(new Location(world, x - halfLength, location.getBlockY(), z + halfLength), pillar) ||
                            SimpleCivilisations.inRangeOfPillar(new Location(world, x - halfLength, location.getBlockY(), z - halfLength), pillar)
                    ) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
