package ml.empee.mysticalBarriers.utils.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public final class Triple<T, K, J> {

  private T first;
  private K second;
  private J third;

}
