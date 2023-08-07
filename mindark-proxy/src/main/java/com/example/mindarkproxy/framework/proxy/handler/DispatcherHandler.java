package com.example.mindarkproxy.framework.proxy.handler;

import com.example.mindarkproxy.common.constant.ProxyConstant;
import com.example.mindarkproxy.web.service.ProxyAccountService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Slf4j
public class DispatcherHandler extends ChannelInboundHandlerAdapter {

    private static final String HOST = "HOST";
    private static final Long MAX_INTERVAL_REPORT_TIME_MS = 1000 * 60 * 5L;
    /**
     * proxy端配置数据
     */
    private ProxyConstant proxyConstant;

    private ProxyAccountService proxyAccountService;

    private Channel outboundChannel;

    private String accountNo;

    private String host;

    private boolean isHandshaking = true;

    private Long version = null;

    private String proxyIp = null;

    public DispatcherHandler(ProxyConstant proxyConstant, ProxyAccountService proxyAccountService) {
        this.proxyConstant = proxyConstant;
        this.proxyAccountService = proxyAccountService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("active");
        ctx.read();
    }

    private static class NettyClientFactory {
        private static Bootstrap b = null;

        public static Bootstrap getClient(EventLoop eventLoop) {
            if (b != null) return b;
            synchronized (NettyClientFactory.class) {
                if (b != null) return b;
                b = new Bootstrap();
                b.group(eventLoop)
                        .channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter());
                            }
                        })
                        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .option(ChannelOption.AUTO_READ, false)
                        .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                        .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                        .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)
                        .option(ChannelOption.TCP_NODELAY, true);
            }
            return b;

        }
    }

    static void closeOnFlush(Channel... chs) {
        if (chs ==null) return;

        for (Channel ch:chs){
            if (ch!=null && ch.isActive()) {
                ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }


}
