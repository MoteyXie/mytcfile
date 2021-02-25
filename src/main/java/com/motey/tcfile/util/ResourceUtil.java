package com.motey.tcfile.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class ResourceUtil {

	@Value("${file.staticAccessPath}")
	private String accessPath;
	@Value("${server.address}")
	private String address;
	@Value("${server.port}")
	private String post;
	@Value("${file.uploadFolder}")
    private String uploadFolder;

	public String getUploadFolder(){
		return uploadFolder;
	}

	public boolean hasFile(String path) {
		return new File(path).exists();
	}

	public String getHttpUrl(String functionName, Map<String, String> params){

		StringBuilder sb = new StringBuilder();
		if(params != null && params.size() > 0){
			for (Map.Entry<String, String> entry : params.entrySet() ) {
				sb.append(entry.getKey() + "=" + entry.getValue()+"&");
			}
		}

		String url = String.format(
				"%s:%s/%s?%s",
				address,
				post,
				functionName,
				sb.substring(0, sb.length() - 1));

		return url;
	}

	public String getResourceUrl(String path, boolean hasIpHort) throws UnsupportedEncodingException {

		if(!path.startsWith(uploadFolder))return "";

		path = path.replace(uploadFolder, "");
		String url = null;
		if(hasIpHort){
			url = String.format(
					"%s:%s%s/%s",
					address,
					post,
					accessPath.replace("*", ""),
					URLEncoder.encode(path,"utf-8"));
		}else{
			url = String.format(
					"%s/%s",
					accessPath.replace("*", ""),
					URLEncoder.encode(path,"utf-8"));
		}
		return url;
	}
	 
	public String getImageUrl(String type, String imageName) throws UnsupportedEncodingException {
		
		if(imageName.lastIndexOf(",") <= 0)imageName = imageName + ".png";
		
		String path = String.format(
				"%s:%s%simages/%s/%s", 
				address,
				post,
				accessPath.replace("*", ""),
				URLEncoder.encode(type,"utf-8"),
				URLEncoder.encode(imageName, "utf-8"));
		return path;
	}
	
	public String getImageRelativeUrl(String type, String imageName) throws UnsupportedEncodingException {
		if(imageName.lastIndexOf(",") <= 0)imageName = imageName + ".png";
		
		String path = String.format(
				"%simages/%s/%s", 
				accessPath.replace("*", ""),
				URLEncoder.encode(type,"utf-8"),
				URLEncoder.encode(imageName, "utf-8"));
		System.out.println(path);
		return path;
	}
	
	public String getImagePath(String type, String imageName) throws UnsupportedEncodingException {
		if(imageName.lastIndexOf(",") <= 0)imageName = imageName + ".png";
		String path = String.format(
				"%simages/%s/%s", 
				uploadFolder.replace("//", "/"),
				URLEncoder.encode(type,"utf-8"),
				URLEncoder.encode(imageName, "utf-8"));
		System.out.println(path);
		return path;
	}
}
