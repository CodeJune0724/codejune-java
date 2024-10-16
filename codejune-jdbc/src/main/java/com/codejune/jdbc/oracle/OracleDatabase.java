package com.codejune.jdbc.oracle;

import com.codejune.core.BaseException;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.database.SqlDatabase;
import com.codejune.jdbc.table.SqlTable;
import java.util.ArrayList;
import java.util.List;

/**
 * OracleDatabase
 *
 * @author ZJ
 * */
public final class OracleDatabase extends SqlDatabase {

    OracleDatabase(OracleJdbc oracleJdbc, String name) {
        super(oracleJdbc, name);
    }

    @Override
    public OracleJdbc getJdbc() {
        return (OracleJdbc) super.getJdbc();
    }

    @Override
    public OracleTable getTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            throw new BaseException("tableName is null");
        }
        return new OracleTable(this, tableName);
    }

    @Override
    public List<OracleTable> getTable() {
        List<OracleTable> result = new ArrayList<>();
        for (SqlTable sqlTable : super.getTable()) {
            result.add(this.getTable(sqlTable.getName()));
        }
        return result;
    }

}