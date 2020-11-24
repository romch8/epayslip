package com.epayslip.interview.vo;

import com.epayslip.interview.po.EpayFile;

public class FileBo extends EpayFile {
	private static final long serialVersionUID = 6048913792598174107L;
	private String displaySize;
	private String lastModifiedStr;

	public String getDisplaySize() {
		return displaySize;
	}

	public void setDisplaySize(String displaySize) {
		this.displaySize = displaySize;
	}

	public String getLastModifiedStr() {
		return lastModifiedStr;
	}

	public void setLastModifiedStr(String lastModifiedStr) {
		this.lastModifiedStr = lastModifiedStr;
	}

}
