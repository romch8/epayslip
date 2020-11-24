package com.epayslip.interview.po;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;

public class EpayFile implements Serializable {
	private static final long serialVersionUID = -7673194098424688420L;

	private Long fileId;
	private String path;
	private String fileName;
	private long size;
	private String fileType;
	private Date lastModified;

	@Id
	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

}
