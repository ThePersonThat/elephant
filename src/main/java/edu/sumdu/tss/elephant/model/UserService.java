package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.utils.CmdUtil;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.sql2o.Connection;

import java.io.File;
import java.util.List;

public class UserService {


    private static final ParameterizedStringFactory CREATE_TABLESPACE_SQL
            = new ParameterizedStringFactory("CREATE TABLESPACE :name" +
            "    OWNER :name" +
            "    LOCATION ':path'");


    private static final String USER_BY_TOKEN_SQL = "SELECT * FROM users WHERE token = :token";
    private static final String INSERT_SQL =
            "insert into USERS(login, password, role, username, dbPassword, publickey, privatekey, token) " +
                    "values (:login, :password, :role, :username, :dbPassword, :publicKey, :privateKey, :token)";
    private static final String UPDATE_SQL =
            "UPDATE users" +
                    " SET password = :password, role = :role, username= :username, dbPassword=:dbPassword," +
                    " publicKey = :publicKey, privateKey=:privateKey, token=:token, language= :language" +
                    " WHERE id = :id";

    private static final String USER_BY_LOGIN_SQL = "SELECT * FROM users WHERE login = :login";
    public static User byLogin(String login) {
        try (Connection con = DBPool.getConnection().open()) {
            return con.createQuery(USER_BY_LOGIN_SQL).addParameter("login", login).executeAndFetchFirst(User.class);
        }
    }

    private static final String USER_LIST_SQL = "SELECT id, login, username FROM users";
    public static List<User> list() {
        try (Connection con = DBPool.getConnection().open()) {
            return con.createQuery(USER_LIST_SQL).executeAndFetch(User.class);
        }
    }

    public static void save(User user) {
        try (Connection con = DBPool.getConnection().open()) {
            String query = user.getId() == null ? INSERT_SQL : UPDATE_SQL;
            long id = con.createQuery(query).bind(user).executeUpdate().getKey(Long.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private static final String USER_BY_KEY_SQL = "SELECT id, login, role, username, language, publicKey, privateKey FROM users WHERE publicKey = :publicKey";
    private static final String[] SPACE_SCOPES = new String[]{"tablespace", "scripts", "backups"};

    //"    [ WITH ( параметр_табличного_пространства = значение [, ... ] ) ]"
    public static void createTablespace(String owner, String path) {
        String create_tablespace = CREATE_TABLESPACE_SQL.addParameter("name", owner).addParameter("path", path.replace("\\", "\\\\")).toString();
        System.out.println(create_tablespace);
        DBPool.getConnection().open()
                .createQuery(create_tablespace, false)
                .executeUpdate();
    }

    private static final ParameterizedStringFactory CHANGE_OWNER
            = new ParameterizedStringFactory("sudo chown -f :user :path");
    private static final ParameterizedStringFactory CHANGE_MODE
            = new ParameterizedStringFactory("chmod 700 :path");

    public static User byPublicKey(String publicKey) {
        try (Connection con = DBPool.getConnection().open()) {
            return con.createQuery(USER_BY_KEY_SQL).addParameter("publicKey", publicKey).executeAndFetchFirst(User.class);
        }
    }

    public static String userStoragePath(String owner) {
        return Keys.get("DB.LOCAL_PATH") + owner;
    }

    public static void initUserStorage(String owner) {
        try {
            String path = userStoragePath(owner);
            for (var scope : SPACE_SCOPES) {
                File subPath = new File(path + File.separator + scope);
                FileUtils.forceMkdir(subPath);
                if (scope.equals("tablespace") && !SystemUtils.IS_OS_WINDOWS) {
                    /*
                    //Under *nix OS tablespace folder must be ownerd by RDBMS-owner user
                    Path tablespace = Path.of(subPath.getPath());
                    FileOwnerAttributeView view = Files.getFileAttributeView(tablespace,
                            FileOwnerAttributeView.class);
                    UserPrincipalLookupService lookupService = FileSystems.getDefault()
                            .getUserPrincipalLookupService();
                    UserPrincipal userPrincipal = lookupService.lookupPrincipalByName(Keys.get("DB.OS_USER"));

                    Files.setOwner(tablespace, userPrincipal);
                     */
                    CmdUtil.exec(CHANGE_MODE.addParameter("path", subPath.getPath()).toString());
                    CmdUtil.exec(CHANGE_OWNER.addParameter("user", Keys.get("DB.OS_USER")).addParameter("path", subPath.getPath()).toString());
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static long storageSize(String owner) {
        try {
            return FileUtils.sizeOfDirectory(new File(UserService.userStoragePath(owner)));
        } catch (IllegalArgumentException ex) {
            System.out.println("Folder not found");
            return 0;
        }
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
