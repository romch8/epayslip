package com.epayslip.interview.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epayslip.interview.base.NoIdAnnotationFoundException;
import com.epayslip.interview.base.Pagination;
import com.epayslip.interview.base.SqlParamsPairs;

public class PagingUtils {
	private static Logger logger = LoggerFactory.getLogger(PagingUtils.class);

	public static <T> Pagination<T> getPagination(Integer currentPage, Integer numPerPage) {
		Pagination<T> page = new Pagination<T>();

		if (currentPage == null || currentPage < 1) {
			page.setCurrentPage(Pagination.DEFAULT_CURRENT_PAGE);
		} else {
			page.setCurrentPage(currentPage);
		}

		if (numPerPage == null || numPerPage < 1) {
			page.setNumPerPage(Pagination.DEFAULT_PER_PAGE);
		} else {
			page.setNumPerPage(numPerPage);
		}

		return page;
	}

	public static String getPagingQuery(String sql, boolean hasOffset) {
		sql = sql.trim();
		boolean isForUpdate = false;
		if (sql.toLowerCase().endsWith(" for update")) {
			sql = sql.substring(0, sql.length() - 11);
			isForUpdate = true;
		}

		StringBuffer pagingSelect = new StringBuffer(sql.length() + 100);
		if (hasOffset) {
			pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		} else {
			pagingSelect.append("select * from ( ");
		}
		pagingSelect.append(sql);
		if (hasOffset) {
			pagingSelect.append(" ) row_ where rownum <= ?) where rownum_ >= ?");
		} else {
			pagingSelect.append(" ) where rownum <= ?");
		}

		if (isForUpdate) {
			pagingSelect.append(" for update");
		}
		return pagingSelect.toString();
	}

	public static String getMySqlPagingQuery(String sql) {
		sql = sql.trim();
		boolean isForUpdate = false;
		if (sql.toLowerCase().endsWith(" for update")) {
			sql = sql.substring(0, sql.length() - 11);
			isForUpdate = true;
		}

		StringBuilder pagingSelect = new StringBuilder();
		pagingSelect.append(sql);
		pagingSelect.append(" limit ?,?");

		if (isForUpdate) {
			pagingSelect.append(" for update");
		}
		return pagingSelect.toString();
	}

	/**
	 * Calculate how many page when do pagination
	 * 
	 * @param totalSize
	 * @param pageSize
	 * @return
	 */
	public static int getTotalPages(double totalSize, int pageSize) {
		double totalPages = totalSize / pageSize;
		return (int) Math.ceil(totalPages);
	}

	public static <T> SqlParamsPairs getInsertFromObject(T po) throws Exception {

		StringBuilder insertSql = new StringBuilder();

		StringBuilder paramsSql = new StringBuilder();

		List<Object> params = new ArrayList<Object>();

		String tableName = getTableName(po.getClass());

		insertSql.append("insert into " + tableName + " (");

		int count = 0;

		Field[] fields = po.getClass().getDeclaredFields();

		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()) || null != f.getAnnotation(Transient.class)) {
				continue;
			}

			Method getter = getGetter(po.getClass(), f);

			if (getter == null) {
				continue;
			}

			Object value = getter.invoke(po);
			if (value == null) {
				continue;
			}

			Transient tranAnno = getter.getAnnotation(Transient.class);
			if (tranAnno != null) {
				continue;
			}

			String columnName = getColumnNameFromGetter(getter, f);

			if (count != 0) {
				insertSql.append(",");
			}
			insertSql.append(columnName);

			if (count != 0) {
				paramsSql.append(",");
			}
			paramsSql.append("?");

			params.add(value);
			count++;
		}

		insertSql.append(") values (");
		insertSql.append(paramsSql + ")");

		SqlParamsPairs sqlAndParams = new SqlParamsPairs(insertSql.toString(), params.toArray());
		logger.debug(sqlAndParams.toString());

		return sqlAndParams;

	}

	private static <T> Method getGetter(Class<T> clazz, Field f) {
		String getterName = "get" + ColnumNameUtils.capitalize(f.getName());
		Method getter = null;
		try {
			getter = clazz.getMethod(getterName);
		} catch (Exception e) {
			logger.debug(getterName + " doesn't exist!", e);
			if (f.getType().equals(boolean.class)) {
				getterName = "is" + ColnumNameUtils.capitalize(f.getName());
				try {
					getter = clazz.getMethod(getterName);
				} catch (Exception e1) {
					logger.debug(getterName + " doesn't exist!", e1);
				}
			}
		}
		return getter;
	}

	private static <T> String getTableName(Class<T> clazz) {

		Table tableAnno = clazz.getAnnotation(Table.class);
		if (tableAnno != null) {
			if (!StringUtils.isBlank(tableAnno.catalog())) {
				return tableAnno.catalog() + "." + tableAnno.name();
			}
			return tableAnno.name();
		}
		// if Table annotation is null
		String className = clazz.getName();

		return MapperUtil.unCapitalize(className.substring(className.lastIndexOf(".") + 1));
	}

	public static SqlParamsPairs getUpdateFromObject(Object po) throws Exception {

		StringBuilder updateSql = new StringBuilder();

		StringBuilder whereSql = new StringBuilder();

		List<Object> params = new ArrayList<>();
		List<Object> idParams = new ArrayList<>();

		String tableName = getTableName(po.getClass());

		updateSql.append(" update " + tableName + " set ");

		Field[] fields = po.getClass().getDeclaredFields();

		int count = 0;
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()) || null != f.getAnnotation(Transient.class)) {
				continue;
			}

			Method getter = getGetter(po.getClass(), f);

			if (getter == null) {
				continue;
			}

			Object value = getter.invoke(po);

			Transient tranAnno = getter.getAnnotation(Transient.class);
			if (tranAnno != null) {
				continue;
			}

			String columnName = getColumnNameFromGetter(getter, f);

			Id idAnno = getter.getAnnotation(Id.class);
			if (idAnno != null) {
				if (whereSql.length() > 0) {
					whereSql.append("and");
				}
				whereSql.append(" " + columnName + " = ? ");
				idParams.add(value);
				continue;
			}

			params.add(value);

			if (count != 0) {
				updateSql.append(",");
			}
			updateSql.append(" " + columnName + " = ? ");

			count++;
		}

		updateSql.append(" where ");

		if (whereSql.length() == 0) {
			throw new NoIdAnnotationFoundException(po.getClass());
		} else {
			updateSql.append(whereSql);
		}

		params.addAll(idParams);

		SqlParamsPairs sqlAndParams = new SqlParamsPairs(updateSql.toString(), params.toArray());
		logger.debug(sqlAndParams.toString());

		return sqlAndParams;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static SqlParamsPairs getDeleteFromObject(Object po) throws Exception {

		StringBuilder deleteSql = new StringBuilder();
		StringBuilder whereSql = new StringBuilder();

		List<Object> params = new ArrayList<Object>();

		String tableName = getTableName(po.getClass());

		deleteSql.append("delete from " + tableName + " where ");

		Class clazz = po.getClass();

		Field[] fields = clazz.getDeclaredFields();

		Id idAnno = null;
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];

			Method getter = getGetter(clazz, f);

			if (getter == null) {
				continue;
			}

			idAnno = getter.getAnnotation(Id.class);
			if (idAnno == null) {
				continue;
			}

			String columnName = getColumnNameFromGetter(getter, f);

			if (whereSql.length() > 0) {
				whereSql.append("and");
			}
			whereSql.append(" " + columnName + " = ? ");

			params.add(getter.invoke(po, new Object[] {}));

		}

		if (whereSql.length() == 0) {
			throw new NoIdAnnotationFoundException(clazz);
		} else {
			deleteSql.append(whereSql);
		}

		SqlParamsPairs sqlAndParams = new SqlParamsPairs(deleteSql.toString(), params.toArray());
		logger.debug(sqlAndParams.toString());

		return sqlAndParams;

	}

	public static SqlParamsPairs getLoadFromObject(Object po) throws Exception {
		Class<?> clazz = po.getClass();
		StringBuilder loadSql = new StringBuilder();
		StringBuilder whereSql = new StringBuilder();
		String tableName = getTableName(clazz);

		loadSql.append("select * from " + tableName + " where ");

		List<Object> params = new ArrayList<>();

		Field[] fields = clazz.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];

			Method getter = getGetter(clazz, f);

			if (getter == null) {
				continue;
			}

			if (getter.getAnnotation(Id.class) == null) {
				continue;
			}

			// get column name
			String columnName = getColumnNameFromGetter(getter, f);

			if (whereSql.length() > 0) {
				whereSql.append("and");
			}
			whereSql.append(" " + columnName + " = ? ");

			params.add(getter.invoke(po, new Object[] {}));

		}

		if (whereSql.length() == 0) {
			throw new NoIdAnnotationFoundException(clazz);
		} else {
			loadSql.append(whereSql);
		}

		SqlParamsPairs sqlAndParams = new SqlParamsPairs(loadSql.toString(), params.toArray());
		logger.debug(sqlAndParams.toString());

		return sqlAndParams;
	}

	/**
	 * use getter to guess column name, if there is annotation then use
	 * annotation value, if not then guess from field name
	 * 
	 * @param getter
	 * @param clazz
	 * @param f
	 * @return
	 * @throws NoColumnAnnotationFoundException
	 */
	private static String getColumnNameFromGetter(Method getter, Field f) {
		String columnName = "";
		Column columnAnno = getter.getAnnotation(Column.class);
		if (columnAnno != null) {
			columnName = columnAnno.name();
		}

		if (columnName == null || "".equals(columnName)) {
			columnName = MapperUtil.unCapitalize(f.getName());
		}
		return columnName;
	}
}
