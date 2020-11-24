package com.epayslip.interview.base;

import java.io.Serializable;

public class TableField implements Serializable {
	private static final long serialVersionUID = 8150553320960862448L;
	private String fieldId;
	private String fieldType;

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	@Override
	public String toString() {
		return "TableField [fieldId=" + fieldId + ", fieldType=" + fieldType + "]";
	}

}
