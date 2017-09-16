package com.caas.dao;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.yzx.db.dao.BaseDao;
/**
 * 
 * @author xupiao 2017年8月16日
 *
 */
@Repository
public class CaasDao extends BaseDao {

	@Override
	@Resource(name = "caas_sqlSessionTemplate")
	protected void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}
	
}
