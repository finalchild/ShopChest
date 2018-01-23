package de.epiceric.shopchest.command;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

class ShopTabCompleter implements TabCompleter {

    private ShopChest plugin;

    ShopTabCompleter(ShopChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(plugin.getShopChestConfig().main_command_name)) {

            List<String> createSubCommands = Arrays.asList("admin", "normal");
            List<String> configSubCommands = Arrays.asList("add", "remove", "set");
            List<String> areaShopRemoveEvents = Arrays.asList("DELETE", "RESELL", "SELL", "UNRENT");
            List<String> townyShopPlots = Arrays.asList("ARENA", "COMMERCIAL", "EMBASSY", "FARM", "INN", "JAIL", "RESIDENTIAL", "SPLEEF", "WILDS");

            Set<String> configValues = plugin.getConfig().getKeys(true);

            ArrayList<String> returnCompletions = new ArrayList<>();

            switch (args.length) {
                case 2:
                    if (args[0].equals("config")) {
                        if (!args[1].equals("")) {
                            for (String s : configSubCommands) {
                                if (s.startsWith(args[1])) {
                                    returnCompletions.add(s);
                                }
                            }

                            return returnCompletions;
                        } else {
                            return configSubCommands;
                        }
                    }
                    break;
                case 3:
                    if (args[0].equals("config")) {
                        if (!args[2].equals("")) {
                            for (String s : configValues) {
                                if (s.startsWith(args[2])) {
                                    returnCompletions.add(s);
                                }
                            }

                            return returnCompletions;
                        } else {
                            return new ArrayList<>(configValues);
                        }
                    }
                    break;
                case 4:
                    if (args[0].equals("config")) {
                        if (args[2].equals("towny-shop-plots")) {
                            if (!args[3].equals("")) {
                                for (String s : townyShopPlots) {
                                    if (s.startsWith(args[3])) {
                                        returnCompletions.add(s);
                                    }
                                }

                                return returnCompletions;
                            } else {
                                return townyShopPlots;
                            }
                        } else if (args[2].equals("areashop-remove-shops")) {
                            if (!args[3].equals("")) {
                                for (String s : areaShopRemoveEvents) {
                                    if (s.startsWith(args[3])) {
                                        returnCompletions.add(s);
                                    }
                                }

                                return returnCompletions;
                            } else {
                                return areaShopRemoveEvents;
                            }
                        }
                    }
                    break;
                case 5:
                    if (args[0].equals("create")) {
                        if (!args[4].equals("")) {
                            for (String s : createSubCommands) {
                                if (s.startsWith(args[4])) {
                                    returnCompletions.add(s);
                                }
                            }

                            return returnCompletions;
                        } else {
                            return createSubCommands;
                        }
                    }
                    break;
            }
        }

        return new ArrayList<>();
    }
}
