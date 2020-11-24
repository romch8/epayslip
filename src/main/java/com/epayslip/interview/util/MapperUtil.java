package com.epayslip.interview.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epayslip.interview.base.PoField;

public class MapperUtil {

	private static final Logger logger = LoggerFactory.getLogger(MapperUtil.class);

	private static final Pattern PATTERN = Pattern.compile("(.)([A-Z])");

	private static final String REPLACEMENT = "$1_$2";

	public static String unCapitalize(String s) {
		return PATTERN.matcher(s).replaceAll(REPLACEMENT).toLowerCase();
	}

	public static String unCapitalize(String str, boolean firstUpperCase) {
		char[] charArray = str.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (char tmpChar : charArray) {
			if (tmpChar == '_' || tmpChar == '-') {
				firstUpperCase = true;
			} else if (firstUpperCase) {
				sb.append(Character.toUpperCase(tmpChar));
				firstUpperCase = false;
			} else {
				sb.append(tmpChar);
			}
		}

		return sb.toString();
	}

	public static String convert2JavaFieldName(String columnName) {
		return unCapitalize(columnName, false);
	}

	public static String convert2JavaClazzName(String tableName) {
		return unCapitalize(tableName, true);
	}

	public static Map<String, Object> fillMap(Object object) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			for (Field field : object.getClass().getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || null != field.getAnnotation(Transient.class)) {
					continue;
				}

				String fieldType = field.getType().getSimpleName().toLowerCase();
				Object o = PropertyUtils.getProperty(object, field.getName());
				if (field.getType().isEnum()) {
					if (o == null) {
						map.put(unCapitalize(field.getName()), o);
					} else {
						try {
							map.put(unCapitalize(field.getName()), field.getType().getMethod("getValue").invoke(o));
						} catch (Exception e) {
							logger.info(e.toString());
						}
					}
				} else if (fieldType.equals("float")) {
					map.put(unCapitalize(field.getName()), (Float) o);
				} else if (fieldType.equals("double")) {
					map.put(unCapitalize(field.getName()), (Double) o);
				} else if (fieldType.equals("bigdecimal")) {
					map.put(unCapitalize(field.getName()), (BigDecimal) o);
				} else if (fieldType.equals("long")) {
					map.put(unCapitalize(field.getName()), (Long) o);
				} else if (fieldType.equals("integer") || fieldType.equals("int")) {
					map.put(unCapitalize(field.getName()), (Integer) o);
				} else if (fieldType.equals("byte")) {
					map.put(unCapitalize(field.getName()), (Byte) o);
				} else if (fieldType.equals("string")) {
					if (StringUtils.isBlank((String) o)) {
						map.put(unCapitalize(field.getName()), null);
					} else {
						map.put(unCapitalize(field.getName()), (String) o);
					}
				} else if (fieldType.equals("date")) {
					if (o != null)
						map.put(unCapitalize(field.getName()), o);
				}
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
		return map;
	}

	public static List<PoField> getPoFieldList(Object entity) {
		List<PoField> list = new ArrayList<>();
		try {
			for (Field field : entity.getClass().getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || null != field.getAnnotation(Transient.class)) {
					continue;
				}

				String fieldType = field.getType().getSimpleName().toLowerCase();
				Object fieldVal = PropertyUtils.getProperty(entity, field.getName());
				String columnName = unCapitalize(field.getName());

				PoField poField = new PoField();
				poField.setColumnName(columnName);
				poField.setType(field.getType());

				if (null != fieldVal) {
					if (field.getType().isEnum()) {
						poField.setValue(field.getType().getMethod("getValue").invoke(fieldVal));
					} else if (fieldType.equals("string")) {
						if (StringUtils.isNotBlank((String) fieldVal)) {
							poField.setValue(fieldVal);
						}
					} else if (fieldType.equals("byte[]")) {
						poField.setBlob(true);
						poField.setValue(fieldVal);
					} else {
						poField.setValue(fieldVal);
					}
				}

				list.add(poField);
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
		return list;
	}

	public static String[] getColumnArr(List<PoField> poFieldList) {
		String[] columnArr = new String[poFieldList.size()];
		for (int i = 0; i < poFieldList.size(); i++) {
			columnArr[i] = poFieldList.get(i).getColumnName();
		}
		return columnArr;
	}

	public static Map<String, Object> getNameValueMap(List<PoField> poFieldList) {
		Map<String, Object> nameValueMap = new LinkedHashMap<>();
		for (PoField poField : poFieldList) {
			nameValueMap.put(poField.getColumnName(), poField.getValue());
		}
		return nameValueMap;
	}

	public static boolean hasBlobFields(List<PoField> poFieldList) {
		for (PoField poField : poFieldList) {
			if (poField.isBlob()) {
				return true;
			}
		}
		return false;
	}

	public static void setParamterValue(PreparedStatement ps, int parameterIndex, PoField poField) throws SQLException {
		if (null == poField.getValue()) {
			ps.setObject(parameterIndex, null);
		}

		String fieldType = poField.getType().getSimpleName().toLowerCase();
		if (fieldType.equals("string")) {
			ps.setString(parameterIndex, (String) poField.getValue());
		} else if (fieldType.equals("bigdecimal")) {
			ps.setBigDecimal(parameterIndex, (BigDecimal) poField.getValue());
		} else if (fieldType.equals("date")) {
			Date value = (Date) poField.getValue();
			ps.setTimestamp(parameterIndex, new Timestamp(value.getTime()));
		} else if (fieldType.equals("timestamp")) {
			ps.setTimestamp(parameterIndex, (Timestamp) poField.getValue());
		} else if (fieldType.equals("byte")) {
			ps.setByte(parameterIndex, (byte) poField.getValue());
		} else if (fieldType.equals("integer") || fieldType.equals("int")) {
			ps.setInt(parameterIndex, (int) poField.getValue());
		} else if (fieldType.equals("long")) {
			ps.setLong(parameterIndex, (long) poField.getValue());
		} else if (fieldType.equals("boolean")) {
			ps.setBoolean(parameterIndex, (boolean) poField.getValue());
		} else {
			ps.setObject(parameterIndex, poField.getValue());
		}
	}
}
