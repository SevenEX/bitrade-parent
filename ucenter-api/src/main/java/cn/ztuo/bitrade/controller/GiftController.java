package cn.ztuo.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.entity.GiftRecord;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.GiftRecordService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.GiftRecordVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/29 11:14 AM
 */
@RestController
@RequestMapping("gift")
@Slf4j
@Api(tags = "糖果（暂弃）")
public class GiftController extends BaseController {

    @Autowired
    private GiftRecordService giftRecordService;


    /**
     * 查询个人糖果发放记录
     * @param giftRecordVO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "record",method = RequestMethod.POST)
    public MessageResult getGiftRecord(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, GiftRecordVO giftRecordVO) throws Exception{
        log.info("-------查询个人糖果记录----"+ JSONObject.toJSONString(giftRecordVO));
        giftRecordVO.setUserId(user.getId());
        Page<GiftRecord> result = giftRecordService.getByPage(giftRecordVO);
        return successDataAndTotal(result.getContent(),result.getTotalElements());
    }
}
