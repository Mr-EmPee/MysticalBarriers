package core.controllers.guis.themes.global;

import core.configs.client.resources.MessagesConfig;
import core.controllers.guis.themes.ITheme;
import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.easygui.model.inventories.containers.ScrollableContainer;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class GlobalTheme extends ITheme {

  private final static List<GlobalTheme> themes = new ArrayList<>();
  public static GlobalTheme get(Type theme) {
    return themes.stream()
        .filter(t -> theme == t.getTheme())
        .findFirst().orElseThrow();
  }

  public GlobalTheme(MessagesConfig messagesConfig) {
    super(messagesConfig);

    themes.add(this);
  }

  public abstract Item previousGUI(Consumer<InventoryClickEvent> action);

  public abstract Item previousPage(ScrollableContainer container);

  public abstract Item nextPage(ScrollableContainer container);

}
