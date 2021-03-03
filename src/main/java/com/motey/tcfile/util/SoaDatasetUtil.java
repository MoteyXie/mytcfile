package com.motey.tcfile.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.teamcenter.soaictstubs.ICCTDataset;
import com.teamcenter.soaictstubs.StringHolder;
import org.apache.commons.io.FileUtils;

import com.teamcenter.services.loose.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData;
import com.teamcenter.services.loose.core._2006_03.FileManagement.DatasetFileInfo;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ClientMetaModel;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.Type;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;

//import com.teamcenter.soaictstubs.ICCTDataset;
//import com.teamcenter.soaictstubs.StringHolder;

public class SoaDatasetUtil {

	public static HashMap<String, String[]> dsTypes = new HashMap<>();
	public static String[] DEFAULT_DSTYPE = new String[] { "CAEAnalysisDS", "CAEAnalysisData" };
	private String lastErrorMsg;
	static {
		// key ： 文件后缀	value ： String[]{所用工具， 引用}
		dsTypes.put("xls", new String[]{"MSExcel", "excel"});
		dsTypes.put("xlsx", new String[]{"MSExcelX", "excel"});
		
		dsTypes.put("doc", new String[]{"MSWord", "word"});
		dsTypes.put("docx", new String[]{"MSWordX", "word"});
		
		dsTypes.put("ppt", new String[]{"MSPowerPoint", "powerpoint"});
		dsTypes.put("pptx", new String[]{"MSPowerPointX", "powerpoint"});
		
		dsTypes.put("zip", new String[]{"Zip", "ZIPFILE"});
		dsTypes.put("rar", new String[]{"Zip", "ZIPFILE"});
		dsTypes.put("7z", new String[]{"Zip", "ZIPFILE"});
		
		dsTypes.put("pdf", new String[]{"PDF", "PDF_Reference"});
		dsTypes.put("jpg", new String[]{"JPEG", "JPEG_Reference"});
		dsTypes.put("jpeg", new String[]{"JPEG", "JPEG_Reference"});
		dsTypes.put("txt", new String[]{"Text", "Text"});
		
		dsTypes.put("dwg", new String[]{"DWG", "SH5_DWG"});
		dsTypes.put("tif", new String[]{"TIF", "TIF_Reference"});
		
		dsTypes.put("sldasm", new String[]{"SolidWorks 装配", "AsmFile"});
		dsTypes.put("slddrw", new String[]{"SolidWorks 图纸", "DrwFile"});
		dsTypes.put("sldprt", new String[]{"SolidWorks 零件", "PrtFile"});
		
		dsTypes.put("htm", new String[]{"HTML", "HTML"});
		dsTypes.put("html", new String[]{"HTML", "HTML"});
		
		dsTypes.put("bmp", new String[]{"Bitmap", "Image"});
		
		dsTypes.put("bmp", new String[]{"Bitmap", "Image"});
		dsTypes.put("log", new String[]{"Text", "Text"});
		dsTypes.put("png", new String[]{"PNG_Thumbnail", "PNG_Reference"});
		
		dsTypes.put("xml", new String[]{"ProgramView", "XML"});
		
	}
	
	private SoaSession soaSession;
	private SoaPropertyManager propertyManager;
	
	public SoaDatasetUtil(SoaSession soaSession) {
		this.soaSession = soaSession;
		this.propertyManager = soaSession.getPropertyManager();
	}

	public String getLastErrorMsg() {
		return lastErrorMsg;
	}

	public List<ImanFile> getImanFiles(ModelObject dataset) throws Exception{
		
		ModelObject[] refs = propertyManager.getModelObjectArrayProperty(dataset, "ref_list");
		
		if(refs == null || refs.length == 0)return null;
		
		List<ImanFile> imanFiles = new ArrayList<>();

		for (int i = 0; i < refs.length; ++i) {
			if (refs[i] instanceof ImanFile) {
				imanFiles.add((ImanFile) refs[i]);
			}
		}
		
		return imanFiles;
	}
	
	public String download(ImanFile imanFile, String path) throws Exception {
		String fileName = propertyManager.getStringProperty(imanFile, "object_string");
		return downloadFile(imanFile, path, fileName);
	}
	
	public List<String> downloadAll(ModelObject dataset, String path) {
		
		String fileName = null;
		
		List<String> fileNames = new ArrayList<>();
		try {
			
			path = path.replace("\\", "/");
			
			while(path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			
			ModelObject[] refs = propertyManager.getModelObjectArrayProperty(dataset, "ref_list");
			
			if(refs == null || refs.length == 0)return null;
			
			List<ImanFile> imanFiles = new ArrayList<>();

			for (int i = 0; i < refs.length; ++i) {
				if (refs[i] instanceof ImanFile) {
					imanFiles.add((ImanFile) refs[i]);
				}
			}
			
			for (ImanFile imanFile : imanFiles) {
				fileName = propertyManager.getStringProperty(imanFile, "object_string");
				downloadFile(imanFile, path, fileName);
				fileNames.add(fileName);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileNames;
	}
	
	public ModelObject create(File file) {
		
		String name = file.getName();
		
		String suffix = null;
		String[] types = null;
		if(name.contains(".")) {
			suffix= name.substring(name.lastIndexOf(".")+1, name.length());
			if(dsTypes.containsKey(suffix)) {
				types = dsTypes.get(suffix);
			}else {
				types = DEFAULT_DSTYPE;
			}
		}else {
			types = DEFAULT_DSTYPE;
		}
		
		return create(file, name, name, types[1], types[0]);
	}
	
	
	public ModelObject create(File file, String name, String datasetName, String refType, String fileType) {
		
		ClientMetaModel metaModel = soaSession.getConnection().getClientMetaModel();
		Type type = metaModel.getType("Dataset", soaSession.getConnection());
		
		ICCTDataset icctds = new ICCTDataset(soaSession.getConnection(),type.getTypeUid(), type.getUid());
		
		StringHolder datasetObj = new StringHolder();
		StringHolder typeObj = new StringHolder();
		
		try {
			
			icctds.create(name, "", fileType, "", "", "", datasetObj, typeObj);
			
			ModelObject mo = soaSession.getConnection().getModelManager().constructObject(typeObj.value, datasetObj.value);
		
			addFileToDatasets((Dataset)mo, false, new File[] {file}, new String[] {refType}, new boolean[] {true}, new boolean[] {false});
			
			return mo;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String[] RegisterTickets(String[] arg0){
		String[] arg1 = new String[arg0.length];

		for (int arg2 = 0; arg2 < arg0.length; ++arg2) {
			arg1[arg2] = arg0[arg2].substring(0, 64);
		}

		return arg1;
	}
	
	public void addFileToDatasets(Dataset dataset, boolean createNewVersion, File[] files, String[] nameReferenceName, boolean[] isText, boolean[] isAllowReplace) {
		// create a file to associate with dataset
		if (files == null) {
			return;
		}

		DatasetFileInfo[] fileInfos = new DatasetFileInfo[files.length];
		for (int i = 0; i < fileInfos.length; ++i) {
			File file = files[i];
			DatasetFileInfo fileInfo = new DatasetFileInfo();

			fileInfo.clientId = "file_upload" + i;
			fileInfo.fileName = file.getAbsolutePath();
			fileInfo.namedReferencedName = nameReferenceName[i];
			fileInfo.isText = isText[i];
			fileInfo.allowReplace = isAllowReplace[i];
			fileInfos[i] = fileInfo;
		}

		GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
		inputData.dataset = dataset;
		inputData.createNewVersion = createNewVersion;
		inputData.datasetFileInfos = fileInfos;

		FileManagementUtility fMSFileManagement = new FileManagementUtility(soaSession.getConnection());

		GetDatasetWriteTicketsInputData[] inputs = new GetDatasetWriteTicketsInputData[1];
		inputs[0] = inputData;

		ServiceData response = fMSFileManagement.putFiles(inputs);

		if (response.sizeOfPartialErrors() > 0)
			System.out.println("FileManagementService upload returned partial errors. \n"+ getErrorStackInfo(response));

	}

	private String getErrorStackInfo(ServiceData serviceData) {
		StringBuffer buffer = new StringBuffer();

		int size = serviceData.sizeOfPartialErrors();

		for (int i = 0; i < size; ++i) {
			ErrorStack stack = serviceData.getPartialError(i);
			try {
				String code = stack.getCodes()[i] + "";
				String level = stack.getLevels()[i] + "";
				String msg = stack.getMessages()[i] + "";

				buffer.append("code:" + code + ", level:" + level + ", msg:"
						+ msg + "\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return buffer.toString();
	}
	
	/**
	 * 下载数据集到本地
	 * 
	 * @param file 数据集文件对象
	 * @param dir 目标文件夹
	 * @param fileName 目标文件名，如果为null或为空串，则使用默认文件名
	 * @return 成功则返回下载后的文件名，否则返回null，出错信息保存在lastErrorMsg中
	 */
	public String downloadFile(ImanFile file, String dir, String fileName) {
		String ret = null;
		try {
			lastErrorMsg = null;
			
			if (file == null) {
				lastErrorMsg = "downloadFile() failed: targer file can not be null!";
				return null;
			}
			
			if (dir == null) {
				lastErrorMsg = "downloadFile() failed: target folder can not be null!";
				return null;
			}
			
			String fileName0 = propertyManager.getStringProperty(file, "file_name");
//			String fileName0 = file.get_file_name();
			int index = fileName0.lastIndexOf("_");
			if (index >= 0 && index < fileName0.length() - 1) {
				fileName0 = fileName0.substring(index + 1);
			}

			if (!(dir.endsWith("\\") || dir.endsWith("/"))) {
				dir += File.separator;
			}
			
			String localFile = dir + fileName0;
			
			File f1 = new File(localFile);
			if (f1.exists()) {
				f1.delete();
			}
			
			FileManagementUtility fileUtil = new FileManagementUtility(soaSession.getConnection());
			
			fileUtil.getFileToLocation(file, localFile, null, null);
			
			String realFile = dir;
			if (fileName == null || fileName.trim().length() == 0) {
				realFile = realFile + file.get_original_file_name();
			} else {
				realFile = realFile + fileName.trim();
			}
			
			File src = new File(localFile);
			File dst =  new File(realFile);
			
			if (dst.exists()) {
				dst.delete();
			}
			
			FileUtils.moveFile(src, dst);
			
			ret = realFile;
			
		} catch(Exception e) {
			lastErrorMsg = "downloadFile() failed: " + e.getMessage();
			e.printStackTrace();
		}
		
		return ret;
	}

}
