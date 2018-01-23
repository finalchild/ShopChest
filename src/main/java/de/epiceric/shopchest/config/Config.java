package de.epiceric.shopchest.config;

import com.google.common.base.Charsets;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.ShopUpdater;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {

    private ShopChest plugin;

    private LanguageConfiguration langConfig;

    /** The quality of hologram and item updating (performance saving, or better quality) **/
    public ShopUpdater.UpdateQuality update_quality;

    /** The item with which a player can click a shop to retrieve information **/
    public ItemStack shop_info_item;

    /** The default value for the custom WorldGuard flag 'create-shop' **/
    public boolean wg_allow_create_shop_default;

    /** The default value for the custom WorldGuard flag 'use-admin-shop' **/
    public boolean wg_allow_use_admin_shop_default;

    /** The default value for the custom WorldGuard flag 'use-shop' **/
    public boolean wg_allow_use_shop_default;

    /** The types of town plots residents are allowed to create shops in **/
    public List<String> towny_shop_plots_residents;

    /** The types of town plots the mayor is allowed to create shops in **/
    public List<String> towny_shop_plots_mayor;

    /** The types of town plots the king is allowed to create shops in **/
    public List<String> towny_shop_plots_king;

    /** The events of AreaShop when shops in that region should be removed **/
    public List<String> areashop_remove_shop_events;

    /** The hostname used in ShopChest's MySQL database **/
    public String database_mysql_host;

    /** The port used for ShopChest's MySQL database **/
    public int database_mysql_port;

    /** The database used for ShopChest's MySQL database **/
    public String database_mysql_database;

    /** The username used in ShopChest's MySQL database **/
    public String database_mysql_username;

    /** The password used in ShopChest's MySQL database **/
    public String database_mysql_password;

    /** The database type used for ShopChest. **/
    public Database.DatabaseType database_type;

    /** The interval in seconds, a ping is sent to the MySQL server **/
    public int database_mysql_ping_interval;

    /**
     * <p>The minimum prices for certain items</p>
     * This returns a key set, which contains e.g "STONE", "STONE:1", of the <i>minimum-prices</i> section in ShopChest's config.
     * To actually retrieve the minimum price for an item, you have to get the double {@code minimum-prices.<key>}.
     **/
    public Set<String> minimum_prices;

    /**
     * <p>The maximum prices for certain items</p>
     * This returns a key set, which contains e.g "STONE", "STONE:1", of the {@code maximum-prices} section in ShopChest's config.
     * To actually retrieve the maximum price for an item, you have to get the double {@code maximum-prices.<key>}.
     **/
    public Set<String> maximum_prices;

    /**
     * <p>List containing items, of which players can't create a shop</p>
     * If this list contains an item (e.g "STONE", "STONE:1"), it's in the blacklist.
     **/
    public List<String> blacklist;

    /** Whether prices may contain decimals **/
    public boolean allow_decimals_in_price;

    /** Whether the buy price of a shop must be greater than or equal the sell price **/
    public boolean buy_greater_or_equal_sell;

    /** Whether shops should be protected by hoppers **/
    public boolean hopper_protection;

    /** Whether shops should be protected by explosions **/
    public boolean explosion_protection;

    /** Whether buys and sells must be confirmed **/
    public boolean confirm_shopping;

    /** Whether quality mode should be enabled **/
    public boolean enable_quality_mode;

    /** Whether hologram interaction should be enabled **/
    public boolean enable_hologram_interaction;

    /** Whether the debug log file should be created **/
    public boolean enable_debug_log;

    /** Whether buys and sells should be logged in the database **/
    public boolean enable_ecomomy_log;

    /** Whether WorldGuard integration should be enabled **/
    public boolean enable_worldguard_integration;

    /** Whether Towny integration should be enabled **/
    public boolean enable_towny_integration;

    /** Whether AuthMe integration should be enabled **/
    public boolean enable_authme_integration;

    /** Whether PlotSquared integration should be enabled **/
    public boolean enable_plotsquared_integration;

    /** Whether uSkyBlock integration should be enabled **/
    public boolean enable_uskyblock_integration;

    /** Whether ASkyBlock integration should be enabled **/
    public boolean enable_askyblock_integration;

    /** Whether IslandWorld integration should be enabled **/
    public boolean enable_islandworld_integration;

    /** Whether GriefPrevention integration should be enabled **/
    public boolean enable_griefprevention_integration;

    /** Whether AreaShop integration should be enabled **/
    public boolean enable_areashop_integration;

    /** Whether the vendor of the shop should get messages about buys and sells **/
    public boolean enable_vendor_messages;

    /** Whether admin shops should be excluded of the shop limits **/
    public boolean exclude_admin_shops;

    /** Whether the extension of a potion or tipped arrow (if available) should be appended to the item name. **/
    public boolean append_potion_level_to_item_name;

    /** Whether the shop items should be shown **/
    public boolean show_shop_items;

    /** Whether players are allowed to sell/buy broken items **/
    public boolean allow_broken_items;

    /** Whether only the shops a player has in sight should be shown to him **/
    public boolean only_show_shops_in_sight;

    /** Whether only the shop a player is looking at should be shown to him **/
    public boolean only_show_first_shop_in_sight;

    /**
     * <p>Whether shops should automatically be removed from the database if an error occurred while loading</p>
     * (e.g. when no chest is found at a shop's location)
     */
    public boolean remove_shop_on_error;

    /** Whether the item amount should be calculated to fit the available money or inventory space **/
    public boolean auto_calculate_item_amount;

    /**
     * <p>Whether the mouse buttons are inverted</p>
     *
     * <b>Default:</b><br>
     * Right-Click: Buy<br>
     * Left-Click: Sell
     **/
    public boolean invert_mouse_buttons;

    /** Whether the hologram's location should be fixed at the bottom **/
    public boolean hologram_fixed_bottom;

    /** Amount every hologram should be lifted **/
    public double hologram_lift;

    /** The maximum distance between a player and a shop to see the hologram **/
    public double maximal_distance;

    /** The maximum distance between a player and a shop to see the shop item **/
    public double maximal_item_distance;

    /** The price a player has to pay in order to create a normal shop **/
    public double shop_creation_price_normal;

    /** The price a player has to pay in order to create an admin shop **/
    public double shop_creation_price_admin;

    /** The default shop limit for players whose limit is not set via a permission **/
    public int default_limit;

    /** The main command of ShopChest <i>(default: shop)</i> **/
    public String main_command_name;

    /** The language file to use (e.g <i>en_US</i>, <i>de_DE</i>) **/
    public String language_file;


    public Config(ShopChest plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();

        reload(true, true, true);
    }

    /**
     * <p>Set a configuration value</p>
     * <i>Config is automatically reloaded</i>
     *
     * @param property Property to change
     * @param value Value to set
     */
    public void set(String property, String value) {
        boolean langChange = property.equalsIgnoreCase("language-file");
        try {
            int intValue = Integer.parseInt(value);
            plugin.getConfig().set(property, intValue);

            plugin.saveConfig();
            reload(false, langChange, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            plugin.getConfig().set(property, doubleValue);

            plugin.saveConfig();
            reload(false, langChange, false);

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            plugin.getConfig().set(property, boolValue);
        } else {
            plugin.getConfig().set(property, value);
        }

        plugin.saveConfig();

        reload(false, langChange, false);
    }

    /**
     * Add a value to a list in the config.yml.
     * If the list does not exist, a new list with the given value will be created
     * @param property Location of the list
     * @param value Value to add
     */
    public void add(String property, String value) {
        List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);

        try {
            int intValue = Integer.parseInt(value);
            list.add(intValue);

            plugin.saveConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.add(doubleValue);

            plugin.saveConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            list.add(boolValue);
        } else {
            list.add(value);
        }

        plugin.saveConfig();

        reload(false, false, false);
    }

    public void remove(String property, String value) {
        List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);

        try {
            int intValue = Integer.parseInt(value);
            list.remove(intValue);

            plugin.saveConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.remove(doubleValue);

            plugin.saveConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            list.remove(boolValue);
        } else {
            list.remove(value);
        }

        plugin.saveConfig();

        reload(false, false, false);
    }

    /**
     * Reload the configuration values from config.yml
     */
    public void reload(boolean firstLoad, boolean langReload, boolean showMessages) {
        plugin.reloadConfig();

        update_quality = ShopUpdater.UpdateQuality.valueOf(plugin.getConfig().getString("update-quality"));
        shop_info_item = ItemUtils.getItemStack(plugin.getConfig().getString("shop-info-item"));
        wg_allow_create_shop_default = plugin.getConfig().getBoolean("worldguard-default-flag-values.create-shop");
        wg_allow_use_admin_shop_default = plugin.getConfig().getBoolean("worldguard-default-flag-values.use-admin-shop");
        wg_allow_use_shop_default = plugin.getConfig().getBoolean("worldguard-default-flag-values.use-shop");
        towny_shop_plots_residents = plugin.getConfig().getStringList("towny-shop-plots.residents");
        towny_shop_plots_mayor = plugin.getConfig().getStringList("towny-shop-plots.mayor");
        towny_shop_plots_king = plugin.getConfig().getStringList("towny-shop-plots.king");
        areashop_remove_shop_events = plugin.getConfig().getStringList("areashop-remove-shops");
        database_mysql_ping_interval = plugin.getConfig().getInt("database.mysql.ping-interval");
        database_mysql_host = plugin.getConfig().getString("database.mysql.hostname");
        database_mysql_port = plugin.getConfig().getInt("database.mysql.port");
        database_mysql_database = plugin.getConfig().getString("database.mysql.database");
        database_mysql_username = plugin.getConfig().getString("database.mysql.username");
        database_mysql_password = plugin.getConfig().getString("database.mysql.password");
        database_type = Database.DatabaseType.valueOf(plugin.getConfig().getString("database.type"));
        minimum_prices = (plugin.getConfig().getConfigurationSection("minimum-prices") == null) ? new HashSet<>() : plugin.getConfig().getConfigurationSection("minimum-prices").getKeys(true);
        maximum_prices = (plugin.getConfig().getConfigurationSection("maximum-prices") == null) ? new HashSet<>() : plugin.getConfig().getConfigurationSection("maximum-prices").getKeys(true);
        allow_decimals_in_price = plugin.getConfig().getBoolean("allow-decimals-in-price");
        allow_broken_items = plugin.getConfig().getBoolean("allow-broken-items");
        auto_calculate_item_amount = (allow_decimals_in_price && plugin.getConfig().getBoolean("auto-calculate-item-amount"));
        blacklist = (plugin.getConfig().getStringList("blacklist") == null) ? new ArrayList<>() : plugin.getConfig().getStringList("blacklist");
        buy_greater_or_equal_sell = plugin.getConfig().getBoolean("buy-greater-or-equal-sell");
        hopper_protection = plugin.getConfig().getBoolean("hopper-protection");
        explosion_protection = plugin.getConfig().getBoolean("explosion-protection");
        confirm_shopping = plugin.getConfig().getBoolean("confirm-shopping");
        enable_quality_mode = plugin.getConfig().getBoolean("enable-quality-mode");
        enable_hologram_interaction = plugin.getConfig().getBoolean("enable-hologram-interaction");
        enable_debug_log = plugin.getConfig().getBoolean("enable-debug-log");
        enable_ecomomy_log = plugin.getConfig().getBoolean("enable-economy-log");
        enable_worldguard_integration = plugin.getConfig().getBoolean("enable-worldguard-integration");
        enable_towny_integration = plugin.getConfig().getBoolean("enable-towny-integration");
        enable_authme_integration = plugin.getConfig().getBoolean("enable-authme-integration");
        enable_plotsquared_integration = plugin.getConfig().getBoolean("enable-plotsquared-integration");
        enable_uskyblock_integration = plugin.getConfig().getBoolean("enable-uskyblock-integration");
        enable_askyblock_integration = plugin.getConfig().getBoolean("enable-askyblock-integration");
        enable_islandworld_integration = plugin.getConfig().getBoolean("enable-islandworld-integration");
        enable_griefprevention_integration = plugin.getConfig().getBoolean("enable-griefprevention-integration");
        enable_areashop_integration = plugin.getConfig().getBoolean("enable-areashop-integration");
        enable_vendor_messages = plugin.getConfig().getBoolean("enable-vendor-messages");
        only_show_shops_in_sight = plugin.getConfig().getBoolean("only-show-shops-in-sight");
        only_show_first_shop_in_sight = plugin.getConfig().getBoolean("only-show-first-shop-in-sight");
        exclude_admin_shops = plugin.getConfig().getBoolean("shop-limits.exclude-admin-shops");
        append_potion_level_to_item_name = plugin.getConfig().getBoolean("append-potion-level-to-item-name");
        show_shop_items = plugin.getConfig().getBoolean("show-shop-items");
        remove_shop_on_error = plugin.getConfig().getBoolean("remove-shop-on-error");
        invert_mouse_buttons = plugin.getConfig().getBoolean("invert-mouse-buttons");
        hologram_fixed_bottom = plugin.getConfig().getBoolean("hologram-fixed-bottom");
        hologram_lift = plugin.getConfig().getDouble("hologram-lift");
        maximal_distance = plugin.getConfig().getDouble("maximal-distance");
        maximal_item_distance = plugin.getConfig().getDouble("maximal-item-distance");
        shop_creation_price_normal = plugin.getConfig().getDouble("shop-creation-price.normal");
        shop_creation_price_admin = plugin.getConfig().getDouble("shop-creation-price.admin");
        default_limit = plugin.getConfig().getInt("shop-limits.default");
        main_command_name = plugin.getConfig().getString("main-command-name");
        language_file = plugin.getConfig().getString("language-file");

        if (firstLoad || langReload) loadLanguageConfig(showMessages);
        if (!firstLoad && langReload) LanguageUtils.load();
    }

    /**
     * @return ShopChest's {@link LanguageConfiguration}
     */
    public LanguageConfiguration getLanguageConfig() {
        return langConfig;
    }

    private Reader getTextResource(String file, boolean showMessages) {
        final InputStream in = plugin.getResource(file);
        if (in == null) {
            if (showMessages) plugin.getLogger().severe("Failed to get file from jar: " + file);
            plugin.debug("Failed to get file from jar: " + file);
            return null;
        }

        return new InputStreamReader(in, Charsets.UTF_8);
    }

    private void loadLanguageConfig(boolean showMessages) {
        langConfig = new LanguageConfiguration(plugin, showMessages);
        Path langFolder = plugin.getDataFolder().toPath().resolve("lang");

        if (Files.notExists(langFolder.resolve("en_US.lang")))
            plugin.saveResource("lang/en_US.lang", false);

        if (Files.notExists(langFolder.resolve("de_DE.lang")))
            plugin.saveResource("lang/de_DE.lang", false);

        String localeToUse = language_file;
        Path langConfigFile = langFolder.resolve(localeToUse + ".lang");
        if (Files.exists(langConfigFile)) {
            try {
                if (showMessages) plugin.getLogger().info("Using locale \"" + localeToUse + "\"");
                langConfig.load(langConfigFile);
            } catch (IOException e) {
                if (showMessages) {
                    plugin.getLogger().warning("Using default language values");
                }

                plugin.debug("Using default language values (#4)");
                plugin.debug(e);
            }
            return;
        }

        localeToUse = "en_US";
        langConfigFile = langFolder.resolve(localeToUse + ".lang");
        if (Files.exists(langConfigFile)) {
            try {
                if (showMessages) plugin.getLogger().info("Using locale \"en_US\"");
                langConfig.load(langConfigFile);
            } catch (IOException e) {
                if (showMessages) {
                    plugin.getLogger().warning("Using default language values");
                }

                plugin.debug("Using default language values (#3)");
                plugin.debug(e);
            }
            return;
        }

        localeToUse = language_file;
        Reader r = getTextResource("lang/" + localeToUse + ".lang", showMessages);

        if (r == null) {
            localeToUse = "en_US";
            r = getTextResource("lang/" + localeToUse + ".lang", showMessages);
        }
        if (r == null) {
            localeToUse = null;
        }

        if (showMessages) {
            if (localeToUse != null) {
                plugin.getLogger().info("Using locale \"" + localeToUse + "\" (Streamed from jar file)");
            } else {
                plugin.getLogger().warning("Using default language values");
            }
        }

        if (localeToUse != null) {
            try {
                langConfig.loadFromReader(r);
            } catch (IOException e) {
                if (showMessages) {
                    plugin.getLogger().warning("Using default language values");
                }

                plugin.debug("Using default language values (#2)");
                plugin.debug(e);
            }
            return;
        }

        plugin.debug("Using default language values (#1)");
    }

}
