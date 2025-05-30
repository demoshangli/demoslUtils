package com.demoshangli.utils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * sql生成工具类 - 可以根据条件生成简易sql。
 */
public class SqlUtils {

    /**
     * 生成INSERT语句
     * @param entity 实体对象
     * @param tableName 表名
     * @return [SQL语句, 参数列表]
     */
    public static <T> SqlResult generateInsert(T entity, String tableName) {
        try {
            List<Object> params = new ArrayList<>();
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            
            for (Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(entity);
                
                if (value != null) {
                    if (!columns.isEmpty()) {
                        columns.append(", ");
                        values.append(", ");
                    }
                    columns.append(camelToSnake(field.getName()));
                    values.append("?");
                    params.add(value);
                }
            }
            
            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", 
                                      tableName, columns, values);
            
            return new SqlResult(sql, params);
        } catch (Exception e) {
            throw new RuntimeException("生成INSERT语句失败", e);
        }
    }

    /**
     * 生成UPDATE语句
     * @param entity 实体对象
     * @param tableName 表名
     * @param conditions WHERE条件
     * @return [SQL语句, 参数列表]
     */
    public static <T> SqlResult generateUpdate(T entity, String tableName, Condition... conditions) {
        try {
            List<Object> params = new ArrayList<>();
            StringBuilder setClause = new StringBuilder();
            
            // 处理SET部分
            for (Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(entity);
                
                if (value != null) {
                    if (!setClause.isEmpty()) setClause.append(", ");
                    setClause.append(camelToSnake(field.getName())).append(" = ?");
                    params.add(value);
                }
            }
            
            // 处理WHERE条件
            StringBuilder whereClause = new StringBuilder();
            for (Condition condition : conditions) {
                if (whereClause.length() > 0) whereClause.append(" AND ");
                whereClause.append(condition.toSql());
                params.add(condition.getValue());
            }
            
            String sql = String.format("UPDATE %s SET %s WHERE %s", 
                                      tableName, setClause, whereClause);
            
            return new SqlResult(sql, params);
        } catch (Exception e) {
            throw new RuntimeException("生成UPDATE语句失败", e);
        }
    }

    /**
     * 生成SELECT语句
     * @param tableName 表名
     * @param columns 查询列
     * @param conditions WHERE条件
     * @return [SQL语句, 参数列表]
     */
    public static SqlResult generateSelect(String tableName, List<String> columns, Condition... conditions) {
        List<Object> params = new ArrayList<>();
        String columnStr = columns.isEmpty() ? "*" : String.join(", ", columns);
        
        StringBuilder whereClause = new StringBuilder();
        for (Condition condition : conditions) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append(condition.toSql());
            params.add(condition.getValue());
        }
        
        String where = whereClause.length() > 0 ? " WHERE " + whereClause : "";
        String sql = String.format("SELECT %s FROM %s%s", columnStr, tableName, where);
        
        return new SqlResult(sql, params);
    }

    /**
     * 生成DELETE语句
     * @param tableName 表名
     * @param conditions WHERE条件
     * @return [SQL语句, 参数列表]
     */
    public static SqlResult generateDelete(String tableName, Condition... conditions) {
        List<Object> params = new ArrayList<>();
        
        StringBuilder whereClause = new StringBuilder();
        for (Condition condition : conditions) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append(condition.toSql());
            params.add(condition.getValue());
        }
        
        String where = whereClause.length() > 0 ? " WHERE " + whereClause : "";
        String sql = "DELETE FROM " + tableName + where;
        
        return new SqlResult(sql, params);
    }

    // 驼峰转下划线命名
    private static String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    // 条件封装类
    public static class Condition {
        private final String column;
        private final String operator;
        private final Object value;

        public Condition(String column, String operator, Object value) {
            this.column = column;
            this.operator = operator;
            this.value = value;
        }

        public String toSql() {
            return column + " " + operator + " ?";
        }

        public Object getValue() {
            return value;
        }
    }

    // SQL结果封装
    public static class SqlResult {
        private final String sql;
        private final List<Object> params;

        public SqlResult(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }

        public String getSql() {
            return sql;
        }

        public List<Object> getParams() {
            return params;
        }
    }
}