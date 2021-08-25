package edu.sumdu.tss.elephant.model;


import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import org.sql2o.data.Table;

public class TableService {

    private static final String TABLE_LIST_SQL = "SELECT table_name as name\n" +
            "  FROM information_schema.tables\n" +
            " WHERE table_type='BASE TABLE';";
    private static final ParameterizedStringFactory TABLE_PREVIEW_SQL = new ParameterizedStringFactory("select * from :table limit :limit offset :offset");

    public static Table list(String database) {
        return DBPool.getConnection(database).open().createQuery(TABLE_LIST_SQL).executeAndFetchTable();
    }

    public static Table byName(String database, String tableName, int limit, int offset) {
        return
                DBPool.getConnection(database).open()
                        .createQuery(TABLE_PREVIEW_SQL.addParameter("table", tableName).toString())
                        .addParameter("limit", limit)
                        .addParameter("offset", offset)
                        .executeAndFetchTable();
    }


}