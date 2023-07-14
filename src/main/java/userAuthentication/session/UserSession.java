package userAuthentication.session;

import userAuthentication.model.User;

public final class UserSession {

   private User user;

  public UserSession() {}

  public static UserSession getInstance() {
    return Holder.INSTANCE;
  }

  private static class Holder {
    private static final UserSession INSTANCE = new UserSession();
  }

  public void startSession(User user) {
    this.user = user;
  }

  public void endSession() {
    this.user = null;
  }

  public User getCurrentUser() {
    return user;
  }
}