package com.epayslip.interview.base;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.incrementer.AbstractSequenceMaxValueIncrementer;

public class MarialDBSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

	private static final char DEFAULT_LEFTPAD_CHAR = '0';
	private static final String DATA_CENTER = "S";
	private static final String DATA_PREFIX = "L";
	private char leftPadStr;

	public MarialDBSequenceMaxValueIncrementer(DataSource dataSource, String seqName) {
		this(dataSource, seqName, 0, DEFAULT_LEFTPAD_CHAR);
	}

	public MarialDBSequenceMaxValueIncrementer(DataSource dataSource, String seqName, int paddingLength) {
		this(dataSource, seqName, paddingLength, DEFAULT_LEFTPAD_CHAR);
	}

	public MarialDBSequenceMaxValueIncrementer(DataSource dataSource, String seqName, int paddingLength, char leftPadStr) {
		super(dataSource, seqName);
		this.paddingLength = paddingLength;
		this.leftPadStr = leftPadStr;
	}

	@Override
	protected String getSequenceQuery() {
		return String.format("select NEXT_VAL('%s','%s')", getIncrementerName(), DATA_CENTER);
	}

	@Override
	public int nextIntValue() throws DataAccessException {
		throw new RuntimeException("Unsupport Int Value");
	}

	@Override
	public long nextLongValue() throws DataAccessException {
		throw new RuntimeException("Unsupport Long Value");
	}

	@Override
	public String nextStringValue() throws DataAccessException {
		String s = getNextValue();
		int len = s.length();
		String prefix = DATA_PREFIX;
		int prefixLen = prefix == null ? 0 : prefix.length();
		int totalLen = prefixLen + len;
		if (totalLen < this.paddingLength) {
			StringBuilder sb = new StringBuilder(this.paddingLength);
			for (int i = 0; i < this.paddingLength - totalLen; i++) {
				sb.append(leftPadStr);
			}
			sb.append(s);
			s = sb.toString();
		}
		s = String.format("%s%s", prefix, s);
		return s;
	}

	/**
	 * Executes the SQL as specified by {@link #getSequenceQuery()}.
	 */
	protected String getNextValue() throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			rs = stmt.executeQuery(getSequenceQuery());
			if (rs.next()) {
				return rs.getString(1);
			} else {
				throw new DataAccessResourceFailureException("Sequence query did not return a result");
			}
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException("Could not obtain sequence value", ex);
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.releaseConnection(con, getDataSource());
		}
	}

}
