package ml.empee.mysticalbarriers.model.exceptions;

public class MysticalBarrierException extends RuntimeException {

  public MysticalBarrierException(String message) {
    super(message);
  }

  public MysticalBarrierException(String message, Throwable cause) {
    super(message, cause);
  }

}
