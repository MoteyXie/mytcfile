package com.motey.tcfile;

import com.motey.tcfile.mapper.ComponentMapper;
import com.motey.tcfile.model.ItemComponentContext;
import com.motey.tcfile.model.ItemRevisionComponentContext;
import com.motey.tcfile.services.DBService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class TcfileApplicationTests {

    @Autowired
    DBService taskService;

    @Test
    void contextLoads() {
        try {
            ComponentMapper componentMapper = taskService.getComponentMapper();
//            ItemComponentContext itemComponentContext = componentMapper.askItemComponentContext("CTuBFZ7vLVoW2D");
//            System.out.println(itemComponentContext.getItemId());
            List<ItemRevisionComponentContext> contexts = componentMapper.askRepresentedByFromPart("I7GHH1250", "A", null);

            for (ItemRevisionComponentContext context: contexts) {
                System.out.println(context.getItemId() + ", " +context.getObjectName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
