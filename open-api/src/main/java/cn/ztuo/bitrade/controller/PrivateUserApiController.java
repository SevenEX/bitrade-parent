package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.Limits;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.exception.GeneralException;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.CheckTraderOrderUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedisUtil;
import cn.ztuo.bitrade.vo.ExchangeOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 个人信息相关
 */
@RestController
@Api(tags = "个人信息相关")
@RequestMapping("/user")
@Slf4j
public class PrivateUserApiController extends BaseController {

    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private ExchangeOrderService orderService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private MemberWalletService walletService;
    @Value("${exchange.max-cancel-times:-1}")
    private int maxCancelTimes;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;

    @Autowired
    private MemberApiKeyService apiKeyService ;

    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation(value = "获取memberId")
    @RequestMapping(value = "get/account",method = RequestMethod.GET)
    @Limits(SysConstant.LIMIT_READ)
    public MessageResult getUserId(HttpServletRequest request){
        String ac = request.getParameter("accessKeyId");
        MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
        JSONObject re = new JSONObject();
        re.put("memberId",memberApiKey.getMemberId());
        return success(re);
    }

    /**
     *  查询个人账户信息
     *  @param memberId 会员id
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "查询账户信息")
    @RequestMapping(value = "/account",method = RequestMethod.GET)
    public MessageResult getUserAccountInfo(HttpServletRequest request,@RequestParam(value = "memberId")Long memberId)throws GeneralException{
        String ac = request.getParameter("accessKeyId");
        MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
        if(!memberApiKey.getMemberId().equals(memberId)){
            return error(msService.getMessage("MEMBER_ID_ERROR"));
        }
        List<MemberWalletForAPI> resList = new ArrayList<>();
        try {
            List<MemberWallet> list = walletService.findAllByMemberId(memberId);
            if(list.size()>0){
                for (MemberWallet meberWallet:list) {
                    MemberWalletForAPI memberAPI = new MemberWalletForAPI();
                    memberAPI.setAddress(meberWallet.getAddress());
                    memberAPI.setBalance(meberWallet.getBalance());
                    memberAPI.setCoinId(meberWallet.getCoin().getName());
                    memberAPI.setIsLock(meberWallet.getIsLock());
                    memberAPI.setFrozenBalance(meberWallet.getFrozenBalance());
                    memberAPI.setMemberId(meberWallet.getMemberId());
                    resList.add(memberAPI);
                }
            }
        } catch (Exception e) {
           log.info(">>>>>>查询账户报错>>>>"+e);
           throw new GeneralException("GET_USER_ACCOUNT_ERROR",e);
        }
        return success(resList);
    }

    /**
     * 添加订单
     * @param exchangeOrder 返回订单id
     * @param request 返回订单id
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "创建订单")
    @RequestMapping(value = "add_order",method = RequestMethod.POST)
    @Limits(SysConstant.LIMIT_TRANSACTION)
    public MessageResult addExchangeOrder(HttpServletRequest request,ExchangeOrderVO exchangeOrder)throws GeneralException{
        String ac = request.getParameter("accessKeyId");
        MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
        if(!memberApiKey.getMemberId().equals(exchangeOrder.getMemberId())){
            return error(msService.getMessage("MEMBER_ID_ERROR"));
        }
        MessageResult reslut = new MessageResult();
        try {
            String key = SysConstant.USER_ADD_EXCHANGE_ORDER_TIME_LIMIT+exchangeOrder.getMemberId();
            int expireTime = SysConstant.USER_ADD_EXCHANGE_ORDER_TIME_LIMIT_EXPIRE_TIME;
            Object object = redisUtil.get(key);
            if(object!=null){
                reslut.setCode(1);
                reslut.setMessage(msService.getMessage("ORDER_OFTEN_ERROR"));
                return reslut;
            }
            ExchangeOrderDirection direction = exchangeOrder.getDirection();
            String symbol = exchangeOrder.getSymbol();
            BigDecimal price = exchangeOrder.getPrice();
            BigDecimal amount = exchangeOrder.getAmount();
            ExchangeOrderType type = exchangeOrder.getType();
            Long memberId = exchangeOrder.getMemberId();
            log.info(">>>>>添加订单参数>>>symbol>>"+symbol+">>price>>"+price+">>>amount>>"+amount+">>>type>>>"+type+">>>memberId>>>"+memberId);
            //判断限价输入值是否小于零
            if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
                return MessageResult.error(500, msService.getMessage("PRICE_ILLEGAL_ERROR"));
            }
            //判断数量小于零
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return MessageResult.error(500, msService.getMessage("QUANTITY_ILLEGAL_ERROR"));
            }
            //根据交易对儿名称（symbol）获取交易对儿信息
            ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
            if (exchangeCoin == null || exchangeCoin.getEnable() != 1) {
                return MessageResult.error(500, msService.getMessage("SYMBOL_NONSUPPORT_ERROR"));
            }

            //获取基准币
            String baseCoin = exchangeCoin.getBaseSymbol();
            //获取交易币
            String exCoin = exchangeCoin.getCoinSymbol();
            log.info("exCoin={},baseCoin={},direction={},type={}", exCoin, baseCoin, direction, type);

            Coin coin;
            //根据交易方向查询币种信息
            if (direction == ExchangeOrderDirection.SELL) {
                coin = coinService.findByUnit(exCoin);
            } else {
                coin = coinService.findByUnit(baseCoin);
            }
            if (coin == null) {
                return MessageResult.error(500, msService.getMessage("SYMBOL_NONSUPPORT_ERROR"));
            }
            //设置价格精度
            price = price.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
            //设置数量精度
            if (direction == ExchangeOrderDirection.BUY && type == ExchangeOrderType.MARKET_PRICE) {
                amount = amount.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
                if (amount.compareTo(exchangeCoin.getMinTurnover()) < 0) {
                    return MessageResult.error(500, msService.getMessage("TURNOVER_MESSAGE") + exchangeCoin.getMinTurnover());
                }
            } else {
                amount = amount.setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_DOWN);
            }
            if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
                return MessageResult.error(500, msService.getMessage("PRICE_ILLEGAL_ERROR"));
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return MessageResult.error(500, msService.getMessage("QUANTITY_ILLEGAL_ERROR"));
            }
            MemberWallet wallet = walletService.findByCoinAndMemberId(coin, memberId);
            if (wallet == null) {
                return MessageResult.error(500, msService.getMessage("SYMBOL_NONSUPPORT_ERROR"));
            }
            if (wallet.getIsLock() == BooleanEnum.IS_TRUE) {
                return MessageResult.error(500, msService.getMessage("WALLET_LOCK_ERROR"));
            }
            //如果有最低卖价限制，出价不能低于此价,且禁止市场价格卖
            if (direction == ExchangeOrderDirection.SELL && exchangeCoin.getMinSellPrice().compareTo(BigDecimal.ZERO) > 0
                    && ((price.compareTo(exchangeCoin.getMinSellPrice()) < 0) || type == ExchangeOrderType.MARKET_PRICE)) {
                return MessageResult.error(500, msService.getMessage("PRICE_ILLEGAL_ERROR"));
            }
            //查看是否启用市价买卖
            if (type == ExchangeOrderType.MARKET_PRICE) {
                if (exchangeCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.BUY) {
                    return MessageResult.error(500, msService.getMessage("MARKET_PRICE_BUY_NONSUPPORT_ERROR"));
                } else if (exchangeCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.SELL) {
                    return MessageResult.error(500, msService.getMessage("MARKET_PRICE_SELL_NONSUPPORT_ERROR"));
                }
            }
            //限制委托数量
            if (exchangeCoin.getMaxTradingOrder() > 0 && orderService.findCurrentTradingCount(memberId, symbol, direction) >= exchangeCoin.getMaxTradingOrder()) {
                return MessageResult.error(500, msService.getMessage("MAX_TRADING_ORDER_MESSAGE") + exchangeCoin.getMaxTradingOrder());
            }
            ExchangeOrder order = new ExchangeOrder();
            BeanUtils.copyProperties(exchangeOrder,order);
            order.setBaseSymbol(baseCoin);
            order.setCoinSymbol(exCoin);
            order.setOrderResource(ExchangeOrderResource.API);
            //
            reslut = orderService.addOrder(memberId,order);
            if(reslut.getCode()==0) {
                redisUtil.set(key, exchangeOrder.getMemberId(), expireTime, TimeUnit.SECONDS);
                JSONObject object1 = new JSONObject();
                object1.put("orderId",order.getOrderId());
                reslut.setData(object1);
                kafkaTemplate.send("exchange-order", symbol, JSON.toJSONString(order));
            }
        } catch (Exception e) {
            log.info(">>>>>>>>下单异常>>>>>",e);
            throw new GeneralException(msService.getMessage("ADD_EXCHANGE_ORDER_ERROR"));
        }
        return reslut;
    }

//    /**
//     * 执行订单 推入盘口中 撮合交易
//     * @param orderId
//     * @return
//     * @throws GeneralException
//     */
//    @RequestMapping(value = "execute",method = RequestMethod.GET)
//    public MessageResult executeExchangeOrderByOrderId(HttpServletRequest request,@RequestParam("orderId")String orderId)throws GeneralException{
//
//        try {
//            if(StringUtils.isEmpty(orderId)||!orderId.startsWith("E")){
//                return MessageResult.error(500,"订单号非法，请核实订单号");
//            }
//            //根据订单号查询订单
//            ExchangeOrder order = orderService.findOne(orderId);
//            String ac = request.getParameter("accessKeyId");
//            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
//            if(!memberApiKey.getMemberId().equals(order.getMemberId())){
//                return error("账户id有误");
//            }
//            if(order==null){
//                return  MessageResult.error(500,"订单不存在，请核实订单号");
//            }
//            //核查订单状态
//            if(!order.getStatus().equals(ExchangeOrderStatus.TRADING)){
//                return MessageResult.error(500,"该订单已完成或已取消，请核实订单记录");
//            }
//            // 发送消息至Exchange系统
//            kafkaTemplate.send("exchange-order", JSON.toJSONString(order));
//        }catch (Exception e){
//            log.info(">>>>>>>执行订单错误>>>>>>",e);
//            throw  new GeneralException("EXCURATE_EXCHANGE_ORDER_ERROR",e.getMessage());
//        }
//        return MessageResult.success("订单执行成功");
//    }

    /**
     * 根据订单Id查询订单详情
     * @param orderId
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "根据订单Id查询订单详情")
    @GetMapping("query/order_detail")
    public MessageResult queryOrderDetailByOrderId(HttpServletRequest request,@RequestParam("orderId")String orderId)throws GeneralException{
        MessageResult result = new MessageResult();
        try {
            if(StringUtils.isEmpty(orderId)|| !orderId.startsWith("E")){
                return MessageResult.error(500,msService.getMessage("ORDER_NUM_ERROR"));
            }
            //根据订单号查询订单
            ExchangeOrder order = orderService.findOne(orderId);
            String ac = request.getParameter("accessKeyId");
            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
            if(!memberApiKey.getMemberId().equals(order.getMemberId())){
                return error(msService.getMessage("MEMBER_ID_ERROR"));
            }
            order.setDetail(exchangeOrderDetailService.findAllByOrderId(order.getOrderId()));
            if(order==null){
                return  MessageResult.error(500,msService.getMessage("ORDER_NUM_EXIST_ERROR"));
            }
            result.setData(order);
        } catch (Exception e) {
            log.info(">>>>>>查询订单详情出错>>>>>",e);
            throw  new GeneralException(msService.getMessage("QUERY_ORDER_ERROR"));
        }
        return result;
    }

    /**
     * 根据用户id和交易对查询订单列表
     * @param memberId
     * @param symbol
     * @return
     * @throws GeneralException
     */
    @PostMapping("query/order")
    @ApiOperation(value = "根据用户id和交易对查询订单列表")
    public MessageResult queryOrderByMemberIdAndSymbol(@RequestParam("memberId")Long memberId ,String symbol,
                                                       ExchangeOrderDirection direction, ExchangeOrderType type,
                                                       String startTime, String endTime ,
                                                       @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") int pageSize,
                                                       HttpServletRequest request)throws GeneralException{
        MessageResult result = new MessageResult();
        try {
            String ac = request.getParameter("accessKeyId");
            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
            if(!memberApiKey.getMemberId().equals(memberId)){
                return error(msService.getMessage("MEMBER_ID_ERROR"));
            }
            Page<ExchangeOrder> page = orderService.findPersonalCurrent(memberId,symbol,type,startTime,endTime,direction,pageNum,pageSize);
            page.getContent().forEach(exchangeOrder -> {
                //获取交易成交详情
                BigDecimal tradedAmount = BigDecimal.ZERO;
                List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
                exchangeOrder.setDetail(details);
                for (ExchangeOrderDetail trade : details) {
                    tradedAmount = tradedAmount.add(trade.getAmount());
                }
                exchangeOrder.setTradedAmount(tradedAmount);
            });
            result.setData(page);
        }catch (Exception e){
            log.info(">>>>>>>>查询用户订单出错>>>",e);
            throw new GeneralException(msService.getMessage("QUERY_ORDER_ERROR"));
        }
        return  result;
    }

    /**
     * 根据订单号取消订单Id
     * @param orderId
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "取消订单")
    @RequestMapping(value = "cancel_order",method = RequestMethod.GET)
    @Limits(SysConstant.LIMIT_TRANSACTION)
    public MessageResult cancelOrderByOrderId(@RequestParam("orderId")String orderId,
                                              @RequestParam("memberId")Long memberId,HttpServletRequest request)throws GeneralException{
        try {
            String ac = request.getParameter("accessKeyId");
            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
            if(!memberApiKey.getMemberId().equals(memberId)){
                return error(msService.getMessage("MEMBER_ID_ERROR"));
            }
            ExchangeOrder order = orderService.findOne(orderId);
            if(!memberId.equals(order.getMemberId())){
                return MessageResult.error(500,msService.getMessage("ILLEGAL_OPERATION_ERROR"));
            }
            if (order.getStatus() != ExchangeOrderStatus.TRADING) {
                return MessageResult.error(500, msService.getMessage("ORDER_NOT_TRADING_ERROR"));
            }
            if (maxCancelTimes > 0 && orderService.findTodayOrderCancelTimes(memberId, order.getSymbol()) >= maxCancelTimes) {
                return MessageResult.error(500, msService.getMessage("MAX_CANCEL_MESSAGE") + maxCancelTimes + msService.getMessage("MAX_CANCEL_MESSAGE_01"));
            }
            if(order.getStatus() == ExchangeOrderStatus.TRADING && CheckTraderOrderUtil.isExchangeOrderExist(order,restTemplate) ){
                // 发送消息至Exchange系统
                kafkaTemplate.send("exchange-order-cancel", order.getSymbol(), JSON.toJSONString(order));
            } else if(System.currentTimeMillis() - order.getTime() > 3600 * 1000) {
                orderService.forceCancelOrder(order);
            }
            else {
                return MessageResult.error(msService.getMessage("INFORMATION_EXPIRED"));
            }
        }catch (Exception e){
            log.info(">>>>>取消订单出错>>>>",e);
            throw  new GeneralException(msService.getMessage("CANCEL_ORDER_ERROR"));
        }
        return MessageResult.success(msService.getMessage("SUCCESS"));
    }

    /**
     * 查询会员当前委托记录
     * @param memberId
     * @param symbol
     * @param pageNum
     * @param pageSize
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "查询当前委托记录")
    @RequestMapping(value = "current",method = RequestMethod.POST)
    public MessageResult queryCurrentOrder(@RequestParam("memberId") Long memberId,  String symbol,
                                             int pageNum, int pageSize,HttpServletRequest request)throws GeneralException{
        MessageResult result = new MessageResult();
        try {
            String ac = request.getParameter("accessKeyId");
            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
            if(!memberApiKey.getMemberId().equals(memberId)){
                return error("账户id有误");
            }
            Page<ExchangeOrder> page = orderService.findCurrent(memberId, symbol, pageNum, pageSize,BooleanEnum.IS_FALSE);
            page.getContent().forEach(exchangeOrder -> {
                //获取交易成交详情
                BigDecimal tradedAmount = BigDecimal.ZERO;
                List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
                exchangeOrder.setDetail(details);
                for (ExchangeOrderDetail trade : details) {
                    tradedAmount = tradedAmount.add(trade.getAmount());
                }
                exchangeOrder.setTradedAmount(tradedAmount);
            });
            result.setData(page);
        }catch (Exception e){
            log.info(">>>>>>>查询当前委托订单异常>>>>",e);
            throw new GeneralException("QUERY_CURRENT_ORDER_ERROR",e.getMessage());
        }
        return result;
    }

    /**
     * 查询用户历史委托
     * @param memberId
     * @param symbol
     * @param pageNum
     * @param pageSize
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "查询历史委托记录")
    @RequestMapping(value = "history",method = RequestMethod.POST)
    public MessageResult queryHistoryOrder(@RequestParam("memberId")Long memberId ,String symbol,
                                           ExchangeOrderDirection direction, ExchangeOrderType type,
                                           String startTime, String endTime ,
                                           @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") int pageSize,
                                           HttpServletRequest request)throws GeneralException{
        MessageResult result = new MessageResult();
        try {
            String ac = request.getParameter("accessKeyId");
            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
            if(!memberApiKey.getMemberId().equals(memberId)){
                return error(msService.getMessage("MEMBER_ID_ERROR"));
            }
            Page<ExchangeOrder> page = orderService.findPersonalHistory(memberId, symbol, type,null,startTime,endTime,direction,pageNum, pageSize);
            page.getContent().forEach(exchangeOrder -> {
                //获取交易成交详情,
                exchangeOrder.setDetail(exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId()));
            });
            result.setData(page);
        }catch (Exception e){
            log.info(">>>>>>>查询当前委托订单异常>>>>",e);
            throw new GeneralException(msService.getMessage("QUERY_CURRENT_ORDER_ERROR"));
        }
        return result;
    }

}
