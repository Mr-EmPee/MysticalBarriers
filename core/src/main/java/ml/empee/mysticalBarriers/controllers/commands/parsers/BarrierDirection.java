package ml.empee.mysticalBarriers.controllers.commands.parsers;

public enum BarrierDirection {

  EAST_WEST("[east=true,west=true,north=false,south=false]"),
  NORTH_SOUTH("[north=true,south=true,east=false,west=false]"),
  ALL("[east=true,west=true,north=true,south=true]"),
  NONE("[east=false,west=false,north=false,south=false]");

  private final String data;

  BarrierDirection(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }

}
