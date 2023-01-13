package ml.empee.mysticalBarriers.utils.helpers;

import ml.empee.itemBuilder.ItemBuilder;
import ml.empee.itemBuilder.utils.ItemNbt;
import org.bukkit.inventory.ItemStack;

public class PluginItem {

  private final String name;
  private final ItemBuilder item;
  private final String version;

  public static PluginItem of(String name, String version, ItemBuilder item) {
    return new PluginItem(name, version, item);
  }

  private PluginItem(String name, String version, ItemBuilder item) {
    this.name = name;
    this.item = item;
    this.version = version;

    item.setNbt(name, version);
  }

  public ItemStack build() {
    return item.build().clone();
  }

  public boolean isPluginItem(ItemStack item, boolean ignoreVersion) {
    String itemVersion = ItemNbt.getString(item, name);
    if(itemVersion == null) {
      return false;
    } else {
      return ignoreVersion || itemVersion.equals(version);
    }
  }

  public boolean isPluginItem(ItemStack item) {
    return isPluginItem(item, false);
  }

}
