package com.motey.tcfile.controller;

import com.motey.tcfile.TeamcenterContext;
import com.motey.tcfile.model.JLAskDrawingFromPartResponse;
import com.motey.tcfile.model.JLAskDrawingFromPartResponses;
import com.motey.tcfile.model.X_DATA_TBL;
import com.motey.tcfile.util.*;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class JLTCController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    private TeamcenterContext teamcenterContext;

    @Resource
    private ResourceUtil resourceUtil;

    @Autowired
    public void setTeamcenterContext(TeamcenterContext teamcenterContext) {
        this.teamcenterContext = teamcenterContext;
    }

    public String download(HttpServletResponse response, String path) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try {

            LOGGER.info("收到下载文件请求");

            //获取文件，是文件夹的话需要获取文件夹下的第一个文件
            File file = new File(path);

            String fileName = file.getName();

            response.setContentType("application/force-download");
            response.setHeader("Content-Type", "application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            byte[] buffer = new byte[1024];

            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            OutputStream outputStream = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                outputStream.write(buffer, 0, i);
                i = bis.read(buffer);
            }
            LOGGER.info(file.getAbsolutePath() + " 下载成功");
            return file.getAbsolutePath() + " 下载成功";

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            return e.getMessage();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String downloadDataset(HttpServletResponse response, String imanFileUid) throws Exception {

        SoaSession soaSession = SoaSessionManager.getSession(
                teamcenterContext.getTc_address(),
                teamcenterContext.getTc_port(),
                teamcenterContext.getUser(),
                teamcenterContext.getPass());

        SoaDatasetUtil datasetUtil = new SoaDatasetUtil(soaSession);

        ModelObject modelObject = soaSession.getPropertyManager().getModelObject(imanFileUid);

        if (!(modelObject instanceof ImanFile)) {
            return "此uid并非ImanFile，无法下载！";
        }

        String path = datasetUtil.download((ImanFile) modelObject, "C:/temp");

        return download(response, path);
    }

    /**
     *
     * @param partNo 物料号/版本号, 每个号间用逗号隔开
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/askDrawingFromJLParts")
    public JLAskDrawingFromPartResponses askDrawingFromParts(String partNo) throws Exception {

        JLAskDrawingFromPartResponses responses = new JLAskDrawingFromPartResponses();

        partNo = partNo.replace("，", ",");
        String[] partNos = partNo.split(",");

        List<JLAskDrawingFromPartResponse> responseList = new ArrayList<>();
        boolean allSuccess = true;
        for (String apartNo : partNos) {
            apartNo = apartNo.replace("\\", "/");

            String[] pp = apartNo.split("/");
            String no = pp[0];
            String revId = pp.length > 1 ? pp[1] : null;

            JLAskDrawingFromPartResponse resp = askDrawingFromPart(no, revId);
            responseList.add(resp);
            if(!"S".equals(resp.getState())){
                allSuccess = false;
            }
        }

        X_DATA_TBL xdt = new X_DATA_TBL();
        xdt.setX_DATA_TBL_ITEM(responseList);
        responses.setX_DATA_TBL(xdt);
        responses.setX_RETURN_STATUS(allSuccess ? "S":"E");
        return responses;
    }

    @RequestMapping(value = "/askDrawingFromJLPart")
    public JLAskDrawingFromPartResponse askDrawingFromPart(String partNo, String partRevNo) throws Exception {

        JLAskDrawingFromPartResponse response = new JLAskDrawingFromPartResponse();

        response.setPartNo(partNo);

        SoaSession soaSession = SoaSessionManager.getSession(
                teamcenterContext.getTc_address(),
                teamcenterContext.getTc_port(),
                teamcenterContext.getUser(),
                teamcenterContext.getPass());
        soaSession.refreshCache();
        SoaItemUtil itemUtil = new SoaItemUtil(soaSession);

        Item item = itemUtil.findItem(partNo);

        if (item == null) {
            //查询结果为空时的处理
            response.setErrorMsg("物料编码【" + partNo + "】未找到");
            response.setState("E");
            return response;
        } else {

            SoaPropertyManager propertyManager = soaSession.getPropertyManager();
            SoaDatasetUtil datasetUtil = new SoaDatasetUtil(soaSession);

            ItemRevision itemRevision = null;
            //获取版本
            if (partRevNo != null && partRevNo.length() > 0) {
                itemRevision = itemUtil.getItemRevision(item, partRevNo);
            } else {
                ItemRevision[] itemRevisions = itemUtil.getDisplayableItemRevisions(item);
                itemRevision = itemRevisions == null || itemRevisions.length == 0 ? null : itemRevisions[0];
            }

            if (itemRevision == null) {
                //itemRevision为空时的处理
                response.setErrorMsg("物料编码【" + partNo + "】下未找到可用版本。");
                response.setState("E");
                return response;
            }

            String itemId = propertyManager.getStringProperty(itemRevision, "item_id");
            String name = propertyManager.getStringProperty(itemRevision, "object_name");
            String itemRevisionId = propertyManager.getStringProperty(itemRevision, "item_revision_id");

            System.out.println("PartRevision is " +itemId+"/"+itemRevisionId+":"+name);
            String drawNo = null;

            //先通过图料关联关系查找图纸,如果没有搭建图料关系通过物料描述的第二段进行查找
            soaSession.getPropertyManager().refreshObject(itemRevision);
            ModelObject[] designObjects = propertyManager.getRepresented_By(itemRevision);
            if (designObjects.length>1){
                response.setErrorMsg("物料编码【" + partNo + "】关联了多个图纸。");
                response.setState("M");
                return response;
            }if(designObjects.length ==1){
                soaSession.getPropertyManager().refreshObject(designObjects[0]);
                drawNo = propertyManager.getStringProperty(designObjects[0],"item_id");
                String type = propertyManager.getProperty(designObjects[0],"object_type").getStringValue();
                System.out.println("图纸类型：" + type);
                if(type.equals("K8_EDesignRevision")){
                    response.setErrorMsg("图纸ID【" + drawNo + "】为工程图，类型错误。");
                    response.setState("W");
                    return response;
                }
            }else{
                String partDesc = propertyManager.getStringProperty(itemRevision, "object_desc");
//              System.out.println("partDesc " + partDesc);
                try {
//              String temp = partDesc.substring(partDesc.indexOf("\\")+1);
                    drawNo = partDesc.split("\\\\")[1];
                } catch (Exception e) {

                }
            }
            //改成坚朗专用的图纸对象获取方式
            //ModelObject[] designRevisions = propertyManager.getModelObjectArrayProperty(itemRevision, "TC_Is_Represented_By");
            //TODO 获取之前先判断物料关联的图纸有几个？如果有多个的话就不继续了

            ModelObject designItem = drawNo == null || drawNo.isEmpty() ? null : itemUtil.findItem(drawNo);

//            propertyManager.refreshObject(designItem);

            if (designItem == null) {
                //关联图纸对象为空时的处理
                response.setErrorMsg("未找到关联图纸");
                response.setState("W");
                return response;
            }

            //获取最大已发布版本
            soaSession.getPropertyManager().refreshObject(designItem);
            ItemRevision designRevision = itemUtil.getLatestReleasedItemRevision((Item)designItem);
            if (designRevision == null) {
                //关联图纸对象为空时的处理
                response.setErrorMsg("未找到已发布的图纸版本");
                response.setState("W");
                return response;
            }
            soaSession.getPropertyManager().refreshObject(designRevision);
            String designRevNo = propertyManager.getStringProperty(designRevision, "item_revision_id");
            String designName = propertyManager.getStringProperty(designRevision, "object_name");
            String designStr = propertyManager.getStringProperty(designRevision, "object_string");
            response.setDrawingNo(drawNo);
            response.setDrawingRevNo(designRevNo);
            response.setDrawingName(designName);

            List<ModelObject> relateds = itemUtil.getRelatedComponents(designRevision, "IMAN_specification", "IMAN_reference", "IMAN_Rendering");

            if (relateds == null || relateds.size() == 0) {
                response.setErrorMsg("图纸【" + designStr + "】未找到关联图纸数据集");
                response.setState("W");
                return response;
            }

            //TODO 找到数据集下载下来后显示
            for (int k = 0; k < relateds.size(); k++) {

                ModelObject related = relateds.get(k);

                if (related instanceof Dataset) {

                    String rname = propertyManager.getStringProperty(related, "object_string");
                    System.out.println("related name = " + rname);

                    List<ImanFile> imanFiles = datasetUtil.getImanFiles(related);

                    if (imanFiles != null && imanFiles.size() > 0) {

                        for (ImanFile imanFile : imanFiles) {

                            String fileName = propertyManager.getStringProperty(imanFile, "original_file_name");
                            String fileSize = propertyManager.getStringProperty(imanFile, "file_size");
                            String mimeType = propertyManager.getStringProperty(imanFile, "mime_type");

                            if(fileName.toUpperCase().endsWith(".PDF")) {
                                //String path = datasetUtil.download(imanFile, resourceUtil.getUploadFolder());
                                //TODO 换成下载imanFile的链接
                                //String url = resourceUtil.getResourceUrl(path);
                                Map<String, String> params = new HashMap<>();
                                params.put("uid", imanFile.getUid());
                                String url = resourceUtil.getHttpUrl("showImanFile", params);
//                                return "redirect:" + url;
                                response.setState("S");
                                response.setUrl(url);
                                return response;
                            }
                        }
                    }
                }
            }
        }
        response.setErrorMsg("该图纸没有PDF文件！");
        response.setState("E");
        return response;
    }
}
