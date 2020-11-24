package com.epayslip.interview.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.epayslip.interview.dao.EpayFileDAO;
import com.epayslip.interview.po.EpayFile;
import com.epayslip.interview.util.CommonUtil;
import com.epayslip.interview.util.Consts;
import com.epayslip.interview.vo.FileBo;

@Service("fileService")
public class FileServiceImpl implements FileService {
	private static Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);

	@Value("${file.bash.path}")
	private String baseFilePath;

	@Value("${file.types}")
	private String fileTypes;

	@Autowired
	private EpayFileDAO epayFileDAO;

	@PostConstruct
	protected void initial() {
		boolean existingData = epayFileDAO.existingData();
		if (!existingData) {
			LOG.info("No File Data Existing, Initializing...");
			File baseFolder = new File(baseFilePath);
			List<String> fileTypeList = Arrays.asList(fileTypes.split(Consts.COMMA));
			List<FileBo> fileList = CommonUtil.getFileList(baseFolder, fileTypeList);
			List<EpayFile> epayFileList = convertToEpayFileList(fileList);
			epayFileDAO.batchCreateEntity(epayFileList);
			LOG.info("Total {} data has been initialized", epayFileList.size());
		}
	}

	private List<EpayFile> convertToEpayFileList(List<FileBo> fileList) {
		if (CollectionUtils.isEmpty(fileList)) {
			return Collections.emptyList();
		}
		List<EpayFile> epayFileList = new ArrayList<>(fileList.size());
		for (FileBo bo : fileList) {
			EpayFile epayFile = new EpayFile();
			BeanUtils.copyProperties(bo, epayFile);
			epayFileList.add(epayFile);
		}
		return epayFileList;
	}

	@Override
	public List<FileBo> getFileBoList() {
		List<EpayFile> epayFileList = epayFileDAO.getList();
		return convertToFileBoList(epayFileList);
	}

	private List<FileBo> convertToFileBoList(List<EpayFile> epayFileList) {
		if (CollectionUtils.isEmpty(epayFileList)) {
			return Collections.emptyList();
		}
		List<FileBo> fileList = new ArrayList<>(epayFileList.size());
		for (EpayFile epayFile : epayFileList) {
			FileBo bo = new FileBo();
			BeanUtils.copyProperties(epayFile, bo);
			bo.setDisplaySize(CommonUtil.getDisplaySize(bo.getSize()));
			bo.setLastModifiedStr(CommonUtil.toDateStr(bo.getLastModified()));
			fileList.add(bo);
		}
		return fileList;
	}
}
