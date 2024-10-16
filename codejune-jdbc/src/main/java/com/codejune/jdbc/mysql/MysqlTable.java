package com.codejune.jdbc.mysql;

import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.table.SqlTable;
import com.codejune.jdbc.util.SqlBuilder;
import java.util.List;
import java.util.Map;

/**
 * MysqlTable
 *
 * @author ZJ
 * */
public final class MysqlTable extends SqlTable {

    MysqlTable(MysqlDatabase mysqlDatabase, String name) {
        super(mysqlDatabase, name);
    }

    @Override
    public MysqlDatabase getDatabase() {
        return (MysqlDatabase) super.getDatabase();
    }

    @Override
    public long delete(Filter filter) {
        return this.getDatabase().getJdbc().execute(new SqlBuilder(this.getName(), MysqlJdbc.class).parseDeleteSql(filter));
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        return this.update(setData, filter, MysqlJdbc.class);
    }

    @Override
    public long count(Filter filter) {
        return ObjectUtil.parse(this.getDatabase().getJdbc().query(new SqlBuilder(this.getName(), MysqlJdbc.class).parseCountSql(filter)).getFirst().get("C"), Long.class);
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        return this.getDatabase().getJdbc().query(new SqlBuilder(this.getName(), MysqlJdbc.class).parseQueryDataSql(query), ArrayUtil.asList("R"));
    }

}