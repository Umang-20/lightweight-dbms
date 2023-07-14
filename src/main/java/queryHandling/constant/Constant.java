package queryHandling.constant;

public class Constant {
  public static String DATABASE_FILE = "./src/main/java/database/database.txt";
  public static String DATABASE_PATH = "./src/main/java/database/";
  public static String CREATE_DATABASE_KEYWORD =
      "create database";
  public static String CREATE_DATABASE_QUERY_SYNTAX =
      "^CREATE\\s+DATABASE\\s+(\\w+)$";
  public static String USE_DATABASE_KEYWORD =
      "use";
  public static String USE_DATABASE_QUERY_SYNTAX =
      "^USE\\s+[\\w_]+\\s*;?\\s*$";
  public static String CREATE_TABLE_KEYWORD =
      "create table";
  public static String CREATE_TABLE_QUERY_SYNTAX =
          "^CREATE\\s+TABLE\\s+(\\w+)\\s*\\(((?:\\s*(\\w+)\\s+(INT|VARCHAR|BOOLEAN|FLOAT)\\s*,?\\s*)+)\\)";
  public static String DROP_DATABASE_KEYWORD =
      "drop database";
  public static String DROP_DATABASE_QUERY_SYNTAX =
      "^DROP\\s+DATABASE\\s+(\\w+)$";
  public static String DROP_TABLE_KEYWORD =
      "drop table";
  public static String DROP_TABLE_QUERY_SYNTAX =
      "^DROP\\s+TABLE\\s+\\w+\\s*;?\\s*$";
  public static String SELECT_KEYWORD =
      "select";
  public static String SELECT_QUERY_SYNTAX =
          "^SELECT\\s*\\*\\s*FROM\\s*(\\w+)\\s*WHERE\\s*((\\w+\\s*=\\s*\\w+)(\\s*AND\\s*(\\w+\\s*=\\s*\\w+))?)$";
   public static String INSERT_KEYWORD =
      "insert";
  public static String INSERT_QUERY_SYNTAX =
          "^INSERT\\sINTO\\s(\\w+)\\s*VALUES\\s*\\(([^)]+)\\);?$";
  public static String UPDATE_KEYWORD =
      "update";
  public static String UPDATE_QUERY_SYNTAX =
          "^UPDATE\\s+(\\w+)\\s+SET\\s+\\((.+?)\\)\\s+WHERE\\s+(.+)$";
    public static String DELETE_KEYWORD =
      "delete";
  public static String DELETE_QUERY_SYNTAX =
          "^DELETE\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(.+))?$";
}
