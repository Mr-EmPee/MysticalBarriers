package ml.empee.mysticalBarriers.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public final class Triple<T, K, J> {

  private T firstValue;
  private K secondValue;
  private J thirdValue;

}
