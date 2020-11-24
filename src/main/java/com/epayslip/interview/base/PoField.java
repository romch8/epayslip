package com.epayslip.interview.base;

public class PoField {
	private String columnName;
	private Object value;
	private Class<?> type;
	private boolean isBlob;

	public PoField() {
	}

	public PoField(String columnName, Object value) {
		this.columnName = columnName;
		this.value = value;
	}

	public PoField(String columnName, Object value, boolean isBlob) {
		this.columnName = columnName;
		this.value = value;
		this.isBlob = isBlob;
	}

	public boolean isBlob() {
		return isBlob;
	}

	public void setBlob(boolean isBlob) {
		this.isBlob = isBlob;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}
}
