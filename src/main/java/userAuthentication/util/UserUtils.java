package userAuthentication.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class UserUtils {

  public static boolean isEmailValid(String email) {
    boolean isEmailValid;
    if (email == null || email.isEmpty()) {
      isEmailValid = false;
    } else {
      isEmailValid = Pattern.matches(
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$",
          email);
    }
    return isEmailValid;
  }

  public static boolean isPasswordValid(String password) {
    boolean isPasswordValid;
    if (password == null || password.isEmpty()) {
      isPasswordValid = false;
    } else {
      isPasswordValid = Pattern.matches(
          "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$",
          password);
    }
    return isPasswordValid;
  }

  public static boolean isSecurityAnswerValid(String securityAnswer) {
    boolean isSecurityAnswerValid;
    if (securityAnswer == null || securityAnswer.isEmpty()) {
      isSecurityAnswerValid = false;
    } else {
      isSecurityAnswerValid = Pattern.matches(
          "[A-Za-z\\d]+",
          securityAnswer);
    }
    return isSecurityAnswerValid;
  }

   public static String encryptPassword(String password) throws NoSuchAlgorithmException {
    if (password == null) {
      return null;
    }
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    return String.format("%064x", new BigInteger(1, messageDigest.digest(password.getBytes(StandardCharsets.UTF_8))));
  }

  public static boolean validatePassword(String password, String targetHash) throws NoSuchAlgorithmException {
    String sourceHash = encryptPassword(password);
    if (sourceHash == null || targetHash == null) {
      return false;
    }
    return sourceHash.equals(targetHash);
  }
}
