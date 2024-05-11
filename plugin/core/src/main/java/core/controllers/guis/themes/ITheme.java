package core.controllers.guis.themes;

import com.cryptomorin.xseries.XMaterial;
import io.github.empee.itembuilder.StackBuilder;
import lombok.RequiredArgsConstructor;
import core.configs.client.resources.MessagesConfig;
import utils.TextUtils;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public abstract class ITheme {

  protected final MessagesConfig messages;

  public enum Type {
    DEFAULT
  }

  public abstract Type getTheme();

  protected StackBuilder parse(XMaterial item, String path) {
    return parse(item, path, Collections.emptyMap());
  }

  protected StackBuilder parse(XMaterial item, String path, Map<String, Object> placeholders) {
    var title = messages.get(String.join(".", path, "title"), placeholders);
    var lore = messages.get(String.join(".", path, "lore"), placeholders);
    var type = messages.get(String.join(".", path, "type"));

    if (type != null) {
      item = XMaterial.valueOf(type);
    }

    var builder = new StackBuilder(item.parseItem());

    if (title != null) {
      builder.withName(TextUtils.colorize(title));
    }

    if (lore != null) {
      builder.withLore(TextUtils.colorize(lore).split("\n"));
    }

    return builder;
  }

}
