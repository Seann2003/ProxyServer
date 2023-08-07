package com.example.mindarkproxy.framework.proxy;

import com.example.mindarkproxy.common.constant.ProxyConstant;
import com.example.mindarkproxy.framework.proxy.handler.DispatcherHandler;
import com.example.mindarkproxy.web.service.ProxyAccountService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public final class ProxyServer {

    @Autowired
    ProxyConstant proxyConstant;
    @Autowired
    ProxyAccountService proxyAccountService;
    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
    private static EventLoopGroup workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));

    @PostConstruct
    public void initNettyServer() {
        // Configure the bootstrap.
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // TCP层：发送buf :32k
                    .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
                    //TCP层 :接收BUF: 32k
                    .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                    //netty服务层缓存参数
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)
                    // 不保证优先转发
                    .childOption(ChannelOption.IP_TOS, 0xB8);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //    .handler(new LoggingHandler(LogLevel.ERROR))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new DispatcherHandler(proxyConstant, proxyAccountService));
                        }
                    })
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(proxyConstant.getLocalPort()).sync()
                    .addListener((ChannelFutureListener) future -> log.info("Proxying on:" + proxyConstant.getLocalPort() + " ..."));


        } catch (Exception e) {
            log.error("netty start exception:", e);
        }
    }


    @PreDestroy
    public void preDestroy() throws InterruptedException {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully().addListener(future -> {
            log.warn("ReportService 已经关闭....");
//            TaskService.destroy();
        });
        workerGroup.awaitTermination(3, TimeUnit.SECONDS);
        log.warn("netty 已经关闭....");

    }
}
