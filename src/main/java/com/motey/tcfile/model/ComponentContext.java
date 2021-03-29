package com.motey.tcfile.model;

public class ComponentContext {
	
	protected String relationType = "";
	protected String objectTypeClass = "";
	protected String objectPuid = "";
	protected String objectName = "";
	protected String objectTypeName = "";
	protected String owningUserPuid = "";
	protected String owningUserId = "";
	protected String owningUserName = "";
	
	public String getOwningUserPuid() {
		return owningUserPuid;
	}
	public void setOwningUserPuid(String owningUserPuid) {
		this.owningUserPuid = owningUserPuid;
	}
	public String getOwningUserId() {
		return owningUserId;
	}
	public void setOwningUserId(String owningUserId) {
		this.owningUserId = owningUserId;
	}
	public String getOwningUserName() {
		return owningUserName;
	}
	public void setOwningUserName(String owningUserName) {
		this.owningUserName = owningUserName;
	}
	public String getRelationType() {
		return relationType;
	}
	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}
	public String getObjectTypeClass() {
		return objectTypeClass;
	}
	public void setObjectTypeClass(String objectTypeClass) {
		this.objectTypeClass = objectTypeClass;
	}
	public String getObjectPuid() {
		return objectPuid;
	}
	public void setObjectPuid(String objectPuid) {
		this.objectPuid = objectPuid;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public String getObjectTypeName() {
		return objectTypeName;
	}
	public void setObjectTypeName(String objectTypeName) {
		this.objectTypeName = objectTypeName;
	}
	
	

}
