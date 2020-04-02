package cn.ztuo.bitrade.controller.ieo;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.entity.EmptionRecord;
import cn.ztuo.bitrade.entity.IeoEmption;
import cn.ztuo.bitrade.service.EmptionRecordService;
import cn.ztuo.bitrade.service.IeoEmptionService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.EmptionRecrodVO;
import cn.ztuo.bitrade.vo.IeoEmptionVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/26 4:02 PM
 */
@RestController
@RequestMapping("ieo")
@Slf4j
@Api(tags = "IEO")
public class ieoController extends BaseController {

    @Autowired
    private IeoEmptionService ieoEmptionService;

    @Autowired
    private EmptionRecordService emptionRecordService;


    /**
     * 分页查询IEO
     * @param ieoEmptionVO
     * @return
     */
    @RequiresPermissions("ieo:page-query")
    @RequestMapping(value = "page-query",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.IEO, operation = "分页查找IEO")
    public MessageResult getByPage(@RequestBody IeoEmptionVO ieoEmptionVO){
        try {
        log.info("------分页查询ieo------"+ JSONObject.toJSONString(ieoEmptionVO));
        Page<IeoEmption> result = ieoEmptionService.getByPage(ieoEmptionVO);
        List<IeoEmption> ieoEmptionList = result.getContent();
        return successDataAndTotal(ieoEmptionList,result.getTotalElements());
        } catch (Exception e) {
            log.info("--------分页查询ieo异常={}",e);
        }
        return success();

    }

    /**
     * 保存/修改IEO
     * @param admin
     * @param ieoEmption
     * @return
     */
    @RequiresPermissions("ieo:save")
    @RequestMapping(value = "save",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.IEO, operation = "保存/修改IEO")
    public MessageResult saveIEO(@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,@RequestBody IeoEmption ieoEmption){
        log.info("------保存/修改IEO------"+ JSONObject.toJSONString(ieoEmption));
        Date date = new Date();
        if(ieoEmption.getId() == null){
            ieoEmption.setCreateTime(date);
            ieoEmption.setFee(BigDecimal.ZERO);
            ieoEmption.setSurplusAmount(ieoEmption.getSaleAmount());
            ieoEmption.setCreateUser(admin.getUsername());
            ieoEmptionService.save(ieoEmption);
            return success("保存成功");
        }
        ieoEmption.setFee(BigDecimal.ZERO);
        ieoEmption.setSurplusAmount(ieoEmption.getSaleAmount());
        ieoEmption.setUpdateTime(date);
        ieoEmption.setUpdateUser(admin.getUsername());
        ieoEmptionService.save(ieoEmption);
        return success("修改成功");
    }


    /**
     * 根据ID删除IEO
     * @return
     */
    @RequiresPermissions("ieo:del")
    @RequestMapping(value = "del/{id}",method = RequestMethod.GET)
    @AccessLog(module = AdminModule.IEO, operation = "删除IEO")
    public MessageResult delIEO(@PathVariable("id")Long id){
        IeoEmption ieoEmption = ieoEmptionService.findById(id);
        Date date = new Date();
        //判断当前时间大于开始时间时可以删除
        int result = DateUtil.compare(date,ieoEmption.getStartTime());
        if (result == 1){
            return error("无法删除");
        }
        ieoEmptionService.del(id);
        return success("删除成功");
    }





    /**
     * 分页查询IEO认购记录
     * @param emptionRecrodVO
     * @return
     */
    @RequiresPermissions("ieo:record:page-query")
    @RequestMapping(value = "record/page-query",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.IEO, operation = "分页查找IEO认购记录")
    public MessageResult getRecordByPage(@RequestBody EmptionRecrodVO emptionRecrodVO){
        log.info("------分页查找IEO认购记录------"+ JSONObject.toJSONString(emptionRecrodVO));
        try {
            Page<EmptionRecord> result = emptionRecordService.getByPage(emptionRecrodVO);
            return successDataAndTotal(result.getContent(),result.getTotalElements());
        } catch (Exception e) {
            log.info("--------分页查找IEO认购记录异常={}",e);
        }
        return success();


    }


}
