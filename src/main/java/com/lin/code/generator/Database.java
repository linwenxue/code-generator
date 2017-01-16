package com.lin.code.generator;

import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenxuelin on 2017/1/8.
 */
public class Database {
    private DBConfig dbConfig;

    public Database(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    private Connection openConnection() throws ClassNotFoundException, SQLException {
        Class.forName(dbConfig.getDriver());
        Connection conn = DriverManager.getConnection(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
        return conn;
    }

    public List<DBTable> getTableInfo(List<TableGroup> tableGroups) {
        Connection conn = null;
        Statement tableStmt = null;
        Statement columnStmt = null;
        ResultSet tableRs = null;
        ResultSet columnRs = null;

        List<DBTable> tables = new ArrayList<DBTable>();
        DBTable table;
        DBColumn column;
        if(tableGroups != null) {
            try {
                conn = openConnection();
                for (TableGroup tableGroup : tableGroups) {
                    String sql = getQueryTableSQL(tableGroup);
                    tableStmt = conn.createStatement();
                    tableRs = tableStmt.executeQuery(sql);
                    while(tableRs != null && tableRs.next()) {
                        table = new DBTable();
                        table.setGroup(tableGroup.getGroup());
                        table.setName(tableRs.getString("table_name"));
                        table.setComment(tableRs.getString("comments"));

                        sql = getQueryColumnSQL(table.getName());
                        columnStmt = conn.createStatement();
                        columnRs = columnStmt.executeQuery(sql);
                        while(columnRs != null && columnRs.next()) {
                            if(table.getColumns() == null) {
                                table.setColumns(new ArrayList<DBColumn>());
                            }
                            column = new DBColumn();
                            column.setName(columnRs.getString("column_name"));
                            column.setType(columnRs.getString("data_type"));
                            column.setComment(columnRs.getString("comments"));
                            if(StringUtils.isBlank(column.getComment())) {
                                //column.setComment(StringUtils.replace(table.getComment(), "\r\n", "  "));;
                            }
                            table.getColumns().add(column);
                        }
                        columnRs.close();
                        columnStmt.close();

                        sql = getQueryPrimaryKeySQL(table.getName());
                        columnStmt = conn.createStatement();
                        columnRs = columnStmt.executeQuery(sql);
                        while(columnRs != null && columnRs.next()) {
                            if(table.getPrimaryKeys() == null) {
                                table.setPrimaryKeys(new ArrayList<String>());
                            }
                            table.getPrimaryKeys().add(columnRs.getString("column_name"));
                        }
                        columnRs.close();
                        columnStmt.close();

                        if(StringUtils.equalsIgnoreCase(dbConfig.getDbType(), "mysql")) {
                            sql = getQueryAutoIncrementSQL(table.getName());
                            columnStmt = conn.createStatement();
                            columnRs = columnStmt.executeQuery(sql);
                            while(columnRs != null && columnRs.next()) {
                                table.setAutoIncrement(columnRs.getString("column_name"));
                            }
                            columnRs.close();
                            columnStmt.close();
                        }

                        tables.add(table);
                    }
                    tableRs.close();
                    tableStmt.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(tableRs != null) {
                    try {
                        tableRs.close();
                    } catch (SQLException e) {
                    }
                }
                if(columnRs != null) {
                    try {
                        columnRs.close();
                    } catch (SQLException e) {
                    }
                }
                if(tableStmt != null) {
                    try {
                        tableStmt.close();
                    } catch (SQLException e) {
                    }
                }
                if(columnStmt != null) {
                    try {
                        columnStmt.close();
                    } catch (SQLException e) {
                    }
                }
                if(conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }
        return tables;
    }

    private String getQueryTableSQL(TableGroup tableGroup) {
        StringBuilder sql = new StringBuilder();
        //mysql
        if(StringUtils.equalsIgnoreCase(dbConfig.getDbType(), "mysql")) {
            sql.append("select table_name,table_comment as comments from information_schema.tables where table_schema='");
            sql.append(dbConfig.getSchema()).append("'");
            if(tableGroup.getTables() == null || tableGroup.getTables().size() == 0) { //未指定数据库表，不允许查出记录
                sql.append(" and 1 = 2");
            } else {
                sql.append(" and table_name in (");
                for(String table : tableGroup.getTables()) {
                    sql.append("'").append(table).append("',");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(")");
            }
        } else { //oracle
            sql.append("select a.table_name, b.comments from user_tables a, user_tab_comments b where a.table_name = b.table_name");
            if(tableGroup.getTables() == null || tableGroup.getTables().size() == 0) { //未指定数据库表，不允许查出记录
                sql.append(" and 1 = 2");
            }
            else {
                sql.append(" and a.table_name in (");
                for(String table : tableGroup.getTables()) {
                    sql.append("'").append(table).append("',");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(")");
            }
        }
        return sql.toString();
    }

    private String getQueryColumnSQL(String tableName) {
        StringBuilder sql = new StringBuilder();
        if(StringUtils.equalsIgnoreCase(dbConfig.getDbType(), "mysql")) {//mysql
            sql.append("select column_name,data_type,column_comment as comments from information_schema.columns");
            sql.append(" where table_schema='").append(dbConfig.getSchema()).append("'");
            sql.append(" and table_name='").append(tableName).append("'");
        }
        else { //oracle
            sql.append("select a.column_name, case a.data_type when 'TIMESTAMP(6)' then 'TIMESTAMP' when 'VARCHAR2' then 'VARCHAR' else a.data_type end as data_type, b.comments from user_tab_columns a, user_col_comments b ");
            sql.append("where a.table_name = b.table_name and a.column_name = b.column_name and a.table_name = '");
            sql.append(tableName).append("'");
        }
        return sql.toString();
    }

    private String getQueryPrimaryKeySQL(String tableName) {
        StringBuilder sql = new StringBuilder();//mysql
        if(StringUtils.equalsIgnoreCase(dbConfig.getDbType(), "mysql")) {//mysql
            sql.append("select column_name from information_schema.columns");
            sql.append(" where table_schema='").append(dbConfig.getSchema()).append("'");
            sql.append(" and table_name='").append(tableName).append("'");
            sql.append(" and column_key='PRI'");
        }
        else { //oracle
            sql.append("select b.column_name from user_constraints a, user_cons_columns b ");
            sql.append("where a.constraint_name = b.constraint_name and a.constraint_type = 'P' and a.table_name = '");
            sql.append(tableName).append("'");
        }
        return sql.toString();
    }

    private String getQueryAutoIncrementSQL(String tableName) {
        StringBuilder sql = new StringBuilder();//mysql
        if(StringUtils.equalsIgnoreCase(dbConfig.getDbType(), "mysql")) {//mysql
            sql.append("select column_name from information_schema.columns");
            sql.append(" where table_schema='").append(dbConfig.getSchema()).append("'");
            sql.append(" and table_name='").append(tableName).append("'");
            sql.append(" and extra='AUTO_INCREMENT'");
        }
        return sql.toString();
    }
}