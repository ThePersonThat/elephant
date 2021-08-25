package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.BackupException;
import edu.sumdu.tss.elephant.helper.exception.HttpError500;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.CmdUtil;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import org.apache.commons.io.FileUtils;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.io.File;
import java.util.Date;
import java.util.List;

public class BackupService {

    private static final String LIST_BACKUP_SQL = "SELECT * from backups where database = :database";
    private static final String GET_BY_NAME_SQL = "SELECT * from backups where database = :database and point = :point";
    private static final String INSERT_SQL =
            "insert into backups(database, point, status, \"createdAt\", \"updatedAt\") " +
                    "values (:database, :point, :status, :createdAt, :updatedAt)" +
                    "ON CONFLICT(database, point) DO UPDATE \n" +
                    "  SET status = excluded.status, \n" +
                    "      \"updatedAt\" = now();";
    private static final String DELETE_BACKUP = "DELETE FROM backups WHERE database = :database and point = :point;";
    private static final ParameterizedStringFactory DROP_DB = new ParameterizedStringFactory("DROP DATABASE :database WITH (FORCE);");

    public static List<Backup> list(String dbName) {
        try (Connection con = DBPool.getConnection().open()) {
            return con.createQuery(LIST_BACKUP_SQL).addParameter("database", dbName).executeAndFetch(Backup.class);
        }
    }

    public static Backup byName(String database, String point) {
        try (Connection con = DBPool.getConnection().open()) {
            Backup backup = con.createQuery(GET_BY_NAME_SQL).addParameter("database", database).addParameter("point", point).executeAndFetchFirst(Backup.class);
            if (backup == null) {
                throw new NotFoundException(String.format("Backup %s for database %s", point, database));
            }
            return backup;
        }
    }

    public static void perform(String database, String pointName) throws BackupException {
        try (Connection con = DBPool.getConnection().open()) {
            Backup point;
            try {
                point = con.createQuery(GET_BY_NAME_SQL).addParameter("database", database).addParameter("point", pointName).executeAndFetchFirst(Backup.class);
            } catch (NotFoundException ex) {
                point = new Backup();
                point.setDatabase(database);
                point.setPoint(pointName);
                point.setCreatedAt(new Date());
            }
            point.setUpdatedAt(new Date());
            point.setStatus(Backup.BACKUP_STATE.PERFORMED.name());
            BackupService.save(point);
            BackupService.createBackup(database, pointName);
            point.setUpdatedAt(new Date());
            point.setStatus(Backup.BACKUP_STATE.DONE.name());
            BackupService.save(point);
        } catch (Sql2oException ex) {
            throw new BackupException(ex);
        }
    }

    private static void save(Backup point) {
        try (Connection con = DBPool.getConnection().open()) {
            con.createQuery(INSERT_SQL, false).bind(point).executeUpdate();
        }
    }

    static public void restore(String dbName, String pointName) throws BackupException {
        try (Connection con = DBPool.getConnection().open()) {
            Backup point = con.createQuery(GET_BY_NAME_SQL).addParameter("database", dbName).addParameter("point", pointName).executeAndFetchFirst(Backup.class);
            if (point == null) {
                point = new Backup();
                point.setDatabase(dbName);
                point.setPoint(pointName);
                point.setCreatedAt(new Date());
            }
            point.setUpdatedAt(new Date());
            point.setStatus(Backup.BACKUP_STATE.PERFORMED.name());
            BackupService.save(point);
            BackupService.restoreBackup(dbName, pointName);
            point.setUpdatedAt(new Date());
            point.setStatus(Backup.BACKUP_STATE.DONE.name());
            BackupService.save(point);
        } catch (Sql2oException ex) {
            throw new BackupException(ex);
        }
    }

    public static void delete(String database, String point) {
        String path = filePath(database, point);
        DBPool.getConnection().open().createQuery(DELETE_BACKUP, false).addParameter("database", database).addParameter("point", point).executeUpdate();
        new File(path).delete();
    }

    private static void createBackup(String dbName, String pointName) {
        String path = filePath(dbName, pointName);
        try {
            FileUtils.forceMkdirParent(new File(path));
        } catch (Exception ex) {
            throw new HttpError500(ex);
        }
        CmdUtil.exec(String.format("pg_dump --format=custom --dbname=%s  -f %s", DBPool.dbUtilUrl(dbName), path));
    }

    private static void restoreBackup(String dbName, String pointName) {
        String path = filePath(dbName, pointName);
        DBPool.getConnection().open().createQuery(DROP_DB.addParameter("database", dbName).toString(), false).executeUpdate();
        CmdUtil.exec(String.format("pg_restore --clean --create --dbname=%s %s", DBPool.dbUtilUrl(DBPool.DEFAULT_DATABASE), path));
    }

    public static String filePath(String dbName, String pointName) {
        String path = Keys.get("DB.USER_LOCAL_PATH");
        path = path + File.separatorChar + dbName + File.separatorChar + pointName;
        return path;
    }
}
