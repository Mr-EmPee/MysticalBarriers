package ml.empee.mysticalbarriers.utils.reflection;

import java.lang.reflect.Method;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.LingeringPotionSplashEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils {

  private static Method getThrownPotionMethod;

  @SneakyThrows
  public static ThrownPotion getThrownPotion(LingeringPotionSplashEvent event) {
    if (ServerVersion.isLowerThan(1, 14)) {
      if (getThrownPotionMethod == null) {
        getThrownPotionMethod = LingeringPotionSplashEvent.class.getMethod("getEntity");
      }

      return (ThrownPotion) getThrownPotionMethod.invoke(event);
    }

    return event.getEntity();
  }

}
