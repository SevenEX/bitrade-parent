/*
 * Copyright (c) 2017-2018 阿期米德 All Rights Reserved.
 * @Author: sanfeng
 * @Date: 2018/3/16 16:53
 * @Version: 1.0
 * History:
 * v1.0.0, sanfeng,  2018/3/16 16:53, Create
 */
package cn.ztuo.aqmd.netty.websocket;

import cn.ztuo.aqmd.core.configuration.NettyProperties;
import cn.ztuo.aqmd.netty.codec.Codec;
import cn.ztuo.aqmd.netty.codec.HawkServerDecoder;
import cn.ztuo.aqmd.netty.codec.HawkServerEncoder;
import cn.ztuo.aqmd.netty.server.HawkServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.stomp.StompSubframeDecoder;
import io.netty.handler.codec.stomp.StompSubframeEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Description: </p>
 *
 * @Author: sanfeng
 * @Date: 2018/3/16 16:53
 */
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private Codec codec;
    @Autowired
    private HawkServerHandler handler;
    @Autowired
    private NettyProperties nettyProperties;

    @Override
    protected void initChannel(SocketChannel ch) {

        ChannelPipeline pipeline = ch.pipeline();
        //HttpServerCodec: 针对http协议进行编解码
        pipeline.addLast("logger", new LoggingHandler(LogLevel.WARN))
                .addLast("httpServerCodec", new HttpServerCodec())
                //ChunkedWriteHandler分块写处理，文件过大会将内存撑爆
                .addLast("chunkedWriteHandler", new ChunkedWriteHandler())
                .addLast("httpObjectAggregator", new HttpObjectAggregator(65536))

                //用于处理websocket, /ws为访问websocket时的uri
                //.addLast("webSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/ws"))
                .addLast("websocketDecoder",new WebSocketFrameDecoder())
                .addLast("stompDecoder",new StompSubframeDecoder())
                .addLast("decoder", new HawkServerDecoder(codec))
                .addLast("websocketEncoder",new WebSocketFramePrepender())
                .addLast("stompEncoder",new StompSubframeEncoder())
                .addLast("encoder", new HawkServerEncoder(codec))
                .addLast("idle", new IdleStateHandler(nettyProperties.getReaderIdle(), nettyProperties.getWriterIdle(), nettyProperties.getBothIdle()))
                .addLast("handler", handler);
    }
}