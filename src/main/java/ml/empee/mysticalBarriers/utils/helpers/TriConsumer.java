package ml.empee.mysticalBarriers.utils.helpers;

@FunctionalInterface
public interface TriConsumer<T, J, K> {

  void accept(T o1, J o2, K o3);

}
