package com.codejune.jdbc.oracle;

import com.codejune.jdbc.table.SqlTable;

/**
 * OracleTable
 *
 * @author ZJ
 * */
public final class OracleTable extends SqlTable {

    OracleTable(OracleDatabase oracleDatabase, String name) {
        super(oracleDatabase, name);
    }

    @Override
    public OracleDatabase getDatabase() {
        return (OracleDatabase) super.getDatabase();
    }

}