package cn.ztuo.bitrade.controller.gift;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.GiftConfig;
import cn.ztuo.bitrade.entity.GiftRecord;
import cn.ztuo.bitrade.service.GiftConfigService;
import cn.ztuo.bitrade.service.GiftRecordService;
import cn.ztuo.bitrade.util.JDBCUtils;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.GiftConfigVO;
import cn.ztuo.bitrade.vo.GiftRecordVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/29 7:35 PM
 */
@RestController
@RequestMapping("gift")
@Slf4j
@Api(tags = "糖果管理（暂弃）")
public class AdminGiftController extends BaseController {

    @Autowired
    private GiftConfigService giftConfigService;

    @Autowired
    private JDBCUtils jdbcUtils;

    @Autowired
    private GiftRecordService giftRecordService;


    /**
     * 分页查询糖果设置
     * @param giftConfigVO
     * @return
     * @throws Exception
     */
    @RequiresPermissions("gift:page-query")
    @RequestMapping(value = "page-query",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.GIFT, operation = "分页查询糖果设置")
    @MultiDataSource(name = "second")
    public MessageResult getByPage(@RequestBody GiftConfigVO giftConfigVO) throws Exception{
        log.info("-----------分页糖果设置:"+ JSONObject.toJSONString(giftConfigVO));
        Page<GiftConfig> result = giftConfigService.getByPage(giftConfigVO);
        return successDataAndTotal(result.getContent(),result.getTotalElements());
    }

    /**
     * 新增发放糖果
     * @param giftConfig
     * @return
     * @throws Exception
     */
    @RequiresPermissions("gift:save")
    @RequestMapping(value = "save",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.GIFT, operation = "新增发放糖果")
    public MessageResult saveGiftConfig(@RequestBody GiftConfig giftConfig) throws Exception{
        giftConfig.setCreateTime(new Date());
        giftConfig.setHaveAmount(BigDecimal.ZERO);
        GiftConfig item = giftConfigService.save(giftConfig);
        jdbcUtils.giftJDBC(item.getId());
        return success("发放成功");
    }

    /**
     * 分页查询糖果发放记录
     * @param giftRecordVO
     * @return
     * @throws Exception
     */
    @RequiresPermissions("gift:record:page-query")
    @RequestMapping(value = "record/page-query",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.GIFT, operation = "分页查询糖果发放记录")
    @MultiDataSource(name = "second")
    public MessageResult getByPage(@RequestBody GiftRecordVO giftRecordVO) throws Exception{
        log.info("-----------分页糖果设置:"+ JSONObject.toJSONString(giftRecordVO));
        Page<GiftRecord> result = giftRecordService.getByPage(giftRecordVO);
        return successDataAndTotal(result.getContent(),result.getTotalElements());
    }
}
