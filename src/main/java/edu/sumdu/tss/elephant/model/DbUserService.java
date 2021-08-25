package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.exception.NotImplementedException;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;

public class DbUserService {

    private static final ParameterizedStringFactory CREATE_USER_SQL = new ParameterizedStringFactory("CREATE USER :name WITH PASSWORD ':password' CONNECTION LIMIT 5 IN ROLE customer;");
    private static final ParameterizedStringFactory RESET_USER_SQL = new ParameterizedStringFactory("ALTER USER :name SET PASSWORD = :password");
    private static final ParameterizedStringFactory DELETE_USER_SQL = new ParameterizedStringFactory("DROP USER :name");

    public static void initUser(String username, String password) {
        //Create user
        System.out.println("Username: " + username);
        String create_user_string = CREATE_USER_SQL.addParameter("name", username).addParameter("password", password).toString();
        System.out.println(create_user_string);
        DBPool.getConnection().open().createQuery(create_user_string, false).executeUpdate();
        //Create tablespace
        String path = UserService.tablespacePath(username);
        System.out.println("Tablespace path:" + path);
        UserService.createTablespace(username, path);

        //Create database
        //TODO: move to role change
        //String dbName = StringUtils.randomAlphaString(8);
        //DatabaseService.create(dbName, username, username);
    }

    public static void dbUserPasswordReset(String name, String password) {
        DBPool.getConnection().open().createQuery(RESET_USER_SQL.addParameter("name", name).toString(), false).addParameter("password", password).executeUpdate();
    }

    public static void dropUser(String name) {
        //Drop user
        var connection = DBPool.getConnection();
        try (var context = connection.beginTransaction()) {
            context.createQuery(DELETE_USER_SQL.addParameter("name", name).toString(), false).executeUpdate();
        }
        //Drop tablespace
        throw new NotImplementedException();
    }
}
