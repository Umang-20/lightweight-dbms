package view.executeQuery;

import queryHandling.exception.QueryHandlingException;
import queryHandling.controller.QueryHandlingController;
import queryHandling.model.QueryHandlingResponse;
import userAuthentication.session.UserSession;

import java.io.IOException;
import java.util.Scanner;


public class ExecuteQuery {
    private Scanner scanner;
    private UserSession userSession;

    public ExecuteQuery(Scanner scanner, UserSession userSession) {
        this.scanner = scanner;
        this.userSession = userSession;
    }

    public void executeSQLQuery() {
        QueryHandlingController queryHandlingController = new QueryHandlingController(userSession);
        while (true) {
            System.out.println("1. Run SQL Query");
            System.out.println("2. Log Out");
            System.out.println("Select an option:");

            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    System.out.println("Enter SQL Query:");
                    String sqlQuery = scanner.nextLine();
                    try {
                        QueryHandlingResponse queryHandlingResponse = queryHandlingController.handleQuery(sqlQuery);
                        if (queryHandlingResponse.isSuccess()) {
                            System.out.println(queryHandlingResponse.getResult());
                        } else {
                            System.out.println("Query failed");
                        }
                    } catch (QueryHandlingException e) {
                        System.out.println("Error executing query: " + e.getErrorMessage());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "2":
                    userSession.endSession();
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid option selected.");
                    break;
            }
        }
    }
}
