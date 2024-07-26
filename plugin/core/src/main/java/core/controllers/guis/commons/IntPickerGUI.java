package core.controllers.guis.commons;

import io.github.empee.easygui.guis.inventories.ChestGUI;
import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.itembuilder.StackBuilder;
import lombok.Builder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import utils.TextUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Builder
public class IntPickerGUI {

  private final ChestGUI gui = ChestGUI.of(3);

  private final AtomicInteger value;
  private final Consumer<Integer> action;
  private Item back;

  public void open(Player player) {
    gui.closeHandler(this::onClose);

    if (back != null) {
      gui.inserts(back);
    }

    gui.inserts(remove().slot(1, 2));
    gui.inserts(value().slot(1, 4));
    gui.inserts(add().slot(1, 6));

    gui.open(player);
  }

  private void onClose(InventoryCloseEvent event) {
    action.accept(value.get());
  }

  private Item add() {
    var item = new StackBuilder(Material.GREEN_DYE)
        .withName(TextUtils.colorize("&aAdd"))
        .toItemStack();

    return Item.of(item).clickHandler(c -> {
      value.incrementAndGet();
      gui.update();
    });
  }

  private Item remove() {
    var item = new StackBuilder(Material.RED_DYE)
        .withName(TextUtils.colorize("&cRemove"))
        .toItemStack();

    return Item.of(item).clickHandler(c -> {
      value.decrementAndGet();
      gui.update();
    });
  }

  private Item value() {
    return Item.of(() ->
      new StackBuilder(Material.TARGET)
          .withName(TextUtils.colorize("&e" + value.get()))
          .toItemStack()
    );
  }


}
