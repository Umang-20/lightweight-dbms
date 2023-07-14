package queryHandling.controller;

import queryHandling.exception.QueryHandlingException;
import queryHandling.model.QueryHandlingResponse;
import userAuthentication.session.UserSession;

import static queryHandling.constant.Constant.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class QueryHandlingController {
    private UserSession userSession;
    public String useDBName = null;

    public QueryHandlingController(UserSession userSession) {
        this.userSession = userSession;
    }
    private boolean isDatabaseSelected() {
        return (this.useDBName != null) && (!this.useDBName.isEmpty());
    }
    private static String encodeString(String[] columnList) {
        StringBuilder sb = new StringBuilder();
        for (int i =0 ; i<columnList.length;i++) {
            sb.append(columnList[i]);
            if(i!= columnList.length -1){
                 for (int j =0 ; j<=i; j++) {
                    sb.append("|");
                }
            }
        }
        return sb.toString();
    }

    private static String[] decodeString(String text) {
        String[] newText = text.split("\\|");
        String[] result = Arrays.stream(newText)
                                .filter(s -> !s.isEmpty())
                                .toArray(String[]::new);
        return result;
    }

    private QueryHandlingResponse processCreateDatabaseQuery(String query) throws QueryHandlingException {
    String queryProcessed = query.trim();
    String[] temp = queryProcessed.split("\\s+");
    String dbName = temp[2];
    boolean userDBExist = false;
    try (BufferedReader reader = new BufferedReader(new FileReader(DATABASE_FILE))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] dbRecord = line.split(" ");
            String email = dbRecord[0].trim();
            if (email.equalsIgnoreCase(userSession.getCurrentUser().getEmail())) {
                System.out.println("A database for this user already exists.");
                userDBExist = true;
                break;
            }
        }
        if(!userDBExist) {
            try (FileWriter fileWriter = new FileWriter(DATABASE_FILE, true)) {
                String newEntry = String.format("%s %s%n", userSession.getCurrentUser().getEmail(), dbName );
                fileWriter.append(newEntry);
            } catch (IOException e) {
                throw new QueryHandlingException("Error writing to file: " + e.getMessage());
            }
            String databasePath = DATABASE_PATH + dbName;
            File database = new File(databasePath);
            boolean isDatabaseExists = database.isDirectory();
            if (isDatabaseExists) {
                throw new QueryHandlingException("Error: Database " + dbName + " already exists!");
            }
            boolean isDirectoryCreated = database.mkdir();
            if (isDirectoryCreated) {
                return new QueryHandlingResponse(true, "Database " + dbName + " created successfully!");
            } else {
                throw new QueryHandlingException("Error: Failed to create database " + dbName + "!");
            }
        }
    } catch (IOException e) {
        throw new QueryHandlingException("Error reading file: " + e.getMessage());
    }
    return new QueryHandlingResponse(true,"");
}

    private QueryHandlingResponse processDropDatabaseQuery(String query) throws QueryHandlingException, IOException {
    String[] temp = query.trim().split("\\s+");
    String dbName = temp[2];
    String databasePath = DATABASE_PATH + dbName;
    File database = new File(databasePath);

    if (!database.isDirectory()) {
        throw new QueryHandlingException("Error: Database " + dbName + " does not exist!");
    }

    boolean isValidUser = false;
    try (BufferedReader reader = new BufferedReader(new FileReader(DATABASE_FILE))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] dbRecord = line.split(" ");
            String email = dbRecord[0].trim();
            String databaseName = dbRecord[1].trim();

            if (email.equalsIgnoreCase(userSession.getCurrentUser().getEmail()) && databaseName.equalsIgnoreCase(dbName)) {
                isValidUser = true;
                break;
            }
        }
        if (!isValidUser) {
            throw new QueryHandlingException("Error: You don't have permission to delete " + dbName + ".");
        }
    }

    File[] dbTables = database.listFiles();
    if (dbTables != null) {
        for (File table : dbTables) {
            table.delete();
        }
    }

    boolean isDatabaseDeleted = database.delete();
    if (isDatabaseDeleted) {
        List<String> lines = Files.readAllLines(Paths.get(DATABASE_FILE), StandardCharsets.UTF_8);
        lines.removeIf(line -> line.contains(userSession.getCurrentUser().getEmail() + " " + dbName));

        Files.write(Paths.get(DATABASE_FILE), lines, StandardCharsets.UTF_8);
        return new QueryHandlingResponse(true, "Database " + dbName + " dropped successfully!");
    } else {
        throw new QueryHandlingException("Error: Failed to delete database " + dbName + "!");
    }
}

    private QueryHandlingResponse processUseDatabaseQuery(String query) throws QueryHandlingException {
    String queryProcessed = query.trim();
    String[] temp = queryProcessed.split("\\s+");
    String dbName = temp[1];
    String databasePath = DATABASE_PATH + dbName;
    File database = new File(databasePath);
    boolean isDatabaseExists = database.isDirectory();

    if (!isDatabaseExists) {
        throw new QueryHandlingException("Error: Database " + dbName + " does not exist!");
    }

    boolean userDBExist = false;

    try (BufferedReader reader = new BufferedReader(new FileReader(DATABASE_FILE))) {
        String line;

        while ((line = reader.readLine()) != null) {
            String[] dbRecord = line.split(" ");
            String email = dbRecord[0].trim();
            String databaseName = dbRecord[1].trim();

            if (email.equalsIgnoreCase(userSession.getCurrentUser().getEmail()) && databaseName.equalsIgnoreCase(dbName)) {
                userDBExist = true;
                break;
            }
        }

        if (!userDBExist) {
            throw new QueryHandlingException("Error: User doesn't have access to database " + dbName + "!");
        }
    } catch (IOException e) {
        throw new QueryHandlingException("Error reading file: " + e.getMessage());
    }

    this.useDBName = dbName;
    return new QueryHandlingResponse(true, "Database " + dbName + " is now in use.");
}

    private QueryHandlingResponse processCreateTableQuery(String query) throws QueryHandlingException, IOException {

    // Check if a database is selected
    if (!isDatabaseSelected()) {
        throw new QueryHandlingException("Error: No database selected!");
    }

    // Extract table name and column definitions from query
    String queryProcessed = query.trim();
    String[] temp = queryProcessed.split("\\s+");
    String tableName = temp[2];
    String columns = queryProcessed.substring(queryProcessed.indexOf("(")+1, queryProcessed.lastIndexOf(")"));
    String[] columnNames = columns.split(",");
    String[] columnTypes = new String[columnNames.length];
    for (int i = 0; i < columnNames.length; i++) {
        String[] columnDef = columnNames[i].trim().split("\\s+");
        if (columnDef.length != 2) {
            throw new QueryHandlingException("Invalid column definition: " + columnNames[i]);
        }
        columnTypes[i] = columnDef[1];
    }

    // Check if database directory exists
    String databasePath = DATABASE_PATH + this.useDBName;
    File database = new File(databasePath);
    boolean isDatabaseExists = database.isDirectory();
    if (!isDatabaseExists) {
        throw new QueryHandlingException("Error: Database " + this.useDBName + " does not exists!");
    }

    // Check if table already exists
    File allTablesPath = new File(DATABASE_PATH + this.useDBName + "/");
    File[] allTables = allTablesPath.listFiles();
    boolean isTableExists = false;
    for (File table : allTables) {
      if (table.getName().equalsIgnoreCase(tableName + ".txt")) {
        isTableExists = true;
      }
    }
    if (isTableExists) {
      throw new QueryHandlingException("Error: Table " + tableName + " already exists!");
    }

    // Create new table file and write column headers and types to file
    File tableFile = new File(DATABASE_PATH + this.useDBName + "/" + tableName);
    boolean isTableCreated = tableFile.createNewFile();
    if(!isTableCreated) {
      throw new QueryHandlingException("Error: Failed to create table " + tableName + "!");
    }
    try (FileWriter writer = new FileWriter(tableFile)) {
        String header = encodeString(columnNames) + "\n";
        String types = encodeString(columnTypes) + "\n";
        writer.write(header);
        writer.write(types);
        return new QueryHandlingResponse(true, "Table " + tableName + " created successfully!");
    } catch (IOException e) {
        throw new QueryHandlingException("Error writing to file: " + e.getMessage());
    }
}

    private QueryHandlingResponse processDropTableQuery(String query) throws QueryHandlingException {
    if (!isDatabaseSelected()) {
        throw new QueryHandlingException("Error: No database selected!");
    }

    String[] parts = query.trim().split("\\s+");
    if (parts.length < 3) {
        throw new QueryHandlingException("Invalid DROP TABLE query: " + query);
    }

    String tableName = parts[2];

    String databasePath = DATABASE_PATH + this.useDBName;
    File database = new File(databasePath);
    if (!database.exists()) {
        throw new QueryHandlingException("Error: Database " + this.useDBName + " does not exist!");
    }

    File[] allTables = database.listFiles();
    boolean isTableDeleted = false;
    for (File table : allTables) {
        if (table.getName().equalsIgnoreCase(tableName)) {
            table.delete();
            isTableDeleted = true;
            break;
        }
    }
    if (!isTableDeleted) {
        throw new QueryHandlingException("Error: Table " + tableName + " does not exist!");
    }
    return new QueryHandlingResponse(true,"Table deleted Successfullty");
}

    private QueryHandlingResponse processSelectQuery(String query) throws QueryHandlingException, IOException {
      if (!isDatabaseSelected()) {
            throw new QueryHandlingException("Error: No database selected!");
        }
        String queryProcessed = query.trim().toLowerCase();
        Pattern pattern = Pattern.compile(SELECT_QUERY_SYNTAX);
        Matcher matcher = pattern.matcher(query);
        if(!matcher.find()) {
            throw new QueryHandlingException("Invalid Syntax");
        }
        String tableName = matcher.group(1);
        String databasePath = DATABASE_PATH + this.useDBName;
        File database = new File(databasePath);
         boolean isDatabaseExists = database.isDirectory();
        if (!isDatabaseExists) {
            throw new QueryHandlingException("Error: Database " + this.useDBName + " does not exists!");
        }
        File allTablesPath = new File(DATABASE_PATH + this.useDBName + "/");
        File[] allTables = allTablesPath.listFiles();
        boolean isTableExists = false;
        for (File table : allTables) {
          if (table.getName().equalsIgnoreCase(tableName)) {
            isTableExists = true;
          }
        }
        if (!isTableExists) {
          throw new QueryHandlingException("Error: Table " + tableName + " does not exists!");
        }
        File tableFile = new File(DATABASE_PATH + this.useDBName + "/" + tableName);
        String whereClause = matcher.group(2);
        if (!queryProcessed.contains("where")) {
            throw new QueryHandlingException("Error: Query doesn't have WHERE clause!");
        }
        List<String> ids = Arrays.asList(whereClause.split("AND"));
        HashMap<String, String> map = new HashMap<>();
        for (String id : ids) {
            String[] parts = id.split("=");
            String key = parts[0].trim();
            String values = parts[1].trim();
            map.put(key, values);
        }
        Charset charset = StandardCharsets.UTF_8;
        Path filePath = Paths.get(tableFile.toURI());
        List<String> lines = Files.readAllLines(filePath, charset);
        String[] tem = decodeString(lines.get(0));
        String[] entry = new String[tem.length];
        for (int k=0; k<tem.length; k++) {
            entry[k] = tem[k].trim().split("\\s")[0];
        }
         int cond1 = -1;
         int cond2 = -1;
         for(int j =0; j<entry.length; j++) {
             if(entry[j].equalsIgnoreCase(map.keySet().toArray()[0].toString())) {
                 cond1 = j;
             }
             if(map.keySet().toArray().length > 1 && entry[j].equalsIgnoreCase(map.keySet().toArray()[1].toString())){
                 cond2 = j;
             }
         }
          if(cond1 == -1 && cond2 == -1) {
                throw new QueryHandlingException("Error: Incorrect column names");
            }
          boolean dataFound = false;
          for (int i = 0; i < lines.size(); i++) {
            String[] data = decodeString(lines.get(i));
            if (data[cond1].equals(map.get(map.keySet().toArray()[0].toString())) && (cond2 == -1 || data[cond2].equals(map.get(map.keySet().toArray()[1].toString())))) {
                System.out.println(Arrays.toString(data));
                dataFound = true;
            }
        }
          if(!dataFound) {
              throw new QueryHandlingException("No Data Found");
          }
        return new QueryHandlingResponse(true,"Data Found");
    }

    private QueryHandlingResponse processInsertDataQuery(String query) throws QueryHandlingException {
         if (!isDatabaseSelected()) {
            throw new QueryHandlingException("Error: No database selected!");
        }
        String queryProcessed = query.trim();
        String[] temp = queryProcessed.split("\\s+");
        String tableName = temp[2];
        String columns = queryProcessed.substring(queryProcessed.indexOf("(")+1, queryProcessed.lastIndexOf(")"));
        String[] columnNames = columns.split(",");
        String databasePath = DATABASE_PATH + this.useDBName;
        File database = new File(databasePath);
        boolean isDatabaseExists = database.isDirectory();
        if (!isDatabaseExists) {
            throw new QueryHandlingException("Error: Database " + this.useDBName + " does not exists!");
        }
        File allTablesPath = new File(DATABASE_PATH + this.useDBName + "/");
        File[] allTables = allTablesPath.listFiles();
        boolean isTableExists = false;
        for (File table : allTables) {
          if (table.getName().equalsIgnoreCase(tableName)) {
            isTableExists = true;
          }
        }
        if (!isTableExists) {
          throw new QueryHandlingException("Error: Table " + tableName + " does not exists!");
        }
        File tableFile = new File(DATABASE_PATH + this.useDBName + "/" + tableName);
        try (FileWriter writer = new FileWriter(tableFile,true)) {
            String newEntry = encodeString(columnNames);
            writer.append(newEntry + "\n");
            return new QueryHandlingResponse(true, "Entries are inserted into table");
        } catch (IOException e) {
            throw new QueryHandlingException("Error writing to file: " + e.getMessage());
        }
    }

    private QueryHandlingResponse processUpdateDataQuery(String query) throws QueryHandlingException, IOException {
         if (!isDatabaseSelected()) {
            throw new QueryHandlingException("Error: No database selected!");
        }
        String queryProcessed = query.trim().toLowerCase();
        Pattern pattern = Pattern.compile(UPDATE_QUERY_SYNTAX);
        String newQuery = query.contains(";") ? query.substring(0,query.indexOf(";")) : query;
        Matcher matcher = pattern.matcher(newQuery);
        if(!matcher.find() || matcher.groupCount() < 3) {
            throw new QueryHandlingException("Invalid Syntax");
        }
        String tableName = matcher.group(1);
        String databasePath = DATABASE_PATH + this.useDBName;
        File database = new File(databasePath);
        boolean isDatabaseExists = database.isDirectory();
        if (!isDatabaseExists) {
            throw new QueryHandlingException("Error: Database " + this.useDBName + " does not exists!");
        }
        File allTablesPath = new File(DATABASE_PATH + this.useDBName + "/");
        File[] allTables = allTablesPath.listFiles();
        boolean isTableExists = false;
        for (File table : allTables) {
          if (table.getName().equalsIgnoreCase(tableName)) {
            isTableExists = true;
          }
        }
        if (!isTableExists) {
          throw new QueryHandlingException("Error: Table " + tableName + " does not exists!");
        }
        File tableFile = new File(DATABASE_PATH + this.useDBName + "/" + tableName);
        String setClause = matcher.group(2);
        String whereClause = matcher.group(3);
        if (!queryProcessed.contains("where") | !queryProcessed.contains("set")) {
            throw new QueryHandlingException("Error: Query doesn't have SET/WHERE clause!");
        }
        String[] ids = whereClause.split("AND");
        String[] setids = setClause.split(",");
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> setMap = new HashMap<>();
        for (String id : ids) {
            String[] parts = id.split("=");
            String key = parts[0].trim();
            String values = parts[1].trim();
            map.put(key, values);
        }
        for (String id : setids) {
            String[] parts = id.split("=");
            String key = parts[0].trim();
            String values = parts[1].trim();
            setMap.put(key, values);
        }
        Charset charset = StandardCharsets.UTF_8;
        Path filePath = Paths.get(tableFile.toURI());
        List<String> lines = Files.readAllLines(filePath, charset);
        String[] tem = decodeString(lines.get(0));
        String[] entry = new String[tem.length];
        for (int k=0; k<tem.length; k++) {
            entry[k] = tem[k].trim().split("\\s")[0];
        }
         int cond1 = -1;
         int cond2 = -1;
         for(int j =0; j<entry.length; j++) {
             if(entry[j].equalsIgnoreCase(map.keySet().toArray()[0].toString())) {
                 cond1 = j;
             }
             if(map.keySet().toArray().length > 1 && entry[j].equalsIgnoreCase(map.keySet().toArray()[1].toString())){
                 cond2 = j;
             }
         }
          if(cond1 == -1 && cond2 == -1) {
                throw new QueryHandlingException("Error: Incorrect column names");
            }
          boolean dataFound = false;
          for (int i = 2; i < lines.size(); i++) {
            String[] data = decodeString(lines.get(i));
            if (data[cond1].equals(map.get(map.keySet().toArray()[0].toString())) && (cond2 == -1 || data[cond2].equals(map.get(map.keySet().toArray()[1].toString())))) {
                for(int index=0; index< entry.length; index++) {
                    if(setMap.keySet().contains(entry[index])) {
                        data[index] = setMap.get(entry[index]);
                    }
                }
                lines.set(i, encodeString(data));
                dataFound = true;
            }
        }
          if(!dataFound) {
              throw new QueryHandlingException("No Data Found");
          }
          try (PrintWriter writer = new PrintWriter(tableFile)) {
              StringBuilder stringBuilder = new StringBuilder();
              for(String line : lines){
                  stringBuilder.append(line + "\n");
              }
              writer.print(stringBuilder + "\n");
              return new QueryHandlingResponse(true, "Entries are updated into table");
          } catch (IOException e) {
              throw new QueryHandlingException("Error writing to file: " + e.getMessage());
          }
    }

    private QueryHandlingResponse processDeleteDataQuery(String query) throws QueryHandlingException, IOException {
         if (!isDatabaseSelected()) {
            throw new QueryHandlingException("Error: No database selected!");
        }
        String queryProcessed = query.trim();
        String[] temp = queryProcessed.split("\\s+");
        String tableName = temp[2];
        String databasePath = DATABASE_PATH + this.useDBName;
        File database = new File(databasePath);
        boolean isDatabaseExists = database.isDirectory();
        if (!isDatabaseExists) {
            throw new QueryHandlingException("Error: Database " + this.useDBName + " does not exists!");
        }
        File allTablesPath = new File(DATABASE_PATH + this.useDBName + "/");
        File[] allTables = allTablesPath.listFiles();
        boolean isTableExists = false;
        for (File table : allTables) {
          if (table.getName().equalsIgnoreCase(tableName)) {
            isTableExists = true;
          }
        }
        if (!isTableExists) {
          throw new QueryHandlingException("Error: Table " + tableName + " does not exists!");
        }
        File tableFile = new File(DATABASE_PATH + this.useDBName + "/" + tableName);
        String whereClause;
        if (!temp[3].equalsIgnoreCase("WHERE")) {
            throw new QueryHandlingException("Error: Query doesn't have WHERE clause!");
        }
        whereClause = queryProcessed.substring(queryProcessed.indexOf("where") + 5).trim();
        List<String> ids = Arrays.asList(whereClause.split("and"));
        HashMap<String, String> map = new HashMap<>();
        for (String id : ids) {
            String[] parts = id.split("=");
            String key = parts[0].trim();
            String values = parts[1].trim();
            map.put(key, values);
        }
        Charset charset = StandardCharsets.UTF_8;
        Path filePath = Paths.get(tableFile.toURI());
        List<String> lines = Files.readAllLines(filePath, charset);
        String[] tem = decodeString(lines.get(0));
        String[] entry = new String[tem.length];
        for (int k=0; k<tem.length; k++) {
            entry[k] = tem[k].trim().split("\\s")[0];
        }
         int cond1 = -1;
         int cond2 = -1;
         for(int j =0; j<entry.length; j++) {
             if(entry[j].equalsIgnoreCase(map.keySet().toArray()[0].toString())) {
                 cond1 = j;
             }
             if(map.keySet().toArray().length > 1 && entry[j].equalsIgnoreCase(map.keySet().toArray()[1].toString())){
                 cond2 = j;
             }
         }
          if(cond1 == -1 && cond2 == -1) {
                throw new QueryHandlingException("Error: Incorrect column names");
            }
        for (int i = 0; i < lines.size(); i++) {
            String[] data = decodeString(lines.get(i));
            if (data[cond1].equals(map.get(map.keySet().toArray()[0].toString())) && (cond2 == -1 || data[cond2].equals(map.get(map.keySet().toArray()[1].toString())))) {
                lines.remove(i);
            }
        }
        Files.write(filePath, lines, charset);
        return new QueryHandlingResponse(true, "Entries are deleted from the table");
    }

    public QueryHandlingResponse handleQuery(String query) throws QueryHandlingException, IOException {
    if (query == null || query.trim().isEmpty()) {
        throw new QueryHandlingException("Invalid query syntax!");
    }

    String inputtedQuery = query.trim().toLowerCase();

    if (inputtedQuery.contains(CREATE_DATABASE_KEYWORD)) {
        validateQuerySyntax(query, CREATE_DATABASE_QUERY_SYNTAX, "Invalid CREATE DATABASE query!");
        return processCreateDatabaseQuery(inputtedQuery);
    }

    if (inputtedQuery.contains(USE_DATABASE_KEYWORD)) {
        validateQuerySyntax(query, USE_DATABASE_QUERY_SYNTAX, "Invalid USE DATABASE query!");
        return processUseDatabaseQuery(inputtedQuery);
    }

    if (inputtedQuery.contains(CREATE_TABLE_KEYWORD)) {
        validateQuerySyntax(query, CREATE_TABLE_QUERY_SYNTAX, "Invalid CREATE TABLE query!");
        return processCreateTableQuery(inputtedQuery);
    }

    if (inputtedQuery.contains(DROP_DATABASE_KEYWORD)) {
        validateQuerySyntax(query, DROP_DATABASE_QUERY_SYNTAX, "Invalid DROP DATABASE query!");
        return processDropDatabaseQuery(inputtedQuery);
    }

    if (inputtedQuery.contains(DROP_TABLE_KEYWORD)) {
        validateQuerySyntax(query, DROP_TABLE_QUERY_SYNTAX, "Invalid DROP TABLE query!");
        return processDropTableQuery(inputtedQuery);
    }

    if (inputtedQuery.contains(SELECT_KEYWORD)) {
        validateQuerySyntax(query, SELECT_QUERY_SYNTAX, "Invalid SELECT query!");
        return processSelectQuery(query);
    }

    if (inputtedQuery.contains(INSERT_KEYWORD)) {
        validateQuerySyntax(query, INSERT_QUERY_SYNTAX, "Invalid INSERT query!");
        return processInsertDataQuery(inputtedQuery);
    }

    if (inputtedQuery.contains(UPDATE_KEYWORD)) {
        validateQuerySyntax(query, UPDATE_QUERY_SYNTAX, "Invalid UPDATE query!");
        return processUpdateDataQuery(query);
    }

    if (inputtedQuery.contains(DELETE_KEYWORD)) {
        validateQuerySyntax(query, DELETE_QUERY_SYNTAX, "Invalid DELETE query!");
        return processDeleteDataQuery(inputtedQuery);
    }

    throw new QueryHandlingException("Invalid query syntax!");
}

    private void validateQuerySyntax(String inputtedQuery, String syntax, String errorMessage) throws QueryHandlingException {
    if (!Pattern.matches(syntax, inputtedQuery)) {
        throw new QueryHandlingException(errorMessage);
    }
}

}