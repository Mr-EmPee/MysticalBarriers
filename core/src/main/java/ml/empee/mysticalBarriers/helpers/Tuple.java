package ml.empee.mysticalBarriers.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public final class Tuple<T, K> {

  private T first;
  private K second;

}
