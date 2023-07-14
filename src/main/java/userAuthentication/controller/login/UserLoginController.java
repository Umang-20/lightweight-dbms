package userAuthentication.controller.login;

import userAuthentication.constant.Constant;
import userAuthentication.controller.registration.UserRegistrationController;
import userAuthentication.exception.UserAuthenticationException;
import userAuthentication.model.User;
import userAuthentication.util.UserUtils;

import java.io.*;
import java.security.NoSuchAlgorithmException;

public class UserLoginController extends UserRegistrationController {

    public boolean isLoggedIn = false;
    private void verifyUserCredentials(String username, String password, String securityAnswer) throws UserAuthenticationException {
        boolean isEmailValid = UserUtils.isEmailValid(username);
        boolean isPasswordValid = UserUtils.isPasswordValid(password);
        boolean isSecurityAnswerValid = UserUtils.isSecurityAnswerValid(securityAnswer);
        if (!isEmailValid || !isPasswordValid || !isSecurityAnswerValid) {
            throw new UserAuthenticationException("Invalid credentials");
        }
    }

    private boolean checkIfUserExists(String username, String password, String securityAnswer, String[] userDetailsArr) throws NoSuchAlgorithmException {
        boolean userExists;
        boolean isSameUsername = userDetailsArr[1].equals(username);
        if (isSameUsername) {
            boolean isSamePassword = UserUtils.validatePassword(password, userDetailsArr[2]);
            if (isSamePassword) {
                userExists = userDetailsArr[3].equals(securityAnswer);
            } else {
                userExists = false;
            }
        } else {
            userExists = false;
        }
        return userExists;
    }

    public User getUserDetails(String[] userDetailsArr) {
        long id = Long.parseLong(userDetailsArr[0]);
        String username = userDetailsArr[1];
        String password = userDetailsArr[2];
        String securityAnswer = userDetailsArr[3];
        return new User(id, username, password, securityAnswer);
    }

    private User authenticateUser(String username, String password, String securityAnswer) throws UserAuthenticationException {
        try (BufferedReader usersFileReader = new BufferedReader(new FileReader(Constant.USERS_FILE))) {
            String userDetails;
            while ((userDetails = usersFileReader.readLine()) != null) {
                String[] userDetailsArr = userDetails.split(" ");
                boolean userExists = checkIfUserExists(username, password, securityAnswer, userDetailsArr);
                if (userExists) {
                    return getUserDetails(userDetailsArr);
                }
            }
            throw new UserAuthenticationException("Invalid credentials");
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new UserAuthenticationException("Something went wrong. Please try again later");
        }
    }

    public User loginUser(String username, String password, String securityAnswer) throws UserAuthenticationException {
        verifyUserCredentials(username, password, securityAnswer);
        User authenticatedUser = authenticateUser(username, password, securityAnswer);
        return authenticatedUser;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public boolean isCurrentlyLoggedIn(UserRegistrationController userRegistrationController) {
        return userRegistrationController.isCurrentlyRegistered();
    }
}