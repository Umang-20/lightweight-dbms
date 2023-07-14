package view.userAuthentication;

import userAuthentication.controller.registration.UserRegistrationController;
import userAuthentication.exception.UserAuthenticationException;
import userAuthentication.model.User;

import java.util.*;

public class UserRegistration {
  private Scanner scanner;
  private UserLogin userLogin;

  public UserRegistration() {

  }

public UserRegistration(Scanner scanner,UserLogin userLogin) {
  this.scanner = scanner;
  this.userLogin = userLogin;
}

public void doUserRegistration() {
  System.out.println("Enter email (Must be a valid email)");
  String email = scanner.nextLine();

  System.out.println("Enter password (Password must contain a lower case character, an uppercase character, a number and a special character with length more than 8)");
  String password = scanner.nextLine();

  System.out.println("What is your birth year?");
  String securityAnswer = scanner.nextLine();

  User user = new User(
      email,
      password,
      securityAnswer
  );

  try {
    UserRegistrationController userRegistrationController = new UserRegistrationController();
    boolean isRegistrationSuccessful = userRegistrationController.registerUser(user);
    if (isRegistrationSuccessful) {
      System.out.println("User " + email + " registered successfully!");
    } else {
      System.out.println("User " + email + " registration failed!");
    }
  } catch (UserAuthenticationException e) {
    System.out.println(e.toString());
  }
}
}