package com.motey.tcfile.model;

public class ItemRevisionComponentContext extends ComponentContext {

	protected String itemId = "";
	protected String itemRevId = "";
	protected String itemUid = "";
	protected String itemRevUid = "";
	
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getItemRevId() {
		return itemRevId;
	}
	public void setItemRevId(String itemRevId) {
		this.itemRevId = itemRevId;
	}
	public String getItemUid() {
		return itemUid;
	}
	public void setItemUid(String itemUid) {
		this.itemUid = itemUid;
	}
	public String getItemRevUid(){return itemRevUid;}
	public void setItemRevUid(String itemRevUid){this.itemRevId = itemRevUid;}
	
}
