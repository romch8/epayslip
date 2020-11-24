package com.epayslip.interview.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

public class InterviewBaseDAO extends BaseDAO {

	private static Map<String, DataFieldMaxValueIncrementer> seqIncrementerMap = new ConcurrentHashMap<>();
	
	@Resource(name = "jdbcTemplate")
	protected JdbcTemplate jdbcTemplate;

	@Resource(name = "txManager")
	protected PlatformTransactionManager txManager;

	@Resource(name = "txDefinition")
	protected TransactionDefinition txDefinition;

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public PlatformTransactionManager getTxManager() {
		return txManager;
	}

	@Override
	public TransactionDefinition getTxDefinition() {
		return txDefinition;
	}

	@Override
	public String getColumn() {
		return "*";
	}
	
	@Override
	public Map<String, DataFieldMaxValueIncrementer> getSeqIncrementerMap() {
		return seqIncrementerMap;
	}

}
