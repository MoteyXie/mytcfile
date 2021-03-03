package com.motey.tcfile.controller;

import com.motey.tcfile.TeamcenterContext;
import com.motey.tcfile.util.ResourceUtil;
import com.motey.tcfile.util.SoaDatasetUtil;
import com.motey.tcfile.util.SoaSession;
import com.motey.tcfile.util.SoaSessionManager;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.ImanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

@Controller
public class ResourceController {

	private TeamcenterContext teamcenterContext;

	@Autowired
	public void setTeamcenterContext(TeamcenterContext teamcenterContext) {
		this.teamcenterContext = teamcenterContext;
	}
	@Resource
	private ResourceUtil resourceUtil;

	@RequestMapping(value = "/showImanFile")
	public String showImanFile(String uid) throws Exception {

		SoaSession soaSession = SoaSessionManager.getSession(
				teamcenterContext.getTc_address(),
				teamcenterContext.getTc_port(),
				teamcenterContext.getUser(),
				teamcenterContext.getPass());

		SoaDatasetUtil datasetUtil = new SoaDatasetUtil(soaSession);

		ModelObject modelObject = soaSession.getPropertyManager().getModelObject(uid);

		if (!(modelObject instanceof ImanFile)) {
			return "此uid并非ImanFile，无法下载！";
		}

		String path = datasetUtil.download((ImanFile) modelObject, resourceUtil.getUploadFolder());
		if(path == null){
			String fmsHome = System.getenv("FMS_HOME");
			throw new Exception("Current FMS_HOME is ["+fmsHome + "];\n" + datasetUtil.getLastErrorMsg());
		}
		return "redirect:" + resourceUtil.getResourceUrl(path, false);
	}
	
	@RequestMapping(value = "/icon")
	public String askIcon(String type, String name) throws UnsupportedEncodingException {
		
		String f = "";
		if(resourceUtil.hasFile(resourceUtil.getImagePath(type, name))) {
			f = "redirect:"+resourceUtil.getImageRelativeUrl(type, name);
			
		}else {
			f = "redirect:"+resourceUtil.getImageRelativeUrl(type, "Unknow");
		}
		System.out.println(f);
		return f;
	}
	
	//http://192.168.43.187:9002/askComponentIcon?typeClass=item&typeName=item
	@RequestMapping(value = "/askComponentIcon")
	public String askComponentIcon(String typeClass, String typeName) throws UnsupportedEncodingException {
		System.out.println(typeName);
		
		String type = "component";
		
		if(resourceUtil.hasFile(resourceUtil.getImagePath(type, typeName))) {
			return "redirect:"+resourceUtil.getImageRelativeUrl("component", typeName);
		}
		
		if(resourceUtil.hasFile(resourceUtil.getImagePath(type, typeClass))) {
			return "redirect:"+resourceUtil.getImageRelativeUrl("component", typeClass);
		}
		
		return "redirect:"+resourceUtil.getImageRelativeUrl("component", "Unknow");
		
	}
}
