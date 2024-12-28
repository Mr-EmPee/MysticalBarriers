package core.items;

import io.github.empee.itembuilder.StackBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public abstract class PluginItem {

  private final NamespacedKey id;
  protected final StackBuilder template;

  protected PluginItem(ItemStack item, NamespacedKey id) {
    this.template = new StackBuilder(item);
    this.id = id;

    markAsCustom();
    setup();
  }

  protected abstract void setup();

  private void markAsCustom() {
    template.withPersistentTag(id, PersistentDataType.INTEGER,1);
  }

  protected boolean isItem(ItemStack item) {
    return getVersion(item) != null;
  }

  @Nullable
  protected Integer getVersion(ItemStack item) {
    ItemMeta meta = item.getItemMeta();

    if (meta == null) {
      return null;
    }

    return meta.getPersistentDataContainer().get(id, PersistentDataType.INTEGER);
  }

}
