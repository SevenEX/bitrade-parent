package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.entity.FavorSymbol;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.FavorSymbolService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;


@Slf4j
@RestController
@RequestMapping("/favor")
@Api(tags = "自选")
public class FavorController {
    @Autowired
    private FavorSymbolService favorSymbolService;

    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * 添加自选
     * @param member
     * @param symbol
     * @return
     */
    @RequestMapping(value = "add",method = RequestMethod.POST)
    @ApiOperation(value = "添加自选")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "交易对", required = true, dataType = "String")
    })
    public MessageResult addFavor(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, String symbol){
        if(StringUtils.isEmpty(symbol)){
            return MessageResult.error(500, msService.getMessage("SYMBOL_CANNOT_EMPTY"));
        }
        FavorSymbol favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(),symbol);
        if(favorSymbol != null){
            return MessageResult.error(500, msService.getMessage("SYMBOL_ALREADY_FAVORED"));
        }
        FavorSymbol favor =  favorSymbolService.add(member.getId(),symbol);
        if(favor!= null){
            return MessageResult.success( msService.getMessage("SUCCESS"));
        }
        return MessageResult.error(500, msService.getMessage("ERROR"));
    }

    /**
     * 查询当前用户自选
     * @param member
     * @return
     */
    @RequestMapping(value = "find",method = RequestMethod.POST)
    @ApiOperation(value = "查询当前用户自选")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinName", value = "计价币种", required = true, dataType = "String")
    })
    @MultiDataSource(name = "second")
    public List<FavorSymbol> findFavor(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,String coinName){
        return favorSymbolService.findByMemberIdAndCoinName(member.getId(),coinName);
    }

    /**
     * 删除自选
     * @param member
     * @param symbol
     * @return
     */
    @RequestMapping(value = "delete",method = RequestMethod.POST)
    @ApiOperation(value = "删除自选")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "交易对", required = true, dataType = "String")
    })
    public MessageResult deleteFavor(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,String symbol){
        if(StringUtils.isEmpty(symbol)){
            return MessageResult.error(500, msService.getMessage("SYMBOL_CANNOT_EMPTY"));
        }
        FavorSymbol favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(),symbol);
        if(favorSymbol == null){
            return MessageResult.error(500, msService.getMessage("FAVOR_NOT_EXISTS"));
        }
        favorSymbolService.delete(member.getId(),symbol);
        return MessageResult.success( msService.getMessage("SUCCESS"));
    }
}
