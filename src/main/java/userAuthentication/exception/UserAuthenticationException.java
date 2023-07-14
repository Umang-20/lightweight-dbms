package userAuthentication.exception;

public class UserAuthenticationException extends Exception {

  private String errorMessage;

  public UserAuthenticationException(String errorMessage) {
    super(errorMessage);
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}