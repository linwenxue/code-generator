package ${package}.service.impl;

import com.yongche.etl.base.BaseService;
import ${package}.dao.I${name}Dao;
import ${package}.entity.${name};
import ${package}.service.I${name}Service;
import com.yongche.etl.util.SysException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ${name}Service extends BaseService implements I${name}Service {
    @Autowired
    private I${name}Dao ${name?uncap_first}Dao;

    @Override
    @Transactional
    public ${name} insert(${name} entity) throws SysException {
        entity.setStatus(STS_NORMAL);
        return ${name?uncap_first}Dao.insert(entity);
    }

    @Override
    @Transactional
    public int update(${name} entity) throws SysException {
        entity.setStatus(STS_NORMAL);
        return ${name?uncap_first}Dao.update(entity);
    }

    @Override
    @Transactional
    public int delete(${name} entity) throws SysException {
        entity.setStatus(STS_HISTORY);
        return ${name?uncap_first}Dao.delete(entity);
    }

    @Override
    @Transactional
    public List<${name}> queryList(${name} entity) throws SysException {
        entity.setStatus(STS_NORMAL);
        return ${name?uncap_first}Dao.queryList(entity);
    }

    @Override
    @Transactional
    public ${name} queryBean(${name} entity) throws SysException {
        entity.setStatus(STS_NORMAL);
        return ${name?uncap_first}Dao.queryBean(entity);
    }

    @Override
    @Transactional
    public int deleteBatch(List<${name}> entities) throws SysException {
        int count = 0;
        for(${name} e : entities) {
            e.setStatus(STS_HISTORY);
            count += ${name?uncap_first}Dao.delete(e);
        }
        return count;
    }
}