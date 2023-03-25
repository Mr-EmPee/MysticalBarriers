package ml.empee.mysticalbarriers.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BarrierDTO {
  private int version;
  private String id;
  private String firstCorner;
  private String secondCorner;
  private String material;
  private int activationRange;
  private String blockData;
}
