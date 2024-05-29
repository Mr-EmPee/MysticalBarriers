package core.controllers.guis.themes.global;

import com.cryptomorin.xseries.XMaterial;
import core.configs.client.resources.MessagesConfig;
import io.github.empee.easygui.guis.inventories.InventoryGUI;
import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.easygui.model.inventories.containers.ScrollableContainer;
import io.github.empee.lightwire.annotations.LightWired;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

@LightWired
public class DefaultGlobalTheme extends GlobalTheme {

  public DefaultGlobalTheme(MessagesConfig messagesConfig) {
    super(messagesConfig);
  }

  @Override
  public Item previousPage(ScrollableContainer container) {
    var item = parse(XMaterial.ARROW, "common.buttons.previous-page");

    return Item.of(item)
        .priority(() -> container.hasPrevious() ? 1 : -1)
        .clickHandler(c -> container.previous());
  }

  @Override
  public Item nextPage(ScrollableContainer container) {
    var item = parse(XMaterial.ARROW, "common.buttons.next-page");

    return Item.of(item)
        .priority(() -> container.hasNext() ? 1 : -1)
        .clickHandler(c -> container.next());
  }

  @Override
  public Item previousGUI(Consumer<InventoryClickEvent> action) {
    var item = parse(XMaterial.ARROW, "common.buttons.previous-gui");

    return Item.of(item).clickHandler(action);
  }

  @Override
  public Type getTheme() {
    return Type.DEFAULT;
  }
}
