package cn.ztuo.bitrade.controller.exchange;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.dto.CoinAreaDTO;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.entity.CoinArea;
import cn.ztuo.bitrade.entity.QCoinArea;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.CoinAreaService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.LocalizationExtendService;
import cn.ztuo.bitrade.util.FileUtil;
import cn.ztuo.bitrade.util.MaskUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 币币交易手续费
 * @date 2018/1/19 15:16
 */
@RestController
@RequestMapping("coinArea")
@Api(tags = "币币交易-交易区配置")
public class CoinAreaController extends BaseAdminController{

    @Value("${bdtop.system.md5.key}")
    private String md5Key;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private CoinAreaService coinAreaService;

    @Autowired
    private LocalizationExtendService localizationExtendService;



    @RequiresPermissions("transition:trandArea:page-query ")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易-交易区 新增")
    @ApiOperation(value = "币币交易-交易区 新增")
    public MessageResult CoinAreaList(
            @Valid CoinAreaDTO coinAreaDTO) {
        CoinArea coinArea = new CoinArea();
        coinArea.setName(coinAreaDTO.getCnName());
        coinArea.setStatus(coinAreaDTO.getStatus());
        coinArea.setSort(coinAreaDTO.getSort());
        coinArea = coinAreaService.save(coinArea);
        if(!StringUtils.isEmpty(coinAreaDTO.getCnName()))
             localizationExtendService.updateLocaleInfo("CoinArea", Locale.ZH_CN, coinArea.getId().toString(), "name", coinAreaDTO.getCnName());
        if(!StringUtils.isEmpty(coinAreaDTO.getArName()))
            localizationExtendService.updateLocaleInfo("CoinArea", Locale.AR_AE, coinArea.getId().toString(), "name", coinAreaDTO.getArName());
        if(!StringUtils.isEmpty(coinAreaDTO.getEnName()))
            localizationExtendService.updateLocaleInfo("CoinArea", Locale.EN_US, coinArea.getId().toString(), "name", coinAreaDTO.getEnName());
        if(!StringUtils.isEmpty(coinAreaDTO.getJaName()))
            localizationExtendService.updateLocaleInfo("CoinArea", Locale.JA_JP, coinArea.getId().toString(), "name", coinAreaDTO.getJaName());
        if(!StringUtils.isEmpty(coinAreaDTO.getKoName()))
            localizationExtendService.updateLocaleInfo("CoinArea", Locale.KO_KR, coinArea.getId().toString(), "name", coinAreaDTO.getKoName());
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), coinArea);
    }

    @RequiresPermissions("transition:trandArea:page-query ")
    @PostMapping("page-query")
   // @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易-交易区 分页查询")
    @ApiOperation(value = "币币交易-交易区 分页查询")
    public MessageResult CoinAreaList(PageModel pageModel) {
        List<BooleanExpression> predicates = new ArrayList<>();
        predicates.add(QCoinArea.coinArea.id.isNotNull());
        Page<CoinArea> page = coinAreaService.findAll(PredicateUtils.getPredicate(predicates),pageModel.getPageable());
        PageResult<CoinAreaDTO> pageResult = new PageResult<>(page.getContent().stream().map(x ->
                CoinAreaDTO.builder().cnName(localizationExtendService.getLocaleInfo("CoinArea",Locale.ZH_CN,x.getId().toString(),"name"))
                        .arName(localizationExtendService.getLocaleInfo("CoinArea",Locale.AR_AE,x.getId().toString(),"name"))
                        .enName(localizationExtendService.getLocaleInfo("CoinArea",Locale.EN_US,x.getId().toString(),"name"))
                        .jaName(localizationExtendService.getLocaleInfo("CoinArea",Locale.JA_JP,x.getId().toString(),"name"))
                        .koName(localizationExtendService.getLocaleInfo("CoinArea",Locale.KO_KR,x.getId().toString(),"name"))
                        .status(x.getStatus())
                        .sort(x.getSort())
                        .id(x.getId())
                        .build()
        ).collect(Collectors.toList()),pageModel.getPageNo()+1,page.getSize(),page.getTotalElements());
        return success(pageResult);
    }

    @RequiresPermissions("transition:trandArea:page-query ")
    @PostMapping("deletes")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易-交易区 批量删除")
    @ApiOperation(value = "币币交易-交易区 批量删除")
    public MessageResult deletes(
            @RequestParam(value = "ids") Long[] ids) {
        coinAreaService.deletes(ids);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("transition:trandArea:page-query ")
    @PostMapping("alter-rate")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易-交易区 修改")
    @ApiOperation(value = "币币交易-交易区 修改")
    public MessageResult alterCoinAreaRate(@Valid CoinAreaDTO coinAreaDTO) {
        CoinArea coinArea = coinAreaService.findOne(coinAreaDTO.getId());
        coinArea.setName(coinAreaDTO.getCnName());
        coinArea.setStatus(coinAreaDTO.getStatus());
        coinArea.setSort(coinAreaDTO.getSort());
        coinArea = coinAreaService.save(coinArea);
        if(!StringUtils.isEmpty(coinAreaDTO.getCnName()))
            localizationExtendService.updateLocaleInfo("CoinArea", Locale.ZH_CN, coinArea.getId().toString(), "name", coinAreaDTO.getCnName());
        if(!StringUtils.isEmpty(coinAreaDTO.getArName()))
            localizationExtendService.updateLocaleInfo("CoinArea", Locale.AR_AE, coinArea.getId().toString(), "name", coinAreaDTO.getArName());
        if(!StringUtils.isEmpty(coinAreaDTO.getEnName()))
            localizationExtendService.updateLocaleInfo("CoinArea", Locale.EN_US, coinArea.getId().toString(), "name", coinAreaDTO.getEnName());
        if(!StringUtils.isEmpty(coinAreaDTO.getJaName()))
            localizationExtendService.updateLocaleInfo("CoinArea", Locale.JA_JP, coinArea.getId().toString(), "name", coinAreaDTO.getJaName());
        if(!StringUtils.isEmpty(coinAreaDTO.getKoName()))
            localizationExtendService.updateLocaleInfo("CoinArea", Locale.KO_KR, coinArea.getId().toString(), "name", coinAreaDTO.getKoName());
        return success(messageSource.getMessage("SUCCESS"));
    }

    /**
     * 获取所有交易区
     *
     * @return
     */
    @PostMapping("getAllCoinArea")
    @ApiOperation(value = "获取所有交易区")
    public MessageResult getAllCoinArea() {
        List<CoinArea> list = coinAreaService.findAll();
        return success(messageSource.getMessage("SUCCESS"), list);
    }


}
