package cn.ztuo.bitrade.system;

import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.DataDictionary;
import cn.ztuo.bitrade.service.DataDictionaryService;
import cn.ztuo.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:21
 */
@RestController
@RequestMapping("data-dictionary")
public class DataDictionaryContrller extends BaseController {
    @Autowired
    private DataDictionaryService service;

    @GetMapping("{bond}")
    public MessageResult get(@PathVariable("bond") String bond) {
        DataDictionary data = service.findByBond(bond);
        if (data == null){
            return error("validate bond");
        }
        MessageResult result=MessageResult.success();
        Map map=new HashMap();
        map.put("value",data.getValue());
        map.put("imgUrl",data.getImgUrl());
        result.setData(map);
        return result;
    }

}
