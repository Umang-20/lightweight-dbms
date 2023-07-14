package queryHandling.exception;

public class QueryHandlingException extends Exception {

   private String message;

  public QueryHandlingException(String message) {
    super(message);
    this.message = message;
  }

  public String getErrorMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "QueryHandlingException{" +
        "message='" + message + '\'' +
        '}';
  }
}