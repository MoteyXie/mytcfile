package com.motey.tcfile.mapper;

import com.motey.tcfile.model.ComponentContext;
import com.motey.tcfile.model.ImanFileContext;
import com.motey.tcfile.model.ItemComponentContext;
import com.motey.tcfile.model.ItemRevisionComponentContext;
import com.teamcenter.soa.client.model.strong.ImanFile;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ComponentMapper {

	public ComponentContext askComponentContext(@Param("puid") String puid) throws Exception;
	
	public ItemComponentContext askItemComponentContext(
			@Param("puid") String puid,
			@Param("itemId")String itemId) throws Exception;
	
	public List<ItemRevisionComponentContext> askItemRevisionComponentContext(
			@Param("puid") String puid,
			@Param("itemId")String itemId,
			@Param("itemRevId")String itemRevId,
			@Param("hasStatus")boolean hasStatus
	) throws Exception;
	
	@MapKey("objectPuid")
	public Map<String, ComponentContext> askReferences(@Param("parentPuid") String parentPuid) throws Exception;
	
	@MapKey("objectPuid")
	public Map<String, ComponentContext> askContents(@Param("parentPuid") String parentPuid) throws Exception;

	public String askField(@Param("puid") String puid, @Param("tableName") String tableName, @Param("fieldName") String fieldName) throws Exception;

	public List<ItemRevisionComponentContext> askRepresentedByFromPart(
			@Param("partId") String partId,
			@Param("partRevId") String partRevId,
			@Param("objectTypeName") String objectTypeName) throws  Exception;

	public boolean isDataset(@Param("puid") String puid) throws Exception;

	public List<ImanFileContext> askImanFiles(@Param("datasetUid")String datasetUid) throws Exception;
}
