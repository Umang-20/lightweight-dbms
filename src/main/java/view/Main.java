package view;

import userAuthentication.model.User;
import userAuthentication.session.UserSession;
import view.executeQuery.ExecuteQuery;
import view.userAuthentication.UserLogin;
import view.userAuthentication.UserRegistration;

import java.util.*;

public class Main {
    private UserRegistration userRegistration;
    private UserLogin userLogin;

    private void userRegistration(Scanner scanner) {
        UserRegistration userRegistration = new UserRegistration(scanner,userLogin);
        userRegistration.doUserRegistration();
    }
  private User userLogin(Scanner scanner) {
        UserLogin userLogin = new UserLogin(scanner,userRegistration);
        return userLogin.doLogin();
  }


    public static void main(String[] args) {
        Main entry = new Main();
        Scanner scanner = new Scanner(System.in);
        UserSession userSession = UserSession.getInstance();
          while (true) {
              System.out.println("1. SignUp User");
              System.out.println("2. Login User");
              System.out.println("3. Exit");
              System.out.println("Select an option:");
              String input = scanner.nextLine();

          switch (input) {
            case "1":
                entry.userRegistration(scanner);
                break;
            case "2":
                User user = entry.userLogin(scanner);
              if (user != null) {
                userSession.startSession(user);
                ExecuteQuery executeSQLQueryView = new ExecuteQuery(scanner, userSession);
                executeSQLQueryView.executeSQLQuery();
              }
              break;
            case "3":
              userSession.endSession();
              System.exit(0);
            default:
              break;
          }
        }
    }
}