package com.motey.tcfile.controller;

import com.motey.tcfile.TeamcenterContext;
import com.motey.tcfile.model.*;
import com.motey.tcfile.util.*;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TcController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    private TeamcenterContext teamcenterContext;

    @Autowired
    public void setTeamcenterContext(TeamcenterContext teamcenterContext) {
        this.teamcenterContext = teamcenterContext;
    }

    @RequestMapping(value = "/download")
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
            while(i != -1) {
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
            if(bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequestMapping(value = "/downloadDataset")
    public String downloadDataset(HttpServletResponse response, String imanFileUid) throws Exception {

        SoaSession soaSession = SoaSessionManager.getSession(
                teamcenterContext.getTc_address(),
                teamcenterContext.getTc_port(),
                teamcenterContext.getUser(),
                teamcenterContext.getPass());

        SoaDatasetUtil datasetUtil = new SoaDatasetUtil(soaSession);

        ModelObject modelObject = soaSession.getPropertyManager().getModelObject(imanFileUid);

        if(!(modelObject instanceof ImanFile)) {
            return "此uid并非ImanFile，无法下载！";
        }

        String path = datasetUtil.download((ImanFile)modelObject, "C:/temp");

        return download(response, path);
    }

    @RequestMapping(value = "/askDrawingFromPart")
    public AskDrawingFromPartResponse askDrawingFromPart(String partId, String partRevId) throws Exception {

        SoaSession soaSession = SoaSessionManager.getSession(
                teamcenterContext.getTc_address(),
                teamcenterContext.getTc_port(),
                teamcenterContext.getUser(),
                teamcenterContext.getPass());

        SoaItemUtil itemUtil = new SoaItemUtil(soaSession);

        Item item = itemUtil.findItem(partId);

        AskDrawingFromPartResponse response = new AskDrawingFromPartResponse();

        if(item == null){
            //查询结果为空时的处理
            response.setMessage("物料编码【"+partId+"】未找到");
            response.setSuccess(false);
            return response;
        }else{

            SoaPropertyManager propertyManager = soaSession.getPropertyManager();
            SoaDatasetUtil datasetUtil = new SoaDatasetUtil(soaSession);

            ItemRevision[] itemRevisions = null;
            //获取版本
            if(partRevId != null && partRevId.length() > 0){
                itemRevisions = new ItemRevision[1];
                itemRevisions[0] = itemUtil.getItemRevision(item, partRevId);
            }else{
                itemRevisions = itemUtil.getDisplayableItemRevisions(item);
            }

            if(itemRevisions == null || itemRevisions.length == 0){
                //itemRevision为空时的处理
                response.setMessage("物料编码【"+partId+"】下未找到可用版本。");
                response.setSuccess(false);
                return response;
            }

            TcPart[] tcParts = new TcPart[itemRevisions.length];

            response.setParts(tcParts);

            for (int i = 0; i < itemRevisions.length; i++) {

                tcParts[i] = new TcPart();

                String itemId = propertyManager.getStringProperty(itemRevisions[i], "item_id");
                String name = propertyManager.getStringProperty(itemRevisions[i], "object_name");
                String itemRevisionId = propertyManager.getStringProperty(itemRevisions[i], "item_revision_id");

                tcParts[i].setUid(itemRevisions[i].getUid());
                tcParts[i].setItemId(itemId);
                tcParts[i].setName(name);
                tcParts[i].setItemRevisionId(itemRevisionId);

                System.out.println("ItemRevision is " + name);

                ModelObject[] designRevisions = propertyManager.getModelObjectArrayProperty(itemRevisions[i], "TC_Is_Represented_By");

                if(designRevisions == null || designRevisions.length == 0) {
                    //关联图纸对象为空时的处理
                    continue;
                }

                tcParts[i].setDrawings(new TcDrawing[designRevisions.length]);

                //获取数据集
                for (int j = 0; j < designRevisions.length; j++) {

                    String designItemId = propertyManager.getStringProperty(designRevisions[j], "item_id");
                    String designName = propertyManager.getStringProperty(designRevisions[j], "object_name");
                    String designRevId = propertyManager.getStringProperty(designRevisions[j], "item_revision_id");

                    tcParts[i].getDrawings()[j] = new TcDrawing();
                    tcParts[i].getDrawings()[j].setUid(designRevisions[j].getUid());
                    tcParts[i].getDrawings()[j].setItemId(designItemId);
                    tcParts[i].getDrawings()[j].setName(designName);
                    tcParts[i].getDrawings()[j].setItemRevisionId(designRevId);

                    List<ModelObject> relateds = itemUtil.getRelatedComponents(designRevisions[j], "IMAN_specification", "IMAN_reference","IMAN_Rendering");

                    if(relateds == null || relateds.size() == 0) {
                        continue;
                    }

                    List<TcDataset> datasetList = new ArrayList<>();

                    for (int k = 0; k < relateds.size(); k++) {

                        ModelObject related = relateds.get(k);

                        if(related instanceof Dataset){

                            TcDataset tcDataset = new TcDataset();
                            String rname = propertyManager.getStringProperty(related, "object_string");
                            System.out.println("related name = " + rname);
                            tcDataset.setName(rname);
                            tcDataset.setUid(related.getUid());
                            datasetList.add(tcDataset);

                            List<ImanFile> imanFiles = datasetUtil.getImanFiles(related);
                            List<TcImanFile> tcImanFileList = new ArrayList<>();

                            if(imanFiles != null && imanFiles.size() > 0) {

                                for (ImanFile imanFile : imanFiles) {

                                    TcImanFile tcImanFile = new TcImanFile();

                                    String fileName = propertyManager.getStringProperty(imanFile, "original_file_name");
                                    String fileSize = propertyManager.getStringProperty(imanFile, "file_size");
                                    String mimeType = propertyManager.getStringProperty(imanFile, "mime_type");

                                    tcImanFile.setUid(imanFile.getUid());
                                    tcImanFile.setName(fileName);
                                    tcImanFile.setFileSize(fileSize);
                                    tcImanFile.setMimeType(mimeType);

                                    tcImanFileList.add(tcImanFile);
                                }
                            }

                            tcDataset.setImanFiles(tcImanFileList.toArray(new TcImanFile[tcImanFileList.size()]));
                        }
                    }

                    tcParts[i].getDrawings()[j].setDatasets(datasetList.toArray(new TcDataset[datasetList.size()]));

                }
            }
            response.setSuccess(true);
        }
        return response;
    }
}
