package cn.ztuo.bitrade.handler;

import cn.ztuo.aqmd.core.annotation.HawkBean;
import cn.ztuo.aqmd.core.annotation.HawkMethod;
import cn.ztuo.aqmd.netty.common.NettyCacheUtils;
import cn.ztuo.aqmd.netty.push.HawkPushServiceApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.NettyCommand;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.netty.QuoteMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 处理Netty订阅与取消订阅
 */
@HawkBean
@Slf4j
public class NettyHandler implements MarketHandler {
    @Autowired
    private HawkPushServiceApi hawkPushService;
    private String topicOfSymbol = "SYMBOL_THUMB";

    public void subscribeTopic(Channel channel, String topic){
        String userKey = channel.id().asLongText();
        if(!NettyCacheUtils.keyChannelCache.containsKey(channel)) {
            NettyCacheUtils.keyChannelCache.put(channel, userKey);
        }
        NettyCacheUtils.storeChannel(topic,channel);
        if(NettyCacheUtils.userKey.containsKey(userKey)){
            NettyCacheUtils.userKey.get(userKey).add(topic);
        }
        else{
            Set<String> userkeys=new HashSet<>();
            userkeys.add(topic);
            NettyCacheUtils.userKey.put(userKey,userkeys);
        }
    }

    public void unsubscribeTopic(Channel channel,String topic){
        String userKey = channel.id().asLongText();
        if(NettyCacheUtils.userKey.containsKey(userKey)) {
            NettyCacheUtils.userKey.get(userKey).remove(topic);
        }
        NettyCacheUtils.keyChannelCache.remove(channel);
    }

    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_SYMBOL_THUMB,version = NettyCommand.COMMANDS_VERSION)
    public QuoteMessage.SimpleResponse subscribeSymbolThumb(byte[] body, ChannelHandlerContext ctx){
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        subscribeTopic(ctx.channel(),topicOfSymbol);
        response.setCode(0).setMessage("订阅成功");
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_SYMBOL_THUMB)
    public QuoteMessage.SimpleResponse unsubscribeSymbolThumb(byte[] body, ChannelHandlerContext ctx){
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        unsubscribeTopic(ctx.channel(),topicOfSymbol);
        response.setCode(0).setMessage("取消成功");
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_EXCHANGE)
    public QuoteMessage.SimpleResponse subscribeExchange(byte[] body, ChannelHandlerContext ctx){
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        JSONObject json = JSON.parseObject(new String(body));
        String symbol = json.getString("symbol");
        String uid = json.getString("uid");
        if(StringUtils.isNotEmpty(uid)){
            subscribeTopic(ctx.channel(),symbol+"-"+uid);
        }
        subscribeTopic(ctx.channel(),symbol);
        response.setCode(0).setMessage("订阅成功");
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_EXCHANGE)
    public QuoteMessage.SimpleResponse unsubscribeExchange(byte[] body, ChannelHandlerContext ctx){
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        JSONObject json = JSON.parseObject(new String(body));
        String symbol = json.getString("symbol");
        String uid = json.getString("uid");
        if(StringUtils.isNotEmpty(uid)){
            unsubscribeTopic(ctx.channel(),symbol+"-"+uid);
        }
        unsubscribeTopic(ctx.channel(), symbol);
        response.setCode(0).setMessage("取消订阅成功");
        return response.build();
    }

    @Override
    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {
        hawkPushService.pushMsg(NettyCacheUtils.getChannel(symbol),NettyCommand.PUSH_EXCHANGE_TRADE,JSONObject.toJSONString(exchangeTrade).getBytes());
    }

    public void pushTrade(String symbol, List<ExchangeTrade> exchangeTrades){
        hawkPushService.pushMsg(NettyCacheUtils.getChannel(symbol),NettyCommand.PUSH_EXCHANGE_TRADE,JSONObject.toJSONString(exchangeTrades).getBytes());
    }

    public void pushThumb(String symbol, CoinThumb thumb){
        byte[] body = JSON.toJSONString(thumb).getBytes();
        hawkPushService.pushMsg(NettyCacheUtils.getChannel(topicOfSymbol),NettyCommand.PUSH_SYMBOL_THUMB, body);
    }

    public void pushKLine(String symbol, KLine kLine){
        hawkPushService.pushMsg(NettyCacheUtils.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_KLINE, JSONObject.toJSONString(kLine).getBytes());
    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        // 改为每500毫秒推送最新状态
//        if ("1min".equals(kLine.getPeriod())) {
//            kLine.setSymbol(symbol);
//            hawkPushService.pushMsg(NettyCacheUtils.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_KLINE, JSONObject.toJSONString(kLine).getBytes());
//        }
    }

    public void handlePlate(String symbol,TradePlate plate){
        //推送盘口
        log.info("推送盘口信息");
        Set<Channel> channelSet = NettyCacheUtils.getChannel(symbol);
        log.info("chanelSet:"+channelSet);
        hawkPushService.pushMsg(NettyCacheUtils.getChannel(symbol),NettyCommand.PUSH_EXCHANGE_PLATE, JSON.toJSONString(plate.toTradePlateDto(10)).getBytes());
        //推送深度
        log.info("推送深度信息");
        hawkPushService.pushMsg(NettyCacheUtils.getChannel(symbol),NettyCommand.PUSH_EXCHANGE_DEPTH, JSON.toJSONString(plate.toTradePlateDto()).getBytes());
    }

    public void handleOrder(short command, ExchangeOrder order){
        try {
            String topic = order.getSymbol() + "-" + order.getMemberId();
            hawkPushService.pushMsg(NettyCacheUtils.getChannel(topic), command, JSON.toJSONString(order).getBytes());
        }
        catch (Exception e){
           log.info("推送Order异常={}",e);
        }
    }
}
