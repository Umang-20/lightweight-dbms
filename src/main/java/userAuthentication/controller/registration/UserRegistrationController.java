package userAuthentication.controller.registration;

import userAuthentication.constant.Constant;
import userAuthentication.controller.login.UserLoginController;
import userAuthentication.exception.UserAuthenticationException;
import userAuthentication.model.User;
import userAuthentication.util.UserUtils;

import java.io.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;

public class UserRegistrationController {

  public boolean isRegistered = false;

  private void validateUser(User user) throws UserAuthenticationException {
    boolean isEmailValid = UserUtils.isEmailValid(user.getEmail());
    boolean isPasswordValid = UserUtils.isPasswordValid(user.getPassword());
    boolean isSecurityAnswerValid = UserUtils.isSecurityAnswerValid(user.getSecurityQuestionAnswer());

    if (!isEmailValid) {
      throw new UserAuthenticationException("Invalid email!");
    }

    if (!isPasswordValid) {
      throw new UserAuthenticationException("Invalid password!");
    }

    if (!isSecurityAnswerValid) {
      throw new UserAuthenticationException("Invalid security question answer!");
    }
  }

  private boolean isUserExists(User user) throws UserAuthenticationException {
    try (BufferedReader fileReader = new BufferedReader(new FileReader(Constant.USERS_FILE))) {
      String userDetails;
      while ((userDetails = fileReader.readLine()) != null) {
        String[] userDetailsArr = userDetails.split(" ");
        if (userDetailsArr[1].equals(user.getEmail())) {
          return true;
        }
      }
      return false;
    } catch (IOException e) {
      throw new UserAuthenticationException("Something went wrong. Please try again after sometime!");
    }
  }

  private boolean saveUser(User user) throws UserAuthenticationException {
    try (FileWriter fileWriter = new FileWriter(Constant.USERS_FILE, true)) {
      long userId = Files.lines(Paths.get(Constant.USERS_FILE)).count() + 1;
      String newUser = String.format("%d %s %s %s%n", userId, user.getEmail(), UserUtils.encryptPassword(user.getPassword()), user.getSecurityQuestionAnswer());
      fileWriter.append(newUser);
      return true;
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new UserAuthenticationException("Something went wrong. Please try again after sometime!");
    }
  }

  public boolean registerUser(User user) throws UserAuthenticationException {
    validateUser(user);

    if (isUserExists(user)) {
      throw new UserAuthenticationException("User already exists!");
    }
    return saveUser(user);
  }

  public boolean isCurrentlyRegistered() {
    return isRegistered;
  }

  public boolean isRegistered(UserLoginController userLoginController) {
    return userLoginController.isLoggedIn();
  }
}