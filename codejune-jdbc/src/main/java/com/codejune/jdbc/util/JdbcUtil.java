package com.codejune.jdbc.util;

import com.codejune.common.exception.InfoException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * JdbcUtil
 *
 * @author ZJ
 * */
public final class JdbcUtil {

    /**
     * 关闭PreparedStatement
     *
     * @param preparedStatement preparedStatement
     * */
    public static void close(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (Exception e) {
                throw new InfoException(e.getMessage());
            }
        }
    }

    /**
     * 关闭ResultSet
     *
     * @param resultSet resultSet
     * */
    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception e) {
                throw new InfoException(e.getMessage());
            }
        }
    }

}