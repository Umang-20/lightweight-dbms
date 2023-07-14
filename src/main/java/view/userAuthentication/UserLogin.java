package view.userAuthentication;

import userAuthentication.controller.login.UserLoginController;
import userAuthentication.exception.UserAuthenticationException;
import userAuthentication.model.User;

import java.util.Scanner;

public class UserLogin extends UserRegistration {
  private Scanner scanner;
  private UserRegistration userRegistration;


  public UserLogin() {

  }

  public UserLogin(Scanner scanner,UserRegistration userRegistration) {
    this.scanner = scanner;
    this.userRegistration = userRegistration;
  }

  public User doLogin() {
    System.out.println("Enter email");
    String user = scanner.nextLine();

    System.out.println("Enter password");
    String password = scanner.nextLine();

    System.out.println("What is your birth year?");
    String securityAnswer = scanner.nextLine();

    try {
      UserLoginController userLoginController = new UserLoginController();
      User loggedInUser = userLoginController.loginUser(user, password, securityAnswer);
      System.out.println("Logged in as: " + loggedInUser.getEmail());
      return loggedInUser;
    } catch (UserAuthenticationException e) {
      System.out.println(e);
      return null;
    }
  }
}