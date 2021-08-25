package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.exception.HttpError500;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.sql2o.Connection;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UserService {


    private static final String USER_BY_KEY_SQL = "SELECT id, login, role, username FROM users WHERE publicKey = :publicKey";
    private static final ParameterizedStringFactory CREATE_TABLESPACE_SQL = new ParameterizedStringFactory("CREATE TABLESPACE :name" +
            "    OWNER :name" +
            "    LOCATION ':path'");

    private static final String USER_BY_LOGIN_SQL = "SELECT * FROM users WHERE login = :login";
    private static final String USER_LIST_SQL =
            "SELECT id, login, username FROM users";
    private static final String USER_BY_TOKEN_SQL = "SELECT * FROM users WHERE token = :token";
    private static final String INSERT_SQL =
            "insert into USERS(login, password, role, username, dbPassword, publickey, privatekey, token) " +
                    "values (:login, :password, :role, :username, :dbPassword, :publicKey, :privateKey, :token)";
    private static final String UPDATE_SQL =
            "UPDATE users" +
                    " SET password = :password, role = :role, username= :username, dbPassword=:dbPassword," +
                    " publicKey = :publicKey, privateKey=:privateKey, token =:token" +
                    " WHERE id = :id";

    public static User byLogin(String login) {
        try (Connection con = DBPool.getConnection().open()) {
            return con.createQuery(USER_BY_LOGIN_SQL).addParameter("login", login).executeAndFetchFirst(User.class);
        }
    }

    public static List<User> list() {
        try (Connection con = DBPool.getConnection().open()) {
            return con.createQuery(USER_LIST_SQL).executeAndFetch(User.class);
        }
    }

    public static void save(User user) {
        try (Connection con = DBPool.getConnection().open()) {
            String query = user.getId() == null ? INSERT_SQL : UPDATE_SQL;
            System.out.println("SQL fro user save:" + query);
            long id = con.createQuery(query).bind(user).executeUpdate().getKey(Long.class);
            System.out.println("User id:" + id);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static User getUserByPublicKey(String publicKey) {
        try (Connection con = DBPool.getConnection().open()) {
            return con.createQuery(USER_BY_KEY_SQL).addParameter("publicKey", publicKey).executeAndFetchFirst(User.class);
        }
    }

    //"    [ WITH ( параметр_табличного_пространства = значение [, ... ] ) ]"
    public static void createTablespace(String owner, String path) {
        try {
            FileUtils.forceMkdir(new File(path));
        } catch (IOException e) {
            throw new HttpError500(e);
        }

        String create_tablespace = CREATE_TABLESPACE_SQL.addParameter("name", owner).addParameter("path", path.replace("\\", "\\\\")).toString();
        System.out.println(create_tablespace);
        DBPool.getConnection().open()
                .createQuery(create_tablespace, false)
                .executeUpdate();
    }

    public static long storageSize(String owner) {
        try {
            return FileUtils.sizeOfDirectory(new File(UserService.tablespacePath(owner)));
        } catch (IllegalArgumentException ex) {
            System.out.println("Folder not found");
            return 0;
        }
    }

    public static String tablespacePath(String owner) {
        return Keys.get("DB.LOCAL_PATH") + owner;
    }

    public static User newDefaultUser() {
        User user = new User();
        user.setRole(UserRole.UNCHEKED.getValue());
        user.setPrivateKey(StringUtils.randomAlphaString(20));
        user.setPublicKey(StringUtils.randomAlphaString(20));
        user.setToken(StringUtils.uuid());
        user.setUsername(StringUtils.randomAlphaString(8));
        user.setDbPassword(StringUtils.randomAlphaString(20));
        return user;
    }

    public static User byToken(String token) {
        try (Connection con = DBPool.getConnection().open()) {
            System.out.println("try to find user by token:" + token);
            return con.createQuery(USER_BY_TOKEN_SQL).addParameter("token", token).executeAndFetchFirst(User.class);
        }
    }
}
