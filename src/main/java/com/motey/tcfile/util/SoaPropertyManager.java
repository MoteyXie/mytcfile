package com.motey.tcfile.util;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateFolderInput;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateFoldersResponse;
import com.teamcenter.services.strong.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.strong.core._2013_05.DataManagement.ReviseIn;
import com.teamcenter.services.strong.core._2013_05.DataManagement.ReviseObjectsResponse;
import com.teamcenter.services.strong.core._2014_10.DataManagement.ChildrenInputData;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Folder;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class SoaPropertyManager {

	public DataManagementService dmService;
	public ServiceData serviceData;
	public Set<Integer> cache = new HashSet<Integer>();
		
	public void init(DataManagementService dmService){
		this.dmService = dmService;
	}
	
	public void init(Connection conn){
		this.dmService = DataManagementService.getService(conn);
	}
	
	public SoaPropertyManager(Connection conn) {
		init(conn);
	}
	
	public SoaPropertyManager(DataManagementService dmService) {
		init(dmService);
	}
	
	public void setProperty(ModelObject object, String propertyName, String propertyValue){
		
		ModelObject[] objects = new ModelObject[]{object};
		Map<String, VecStruct> properties = new HashMap<String, VecStruct>();
		VecStruct vecs = new VecStruct();
		vecs.stringVec = new String[]{propertyValue};
		properties.put(propertyName, vecs);
		dmService.setProperties(objects, properties);
		dmService.refreshObjects(objects);
	}
	
	public void setModelObjectArrayProperty(ModelObject object, String propertyName, String[] uids){
		
		ModelObject[] objects = new ModelObject[]{object};
		Map<String, VecStruct> properties = new HashMap<String, VecStruct>();
		VecStruct vecs = new VecStruct();
		vecs.stringVec = uids;
		properties.put(propertyName, vecs);
		dmService.setProperties(objects, properties);
		dmService.refreshObjects(objects);
	}
	
	public void addChildren(ModelObject parent, ModelObject[] children, String ref) {
		
		ChildrenInputData inputData = new ChildrenInputData();
		inputData.childrenObj = children;
		inputData.clientId = "0";
		inputData.parentObj = parent;
		inputData.propertyName = ref;
		dmService.addChildren(new ChildrenInputData[] {inputData});
	}
	
	public ModelObject revise(ModelObject mo) {
		
		ReviseIn[] info = new ReviseIn[] {new ReviseIn()};
		
		info[0].targetObject = mo;
		
		ReviseObjectsResponse response = dmService.reviseObjects(info);
		
		try {
			return response.output[0].objects[0];
		}catch(Exception e) {
			
		}
		return null;
		
	}
	
	public ModelObject refreshObject(ModelObject object){
		refreshObjects(new ModelObject[]{object});
		return object;
	}
	
	public ModelObject[] refreshObjects(ModelObject[] objects){
		dmService.refreshObjects(objects);
		return objects;
	}
	
	public Property getProperty(ModelObject object, String propertyName) throws NotLoadedException{
	    
		int hashCode = object.hashCode() + propertyName.hashCode();
		if(!cache.contains(hashCode)){
			serviceData = dmService.getProperties(new ModelObject[]{object}, new String[]{propertyName});
			cache.add(hashCode);
		}
    	return object.getPropertyObject(propertyName);
    }
	
	public Map<String, String> getProperties(ModelObject object, String[] propertyNames) throws Exception{
		
		serviceData = dmService.getProperties(new ModelObject[]{object}, propertyNames);
		
		Map<String, String> values = new HashMap<>();
		
		for (String propertyName : propertyNames) {
			values.put(propertyName, object.getPropertyObject(propertyName).getDisplayableValue());
		}
		
		return values;
	}
	
	public void delete(ModelObject[] objects){
		dmService.deleteObjects(objects);
		
	}
	
	/**
	 * 
	 * @param name 文件夹名称
	 * @param parent 关联的父对象
	 * @return 
	 */
	public Folder createFolder(String name, ModelObject parent, String ref) {
		CreateFolderInput arg = new CreateFolderInput();
		arg.clientId = "0";
		arg.desc = "desc";
		arg.name = name;
		
		CreateFoldersResponse response = dmService.createFolders(new CreateFolderInput[] {arg}, null, null);
		return response.output[0].folder;
		
		
	}
	
	public void remove(ModelObject parent, ModelObject[] children, String relName) {
		ChildrenInputData arg2 = new ChildrenInputData();
		arg2.childrenObj = children;
		arg2.clientId = "0";
		arg2.parentObj = parent;
		arg2.propertyName = relName;
		
		dmService.removeChildren(new ChildrenInputData[] {arg2});
	}
	
	public Date getDateProperty(ModelObject object, String propertyName) throws NotLoadedException{
		
		try{
			Property property = getProperty(object, propertyName);
			
			return property.getCalendarValue().getTime();
		}catch(Exception e){
			e.printStackTrace();
			return new Date();
		}
		
	}
	
	public String getStringProperty(ModelObject object, String propertyName) throws Exception{
		Property property = getProperty(object, propertyName);
		
		return property.getDisplayableValue();
	}
	
	public List<String> getStringArrayProperty(ModelObject object, String propertyName) throws NotLoadedException{
		Property property = getProperty(object, propertyName);
		return property.getDisplayableValues();
	}
	
	public boolean getBooleanProperty(ModelObject object, String propertyName) throws NotLoadedException{
		Property property = getProperty(object, propertyName);
		return property.getBoolValue();
	}
	
	public ModelObject getModelObjectProperty(ModelObject object, String propertyName) throws NotLoadedException{
		Property property = getProperty(object, propertyName);
		property.getPropertyDescription();
		return property.getModelObjectValue();
	}

	public ModelObject[] getModelObjectArrayProperty(ModelObject object, String propertyName) throws NotLoadedException{
		Property property = getProperty(object, propertyName);
		return property.getModelObjectArrayValue();
	}
	
	public List<ModelObject> getModelObjectListProperty(ModelObject object, String propertyName) throws NotLoadedException{
		Property property = getProperty(object, propertyName);
		return property.getModelObjectListValue();
	}

	public ModelObject[] getRepresented_By(ModelObject object) throws NotLoadedException{
		refreshObjects(new ModelObject[]{object});
		return getModelObjectArrayProperty(object,"TC_Is_Represented_By");
	}
	
	public ModelObject[] getContents(ModelObject object) throws NotLoadedException{
		return getModelObjectArrayProperty(object, "contents");
	}
	
	public ModelObject getModelObject(String uid) {
		ServiceData serviceData = dmService.loadObjects(new String[] {uid});
		if(serviceData.sizeOfPlainObjects() == 0)return null;
		return serviceData.getPlainObject(0);
	}
	
	public ModelObject[] getModelObjects(String[] uids) {
		
		ServiceData serviceData = dmService.loadObjects(uids);
		
		if(serviceData.sizeOfPlainObjects() == 0)return null;
		
		ModelObject[] mos = new ModelObject[serviceData.sizeOfPlainObjects()];
		
		for(int i = 0; i < mos.length; i++) {
			mos[i] = serviceData.getPlainObject(i);
		}
		return mos;
	}
}
