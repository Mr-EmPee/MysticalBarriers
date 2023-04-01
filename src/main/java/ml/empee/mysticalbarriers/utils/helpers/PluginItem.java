package ml.empee.mysticalbarriers.utils.helpers;


import ml.empee.itembuilder.ItemBuilder;
import ml.empee.itembuilder.utils.ItemNbt;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

/** Allow you to easily create a custom item **/

public class PluginItem {

  protected final JavaPlugin plugin;
  protected final String name;
  protected final ItemBuilder item;
  protected final String version;

  public PluginItem(JavaPlugin plugin, String name, String version, ItemBuilder item) {
    this.plugin = plugin;
    this.name = name;
    this.item = item;
    this.version = version;

    item.plugin(plugin);
    item.setNbt(name, version);
  }

  public PluginItem(JavaPlugin plugin, String name, String version, Material item) {
    this(plugin, name, version, ItemBuilder.from(item));
  }

  public ItemStack build() {
    return item.build().clone();
  }

  /**
   * Check if an itemStack is the custom item
   */
  public boolean isPluginItem(@Nullable ItemStack item, boolean ignoreVersion) {
    if (item == null || !item.hasItemMeta()) {
      return false;
    }

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
  public boolean isPluginItem(@Nullable ItemStack item) {
    return isPluginItem(item, false);
  }

}
