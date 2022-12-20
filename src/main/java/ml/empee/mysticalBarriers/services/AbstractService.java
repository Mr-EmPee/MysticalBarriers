package ml.empee.mysticalBarriers.services;

public abstract class AbstractService {

  public final void stop() {
    onDisable();
  }

  protected void onDisable() {}

}
