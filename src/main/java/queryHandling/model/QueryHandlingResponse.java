package queryHandling.model;

public class QueryHandlingResponse {

  private boolean success;
  private String result;

  public QueryHandlingResponse(boolean success, String result) {
    this.success = success;
    this.result = result;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getResult() {
    return result;
  }

}