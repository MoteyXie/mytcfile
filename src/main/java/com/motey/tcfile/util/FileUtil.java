package com.motey.tcfile.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtil {
	
	/**
	 * 
	 * <p>Title: getContent</p>  
	 * <p>Description:根据文件路径读取文件转出byte[] </p>  
	 * @param filePath文件路径
	 * @return 字节数组
	 * @throws IOException
	 */
	public static byte[] getContent(File file) throws IOException { 
        long fileSize = file.length();  
        if (fileSize > Integer.MAX_VALUE) {  
        	//logger.info("file too big...");  
        	System.out.println("file too big...");
            return null;  
        }  
        FileInputStream fi = new FileInputStream(file);  
        byte[] buffer = new byte[(int) fileSize];  
        int offset = 0;  
        int numRead = 0;  
        while (offset < buffer.length  
        && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {  
            offset += numRead;  
        }  
        // 确保所有数据均被读取  
        if (offset != buffer.length) {  
        	throw new IOException("Could not completely read file "  
                    + file.getName());  
        }  
        fi.close();  
        return buffer;  
    }
	
}
