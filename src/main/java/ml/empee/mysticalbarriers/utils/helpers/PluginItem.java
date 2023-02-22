package ml.empee.mysticalbarriers.utils.helpers;


import ml.empee.itembuilder.ItemBuilder;
import ml.empee.itembuilder.utils.ItemNbt;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Allow you to easily create a custom item
 **/

public class PluginItem {

  protected final JavaPlugin plugin;
  protected final String name;
  protected final ItemBuilder item;
  protected final String version;

  protected PluginItem(JavaPlugin plugin, String name, String version, ItemBuilder item) {
    this.plugin = plugin;
    this.name = name;
    this.item = item;
    this.version = version;

    item.plugin(plugin);
    item.setNbt(name, version);
  }

  protected PluginItem(JavaPlugin plugin, String name, String version, Material item) {
    this(plugin, name, version, ItemBuilder.from(item));
  }

  public static PluginItem of(JavaPlugin plugin, String name, String version, ItemBuilder item) {
    return new PluginItem(plugin, name, version, item);
  }

  public static PluginItem of(JavaPlugin plugin, String name, String version, Material item) {
    return new PluginItem(plugin, name, version, item);
  }

  public ItemStack build() {
    return item.build().clone();
  }

  /**
   * Check if an itemStack is the custom item
   */
  public boolean isPluginItem(ItemStack item, boolean ignoreVersion) {
    String itemVersion = ItemNbt.getString(plugin, item, name);
    if (itemVersion == null) {
      return false;
    } else {
      return ignoreVersion || itemVersion.equals(version);
    }
  }

  /**
   * Check if an itemStack is the custom item
   */
  public boolean isPluginItem(ItemStack item) {
    return isPluginItem(item, false);
  }

}
