package com.motey.tcfile.controller;

import com.motey.tcfile.TeamcenterContext;
import com.motey.tcfile.model.JLAskDrawingFromPartResponse;
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
    public List<JLAskDrawingFromPartResponse> askDrawingFromParts(String partNo) throws Exception {
        partNo = partNo.replace("，", ",");
        String[] partNos = partNo.split(",");

        List<JLAskDrawingFromPartResponse> responseList = new ArrayList<>();
        for (String apartNo : partNos) {
            apartNo = apartNo.replace("\\", "/");

            String[] pp = apartNo.split("/");
            String no = pp[0];
            String revId = pp.length > 1 ? pp[1] : null;

            JLAskDrawingFromPartResponse resp = askDrawingFromPart(no, revId);
            responseList.add(resp);
        }

        return responseList;
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

        SoaItemUtil itemUtil = new SoaItemUtil(soaSession);

        Item item = itemUtil.findItem(partNo);

        if (item == null) {
            //查询结果为空时的处理
            response.setErrorMsg("物料编码【" + partNo + "】未找到");
            response.setState("error");
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
                response.setState("error");
                return response;
            }

            String itemId = propertyManager.getStringProperty(itemRevision, "item_id");
            String name = propertyManager.getStringProperty(itemRevision, "object_name");
            String itemRevisionId = propertyManager.getStringProperty(itemRevision, "item_revision_id");

            System.out.println("ItemRevision is " + name);

            //改成坚朗专用的图纸对象获取方式
            //ModelObject[] designRevisions = propertyManager.getModelObjectArrayProperty(itemRevision, "TC_Is_Represented_By");
            //TODO 获取之前先判断物料关联的图纸有几个？如果有多个的话就不继续了
            String partDesc = propertyManager.getStringProperty(itemRevision, "object_desc");
            String drawNo = null;
            try {
                drawNo = partDesc.split("/")[1];
            } catch (Exception e) {
            }

            ModelObject designItem = drawNo == null || drawNo.isEmpty() ? null : itemUtil.findItem(drawNo);

            if (designItem == null) {
                //关联图纸对象为空时的处理
                response.setErrorMsg("未找到关联图纸");
                response.setState("error");
                return response;
            }

            //TODO 现在是获取图纸的最大版本，可能要调整图纸的版本获取规则
            ItemRevision designRevision = itemUtil.getLatestItemRevision2((Item)designItem);

            String designRevNo = propertyManager.getStringProperty(designRevision, "item_revision_id");
            String designName = propertyManager.getStringProperty(designRevision, "object_name");
            String designStr = propertyManager.getStringProperty(designRevision, "object_string");
            response.setDrawingNo(drawNo);
            response.setDrawingRevNo(designRevNo);
            response.setDrawingName(designName);

            List<ModelObject> relateds = itemUtil.getRelatedComponents(designRevision, "IMAN_specification", "IMAN_reference", "IMAN_Rendering");

            if (relateds == null || relateds.size() == 0) {
                response.setErrorMsg("图纸【" + designStr + "】未找到关联图纸数据集");
                response.setState("error");
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
                                response.setState("succeed");
                                response.setUrl(url);
                                return response;
                            }
                        }
                    }
                }
            }
        }
        response.setErrorMsg("无法找到对应资源！");
        response.setState("error");
        return response;
    }
}
