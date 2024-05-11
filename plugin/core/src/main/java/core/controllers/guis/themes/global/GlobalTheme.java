package core.controllers.guis.themes.global;

import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.easygui.model.inventories.containers.PageContainer;
import core.configs.client.resources.MessagesConfig;
import core.controllers.guis.themes.ITheme;

import java.util.ArrayList;
import java.util.List;

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

  public abstract Item previousPage(PageContainer page);

  public abstract Item nextPage(PageContainer page);

}
