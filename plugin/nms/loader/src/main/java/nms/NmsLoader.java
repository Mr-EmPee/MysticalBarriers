package nms;

import nms.api.NmsAPI;
import nms.v1_19.Version1_19;

public class NmsLoader {

  private static NmsAPI versionAPI;

  public static NmsAPI getNmsAPI() {
    if (versionAPI == null) {
      versionAPI = buildNmsAPI();
    }

    return versionAPI;
  }


  private static NmsAPI buildNmsAPI() {
    return new Version1_19();
  }
}
