package cn.ztuo.bitrade.controller.exchange;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.entity.ExchangeCoin;
import cn.ztuo.bitrade.entity.QExchangeCoin;
import cn.ztuo.bitrade.service.ExchangeCoinService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.FileUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.alibaba.fastjson.JSONObject;
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

import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 币币交易手续费
 * @date 2018/1/19 15:16
 */
@RestController
@RequestMapping("exchange/exchange-coin")
@Api(tags = "币币交易-交易对配置")
public class ExchangeCoinController extends BaseAdminController {

    @Value("${bdtop.system.md5.key}")
    private String md5Key;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private ExchangeCoinService exchangeCoinService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;



    @RequiresPermissions("exchange:exchange-coin")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易-交易对 新增")
    @ApiOperation(value = "币币交易-交易对 新增")
    public MessageResult ExchangeCoinList(
            @Valid ExchangeCoin exchangeCoin) {
        if(exchangeCoin.getBaseSymbol().equalsIgnoreCase(exchangeCoin.getCoinSymbol())){
            return MessageResult.error(messageSource.getMessage("Incorrect_Parameters"));
        }
        ExchangeCoin oldCoin=exchangeCoinService.findBySymbol(exchangeCoin.getSymbol());
        if(oldCoin!=null){
            return MessageResult.error("币对已存在！");
        }
        //设置默认交易对儿
        if (exchangeCoin.getDefaultSymbol().equals("1")){
            exchangeCoinService.updateDefault();
        }
        exchangeCoin = exchangeCoinService.save(exchangeCoin);
        if (exchangeCoin != null && exchangeCoin.getEnable() == 1){
            List<ExchangeCoin> result = new ArrayList<>();
            result.add(exchangeCoin);
            //发送Kafka消息，上架行情
            kafkaTemplate.send("exchange-symbol", JSONObject.toJSONString(result));
            kafkaTemplate.send("exchange-market-symbol",JSONObject.toJSONString(result));
        }
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), exchangeCoin);
    }

    @RequiresPermissions("exchange:exchange-coin")
    @PostMapping("page-query")
   // @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易-交易对 分页查询")
    @ApiOperation(value = "币币交易-交易对 分页查询")
    public MessageResult ExchangeCoinList(PageModel pageModel,String symbol,Integer enable) {
        List<BooleanExpression> predicates = new ArrayList<>();
        predicates.add(QExchangeCoin.exchangeCoin.symbol.isNotEmpty());
        if(!StringUtils.isEmpty(symbol))
            predicates.add((QExchangeCoin.exchangeCoin.symbol.like("%"+symbol+"%")));
        if(enable != null)
            predicates.add((QExchangeCoin.exchangeCoin.enable.eq(enable)));
        if (pageModel.getProperty() == null) {
            List<String> list = new ArrayList<>();
            list.add("symbol");
            List<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setProperty(list);
            pageModel.setDirection(directions);
        }
        Page<ExchangeCoin> all = exchangeCoinService.findAll(PredicateUtils.getPredicate(predicates), pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("exchange:exchange-coin")
    @PostMapping("detail")
    //@AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易手续费exchangeCoin 详情")
    @ApiOperation(value = "币币交易-交易对 详情")
    public MessageResult detail(
            @RequestParam(value = "symbol") String symbol) {
        ExchangeCoin exchangeCoin = exchangeCoinService.findOne(symbol);
        notNull(exchangeCoin, "validate symbol!");
        return success(exchangeCoin);
    }

    @RequiresPermissions("exchange:exchange-coin")
    @PostMapping("deletes")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易-交易对 批量删除")
    @ApiOperation(value = "币币交易-交易对 批量删除")
    public MessageResult deletes(
            @RequestParam(value = "ids") String[] ids) {
        exchangeCoinService.deletes(ids);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("exchange:exchange-coin")
    @PostMapping("alter-rate")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易-交易对 修改")
    @ApiOperation(value = "币币交易-交易对 修改")
    public MessageResult alterExchangeCoinRate(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "fee", required = false) BigDecimal fee,//交易币手续费
            @RequestParam(value = "baseFee",required =false)BigDecimal baseFee,//结算币种手续费
            @RequestParam(value = "enable", required = false) Integer enable,
            @RequestParam(value = "sort", required = false) Integer sort,
            @RequestParam(value = "areaId") Integer areaId,
            @RequestParam(value = "enableMarketSell", defaultValue = "1") BooleanEnum enableMarketSell,//允许市价卖
            @RequestParam(value = "enableMarketBuy", defaultValue = "1") BooleanEnum enableMarketBuy,//允许市价买
            @RequestParam(value = "baseCoinScale", required = false) Integer baseCoinScale,//结算币种精度
            @RequestParam(value = "coinScale", required = false) Integer coinScale,//交易币种精度
            @RequestParam(value = "minTurnover", required = false) BigDecimal minTurnover,//最小成交额
            @RequestParam(value = "minSellPrice", required = false) BigDecimal minSellPrice,//最小卖单价
            @RequestParam(value = "maxVolume", required = false) BigDecimal maxOrderQuantity,//最大下单量
            @RequestParam(value = "minVolume", required = false) BigDecimal minOrderQuantity,//最小下单量
            @RequestParam(value = "maxTradingTime", required = false) Integer maxTradingTime,//最大交易时间
            @RequestParam(value = "maxTradingOrder", required = false) Integer maxTradingOrder,//最大交易量
            @RequestParam(value = "flag", required = false) Integer flag,//是否推荐
            @RequestParam(value = "defaultSymbol", required = false) String defaultSymbol,//是否默认
            @RequestParam(value = "newSort", required = false) Integer newSort,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        //password = Encrypt.MD5(password + md5Key);
       // Assert.isTrue(password.equals(admin.getPassword()), messageSource.getMessage("WRONG_PASSWORD"));
        ExchangeCoin exchangeCoin = exchangeCoinService.findOne(symbol);
        Integer oldEnable = exchangeCoin.getEnable();
        notNull(exchangeCoin, "validate symbol!");
        if (fee != null&&fee.compareTo(BigDecimal.ZERO)>=0)
            exchangeCoin.setFee(fee);//修改手续费
        if (sort != null)
            exchangeCoin.setSort(sort);//设置排序
        if (enable != null && enable > 0 && enable < 3)
            exchangeCoin.setEnable(enable);//设置启用 禁用
        if(baseCoinScale!=null) {
            exchangeCoin.setBaseCoinScale(baseCoinScale);
        }
        if(areaId!=null) {
            exchangeCoin.setAreaId(areaId);
        }
        if(coinScale!=null) {
            exchangeCoin.setCoinScale(coinScale);
        }
        if(exchangeCoin.getBaseCoinScale()+exchangeCoin.getCoinScale()>16){
            return MessageResult.error(messageSource.getMessage("ACCURACY_ERROR"));
        }
        if(maxOrderQuantity!=null){
            exchangeCoin.setMaxVolume(maxOrderQuantity);
        }
        if(minOrderQuantity!=null){
            exchangeCoin.setMinVolume(minOrderQuantity);
        }
        if(baseFee!=null&&baseFee.compareTo(BigDecimal.ZERO)>=0){
            exchangeCoin.setBaseFee(baseFee);//修改结算币种手续费
        }
        if(enableMarketSell!=null){
            exchangeCoin.setEnableMarketSell(enableMarketSell);
        }
        if(enableMarketBuy!=null){
            exchangeCoin.setEnableMarketBuy(enableMarketBuy);
        }
        if (minTurnover != null) {
            exchangeCoin.setMinTurnover(minTurnover);
        }
        if (minSellPrice != null) {
            exchangeCoin.setMinSellPrice(minSellPrice);
        }
        if (maxTradingTime != null) {
            exchangeCoin.setMaxTradingTime(maxTradingTime);
        }
        if (maxTradingOrder != null) {
            exchangeCoin.setMaxTradingOrder(maxTradingOrder);
        }
        if (flag != null) {
            exchangeCoin.setFlag(flag);
        }
        if (newSort != null) {
            exchangeCoin.setNewSort(newSort);//设置新币排序
        }

        //设置默认交易对儿
        if (defaultSymbol != null) {
            exchangeCoin.setDefaultSymbol(defaultSymbol);
            if(defaultSymbol.equals("1")){
                exchangeCoinService.updateDefault();
            }
        }
       // exchangeCoinService.save(exchangeCoin);
        exchangeCoin = exchangeCoinService.save(exchangeCoin);
        if (oldEnable != 1 && exchangeCoin.getEnable() == 1){
            List<ExchangeCoin> result = new ArrayList<>();
            result.add(exchangeCoin);
            //发送Kafka消息，上架行情
            kafkaTemplate.send("exchange-symbol", JSONObject.toJSONString(result));
            kafkaTemplate.send("exchange-market-symbol",JSONObject.toJSONString(result));
        }
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("exchange:exchange-coin")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易-交易对 导出")
    @ApiOperation(value = "币币交易-交易对 导出")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = exchangeCoinService.findAll();
        return new FileUtil().exportExcel(request, response, all, "exchangeCoin");
    }

    /**
     * 获取所有交易区币种的单位
     *
     * @return
     */
    @PostMapping("all-base-symbol-units")
    @ApiOperation(value = "获取所有交易区币种的单位")
    public MessageResult getAllBaseSymbolUnits() {
        List<String> list = exchangeCoinService.getBaseSymbol();
        return success(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * 获取交易区币种 所支持的交易 币种
     *
     * @return
     */
    @PostMapping("all-coin-symbol-units")
    @ApiOperation(value = "获取交易区币种 所支持的交易 币种")
    public MessageResult getAllCoinSymbolUnits(@RequestParam("baseSymbol") String baseSymbol) {
        List<String> list = exchangeCoinService.getCoinSymbol(baseSymbol);
        return success(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * 获取所有交易对
     *
     * @return
     */
    @PostMapping("all-symbol")
    @ApiOperation(value = "获取所有交易对")
    public MessageResult getAllSymbol() {
        List<String> list = exchangeCoinService.getAllSymbol();
        return success(messageSource.getMessage("SUCCESS"), list);
    }

}
