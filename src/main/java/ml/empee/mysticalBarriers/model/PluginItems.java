package ml.empee.mysticalBarriers.model;

import lombok.RequiredArgsConstructor;
import ml.empee.itemBuilder.ItemBuilder;
import ml.empee.mysticalBarriers.utils.helpers.PluginItem;
import org.bukkit.Material;

@RequiredArgsConstructor
public enum PluginItems {
  WAND(
      PluginItem.of(
          "barrier_wand", "1",
          ItemBuilder.from(Material.STICK)
              .name("&eBarrier Wand")
              .lore("\n\t&7Right click to set a barrier corner\t\n")
      )
  );

  private final PluginItem item;

  public PluginItem getItem() {
    return item;
  }
}
