package com.epayslip.interview.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

import com.epayslip.interview.exception.NoDataFoundException;
import com.epayslip.interview.util.IdUtils;
import com.epayslip.interview.util.MapperUtil;
import com.epayslip.interview.util.PagingUtils;

public abstract class BaseDAO {
	private static final Logger LOG = LoggerFactory.getLogger(BaseDAO.class);
	private static final char DEFAULT_LEFTPAD_CHAR = '0';
	public static final int MAX_IN_SQL_PARAM = 1000;

	/**
	 * Child Class should tell which location's data source should be using
	 * 
	 * @return
	 */
	public abstract JdbcTemplate getJdbcTemplate();

	/**
	 * Child Class should tell which location's PlatformTransactionManager
	 * should be using
	 * 
	 * @return
	 */
	public abstract PlatformTransactionManager getTxManager();

	/**
	 * Child Class should tell which location's TransactionDefinition should be
	 * using
	 * 
	 * @return
	 */
	public abstract TransactionDefinition getTxDefinition();

	/**
	 * Get Table Columns, Split by comma
	 * 
	 * @return
	 */
	public abstract String getColumn();

	/**
	 * Insert Entity and set PK value to entity, return if create success
	 * 
	 * @param entity
	 * @param pkeyFieldName
	 * @return
	 */
	public boolean createEntity(Object entity, String pkeyFieldName) {
		Map<String, Object> map = MapperUtil.fillMap(entity);
		map.remove(pkeyFieldName);
		String tableName = MapperUtil.unCapitalize(entity.getClass().getSimpleName());
		SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(getJdbcTemplate()).withTableName(tableName);
		simpleJdbcInsert.setGeneratedKeyName(MapperUtil.unCapitalize(pkeyFieldName));
		Set<String> var = map.keySet();
		simpleJdbcInsert.usingColumns(var.toArray(new String[var.size()]));
		long pkey = simpleJdbcInsert.executeAndReturnKey(map).longValue();
		try {
			PropertyUtils.setProperty(entity, pkeyFieldName, pkey);
		} catch (Exception e) {
			LOG.error("set pkey to bean error", e);
			return false;
		}
		return true;
	}

	/**
	 * Batch Create Entities, will return impact row array<br/>
	 * <br/>
	 * <b>Note: not support blob</b>
	 * 
	 * @param entityList
	 * @return
	 */
	public <T> int[] batchCreateEntity(Collection<T> entityList) {
		if (CollectionUtils.isEmpty(entityList)) {
			return null;
		}

		SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(getJdbcTemplate());
		simpleJdbcInsert.withTableName(MapperUtil.unCapitalize(entityList.iterator().next().getClass().getSimpleName()));
		SqlParameterSource[] sqlParameterSource = getSqlParams(entityList);
		return simpleJdbcInsert.executeBatch(sqlParameterSource);
	}

	private <T> SqlParameterSource[] getSqlParams(Collection<T> coll) {
		SqlParameterSource[] batch = new SqlParameterSource[coll.size()];
		Iterator<T> itr = coll.iterator();
		int i = 0;
		while (itr.hasNext()) {
			T entity = itr.next();
			BeanPropertySqlParameterSource sqlParam = new BeanPropertySqlParameterSource(entity);
			batch[i++] = sqlParam;
		}
		return batch;
	}

	/**
	 * Get Entity By PK
	 * 
	 * @param pkName
	 *            java PK filed name, not db column name
	 * @param pkValue
	 *            java PK filed value
	 * @param clazz
	 * @return
	 */
	public <T> T getByPK(String pkName, Object pkValue, Class<T> clazz) {
		try {
			String tableName = MapperUtil.unCapitalize(clazz.getSimpleName());
			String pkColumnName = MapperUtil.unCapitalize(pkName);
			String querySql = String.format("select %s from %s where %s=?", this.getColumn(), tableName, pkColumnName);
			return (T) getJdbcTemplate().queryForObject(querySql, new Object[] { pkValue }, new CommonRowMapper<T>(clazz));
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/**
	 * Get Entity By Compose PK
	 * 
	 * @param pkNameValueMap
	 *            <br/>
	 *            key - java PK filed name, not db column name<br/>
	 *            value - java PK filed value<br/>
	 * @param clazz
	 * @return
	 */
	public <T> T getByComposePK(Map<String, Object> pkNameValueMap, Class<T> clazz) {
		try {
			String tableName = MapperUtil.unCapitalize(clazz.getSimpleName());
			StringBuffer sqlAppender = new StringBuffer("select ");
			sqlAppender.append(this.getColumn());
			sqlAppender.append(" from ");
			sqlAppender.append(tableName);
			sqlAppender.append(" where ");

			List<Object> params = new ArrayList<>();
			Iterator<Entry<String, Object>> iterator = pkNameValueMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();
				String pkName = entry.getKey();
				String pkColumnName = MapperUtil.unCapitalize(pkName);
				sqlAppender.append(pkColumnName).append(" = ?").append(" and ");
				params.add(entry.getValue());
			}
			sqlAppender.delete(sqlAppender.length() - 5, sqlAppender.length());

			return (T) getJdbcTemplate().queryForObject(sqlAppender.toString(), params.toArray(), new CommonRowMapper<T>(clazz));
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/**
	 * get result list by pagination.
	 * 
	 * @param sql
	 * @param params
	 * @param clazz
	 *            result class
	 * @param currentPage
	 *            query start page
	 * @param numPerPage
	 *            page size
	 * @return
	 * @throws DataAccessException
	 */
	public <T> Pagination<T> queryForPage(String sql, Object[] params, Class<T> clazz, Integer currentPage, Integer numPerPage) {

		if (params == null)
			params = new Object[] {};

		Pagination<T> page = PagingUtils.getPagination(currentPage, numPerPage);

		try {

			StringBuffer totalSQL = new StringBuffer(" SELECT count(*) FROM ( ");
			totalSQL.append(sql);
			totalSQL.append(" ) totalTable ");
			page.setTotalRows(getJdbcTemplate().queryForObject(totalSQL.toString(), params, Integer.class));

			page.setTotalPages();
			page.setStartIndex();
			page.setLastIndex();

			if (page.getCurrentPage() <= 1) {
				sql = PagingUtils.getPagingQuery(sql, false);
				params = ArrayUtils.add(params, params.length, page.getLastIndex());
			} else {
				sql = PagingUtils.getPagingQuery(sql, true);
				params = ArrayUtils.add(params, params.length, page.getLastIndex());
				params = ArrayUtils.add(params, params.length, page.getStartIndex());
			}

			List<T> resultList = getJdbcTemplate().query(sql, params, new CommonRowMapper<>(clazz));
			if (CollectionUtils.isEmpty(resultList)) {
				page.setResultList(new ArrayList<T>());
			} else {
				page.setResultList(resultList);
			}
			return page;

		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object p : params) {
				sb.append(p + " | ");
			}
			sb.append("]");
			LOG.error("Error SQL: " + sql + " Params: " + sb.toString());
			throw e;
		}
	}

	public String getLikeSqlPart(String likeVal) {
		return StringUtils.join("%", likeVal, "%");
	}

	public String getRightLikeSqlPart(String likeVal) {
		return StringUtils.join(likeVal, "%");
	}

	public String getLeftLikeSqlPart(String likeVal) {
		return StringUtils.join("%", likeVal);
	}

	/**
	 * Get In SQL Part, <br/>
	 * if size is 1,return " = ?"<br/>
	 * if size more than 1,return " in (?, ? ... ?)"<br/>
	 * 
	 * using getInSqlPart(int size, String columnName) instead
	 * 
	 * @param size
	 * @return
	 */
	@Deprecated
	public String getInSqlPart(int size) {
		return getInSqlPart(size, false);
	}

	public String getInSqlPart(int size, String columnName) {
		return getInSqlPart(size, columnName, false);
	}

	public String getNotInSqlPart(int size, String columnName) {
		return getInSqlPart(size, columnName, true);
	}

	protected String getInSqlPart(int size, boolean notInFlag) {
		StringBuilder sb = new StringBuilder();
		if (size == 1) {
			if (notInFlag) {
				sb.append(" <> ? ");
			} else {
				sb.append(" = ? ");
			}

		} else if (size > 1) {
			if (notInFlag) {
				sb.append(" not in (").append(this.getInPlaceHolder(size)).append(") ");
			} else {
				sb.append(" in (").append(this.getInPlaceHolder(size)).append(") ");
			}
		}
		return sb.toString();
	}

	public String getInSqlPartWithoutAnd(int size, String columnName) {
		return getInSqlPartWithoutAnd(size, columnName, false);
	}

	public String getInSqlPartWithoutAnd(int size, String columnName, boolean notInFlag) {
		if (size < 1) {
			return "";
		}

		if (size <= MAX_IN_SQL_PARAM) {
			return String.format("%s%s", columnName, getInSqlPart(size, notInFlag));
		}

		StringBuilder sb = new StringBuilder("(");
		int totalSize = size;
		int totalPages = PagingUtils.getTotalPages(totalSize, MAX_IN_SQL_PARAM);

		for (int i = 0; i < totalPages; i++) {
			if (i == totalPages - 1) {
				sb.append(columnName).append(getInSqlPart(totalSize - MAX_IN_SQL_PARAM * (totalPages - 1), notInFlag));
			} else {
				sb.append(columnName).append(getInSqlPart(MAX_IN_SQL_PARAM, notInFlag));
				sb.append("or ");
			}
		}
		sb.append(") ");
		return sb.toString();
	}

	protected String getInSqlPart(int size, String columnName, boolean notInFlag) {
		if (size < 1) {
			return "";
		}

		return String.format(" and %s", getInSqlPartWithoutAnd(size, columnName, notInFlag));
	}

	private String getInPlaceHolder(int size) {
		StringBuffer sb = new StringBuffer("?");
		for (int i = 1; i < size; i++) {
			sb.append(", ?");
		}
		return sb.toString();
	}

	public <T> List<T> list(String sql, Object[] params, Class<T> clazz) {
		CommonRowMapper<T> commonRowMapper = new CommonRowMapper<T>(clazz);
		return list(sql, params, commonRowMapper);
	}

	public <T> List<T> list(String sql, Object[] params, CommonRowMapper<T> commonRowMapper) {
		// call jdbcTemplate to query for result
		List<T> list = null;
		if (params == null || params.length == 0) {
			list = query(sql, commonRowMapper);
		} else {
			list = query(sql, params, commonRowMapper);
		}
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> T load(Object po) throws NoDataFoundException {

		// turn class to sql
		SqlParamsPairs sqlAndParams = null;
		try {
			sqlAndParams = PagingUtils.getLoadFromObject(po);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		// query for list
		List list = this.list(sqlAndParams.getSql(), sqlAndParams.getParams(), po.getClass());
		if (CollectionUtils.isEmpty(list)) {
			return (T) list.get(0);
		} else {
			throw new NoDataFoundException(po.getClass());
		}
	}

	public boolean update(Object po) {
		SqlParamsPairs sqlAndParams = null;
		try {
			sqlAndParams = PagingUtils.getUpdateFromObject(po);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		return update(sqlAndParams.getSql(), sqlAndParams.getParams()) > 0;
	}

	public void batchUpdate(String sql, List<Object[]> paramsList) {

		BatchUpdateSetter batchUpdateSetter = new BatchUpdateSetter(paramsList);

		try {
			getJdbcTemplate().batchUpdate(sql, batchUpdateSetter);
		} catch (Exception e) {
			LOG.error("Error SQL: " + sql);
			throw e;
		}

	}

	public boolean createEntity(Object po) {

		try {
			String autoGeneratedColumnName = IdUtils.getAutoGeneratedId(po);
			if (!"".equals(autoGeneratedColumnName)) {
				Object idValue = save(po, autoGeneratedColumnName);
				if (idValue == null) {
					return false;
				}
				IdUtils.setAutoIncreamentIdValue(po, autoGeneratedColumnName, idValue);
			} else {
				SqlParamsPairs sqlAndParams = PagingUtils.getInsertFromObject(po);
				int count = update(sqlAndParams.getSql(), sqlAndParams.getParams());
				if (count != 1) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			LOG.error("Save {} po object have some issue .", po.getClass().getName());
			throw new RuntimeException(e);
		}
	}

	public boolean delete(Object po) {

		SqlParamsPairs sqlAndParams = null;
		try {
			sqlAndParams = PagingUtils.getDeleteFromObject(po);
		} catch (Exception e) {
			LOG.error("Delete {} po object have some issue .", po.getClass().getName());
			throw new RuntimeException(e);
		}

		String sql = sqlAndParams.getSql();

		return update(sql, sqlAndParams.getParams()) > 0;

	}

	private int update(String sql, Object[] params) throws DataAccessException {
		try {
			return getJdbcTemplate().update(sql, params);
		} catch (DataAccessException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object p : params) {
				sb.append(p + " | ");
			}
			sb.append("]");
			LOG.error("Error SQL: " + sql + " Params: " + sb.toString());
			throw e;
		}
	}

	private <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		try {
			return getJdbcTemplate().query(sql, rowMapper);
		} catch (DataAccessException e) {
			LOG.error("Error SQL: " + sql);
			throw e;
		}
	}

	private <T> List<T> query(String sql, Object[] params, RowMapper<T> rowMapper) throws DataAccessException {
		try {
			return getJdbcTemplate().query(sql, params, rowMapper);
		} catch (DataAccessException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object p : params) {
				sb.append(p + " | ");
			}
			sb.append("]");
			LOG.error("Error SQL: " + sql + " Params: " + sb.toString());
			throw e;
		}
	}

	private Object save(Object po, String autoGeneratedColumnName) throws Exception {
		SqlParamsPairs sqlAndParams = PagingUtils.getInsertFromObject(po);
		String sql = sqlAndParams.getSql();
		return insert(sql, sqlAndParams.getParams(), autoGeneratedColumnName);
	}

	private Object insert(String sql, Object[] params, String autoGeneratedColumnName) throws DataAccessException {
		ReturnIdPreparedStatementCreator psc = new ReturnIdPreparedStatementCreator(sql, params, autoGeneratedColumnName);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			getJdbcTemplate().update(psc, keyHolder);
		} catch (DataAccessException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object p : params) {
				sb.append(p + " | ");
			}
			sb.append("]");
			LOG.error("Error SQL: " + sql + " Params: " + sb.toString());
			throw e;
		}

		return autoGeneratedKey(keyHolder);
	}

	private Object autoGeneratedKey(KeyHolder keyHolder) {
		List<Map<String, Object>> list = keyHolder.getKeyList();

		if (list.size() == 0) {
			return null;
		}
		if (list.size() > 1 || list.get(0).size() > 1) {
			throw new InvalidDataAccessApiUsageException(
					"The getKey method should only be used when a single key is returned.  " + "The current key entry contains multiple keys: " + list);
		}
		Iterator<Object> keyIter = list.get(0).values().iterator();
		if (keyIter.hasNext()) {
			Object key = keyIter.next();
			if (key instanceof Number) {
				return ((Number) key).longValue();
			} else if (key instanceof String) {
				return key;
			} else {
				throw new DataRetrievalFailureException("The generated key is not of a supported numeric type or string type. " + "Unable to cast ["
						+ (key != null ? key.getClass().getName() : null) + "] to [" + Number.class.getName() + "] or [" + String.class.getName() + "] ");
			}

		} else {
			throw new DataRetrievalFailureException("Unable to retrieve the generated key. " + "Check that the table has an identity column enabled.");
		}
	}

	/**
	 * Get First Element of Collection, if Collection is Empty will return null
	 * 
	 * @param results
	 * @return
	 */
	public <T> T getFirst(Collection<T> results) {
		if (CollectionUtils.isNotEmpty(results)) {
			return results.iterator().next();
		}
		return null;
	}

	public <T> T queryForFirst(Class<T> clazz, T defaultValue, String sql, Object... args) {
		List<T> list = getJdbcTemplate().queryForList(sql, clazz, args);
		return CollectionUtils.isEmpty(list) ? defaultValue : list.get(0);
	}

	public String queryForFirstString(String sql, Object... args) {
		return queryForFirst(String.class, StringUtils.EMPTY, sql, args);
	}

	public Map<String, Object> queryForCustomMap(String sql, final String key, final String value, Object... args) {
		final Map<String, Object> map = new HashMap<>();
		this.getJdbcTemplate().query(sql, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				map.put(rs.getString(key), rs.getString(value));
			}
		}, args);
		return map;
	}

	public Map<String, Object> queryForFirstMap(String sql, Object... args) {
		List<Map<String, Object>> list = this.getJdbcTemplate().queryForList(sql, args);
		return CollectionUtils.isEmpty(list) ? null : list.get(0);
	}

	public DataFieldMaxValueIncrementer getSeqIncrementer(String seqName) {
		return this.getSeqIncrementer(seqName, 0, DEFAULT_LEFTPAD_CHAR);
	}

	public DataFieldMaxValueIncrementer getSeqIncrementer(String seqName, int paddingLength) {
		return this.getSeqIncrementer(seqName, paddingLength, DEFAULT_LEFTPAD_CHAR);
	}

	public DataFieldMaxValueIncrementer getSeqIncrementer(String seqName, int paddingLength, char leftPadStr) {
		if (!getSeqIncrementerMap().containsKey(seqName)) {
			getSeqIncrementerMap().put(seqName,
					new MarialDBSequenceMaxValueIncrementer(this.getJdbcTemplate().getDataSource(), seqName, paddingLength, leftPadStr));
		}

		return getSeqIncrementerMap().get(seqName);
	}

	public abstract Map<String, DataFieldMaxValueIncrementer> getSeqIncrementerMap();

	/**
	 * Security Query SQL include keywords in, can avoid ORA-01795: maximum
	 * number of expressions in a list is 1000
	 * 
	 * @param sql
	 * @param rowMapper
	 * @param inParamStartIdx
	 *            in param start index value in args array
	 * @param args
	 * @return
	 * @throws DataAccessException
	 */
	public <T> List<T> inQuery(String sql, RowMapper<T> rowMapper, int inParamStartIdx, Object... args) throws DataAccessException {
		return inQuery(sql, args, rowMapper, inParamStartIdx, args.length - 1);
	}

	/**
	 * Security Query SQL include keywords in, can avoid ORA-01795: maximum
	 * number of expressions in a list is 1000
	 * 
	 * @param sql
	 * @param rowMapper
	 * @param inParamStartIdx
	 *            in param start index value in args array
	 * @param inParamEndIdx
	 *            in param end index value in args array
	 * @param args
	 * @return
	 * @throws DataAccessException
	 */
	public <T> List<T> inQuery(String sql, RowMapper<T> rowMapper, int inParamStartIdx, int inParamEndIdx, Object... args) throws DataAccessException {
		return inQuery(sql, args, rowMapper, inParamStartIdx, inParamEndIdx);
	}

	/**
	 * Security Query SQL include keywords in, can avoid ORA-01795: maximum
	 * number of expressions in a list is 1000
	 * 
	 * @param sql
	 * @param args
	 * @param rowMapper
	 * @param inParamStartIdx
	 *            in param start index value in args array
	 * @return
	 * @throws DataAccessException
	 */
	public <T> List<T> inQuery(String sql, Object[] args, RowMapper<T> rowMapper, int inParamStartIdx) throws DataAccessException {
		return inQuery(sql, args, rowMapper, inParamStartIdx, args.length - 1);
	}

	/**
	 * Security Query SQL include keywords in, can avoid ORA-01795: maximum
	 * number of expressions in a list is 1000
	 * 
	 * @param sql
	 * @param args
	 * @param elementType
	 * @param inParamStartIdx
	 *            in param start index value in args array
	 * @return
	 * @throws DataAccessException
	 */
	public <T> List<T> inQueryForList(String sql, Object[] args, Class<T> elementType, int inParamStartIdx) throws DataAccessException {
		return inQuery(sql, args, new SingleColumnRowMapper<T>(elementType), inParamStartIdx, args.length - 1);
	}

	/**
	 * Security Query SQL include keywords in, can avoid ORA-01795: maximum
	 * number of expressions in a list is 1000
	 * 
	 * @param sql
	 * @param args
	 * @param elementType
	 * @param inParamStartIdx
	 *            in param start index value in args array
	 * @param inParamEndIdx
	 *            in param end index value in args array
	 * @return
	 * @throws DataAccessException
	 */
	public <T> List<T> inQueryForList(String sql, Object[] args, Class<T> elementType, int inParamStartIdx, int inParamEndIdx) throws DataAccessException {
		return inQuery(sql, args, new SingleColumnRowMapper<T>(elementType), inParamStartIdx, inParamEndIdx);
	}

	/**
	 * Security Query SQL include keywords in, can avoid ORA-01795: maximum
	 * number of expressions in a list is 1000
	 * 
	 * @param sql
	 * @param requiredType
	 * @param inParamStartIdx
	 *            in param start index value in args array
	 * @param args
	 * @return
	 * @throws DataAccessException
	 */
	public <T> T inQueryForObject(String sql, Class<T> requiredType, int inParamStartIdx, Object... args) throws DataAccessException {
		List<T> results = inQuery(sql, args, new SingleColumnRowMapper<T>(requiredType), inParamStartIdx, args.length - 1);
		return this.getFirst(results);
	}

	/**
	 * Security Query SQL include keywords in, can avoid ORA-01795: maximum
	 * number of expressions in a list is 1000
	 * 
	 * @param sql
	 * @param requiredType
	 * @param inParamStartIdx
	 *            in param start index value in args array
	 * @param inParamEndIdx
	 *            in param end index value in args array
	 * @param args
	 * @return
	 * @throws DataAccessException
	 */
	public <T> T inQueryForObject(String sql, Class<T> requiredType, int inParamStartIdx, int inParamEndIdx, Object... args) throws DataAccessException {
		List<T> results = inQuery(sql, args, new SingleColumnRowMapper<T>(requiredType), inParamStartIdx, inParamEndIdx);
		return this.getFirst(results);
	}

	/**
	 * Security Query SQL include keywords in, can avoid ORA-01795: maximum
	 * number of expressions in a list is 1000
	 * 
	 * @param sql
	 * @param args
	 * @param rowMapper
	 * @param inParamStartIdx
	 *            in param start index value in args array
	 * @param inParamEndIdx
	 *            in param end index value in args array
	 * @return
	 * @throws DataAccessException
	 */
	public <T> List<T> inQuery(String sql, Object[] args, RowMapper<T> rowMapper, int inParamStartIdx, int inParamEndIdx) throws DataAccessException {
		if (args.length <= MAX_IN_SQL_PARAM) {
			return this.getJdbcTemplate().query(sql, args, rowMapper);
		}

		List<Object> inParamList = new ArrayList<>();
		for (int i = inParamStartIdx; i <= inParamEndIdx; i++) {
			inParamList.add(args[i]);
		}

		StringBuilder sb = new StringBuilder();
		int paramIndex = 0;
		int paramCount = 0;
		while ((paramIndex = sql.indexOf("?")) > 0) {
			if (paramCount <= inParamStartIdx) {
				sb.append(sql.substring(0, paramIndex + 1));
				sql = sql.substring(paramIndex + 1);
			} else if (paramCount <= inParamEndIdx) {
				sql = sql.substring(paramIndex + 1);
			} else {
				break;
			}
			paramCount++;
		}

		String sqlBeforeInPart = sb.delete(sb.lastIndexOf(" in ("), sb.length()).toString();
		String sqlAfterInPart = sql.substring(sql.indexOf(")") + 1);
		return combineResult(args, rowMapper, inParamStartIdx, inParamEndIdx, inParamList, sqlBeforeInPart, sqlAfterInPart);
	}

	private <T> List<T> combineResult(Object[] args, RowMapper<T> rowMapper, int inParamStartIdx, int inParamEndIdx, List<Object> inParamList,
			String sqlBeforeInPart, String sqlAfterInPart) {
		List<T> resultList = new ArrayList<>();
		int totalSize = inParamList.size();
		int totalPages = PagingUtils.getTotalPages(totalSize, MAX_IN_SQL_PARAM);

		for (int i = 0; i < totalPages; i++) {
			int start = i * MAX_IN_SQL_PARAM;
			int end = (i + 1) * MAX_IN_SQL_PARAM;
			if (i == totalPages - 1) {
				end = totalSize;
			}

			StringBuilder sqlBuilder = new StringBuilder();
			List<Object> queryParams = new ArrayList<>();

			// part1
			sqlBuilder.append(sqlBeforeInPart);
			for (int j = 0; j < inParamStartIdx; j++) {
				queryParams.add(args[j]);
			}

			// part2
			List<Object> subList = inParamList.subList(start, end);
			sqlBuilder.append(this.getInSqlPart(subList.size()));
			queryParams.addAll(subList);

			// part3
			sqlBuilder.append(sqlAfterInPart);
			for (int j = inParamEndIdx + 1; j < args.length; j++) {
				queryParams.add(args[j]);
			}

			String querySql = sqlBuilder.toString();
			LOG.debug("In Query SQL is : {}", querySql);
			List<T> partResultList = this.getJdbcTemplate().query(querySql, queryParams.toArray(), rowMapper);
			resultList.addAll(partResultList);
		}
		return resultList;
	}

	protected <T> Pagination<T> mysqlQueryForPage(String sql, Object[] params, Class<T> clazz, Integer currentPage, Integer numPerPage) {
		if (params == null)
			params = new Object[] {};

		Pagination<T> page = PagingUtils.getPagination(currentPage, numPerPage);
		try {

			StringBuilder totalSQL = new StringBuilder(" SELECT count(*) FROM ( ");
			totalSQL.append(sql);
			totalSQL.append(" ) totalTable ");
			page.setTotalRows(getJdbcTemplate().queryForObject(totalSQL.toString(), params, Integer.class));
			page.setTotalPages();

			sql = PagingUtils.getMySqlPagingQuery(sql);
			params = ArrayUtils.add(params, params.length, (currentPage - 1) * numPerPage);
			params = ArrayUtils.add(params, params.length, numPerPage);

			List<T> resultList = null;
			if (clazz.equals(String.class) || clazz.equals(Integer.class)) {
				resultList = getJdbcTemplate().queryForList(sql, params, clazz);
			} else {
				resultList = getJdbcTemplate().query(sql, params, new CommonRowMapper<>(clazz));
			}
			if (CollectionUtils.isEmpty(resultList)) {
				page.setResultList(new ArrayList<T>());
			} else {
				page.setResultList(resultList);
			}
			return page;
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object p : params) {
				sb.append(p + " | ");
			}
			sb.append("]");
			LOG.error("Error SQL: " + sql + " Params: " + sb.toString());
			throw e;
		}
	}

	public <R> R querySilently(Function<? super JdbcTemplate, ? extends R> arg0, R defaultValue) {
		try {
			return arg0.apply(getJdbcTemplate());
		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
			return defaultValue;
		}
	}

	public <R> R querySilently(Function<? super JdbcTemplate, ? extends R> arg0) {
		return querySilently(arg0, null);
	}
}
