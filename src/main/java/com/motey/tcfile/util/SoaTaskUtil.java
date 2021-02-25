package com.motey.tcfile.util;

import java.util.ArrayList;
import java.util.List;

import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCSignoffOriginType;
import com.teamcenter.services.internal.loose.core.ICTService;
import com.teamcenter.services.internal.loose.core._2011_06.ICT.Arg;
import com.teamcenter.services.internal.loose.core._2011_06.ICT.InvokeICTMethodResponse;
import com.teamcenter.services.loose.workflow.WorkflowService;
import com.teamcenter.services.loose.workflow._2014_06.Workflow.ApplySignatureInput;
import com.teamcenter.services.loose.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.loose.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.soa.client.model.ModelManager;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Signoff;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.soaictstubs.ICCTException;
import com.teamcenter.soaictstubs.ICCTUser;
import com.teamcenter.soaictstubs.TcUtility;
import com.teamcenter.soaictstubs.uidSeqValue_uHolder;


public class SoaTaskUtil {
	
	public static final String DO_TASK_COMPLETED_ACTION = "SOA_EPM_complete_action";
	public static final String DO_TASK_FAILED_ACTION = "SOA_EPM_fail_action";
	public static final String REVIEW_TASK_APPROVED_ACTION = "SOA_EPM_approve_action";
	public static final String REVIEW_TASK_REJECTED_ACTION = "SOA_EPM_reject_action";
	public static final String REVIEW_TASK_APPROVED_RESULT = "SOA_EPM_approve";
	public static final String REVIEW_TASK_REJECTED_RESULT = "SOA_EPM_reject";
	
	private SoaSession soaSession;
	private SoaPropertyManager propertyManager;
	
	public SoaTaskUtil(SoaSession soaSession) {
		this.soaSession = soaSession;
		this.propertyManager = soaSession.getPropertyManager();
	}
	
	public void doTaskComplete(ModelObject modelObject, String comment){
		doTaskAction(modelObject, DO_TASK_COMPLETED_ACTION, comment);
	}
	
	public void doTaskFailed(ModelObject modelObject, String comment){
		doTaskAction(modelObject, DO_TASK_FAILED_ACTION, comment);
	}
	
	public void reviewTaskApproved(ModelObject modelObject, String userId, String comment) throws Exception{
		reviewTaskAction(modelObject, getUserSignOff(modelObject, userId), REVIEW_TASK_APPROVED_ACTION, REVIEW_TASK_APPROVED_RESULT, comment);
	}
	
	public void reviewTaskRejected(ModelObject modelObject, String userId, String comment) throws Exception{
		reviewTaskAction(modelObject, getUserSignOff(modelObject, userId), REVIEW_TASK_REJECTED_ACTION, REVIEW_TASK_REJECTED_RESULT, comment);
	}
	
	/**
	 * 条件节点
	 * @param modelObject 任务对象
	 * @param condition 条件，可通过此类中的getCondition()方法获取所有条件
	 * @param comment 注释
	 */
	@SuppressWarnings("deprecation")
	public void conditionTaskAction(ModelObject modelObject, String condition, String comment){
		
		WorkflowService localWorkflowService = WorkflowService.getService(soaSession.getConnection());
		ApplySignatureInput[] arrayOfApplySignatureInput = new ApplySignatureInput[0];
		
		localWorkflowService.performActionWithSignature(
				modelObject,
				DO_TASK_COMPLETED_ACTION, 
				comment,
				null,
				condition, 
				modelObject, 
				arrayOfApplySignatureInput);
		
	}
	
	/**
	  *   审核节点
	 * @param modelObject
	 * @param action
	 * @param result
	 * @param comment
	 * @throws Exception 
	 */
	public void reviewTaskAction(ModelObject modelObject, String signoffUserId, String action, String result, String comment) throws Exception{
		ModelObject userSignoff = getUserSignOff(modelObject, signoffUserId);
		if(userSignoff == null) {
			throw new Exception("未找到审核者【"+signoffUserId+"】");
		}
		reviewTaskAction(modelObject, userSignoff, action, result, comment);
		
	}
	
	@SuppressWarnings("deprecation")
	public void reviewTaskAction(ModelObject task, ModelObject userSignoff, String action, String result, String comment) throws Exception{
		
		if(userSignoff == null) {
			throw new Exception("未找到审核者");
		}
//		propertyManager.refreshObject(task);
		WorkflowService localWorkflowService = WorkflowService.getService(soaSession.getConnection());
		ApplySignatureInput[] arrayOfApplySignatureInput = new ApplySignatureInput[0];
		
		localWorkflowService.performActionWithSignature(
				task,
				action,
				comment,
				null,
				result, 
				userSignoff, 
				arrayOfApplySignatureInput);
		
	}
	
	/**
	 *
	 * @param taskObject
	 * @return
	 * @throws Exception 
	 */
	public Signoff getUserSignOff(ModelObject taskObject, String userId) throws Exception {
		
		try {
			propertyManager.refreshObject(taskObject);
			ModelObject[] mos = propertyManager.getModelObjectArrayProperty(taskObject, "user_all_signoffs");
			for (ModelObject mo : mos) {
//				if(MySession.getUserName().equals(MyProperty.getStringProperty(mo, "fnd0Assignee")))
				Signoff so = (Signoff)mo;
				ModelObject user = propertyManager.getModelObjectProperty(so, "fnd0Performer");
				String signoffUserId = propertyManager.getStringProperty(user, "userid");
				if(signoffUserId.equals(userId)  || userId == null)return so;
				return so;	
				
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param taskModel 要执行的任务 ，必须为DO节点
	 * @param command 执行动作  DO_TASK_COMPLETED | DO_TASK_FAILED
	 * @param comment 注释
	 */
	@SuppressWarnings("deprecation")
	public void doTaskAction(ModelObject modelObject, String command, String comment){
		propertyManager.refreshObject(modelObject);
		WorkflowService localWorkflowService = WorkflowService.getService(soaSession.getConnection());
		ApplySignatureInput[] arrayOfApplySignatureInput = new ApplySignatureInput[0];
		
		localWorkflowService.performActionWithSignature(
				modelObject,
				command, comment, null,
				"SOA_EPM_completed", 
				modelObject, 
				arrayOfApplySignatureInput);
		
	}
	
	public String[] getConditions(ModelObject taskObject){
		
		List<String> conditions = new ArrayList<>();
		
		propertyManager.refreshObject(taskObject);
		
		try {
			ModelObject template = propertyManager.getModelObjectProperty(taskObject, "task_template");
			ModelObject[] successors = propertyManager.getModelObjectArrayProperty(taskObject, "successors");
			
			
			for(int i = 0; i < successors.length; i++){
				
				String condition = getCheckCondition(template, successors[i]);
				if(condition != null)conditions.add(condition);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conditions.toArray(new String[conditions.size()]);
	}
	
	public String getCheckCondition(ModelObject template, ModelObject successor) throws Exception{
		
		String templateName = propertyManager.getStringProperty(template, "object_name");
		templateName = "-source_task="+templateName;
		ModelObject successorTemplate = propertyManager.getModelObjectProperty(successor, "task_template");
		ModelObject[] actions = propertyManager.getModelObjectArrayProperty(successorTemplate, "actions");
		ModelObject[] rules = null;
		ModelObject[] handlers = null;
		
		for(int i = 0; i < actions.length; i++){
			
			rules = propertyManager.getModelObjectArrayProperty(actions[i], "rules");
			if(rules == null || rules.length == 0)continue; 
			
			for(int j = 0; j < rules.length; j++){
				
				handlers = propertyManager.getModelObjectArrayProperty(rules[j], "rule_handlers");
				
				for(int k = 0; k < handlers.length; k++){
					
					if("EPM-check-condition".equals(propertyManager.getStringProperty(handlers[k], "object_name"))){
						
						String arguments = propertyManager.getStringProperty(handlers[k], "arguments");
						if(arguments.contains(templateName)){
							String a = arguments.replace(templateName, "").trim();
							a = a.replace("-decision=", "").trim();
							return a;
						}
						
					}
				}
				
			}
		}
	    
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public void completeSignoff(ModelObject task, String userId, String password, String commit) throws Exception {
		WorkflowService workflowService = WorkflowService.getService(soaSession.getConnection());
		SoaPropertyManager spm = soaSession.getPropertyManager();
//		PerformActionInputInfo input = new PerformActionInputInfo();
		
//		input.password = password;
//		input.clientId = userId;
//		System.out.println("password:" + input.password);
//		System.out.println("clientId:" + input.clientId);
//		input.actionableObject = task;
//		input.action = "Completed";
//		workflowService.performAction3(new PerformActionInputInfo[] {input});
		ApplySignatureInput[] arrayOfApplySignatureInput = new ApplySignatureInput[0];
		workflowService.performActionWithSignature(
				task, 
				getSOAActionString(4),
				commit, 
				null, 
				getSOAResultString("Completed", task), 
				task, 
				arrayOfApplySignatureInput);
	}
	
	public void createSignoff(ModelObject task, ModelObject signoffMember, TCSignoffOriginType signoffOriginType, ModelObject profile, boolean paramBoolean, int paramInt) throws Exception {
		
		WorkflowService localWorkflowService = WorkflowService.getService(soaSession.getConnection());
		
	    CreateSignoffInfo localCreateSignoffInfo = new CreateSignoffInfo();
	    
	    String originType = "SOA_EPM_ORIGIN_UNDEFINED";
	    int i = 0;
	    
	    if (signoffOriginType == TCSignoffOriginType.PROFILE) {
	    	originType = "SOA_EPM_SIGNOFF_ORIGIN_PROFILE";
	    } else if (signoffOriginType == TCSignoffOriginType.ADDRESSLIST) {
	    	originType = "SOA_EPM_SIGNOFF_ORIGIN_ADDRESSLIST";
	    }
	    String signoffAction = "SOA_EPM_Review";
	    if (paramInt == 2) {
	    	signoffAction = "SOA_EPM_Acknowledge";
	    } else if (paramInt == 3) {
	    	signoffAction = "SOA_EPM_Notify";
	    }
	    
	    
	    localCreateSignoffInfo.signoffMember = signoffMember;
	    localCreateSignoffInfo.signoffAction = signoffAction;
	    localCreateSignoffInfo.originType = originType;
	    localCreateSignoffInfo.origin = profile;
	    if (paramBoolean) {
	    	localCreateSignoffInfo.signoffRequired = "SOA_EPM_SIGNOFF_REQUIRED_MODIFIABLE";
	    } else {
	    	localCreateSignoffInfo.signoffRequired = "SOA_EPM_SIGNOFF_OPTIONAL";
	    }
	    
	    CreateSignoffInfo[] arrayOfCreateSignoffInfo = new CreateSignoffInfo[1];
	    arrayOfCreateSignoffInfo[0] = localCreateSignoffInfo;
	    CreateSignoffs localCreateSignoffs = new CreateSignoffs();
	    localCreateSignoffs.signoffInfo = arrayOfCreateSignoffInfo;
	    localCreateSignoffs.task = task;
	      
	    CreateSignoffs[] arrayOfCreateSignoffs = new CreateSignoffs[1];
	    arrayOfCreateSignoffs[0] = localCreateSignoffs;
	    ServiceData localServiceData = (ServiceData)localWorkflowService.addSignoffs(arrayOfCreateSignoffs);
	    SoaUtil.checkPartialErrors(localServiceData);
	    
	    if ((i = localServiceData.sizeOfCreatedObjects()) > 0)
	      {
	        ArrayList localArrayList = new ArrayList(i);
	        ModelObject mo = null;
	        for (int j = 0; j < i; j++)
	        {
	          mo = localServiceData.getCreatedObject(j);
	          System.out.println(mo.getTypeObject().getClassName());
//	          if ((localTCComponent instanceof TCComponentSignoff)) {
//	            localArrayList.add((TCComponentSignoff)localTCComponent);
//	          }
	        }
//	        if (localArrayList.size() > 0) {
//	          arrayOfTCComponentSignoff = (TCComponentSignoff[])localArrayList.toArray(new TCComponentSignoff[localArrayList.size()]);
//	        }
	      }
	    
	}
	 
	public void getGroupMembers(String userUid, uidSeqValue_uHolder members, uidSeqValue_uHolder memberTypes) throws Exception {
		ICTService m_service = ICTService.getService( soaSession.getConnection() );
		Arg[] args_ = new Arg[3];
	    args_[0] = TcUtility.createArg("User");
	    args_[1] = TcUtility.createArg("TYPE::User::User::POM_user");
	    args_[2] = TcUtility.createArg(userUid);
	    InvokeICTMethodResponse response = m_service.invokeICTMethod("ICCTUser", "getGroupMembers", args_);
	    if( response.serviceData.sizeOfPartialErrors() > 0)
	    {
	      throw new ICCTException( response.serviceData);
	    }
	    members.value = TcUtility.queryArg(response.output[0], members.value);
	    memberTypes.value = TcUtility.queryArg(response.output[1], memberTypes.value);
	  }
	
	public ModelObject[] getUserGroupMembers(String userUid) throws Exception {
		
		ModelManager modelManager = soaSession.getConnection().getModelManager();
		ModelObject user = modelManager.constructObject(userUid);
		String cls = user.getTypeObject().getClassName();
		System.out.println(cls);
		ICCTUser icct = new ICCTUser(soaSession.getConnection(), "User", "TYPE::User::User::POM_user");
		uidSeqValue_uHolder localuidSeqValue_uHolder1 = new uidSeqValue_uHolder();
	    uidSeqValue_uHolder localuidSeqValue_uHolder2 = new uidSeqValue_uHolder();
	    
//	    icct.getGroupMembers(pm.getStringProperty(user, "user_id"), localuidSeqValue_uHolder1, localuidSeqValue_uHolder2);
	    getGroupMembers(userUid, localuidSeqValue_uHolder1, localuidSeqValue_uHolder2);
	    
	    List<ModelObject> localList = componentVector(localuidSeqValue_uHolder1, localuidSeqValue_uHolder2);
	    
	    return localList.toArray(new ModelObject[localList.size()]);
	}
	
	protected List<ModelObject> componentVector(uidSeqValue_uHolder holder1, uidSeqValue_uHolder holder2)
	  {
	    ArrayList<ModelObject> localArrayList = new ArrayList<>();
	    if (holder1.value.is_seqValue())
	    {
	    	ModelManager modelManager = soaSession.getConnection().getModelManager();
	    	
	    	String[] values = holder1.value.seqValue();
	    	for(int i = 0; i < values.length; i++) {
	    		try {
	    			localArrayList.add(modelManager.constructObject(values[i]));
	    		}catch(Exception e) {
	    			e.printStackTrace();
	    		}
	    		
	    	}
	    }
	    return localArrayList;
	  }
	
	  public String getSOAActionString(int paramInt)
	  {
	    String str = "SOA_EPM_no_action";
	    if (paramInt == 1) {
	      str = "SOA_EPM_assign_action";
	    } else if (paramInt == 2) {
	      str = "SOA_EPM_start_action";
	    } else if (paramInt == 4) {
	      str = "SOA_EPM_complete_action";
	    } else if (paramInt == 5) {
	      str = "SOA_EPM_skip_action";
	    } else if (paramInt == 6) {
	      str = "SOA_EPM_suspend_action";
	    } else if (paramInt == 7) {
	      str = "SOA_EPM_resume_action";
	    } else if (paramInt == 8) {
	      str = "SOA_EPM_undo_action";
	    } else if (paramInt == 9) {
	      str = "SOA_EPM_abort_action";
	    } else if (paramInt == 10) {
	      str = "SOA_EPM_fail_action";
	    } else if (paramInt == 100) {
	      str = "SOA_EPM_perform_action";
	    } else if (paramInt == 101) {
	      str = "SOA_EPM_add_attachment_action";
	    } else if (paramInt == 1102) {
	      str = "SOA_EPM_remove_attachment_action";
	    } else if (paramInt == 104) {
	      str = "SOA_EPM_approve_action";
	    } else if (paramInt == 105) {
	      str = "SOA_EPM_reject_action";
	    } else if (paramInt == 106) {
	      str = "SOA_EPM_promote_action";
	    } else if (paramInt == 107) {
	      str = "SOA_EPM_demote_action";
	    } else if (paramInt == 108) {
	      str = "SOA_EPM_refuse_action";
	    } else if (paramInt == 109) {
	      str = "SOA_EPM_assign_approver_action";
	    }
	    return str;
	  }
	  
	  public String getSOAResultString(String paramString, ModelObject modelObject)
	  {
	    String str1 = null;
	    String str2 = modelObject.getTypeObject().getClassName();
	      if ((str2 != null) && (str2.equals("EPMConditionTask")))
	      {
	        str1 = paramString;
	      }
	      else if (paramString.equals("UnableToComplete"))
	      {
	        str1 = "SOA_EPM_unable_to_complete";
	      }
	      else if (paramString.equals("Completed"))
	      {
	        str1 = "SOA_EPM_completed";
	      }
	      else if (paramString.equals("true"))
	      {
	        str1 = "SOA_EPM_true";
	      }
	      else if (paramString.equals("false"))
	      {
	        str1 = "SOA_EPM_false";
	      }
	      else if (paramString.equals("NoError"))
	      {
	        str1 = "SOA_EPM_no_error";
	      }
	      else if (paramString.equals("Approve"))
	      {
	        str1 = "SOA_EPM_approve";
	      }
	      else if (paramString.equals("Reject"))
	      {
	        str1 = "SOA_EPM_reject";
	      }
	      else if (paramString.equals("No Decision"))
	      {
	        str1 = "SOA_EPM_no_decision";
	      }
	      else if (paramString.equals("Unset"))
	      {
	        str1 = "SOA_EPM_unset";
	      }
//	      else if ((modelObject != null) && (modelObject.isTypeOf("Signoff")))
//	      {
//	        TCComponentSignoff localTCComponentSignoff = (TCComponentSignoff)paramTCComponent;
//	        if (paramString.equals(localTCComponentSignoff.getApproveDecision().toString())) {
//	          str1 = "SOA_EPM_approve";
//	        } else if (paramString.equals(localTCComponentSignoff.getRejectDecision().toString())) {
//	          str1 = "SOA_EPM_reject";
//	        } else if (paramString.equals(localTCComponentSignoff.getNoDecision().toString())) {
//	          str1 = "SOA_EPM_no_decision";
//	        }
//	      }
	      else
	      {
	        str1 = paramString;
	      }
	    
	      return str1;
	  }
	
}
