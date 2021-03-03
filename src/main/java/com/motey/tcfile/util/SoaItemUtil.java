package com.motey.tcfile.util;

import com.teamcenter.services.internal.loose.core._2011_06.ICT.Arg;
import com.teamcenter.services.internal.loose.core.ICTService;
import com.teamcenter.services.internal.loose.core._2011_06.ICT;
import com.teamcenter.services.strong.core._2009_10.DataManagement;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.soaictstubs.*;

import java.util.ArrayList;
import java.util.List;

public class SoaItemUtil {

    private SoaSession soaSession;
    public SoaItemUtil(SoaSession soaSession){
        this.soaSession = soaSession;
    }

    public Item findItem(String itemId) throws Exception {

        DataManagementService dmService = DataManagementService.getService(soaSession.getConnection());

        DataManagement.GetItemFromAttributeInfo[] getItemFromAttributeInfos = new DataManagement.GetItemFromAttributeInfo[1];

        getItemFromAttributeInfos[0] = new DataManagement.GetItemFromAttributeInfo();
        getItemFromAttributeInfos[0].itemAttributes.put("item_id", itemId);

        com.teamcenter.services.strong.core._2007_01.DataManagement.GetItemFromIdPref getItemFromIdPref = new com.teamcenter.services.strong.core._2007_01.DataManagement.GetItemFromIdPref();

        DataManagement.GetItemFromAttributeResponse response = dmService.getItemFromAttribute(getItemFromAttributeInfos,-1, getItemFromIdPref);

        DataManagement.GetItemFromAttributeItemOutput[] output = response.output;

        Item item = null;
        if(output != null && output.length > 0){
            item = output[0].item;
        }
        return item;
    }

    public ItemRevision getItemRevision(Item item, String itemRevisionId) throws Exception {
        soaSession.getPropertyManager().getProperty(item,"revision_list");
        ModelObject[] revisions = item.get_revision_list();
        for (ModelObject mo: revisions) {

            String revId = soaSession.getPropertyManager().getStringProperty(mo, "item_revision_id");
            if(itemRevisionId.equals(revId)){
                return (ItemRevision)mo;
            }

        }
        return null;
    }

    /**
     * TODO 
     * @param item
     * @return
     * @throws Exception
     */
    public ItemRevision[] getDisplayableItemRevisions(Item item) throws Exception {

        soaSession.getPropertyManager().getProperty(item, "displayable_revisions");

        ModelObject[] revisions = item.get_displayable_revisions();

        ItemRevision[] irs = new ItemRevision[revisions.length];

        for(int i = 0; i < revisions.length; i++){
            irs[i] = (ItemRevision)revisions[i];
        }
        return  irs;
    }

    public ItemRevision getLatestItemRevision(Item item) throws Exception {

        String objectType = soaSession.getPropertyManager().getStringProperty(item, "object_type");

        ICTService m_service = ICTService.getService( soaSession.getConnection() );

        Arg[] args_ = new Arg[3];
        args_[0] = TcUtility.createArg(objectType);
        args_[1] = TcUtility.createArg(item.getUid());
        args_[2] = TcUtility.createArg(item.getUid());

        ICT.InvokeICTMethodResponse response = m_service.invokeICTMethod("ICCTItem", "getLatestItemRevision", args_);

        StringHolder itemRevisionSH = new StringHolder();
        StringHolder itemRevisionTypeSH = new StringHolder();

        itemRevisionSH.value = TcUtility.queryArg(response.output[0], itemRevisionSH.value);
        itemRevisionTypeSH.value = TcUtility.queryArg(response.output[1], itemRevisionTypeSH.value);

        return  (ItemRevision) soaSession.getPropertyManager().getModelObject(itemRevisionSH.value);
    }

    public ItemRevision getLatestItemRevision2(Item item) throws Exception {

        soaSession.getPropertyManager().getProperty(item,"revision_list");

        ModelObject[] revisions = item.get_revision_list();

        String maxRevId = null;
        ModelObject maxRev = null;

        for (ModelObject mo: revisions) {
            String revId = soaSession.getPropertyManager().getStringProperty(mo, "item_revision_id");
            if(maxRevId == null){
                maxRevId = revId;
                maxRev = mo;
                continue;
            }
            if(maxRevId.compareToIgnoreCase(revId) < 0){
                maxRevId = revId;
                maxRev = mo;
            }
        }

        return (ItemRevision) maxRev;
    }

    public List<ModelObject> getRelatedComponents(ModelObject modelObject, String... relations) throws NotLoadedException {

        SoaPropertyManager propertyManager = soaSession.getPropertyManager();
        List<ModelObject> modelObjectList = new ArrayList<>();
        for (String relation : relations) {
            List<ModelObject> list = propertyManager.getModelObjectListProperty(modelObject, relation);
            if(list != null && list.size() > 0){
                modelObjectList.addAll(list);
            }
        }
        return modelObjectList;
    }

    public ModelObject[] getRelatedComponents(ModelObject modelObject) throws Exception {

        stringSeqValue_u relatedTypes = new stringSeqValue_u();
        uidSeqValue_uHolder components = new uidSeqValue_uHolder();
        uidSeqValue_uHolder componentTypes = new uidSeqValue_uHolder();
        stringSeqValue_uHolder componentContextNames = new stringSeqValue_uHolder();

        SoaPropertyManager propertyManager = soaSession.getPropertyManager();

        String objectType = propertyManager.getStringProperty(modelObject, "object_type");

        Arg[] args_ = new Arg[4];
        args_[0] = TcUtility.createArg(objectType);
        args_[1] = TcUtility.createArg(modelObject.getUid());
        args_[2] = TcUtility.createArg(modelObject.getUid());
        args_[3] = TcUtility.createArg(relatedTypes);

        ICTService m_service = ICTService.getService( soaSession.getConnection() );

        ICT.InvokeICTMethodResponse response = m_service.invokeICTMethod("ICCT", "getRelatedInfo", args_);

        if( response.serviceData.sizeOfPartialErrors() > 0)
        {
            throw new ICCTException( response.serviceData);
        }
        components.value = TcUtility.queryArg(response.output[0], components.value);
        componentTypes.value = TcUtility.queryArg(response.output[1], componentTypes.value);
        componentContextNames.value = TcUtility.queryArg(response.output[2], componentContextNames.value);

        String[] uids = components.value.seqValue();

        ModelObject[] mos = new ModelObject[uids.length];

        for(int i = 0; i < uids.length; i++){
            mos[i] = propertyManager.getModelObject(uids[i]);
        }

        return mos;

    }

}
