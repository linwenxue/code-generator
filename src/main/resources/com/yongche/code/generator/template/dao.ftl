package ${package}.dao.impl;

import com.yongche.etl.base.BaseDao;
import ${package}.dao.I${name}Dao;
import ${package}.entity.${name};
import com.yongche.etl.util.SysException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ${name}Dao extends BaseDao implements I${name}Dao {
    private static Logger logger = LoggerFactory.getLogger(${name}Dao.class);

    @Override
    public ${name} insert(final ${name} entity) throws SysException {
        final StringBuilder sql = new StringBuilder();
        <#if primaryKeyFields??&& autoIncrement??>
        if(entity.get${autoIncrement?cap_first}()==null||entity.get${autoIncrement?cap_first}()==""){
            sql.append("INSERT INTO ${tableName} (<#list fields as field><#if field.name?upper_case == autoIncrement?upper_case><#else>${field.dbName}<#if field_has_next>,</#if></#if></#list>) ");
            sql.append("VALUES (<#list fields as field><#if field.name?upper_case == autoIncrement?upper_case><#else><#if field.type == "datetime">str_to_date(?,'%Y-%m-%d %H:%i:%s')<#else>?</#if><#if field_has_next>,</#if></#if></#list>)");
            try {
                logger.info(sql.toString());
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(
                    new PreparedStatementCreator(){
                        public PreparedStatement createPreparedStatement(Connection conn) throws SQLException{
                            int i = 0;
                            PreparedStatement ps = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
                            <#list fields as field>
                            <#if field.name?upper_case == autoIncrement?upper_case>
                            <#else>
                            <#switch field.type>
                            <#case "longblob">
                            ps.setBytes(++i, entity.get${field.name?cap_first}());
                            <#break>
                            <#case "blob">
                            ps.setBytes(++i, entity.get${field.name?cap_first}());
                            <#break>
                            <#default>
                            ps.setString(++i, StringUtils.trimToNull(entity.get${field.name?cap_first}()));
                            </#switch>
                            </#if>
                            </#list>
                            return ps;
                        }
                    },keyHolder);

                String generatedId = Long.toString(keyHolder.getKey().longValue());
                <#list primaryKeys as primaryKey>
                    <#if autoIncrement?cap_first == primaryKey?cap_first>
                entity.set${primaryKey?cap_first}(generatedId);
                    </#if>
                </#list>
            }catch (DataAccessException e) {
                logger.error("增加${tableName}错误：{}", e.getMessage());
                throw new SysException("10000", "增加${tableName}错误", e);
            }
        }else{
            sql.append("INSERT INTO ${tableName} (<#list fields as field>${field.dbName}<#if field_has_next>,</#if></#list>) ");
            sql.append("VALUES (<#list fields as field><#if field.type == "datetime">str_to_date(?,'%Y-%m-%d %H:%i:%s')<#else>?</#if><#if field_has_next>,</#if></#list>)");
            try {
                logger.info(sql.toString());
                jdbcTemplate.update(
                    new PreparedStatementCreator(){
                        public PreparedStatement createPreparedStatement(Connection conn) throws SQLException{
                        int i = 0;
                        PreparedStatement ps = conn.prepareStatement(sql.toString());
                        <#list fields as field>
                        <#switch field.type>
                        <#case "longblob">
                        ps.setBytes(++i, entity.get${field.name?cap_first}());
                        <#break>
                        <#case "blob">
                        ps.setBytes(++i, entity.get${field.name?cap_first}());
                        <#break>
                        <#default>
                        ps.setString(++i, StringUtils.trimToNull(entity.get${field.name?cap_first}()));
                        </#switch>
                        </#list>
                        return ps;
                        }
                    });
            } catch (DataAccessException e) {
                logger.error("增加${tableName}错误：{}", e.getMessage());
                throw new SysException("10000", "增加${tableName}错误", e);
            }
        }
        <#else>
        sql.append("INSERT INTO ${tableName} (<#list fields as field>${field.dbName}<#if field_has_next>,</#if></#list>) ");
        sql.append("VALUES (<#list fields as field><#if field.type == "datetime">str_to_date(?,'%Y-%m-%d %H:%i:%s')<#else>?</#if><#if field_has_next>,</#if></#list>)");
        try {
            logger.info(sql.toString());
            jdbcTemplate.update(
                new PreparedStatementCreator(){
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        int i = 0;
                        PreparedStatement ps = conn.prepareStatement(sql.toString());

                        <#list fields as field>
                        <#switch field.type>
                        <#case "longblob">
                        ps.setBytes(++i, entity.get${field.name?cap_first}());
                        <#break>
                        <#case "blob">
                        ps.setBytes(++i, entity.get${field.name?cap_first}());
                        <#break>
                        <#default>
                        ps.setString(++i, StringUtils.trimToNull(entity.get${field.name?cap_first}()));
                        </#switch>
                        </#list>
                        return ps;
                    }
               });
            } catch (DataAccessException e) {
                logger.error("增加${tableName}错误：{}", e.getMessage());
                throw new SysException("10000", "增加${tableName}错误", e);
            }
        </#if>
        return entity;
    }

    @Override
    public int update(${name} entity) throws SysException {
        int rowsAffected;
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ${tableName} SET ");
        <#if primaryKeyFields??>
             <#list primaryKeyFields as primaryKey>
        // 判断主键是否为空
        if (StringUtils.isBlank(entity.get${primaryKey.name?cap_first}())){
            throw new SysException("100001", "修改${tableName}错误：主键${primaryKey.name?cap_first}为空", null);
        }
            </#list>
        </#if>
        List<Object> params = new ArrayList<Object>();
        try {
            <#list fields as field>
            <#assign isPrimaryKey = false>
            <#if dbPrimaryKeys??>
                <#list dbPrimaryKeys as primaryKey>
                    <#if primaryKey == field.dbName>
                        <#assign isPrimaryKey = true>
                        <#break>
                    </#if>
                </#list>
            </#if>

            <#if !isPrimaryKey>
            <#if field.type == "longblob" || field.type == "blob">
            if (entity.get${field.name?cap_first}() != null) {
            <#else>
            if(StringUtils.isNotBlank(entity.get${field.name?cap_first}())){
            </#if>
                sql.append("${field.dbName}<#switch field.type>
                <#case "datetime">
                    <#lt>=str_to_date(?,'%Y-%m-%d %H:%i:%s')<#break>
                <#default>
                    <#lt>=?<#break>
                </#switch>,");
                params.add(entity.get${field.name?cap_first}());
            }
            </#if>
            </#list>
            sql.deleteCharAt(sql.length() - 1);
            sql.append(" WHERE <#if dbPrimaryKeys??><#list dbPrimaryKeys as primaryKey>${primaryKey}=?<#if primaryKey_has_next> and </#if></#list><#else>1=2</#if>");
            <#if primaryKeys??>
            <#list primaryKeys as primaryKey>
            params.add(entity.get${primaryKey?cap_first}());
            </#list>
            </#if>
            logger.info(sql.toString());
            rowsAffected = jdbcTemplate.update(sql.toString(), params.toArray());
        } catch (DataAccessException e) {
            e.printStackTrace();
            logger.error("更新${tableName}错误：{}", e.getMessage());
            throw new SysException("10000", "更新${tableName}错误", e);
        }
        return rowsAffected;
    }

    @Override
    public int delete(${name} entity) throws SysException {
        int rowsAffected;
        StringBuilder sql = new StringBuilder();
        <#assign hasSts = false>
        <#assign hasUpdateTime = false>
        <#if primaryKeyFields??>
             <#list primaryKeyFields as primaryKey>
        // 判断主键是否为空
        if (StringUtils.isBlank(entity.get${primaryKey.name?cap_first}())){
            throw new SysException("100001", "删除${tableName}错误：主键${primaryKey.name?cap_first}为空", null);
        }
            </#list>
        </#if>
        <#list fields as field>
            <#if field.dbName == "status">
                <#assign hasSts = true>
            <#elseif field.dbName == "update_time">
                <#assign hasUpdateTime = true>
            </#if>
        </#list>
        <#if hasSts>
        sql.append("UPDATE ${tableName} ");
        sql.append("SET status=?<#if hasUpdateTime>,update_time=str_to_date(?,'%Y-%m-%d %H:%i:%s')</#if> WHERE <#if dbPrimaryKeys??><#list dbPrimaryKeys as primaryKey>${primaryKey}=?<#if primaryKey_has_next> AND </#if></#list><#else>1=2</#if>");
        <#else>
        sql.append("DELETE FROM ${tableName} WHERE <#if dbPrimaryKeys??><#list dbPrimaryKeys as primaryKey>${primaryKey}=?<#if primaryKey_has_next> AND </#if></#list><#else>1=2</#if>");
        </#if>

        try {
            logger.info(sql.toString());
            rowsAffected = jdbcTemplate.update(sql.toString()<#if hasSts || dbPrimaryKeys??>,<#else>);</#if>
            <#if hasSts>
                   entity.getStatus()<#if hasUpdateTime || dbPrimaryKeys??>,<#else>);</#if>
            </#if>
            <#if hasUpdateTime>
                   entity.getUpdateTime()<#if dbPrimaryKeys??>,<#else>);</#if>
            </#if>
            <#if primaryKeys??>
                <#list primaryKeys as primaryKey>
                   entity.get${primaryKey?cap_first}()<#if primaryKey_has_next>,<#else>);</#if>
                </#list>
            </#if>
        } catch (DataAccessException e) {
            logger.error("删除${tableName}错误：{}", e.getMessage());
            throw new SysException("10000", "删除${tableName}错误", e);
        }
        return rowsAffected;
    }

    @Override
    public List<${name}> queryList(${name} entity) throws SysException {
    StringBuffer sql = new StringBuffer();
        sql.append("SELECT <#list fields as field><#if field.type == "datetime">date_format(${field.dbName},'%Y-%m-%d %H:%i:%s')${field.dbName}<#else>${field.dbName}</#if><#if field_has_next>,</#if></#list> ");
        sql.append("FROM ${tableName} ");
        sql.append("WHERE 1=1");

        List<${name}> resultList = null;
        List<Object> params = new ArrayList<Object>();
        try {
            if (entity != null) {
            <#list fields as field>
            <#if field.type == "longblob" || field.type == "blob">
                if (entity.get${field.name?cap_first}() != null) {
            <#else>
                if (StringUtils.isNotBlank(entity.get${field.name?cap_first}())) {
            </#if>
                    sql.append(" AND ${field.dbName}<#if field.dbName?upper_case == "REMARK"> like ? ");
                    params.add("%" + entity.get${field.name?cap_first}() + "%");
                    <#else><#switch field.type>
                    <#case "datetime"><#lt>=str_to_date(?,'%Y-%m-%d %H:%i:%s')<#break>
                    <#default><#lt>=?</#switch>");
                    params.add(entity.get${field.name?cap_first}());
                    </#if>
                }
            </#list>
            } else {
                sql.append(" AND 1=2");
            }
            logger.info(sql.toString());
            resultList = jdbcTemplate.query(sql.toString(),
                    params.toArray(),
                    new BeanPropertyRowMapper<${name}>(${name}.class));
        } catch (DataAccessException e) {
            logger.error("查询${tableName}错误：{}", e.getMessage());
            throw new SysException("10000", "查询${tableName}错误", e);
        }
        return resultList;
    }

    @Override
    public ${name} queryBean(${name} entity) throws SysException {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT <#list fields as field><#if field.dbName == "SHARDING_ID"><#assign isShardingId = true></#if><#if field.type == "datetime">DATE_FORMAT(${field.dbName},'%Y-%m-%d %H:%i:%s')${field.dbName}<#else>${field.dbName}</#if><#if field_has_next>,</#if></#list> ");
        sql.append("FROM ${tableName} ");
        sql.append("WHERE 1=1");

        List<Object> params = new ArrayList<Object>();
        try {
            if (entity != null) {
            <#if primaryKeyFields??>
                <#list primaryKeyFields as field>
                if (StringUtils.isBlank(entity.get${field.name?cap_first}())){
                    throw new SysException("100001", "根据主键查询${tableName?upper_case}错误：主键${field.dbName}为空", null);
                }
                sql.append(" AND ${field.dbName}=?");
                params.add(entity.get${field.name?cap_first}());
                </#list>
            <#else>
                sql.append(" AND 1=2");
            </#if>
            } else {
                sql.append(" AND 1=2");
            }
            logger.info(sql.toString());
            entity = jdbcTemplate.queryForObject(sql.toString(),
                    params.toArray(),
                new BeanPropertyRowMapper<${name}>(${name}.class));
        } catch (Exception e) {
            logger.error("查询${tableName}错误：{}", e.getMessage());
            throw new SysException("10000", "查询${tableName}错误", e);
        }
        return entity;
    }
}