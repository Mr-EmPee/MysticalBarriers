package core.controllers.guis.themes.global;

import com.cryptomorin.xseries.XMaterial;
import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.easygui.model.inventories.containers.PageContainer;
import io.github.empee.lightwire.annotations.LightWired;
import core.configs.client.resources.MessagesConfig;

@LightWired
public class DefaultGlobalTheme extends GlobalTheme {

  public DefaultGlobalTheme(MessagesConfig messagesConfig) {
    super(messagesConfig);
  }

  @Override
  public Item previousPage(PageContainer page) {
    var item = parse(XMaterial.ARROW, "common.buttons.previous-page");

    return Item.of(item)
        .visibility(page::hasPrevious)
        .clickHandler(c -> {
          page.previous();
          page.update();
        });
  }

  @Override
  public Item nextPage(PageContainer page) {
    var item = parse(XMaterial.ARROW, "common.buttons.next-page");

    return Item.of(item)
        .visibility(page::hasNext)
        .clickHandler(c -> {
          page.previous();
          page.update();
        });
  }

  @Override
  public Type getTheme() {
    return Type.DEFAULT;
  }
}
