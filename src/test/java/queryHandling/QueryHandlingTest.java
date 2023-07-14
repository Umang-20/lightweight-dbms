package queryHandling;

import static org.junit.Assert.*;
import static queryHandling.constant.Constant.DATABASE_FILE;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import queryHandling.controller.QueryHandlingController;
import queryHandling.exception.QueryHandlingException;
import queryHandling.model.QueryHandlingResponse;
import userAuthentication.controller.login.UserLoginController;
import userAuthentication.model.User;
import userAuthentication.session.UserSession;

public class QueryHandlingTest {

   @Test
   public void testProcessCreateAndDropDatabaseQuery() throws Exception {
    String databasePath = "./src/main/java/database/testdatabase";
    // Set up a temporary database and table with some initial data
    File database = new File(databasePath);

    if(database.isDirectory()){
     database.delete();
    }

    // Initialize User Session
    UserSession userSession = new UserSession();
    UserLoginController userLoginController = new UserLoginController();
    String[] userDetails = {"1","Test@dal.ca","Qwerty@1","2023"};
    User user = userLoginController.getUserDetails(userDetails);
    userSession.startSession(user);

    QueryHandlingController queryHandler = new QueryHandlingController(userSession);

    // Test updating a single record
    QueryHandlingResponse response = queryHandler.handleQuery("CREATE DATABASE testdatabase");
    assertTrue(response.isSuccess());
    boolean isDatabaseExists = database.isDirectory();
    assertTrue(isDatabaseExists);

    QueryHandlingResponse response1 = queryHandler.handleQuery("DROP DATABASE testdatabase");
    assertTrue(response1.isSuccess());
    boolean isDatabase = database.isDirectory();
    assertFalse(isDatabase);
    userSession.endSession();
   }

   @Test
   public void testProcessUseDatabaseQuery() throws Exception {
    String databasePath = "./src/main/java/database/testquery";
    // Set up a temporary database and table with some initial data
    File database = new File(databasePath);

    if(database.isDirectory()){
     database.delete();
    }

    database.mkdir();

    // Initialize User Session
    UserSession userSession = new UserSession();
    UserLoginController userLoginController = new UserLoginController();
    String[] userDetails = {"2","TestQuery@dal.ca","Qwerty@1","2023"};
    User user = userLoginController.getUserDetails(userDetails);
    userSession.startSession(user);

    QueryHandlingController queryHandler = new QueryHandlingController(userSession);

    // Test updating a single record
    QueryHandlingResponse response = queryHandler.handleQuery("USE testquery");
    assertTrue(response.isSuccess());
    assertEquals(queryHandler.useDBName,"testquery");

    database.delete();
    userSession.endSession();
   }

   @Test
   public void testProcessUpdateDataQuery() throws Exception {
    String databasePath = "./src/main/java/database/testquery";
    // Set up a temporary database and table with some initial data
    File database = new File(databasePath);
    String tableName = "testTable";


    if(database.isDirectory()){
     File[] allTables = database.listFiles();
     for (File table : allTables) {
         if (table.getName().equalsIgnoreCase(tableName)) {
             table.delete();
             break;
         }
     }
     database.delete();
    }

    database.mkdir();

    File tableFile = new File(database, tableName);

    // Initialize User Session
    UserSession userSession = new UserSession();
    UserLoginController userLoginController = new UserLoginController();
    String[] userDetails = {"2","TestQuery@dal.ca","Qwerty@1","2023"};
    User user = userLoginController.getUserDetails(userDetails);
    userSession.startSession(user);

    QueryHandlingController queryHandler = new QueryHandlingController(userSession);

   queryHandler.handleQuery("USE testquery");


    // Test updating a single record
   QueryHandlingResponse response = queryHandler.handleQuery("CREATE TABLE testTable (no INT,name VARCHAR,number VARCHAR)");
    assertTrue(response.isSuccess());
    assertEquals("Table testtable created successfully!", response.getResult());
    List<String> updatedLines = Files.readAllLines(tableFile.toPath());
    assertEquals("no int|name varchar||number varchar", updatedLines.get(0));
    assertEquals("int|varchar||varchar", updatedLines.get(1));

    tableFile.delete();
    database.delete();
    userSession.endSession();
   }
}