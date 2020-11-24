package com.epayslip.interview.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epayslip.interview.rest.FileRestController;
import com.epayslip.interview.vo.FileBo;

public class CommonUtil {
	private static final Logger LOG = LoggerFactory.getLogger(FileRestController.class);
	private static BigDecimal KILO = new BigDecimal("1024");

	public static List<FileBo> getFileList(File baseFolder, List<String> fileTypeList) {
		List<FileBo> fileList = new ArrayList<>();
		getFileList(baseFolder, fileList, fileTypeList);
		return fileList;
	}

	private static void getFileList(File baseFolder, List<FileBo> fileList, List<String> fileTypeList) {
		File[] listFiles = baseFolder.listFiles();
		if (null == listFiles) {
			return;
		}
		for (File file : listFiles) {
			if (file.isDirectory()) {
				getFileList(file, fileList, fileTypeList);
			} else {
				String fileType = getFileType(file.getName());
				if (fileTypeList.contains(fileType)) {
					FileBo bo = new FileBo();

					bo.setFileName(file.getName());
					bo.setFileType(fileType);
					bo.setLastModified(new Date(file.lastModified()));
					bo.setPath(getEncodedPath(file));
					bo.setSize(file.length());
					bo.setDisplaySize(getDisplaySize(file.length()));

					fileList.add(bo);
				}
			}
		}
	}

	private static String getEncodedPath(File file) {
		try {
			return URLEncoder.encode(file.getAbsolutePath(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public static String getFileType(String fileName) {
		int lastIndexOf = fileName.lastIndexOf(Consts.DOT);
		if (lastIndexOf > -1) {
			return fileName.substring(lastIndexOf + 1);
		}
		return fileName;
	}

	public static String getDisplaySize(long totalSpace) {
		BigDecimal size = new BigDecimal(String.valueOf(totalSpace));
		BigDecimal tmpVal = size.divide(KILO, 2, RoundingMode.HALF_UP);
		if (tmpVal.compareTo(KILO) < 0) {
			return String.format("%sKB", tmpVal.toString());
		}

		tmpVal = tmpVal.divide(KILO, 2, RoundingMode.HALF_UP);
		if (tmpVal.compareTo(KILO) < 0) {
			return String.format("%sMB", tmpVal.toString());
		}

		tmpVal = tmpVal.divide(KILO, 2, RoundingMode.HALF_UP);
		if (tmpVal.compareTo(KILO) < 0) {
			return String.format("%sGB", tmpVal.toString());
		}

		tmpVal = tmpVal.divide(KILO, 2, RoundingMode.HALF_UP);
		return String.format("%sTB", tmpVal.toString());
	}

	public static String toDateStr(Date date) {
		return toDateStr(date, "dd-MM-yyyy HH:mm:ss");
	}

	public static String toDateStr(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		return sdf.format(date);
	}

	public static String getContentType(String fileName) {
		String fileType = getFileType(fileName);
		if ("mp4".equals(fileType)) {
			return "video/mpeg4;charset=UTF-8";
		}

		return "application/octet-stream;charset=UTF-8";
	}
}
