package com.epayslip.interview.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.epayslip.interview.base.CommonRowMapper;
import com.epayslip.interview.base.InterviewBaseDAO;
import com.epayslip.interview.po.EpayFile;

@Repository("epayFileDAO")
public class EpayFileDAO extends InterviewBaseDAO {
	private static final String TABLE_NAME = "epay_file";
	private static final String COLUMN = "file_id,path,file_name,size,file_type,last_modified";
	private static CommonRowMapper<EpayFile> epayFileRowMapper = new CommonRowMapper<>(EpayFile.class);

	@Override
	public String getColumn() {
		return COLUMN;
	}

	public boolean existingData() {
		String querySql = String.format("select count(1) from %s", TABLE_NAME);
		return getJdbcTemplate().queryForObject(querySql, Integer.class) > 0;
	}

	public List<EpayFile> getList() {
		String querySql = String.format("select %s from %s", this.getColumn(), TABLE_NAME);
		return getJdbcTemplate().query(querySql, epayFileRowMapper);
	}

	public EpayFile getById(String fileId) {
		String querySql = String.format("select %s from %s where file_id = ?", this.getColumn(), TABLE_NAME);
		List<EpayFile> results = getJdbcTemplate().query(querySql, epayFileRowMapper, fileId);
		return this.getFirst(results);
	}
}
