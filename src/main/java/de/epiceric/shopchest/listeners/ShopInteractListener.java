package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.event.ShopBuySellEvent;
import de.epiceric.shopchest.event.ShopCreateEvent;
import de.epiceric.shopchest.event.ShopInfoEvent;
import de.epiceric.shopchest.event.ShopRemoveEvent;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;

import java.util.HashMap;
import java.util.Map;

public class ShopInteractListener implements Listener {

    private ShopChest plugin;
    private Permission perm;
    private Economy econ;
    private Database database;
    private ShopUtils shopUtils;

    public ShopInteractListener(ShopChest plugin) {
        this.plugin = plugin;
        this.perm = plugin.getPermission();
        this.econ = plugin.getEconomy();
        this.database = plugin.getShopDatabase();
        this.shopUtils = plugin.getShopUtils();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractCreate(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (ClickType.getPlayerClickType(p) != null) {
                        if (ClickType.getPlayerClickType(p).getClickType() == ClickType.EnumClickType.CREATE) {
                            if (!shopUtils.isShop(b.getLocation())) {
                                if (e.isCancelled() && !perm.has(p, "shopchest.create.protected")) {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE_PROTECTED));
                                    ClickType.removePlayerClickType(p);
                                    plugin.debug(p.getName() + " is not allowed to create a shop on the selected chest");
                                    return;
                                }

                                e.setCancelled(true);

                                if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
                                    ClickType clickType = ClickType.getPlayerClickType(p);
                                    ItemStack product = clickType.getProduct();
                                    double buyPrice = clickType.getBuyPrice();
                                    double sellPrice = clickType.getSellPrice();
                                    ShopType shopType = clickType.getShopType();

                                    create(p, b.getLocation(), product, buyPrice, sellPrice, shopType);
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_BLOCKED));
                                    plugin.debug("Chest is blocked");
                                }
                            } else {
                                e.setCancelled(true);
                                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_ALREADY_SHOP));
                                plugin.debug("Chest is already a shop");
                            }

                            ClickType.removePlayerClickType(p);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {

            if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {

                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                    if (ClickType.getPlayerClickType(p) != null) {

                        switch (ClickType.getPlayerClickType(p).getClickType()) {
                            case INFO:
                                e.setCancelled(true);

                                if (shopUtils.isShop(b.getLocation())) {
                                    Shop shop = shopUtils.getShop(b.getLocation());
                                    info(p, shop);
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_NO_SHOP));
                                    plugin.debug("Chest is not a shop");
                                }

                                ClickType.removePlayerClickType(p);
                                break;

                            case REMOVE:
                                e.setCancelled(true);

                                if (shopUtils.isShop(b.getLocation())) {
                                    Shop shop = shopUtils.getShop(b.getLocation());

                                    if (shop.getVendor().getUniqueId().equals(p.getUniqueId()) || perm.has(p, "shopchest.removeOther")) {
                                        remove(p, shop);
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_REMOVE_OTHERS));
                                        plugin.debug(p.getName() + " is not permitted to remove another player's shop");
                                    }

                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_NO_SHOP));
                                    plugin.debug("Chest is not a shop");
                                }

                                ClickType.removePlayerClickType(p);
                                break;

                        }

                    } else {

                        if (shopUtils.isShop(b.getLocation())) {
                            e.setCancelled(true);
                            Shop shop = shopUtils.getShop(b.getLocation());

                            if (p.isSneaking()) {
                                if (!shop.getVendor().getUniqueId().equals(p.getUniqueId())) {
                                    if (perm.has(p, "shopchest.openOther")) {
                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.OPENED_SHOP, new LocalizedMessage.ReplacedRegex(Regex.VENDOR, shop.getVendor().getName())));
                                        plugin.debug(p.getName() + " is opening " + shop.getVendor().getName() + "'s shop (#" + shop.getID() + ")" );
                                        e.setCancelled(false);
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_OPEN_OTHERS));
                                        plugin.debug(p.getName() + " is not permitted to open another player's shop");
                                    }
                                } else {
                                    e.setCancelled(false);
                                }
                            } else {
                                if (shop.getShopType() == ShopType.ADMIN || !shop.getVendor().getUniqueId().equals(p.getUniqueId())) {
                                    plugin.debug(p.getName() + " wants to buy");
                                    if (shop.getBuyPrice() > 0) {
                                        if (perm.has(p, "shopchest.buy")) {
                                            if (shop.getShopType() == ShopType.ADMIN) {
                                                buy(p, shop);
                                            } else {
                                                Chest c = (Chest) b.getState();
                                                if (Utils.getAmount(c.getInventory(), shop.getProduct()) >= shop.getProduct().getAmount()) {
                                                    buy(p, shop);
                                                } else {
                                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.OUT_OF_STOCK));
                                                    plugin.debug("Shop is out of stock");
                                                }
                                            }
                                        } else {
                                            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_BUY));
                                            plugin.debug(p.getName() + " is not permitted to buy");
                                        }
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUYING_DISABLED));
                                        plugin.debug("Buying is disabled");
                                    }
                                } else {
                                    e.setCancelled(false);
                                }
                            }
                        }

                    }


                } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

                    if (shopUtils.isShop(b.getLocation())) {
                        if (p.isSneaking())
                            return;

                        e.setCancelled(true);
                        Shop shop = shopUtils.getShop(b.getLocation());

                        if ((shop.getShopType() == ShopType.ADMIN) || (!shop.getVendor().getUniqueId().equals(p.getUniqueId()))) {
                            plugin.debug(p.getName() + " wants to sell");
                            if (shop.getSellPrice() > 0) {
                                if (perm.has(p, "shopchest.sell")) {
                                    if (Utils.getAmount(p.getInventory(), shop.getProduct()) >= shop.getProduct().getAmount()) {
                                        sell(p, shop);
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NOT_ENOUGH_ITEMS));
                                        plugin.debug(p.getName() + " doesn't have enough items");
                                    }
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_SELL));
                                    plugin.debug(p.getName() + " is not permitted to sell");
                                }
                            } else {
                                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELLING_DISABLED));
                                plugin.debug("Selling is disabled");
                            }
                        } else {
                            e.setCancelled(false);
                        }
                    }

                }

            }

        } else {
            if (ClickType.getPlayerClickType(p) != null) ClickType.removePlayerClickType(p);
        }

    }

    /**
     * Create a new shop
     *
     * @param executor  Player, who executed the command, will receive the message and become the vendor of the shop
     * @param location  Where the shop will be located
     * @param product   Product of the Shop
     * @param buyPrice  Buy price
     * @param sellPrice Sell price
     * @param shopType  Type of the shop
     */
    private void create(Player executor, Location location, ItemStack product, double buyPrice, double sellPrice, ShopType shopType) {
        plugin.debug(executor.getName() + " is creating new shop...");

        int id = database.getNextFreeID();
        double creationPrice = (shopType == ShopType.NORMAL) ? plugin.getShopChestConfig().shop_creation_price_normal : plugin.getShopChestConfig().shop_creation_price_admin;

        ShopCreateEvent event = new ShopCreateEvent(executor, Shop.createImaginaryShop(executor, product, location, buyPrice, sellPrice,shopType), creationPrice);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            plugin.debug("Create event cancelled (#" + id + ")");
            return;
        }
        EconomyResponse r = plugin.getEconomy().withdrawPlayer(executor, creationPrice);
        if (!r.transactionSuccess()) {
            plugin.debug("Economy transaction failed: " + r.errorMessage);
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r.errorMessage)));
            return;
        }

        Shop shop = new Shop(id, plugin, executor, product, location, buyPrice, sellPrice, shopType);

        plugin.debug("Shop created (#" + id + ")");
        shopUtils.addShop(shop, true);
        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_CREATED));

        for (Player p : Bukkit.getOnlinePlayers()) {
            Bukkit.getPluginManager().callEvent(new PlayerMoveEvent(p, p.getLocation(), p.getLocation()));
        }

    }

    /**
     * Remove a shop
     * @param executor Player, who executed the command and will receive the message
     * @param shop Shop to be removed
     */
    private void remove(Player executor, Shop shop) {
        plugin.debug(executor.getName() + " is removing shop (#" + shop.getID() + ")");
        ShopRemoveEvent event = new ShopRemoveEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Remove event cancelled (#" + shop.getID() + ")");
            return;
        }

        shopUtils.removeShop(shop, true);
        plugin.debug("Removed shop (#" + shop.getID() + ")");
        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_REMOVED));
    }

    /**
     *
     * @param executor Player, who executed the command and will retrieve the information
     * @param shop Shop from which the information will be retrieved
     */
    private void info(Player executor, Shop shop) {
        plugin.debug(executor.getName() + " is retrieving shop info (#" + shop.getID() + ")");
        ShopInfoEvent event = new ShopInfoEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Info event cancelled (#" + shop.getID() + ")");
            return;
        }
        Chest c = (Chest) shop.getLocation().getBlock().getState();

        int amount = Utils.getAmount(c.getInventory(), shop.getProduct());
        Material type = shop.getProduct().getType();

        String vendor = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_VENDOR, new LocalizedMessage.ReplacedRegex(Regex.VENDOR, shop.getVendor().getName()));
        String product = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_PRODUCT, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(shop.getProduct())));
        String enchantmentString = "";
        String potionEffectString = "";
        String musicDiscName = LanguageUtils.getMusicDiscName(type);
        String price = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_PRICE, new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(shop.getBuyPrice())),
                new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(shop.getSellPrice())));
        String shopType = LanguageUtils.getMessage(shop.getShopType() == ShopType.NORMAL ? LocalizedMessage.Message.SHOP_INFO_NORMAL : LocalizedMessage.Message.SHOP_INFO_ADMIN);
        String stock = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_STOCK, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(amount)));

        boolean potionExtended = false;

        Map<Enchantment, Integer> enchantmentMap;

        if (Utils.getMajorVersion() >= 9) {
            if (type == Material.TIPPED_ARROW || type == Material.LINGERING_POTION || type == Material.SPLASH_POTION) {
                potionEffectString = LanguageUtils.getPotionEffectName(shop.getProduct());
                PotionMeta potionMeta = (PotionMeta) shop.getProduct().getItemMeta();
                potionExtended = potionMeta.getBasePotionData().isExtended();

                if (potionEffectString == null)
                    potionEffectString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_NONE);
            }
        }

        if (type == Material.POTION) {
            potionEffectString = LanguageUtils.getPotionEffectName(shop.getProduct());
            if (Utils.getMajorVersion() < 9) {
                Potion potion = Potion.fromItemStack(shop.getProduct());
                potionExtended = potion.hasExtendedDuration();
            } else {
                PotionMeta potionMeta = (PotionMeta) shop.getProduct().getItemMeta();
                potionExtended = potionMeta.getBasePotionData().isExtended();
            }

            if (potionEffectString == null)
                potionEffectString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_NONE);
        }


        if (shop.getProduct().getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) shop.getProduct().getItemMeta();
            enchantmentMap = esm.getStoredEnchants();
        } else {
            enchantmentMap = shop.getProduct().getEnchantments();
        }

        Enchantment[] enchantments = enchantmentMap.keySet().toArray(new Enchantment[enchantmentMap.size()]);

        for (int i = 0; i < enchantments.length; i++) {
            Enchantment enchantment = enchantments[i];

            if (i == enchantments.length - 1) {
                enchantmentString += LanguageUtils.getEnchantmentName(enchantment, enchantmentMap.get(enchantment));
            } else {
                enchantmentString += LanguageUtils.getEnchantmentName(enchantment, enchantmentMap.get(enchantment)) + ", ";
            }
        }

        executor.sendMessage(" ");
        if (shop.getShopType() != ShopType.ADMIN) executor.sendMessage(vendor);
        executor.sendMessage(product);
        if (shop.getShopType() != ShopType.ADMIN) executor.sendMessage(stock);
        if (enchantmentString.length() > 0)
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_ENCHANTMENTS, new LocalizedMessage.ReplacedRegex(Regex.ENCHANTMENT, enchantmentString)));
        if (potionEffectString.length() > 0)
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_POTION_EFFECT, new LocalizedMessage.ReplacedRegex(Regex.POTION_EFFECT, potionEffectString),
                    new LocalizedMessage.ReplacedRegex(Regex.EXTENDED, (potionExtended ? LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_EXTENDED) : ""))));
        if (musicDiscName.length() > 0)
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_MUSIC_TITLE, new LocalizedMessage.ReplacedRegex(Regex.MUSIC_TITLE, musicDiscName)));
        executor.sendMessage(price);
        executor.sendMessage(shopType);
        executor.sendMessage(" ");
    }

    /**
     * A player buys from a shop
     * @param executor Player, who executed the command and will buy the product
     * @param shop Shop, from which the player buys
     */
    private void buy(Player executor, Shop shop) {
        plugin.debug(executor.getName() + " is buying (#" + shop.getID() + ")");
        if (econ.getBalance(executor) >= shop.getBuyPrice()) {
            plugin.debug(executor.getName() + " has enough money (#" + shop.getID() + ")");

            Block b = shop.getLocation().getBlock();
            Chest c = (Chest) b.getState();

            HashMap<Integer, Integer> slotFree = new HashMap<>();
            ItemStack product = new ItemStack(shop.getProduct());
            Inventory inventory = executor.getInventory();

            for (int i = 0; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) {
                    slotFree.put(i, product.getMaxStackSize());
                } else {
                    if (item.isSimilar(product)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = product.getMaxStackSize() - amountInSlot;
                        slotFree.put(i, amountToFullStack);
                    }
                }
            }

            if (Utils.getMajorVersion() >= 9) {
                ItemStack item = inventory.getItem(40);
                if (item == null) {
                    slotFree.put(40, product.getMaxStackSize());
                } else {
                    if (item.isSimilar(product)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = product.getMaxStackSize() - amountInSlot;
                        slotFree.put(40, amountToFullStack);
                    }
                }
            }

            int freeAmount = 0;
            for (int value : slotFree.values()) {
                freeAmount += value;
            }

            if (freeAmount >= product.getAmount()) {
                plugin.debug(executor.getName() + " has enough inventory space (#" + shop.getID() + ")");

                EconomyResponse r = econ.withdrawPlayer(executor, shop.getBuyPrice());
                EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.depositPlayer(shop.getVendor(), shop.getBuyPrice()) : null;

                if (r.transactionSuccess()) {
                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY);
                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                econ.depositPlayer(executor, shop.getBuyPrice());
                                econ.withdrawPlayer(shop.getVendor(), shop.getBuyPrice());
                                plugin.debug("Buy event cancelled (#" + shop.getID() + ")");
                                return;
                            }

                            addToInventory(inventory, product);
                            removeFromInventory(c.getInventory(), product);
                            executor.updateInventory();
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_SUCCESS, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(product.getAmount())),
                                    new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(shop.getBuyPrice())),
                                    new LocalizedMessage.ReplacedRegex(Regex.VENDOR, shop.getVendor().getName())));

                            plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline()) {
                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SOMEONE_BOUGHT, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(product.getAmount())),
                                        new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(shop.getBuyPrice())),
                                        new LocalizedMessage.ReplacedRegex(Regex.PLAYER, executor.getName())));
                            }

                        } else {
                            plugin.debug("Economy transaction failed: " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r2.errorMessage)));
                        }
                    } else {
                        ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            econ.depositPlayer(executor, shop.getBuyPrice());
                            plugin.debug("Buy event cancelled (#" + shop.getID() + ")");
                            return;
                        }

                        addToInventory(inventory, product);
                        executor.updateInventory();
                        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_SUCESS_ADMIN, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(product.getAmount())),
                                new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(shop.getBuyPrice()))));

                        plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");
                    }
                } else {
                    plugin.debug("Economy transaction failed: " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r.errorMessage)));
                }
            } else {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NOT_ENOUGH_INVENTORY_SPACE));
            }
        } else {
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NOT_ENOUGH_MONEY));
        }
    }

    /**
     * A player sells to a shop
     * @param executor Player, who executed the command and will sell the product
     * @param shop Shop, to which the player sells
     */
    private void sell(Player executor, Shop shop) {
        plugin.debug(executor.getName() + " is selling (#" + shop.getID() + ")");

        if (econ.getBalance(shop.getVendor()) >= shop.getSellPrice() || shop.getShopType() == ShopType.ADMIN) {
            plugin.debug("Vendor has enough money (#" + shop.getID() + ")");

            Block block = shop.getLocation().getBlock();
            Chest chest = (Chest) block.getState();

            HashMap<Integer, Integer> slotFree = new HashMap<>();
            ItemStack product = new ItemStack(shop.getProduct());
            Inventory inventory = chest.getInventory();

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) {
                    slotFree.put(i, product.getMaxStackSize());
                } else {
                    if (item.isSimilar(product)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = product.getMaxStackSize() - amountInSlot;
                        slotFree.put(i, amountToFullStack);
                    }
                }
            }

            int freeAmount = 0;
            for (int value : slotFree.values()) {
                freeAmount += value;
            }

            if (freeAmount >= product.getAmount()) {
                plugin.debug("Chest has enough inventory space (#" + shop.getID() + ")");

                EconomyResponse r = econ.depositPlayer(executor, shop.getSellPrice());
                EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.withdrawPlayer(shop.getVendor(), shop.getSellPrice()) : null;

                if (r.transactionSuccess()) {
                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL);
                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                econ.withdrawPlayer(executor, shop.getBuyPrice());
                                econ.depositPlayer(shop.getVendor(), shop.getBuyPrice());
                                plugin.debug("Sell event cancelled (#" + shop.getID() + ")");
                                return;
                            }

                            addToInventory(inventory, product);
                            removeFromInventory(executor.getInventory(), product);
                            executor.updateInventory();
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELL_SUCESS, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(product.getAmount())),
                                    new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(shop.getSellPrice())),
                                    new LocalizedMessage.ReplacedRegex(Regex.VENDOR, shop.getVendor().getName())));

                            plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline()) {
                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SOMEONE_SOLD, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(product.getAmount())),
                                        new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(shop.getSellPrice())),
                                        new LocalizedMessage.ReplacedRegex(Regex.PLAYER, executor.getName())));
                            }

                        } else {
                            plugin.debug("Economy transaction failed: " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r2.errorMessage)));
                        }

                    } else {
                        ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            econ.withdrawPlayer(executor, shop.getBuyPrice());
                            plugin.debug("Sell event cancelled (#" + shop.getID() + ")");
                            return;
                        }

                        removeFromInventory(executor.getInventory(), product);
                        executor.updateInventory();
                        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELL_SUCESS_ADMIN, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(product.getAmount())),
                                new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(shop.getSellPrice()))));

                        plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");
                    }

                } else {
                    plugin.debug("Economy transaction failed: " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r.errorMessage)));
                }

            } else {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
            }

        } else {
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.VENDOR_NOT_ENOUGH_MONEY));
        }
    }

    /**
     * Adds items to an inventory
     * @param inventory The inventory, to which the items will be added
     * @param itemStack Items to add
     * @return Whether all items were added to the inventory
     */
    private boolean addToInventory(Inventory inventory, ItemStack itemStack) {
        plugin.debug("Adding items to inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        int amount = itemStack.getAmount();
        int added = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getMajorVersion() >= 9) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (added < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null) {
                    if (item.isSimilar(itemStack)) {
                        if (item.getAmount() != item.getMaxStackSize()) {
                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(item.getAmount() + 1);
                            inventory.setItem(slot, newItemStack);
                            added++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    ItemStack newItemStack = new ItemStack(itemStack);
                    newItemStack.setAmount(1);
                    inventory.setItem(slot, newItemStack);
                    added++;
                }
            }
        }

        return (added == amount);
    }

    /**
     * Removes items to from an inventory
     * @param inventory The inventory, from which the items will be removed
     * @param itemStack Items to remove
     * @return Whether all items were removed from the inventory
     */
    private boolean removeFromInventory(Inventory inventory, ItemStack itemStack) {
        plugin.debug("Removing items from inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        int amount = itemStack.getAmount();
        int removed = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getMajorVersion() >= 9) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (removed < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null) {
                    if (item.isSimilar(itemStack)) {
                        if (item.getAmount() > 0) {
                            int newAmount = item.getAmount() - 1;

                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(newAmount);

                            if (newAmount == 0)
                                inventory.setItem(slot, null);
                            else
                                inventory.setItem(slot, newItemStack);

                            removed++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    continue slotLoop;
                }

            }
        }

        return (removed == amount);
    }

}