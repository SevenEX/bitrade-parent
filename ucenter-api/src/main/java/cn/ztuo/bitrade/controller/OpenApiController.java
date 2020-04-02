package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.annotation.SecurityVerification;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.MemberApiKey;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.MemberApiKeyService;
import cn.ztuo.bitrade.util.GeneratorUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @description: OpenApiController
 * @author: MrGao
 * @create: 2019/05/07 10:33
 */
@Slf4j
@RestController
@RequestMapping("open")
@Api(tags = "OpenApi")
public class OpenApiController extends BaseController {

    @Autowired
    private MemberApiKeyService memberApiKeyService;

    /**
     * 获取ApiKey
     * @param member
     * @return
     */
    @RequestMapping(value = "get_key",method = RequestMethod.GET)
    @ApiOperation(value = "获取ApiKey")
    @MultiDataSource(name = "second")
    public MessageResult queryApiKey(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member){
        List<MemberApiKey> result = memberApiKeyService.findAllByMemberId(member.getId());
        return success(result);
    }


    /**
     * 新增api-key
     * @param member
     * @param memberApiKey
     * @return
     */
    @RequestMapping(value = "api/save",method = RequestMethod.POST)
    @SecurityVerification(SysConstant.TOKEN_API_BIND)
    @ApiOperation(value = "新增api-key")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "remark", value = "备注", required = true, dataType = "String"),
            @ApiImplicitParam(name = "bindIp", value = "绑定ip 多个以逗号分割", required = false, dataType = "String"),
            @ApiImplicitParam(name = "powerLimit", value = "权限（0、读写、1、提币、2、交易 多个以逗号分割）", required = false, dataType = "String"),
    })
    public MessageResult saveApiKey(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,MemberApiKey memberApiKey){
        log.info("-------新增API-key:"+ JSONObject.toJSONString(memberApiKey));
        List<MemberApiKey> all = memberApiKeyService.findAllByMemberId(member.getId());
        if (all.isEmpty() || all.size()<5){
            memberApiKey.setId(null);
            if (StringUtils.isBlank(memberApiKey.getBindIp())){
                //不绑定IP时默认90天过期
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH,90);
                memberApiKey.setExpireTime(calendar.getTime());
            }
            memberApiKey.setApiName(member.getId()+"");
            memberApiKey.setApiKey(GeneratorUtil.getUUID());
            String secret = GeneratorUtil.getUUID();
            memberApiKey.setSecretKey(secret);
            memberApiKey.setMemberId(member.getId());
            memberApiKey.setCreateTime(new Date());
            memberApiKey.setStatus(CommonStatus.NORMAL);
            memberApiKeyService.save(memberApiKey);
            return success(msService.getMessage("ADD_SUCCESS"),memberApiKey);
        }else {
            return error(msService.getMessage("MAX_AMOUNT_OVER"));
        }
    }


    /**
     * 修改API-key
     * @param member
     * @param memberApiKey
     * @return
     */
    @RequestMapping(value = "api/update",method = RequestMethod.POST)
    @ApiOperation(value = "修改API-key")
    public MessageResult updateApiKey(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,MemberApiKey memberApiKey){
        log.info("-------修改API-key:"+ JSONObject.toJSONString(memberApiKey));
        if (memberApiKey.getId() != null){
            MemberApiKey findMemberApiKey = memberApiKeyService.findByMemberIdAndId(member.getId(),memberApiKey
                    .getId());
            if (findMemberApiKey != null){
                if (!memberApiKey.getRemark().equals(findMemberApiKey.getRemark())){
                    findMemberApiKey.setRemark(memberApiKey.getRemark());
                }
                if (StringUtils.isNotEmpty(findMemberApiKey.getBindIp())&&StringUtils.isNotEmpty(memberApiKey.getBindIp())){
                    findMemberApiKey.setBindIp(memberApiKey.getBindIp());
                }else if(StringUtils.isNotEmpty(findMemberApiKey.getBindIp())&&StringUtils.isEmpty(memberApiKey.getBindIp())){
                    findMemberApiKey.setBindIp(null);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH,90);
                    findMemberApiKey.setExpireTime(calendar.getTime());
                }else if(StringUtils.isEmpty(findMemberApiKey.getBindIp())&&StringUtils.isNotEmpty(memberApiKey.getBindIp())){
                    findMemberApiKey.setBindIp(memberApiKey.getBindIp());
                    findMemberApiKey.setExpireTime(null);
                }
                if(StringUtils.isNotEmpty(memberApiKey.getPowerLimit())) {
                    findMemberApiKey.setPowerLimit(memberApiKey.getPowerLimit());
                }

                memberApiKeyService.save(findMemberApiKey);
                return success(msService.getMessage("UPDATE_SUCCESS"));
            }else {
                return error(msService.getMessage("RECORD_NOT_EXIST"));

            }
        }else {
            return error(msService.getMessage("RECORD_NOT_EXIST"));
        }

    }


    /**
     * 删除API-key
     * @param member
     * @param id
     * @return
     */
    @RequestMapping(value = "api/del/{id}",method = RequestMethod.GET)
    @ApiOperation(value = "删除API-key")
    public MessageResult updateApiKey(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable("id")Long id){
        log.info("------删除api-key：memberId={},id={}",member.getId(),id);
        memberApiKeyService.del(id);
        return success(msService.getMessage("DELETE_ADDRESS_SUCCESST"));
    }

}
