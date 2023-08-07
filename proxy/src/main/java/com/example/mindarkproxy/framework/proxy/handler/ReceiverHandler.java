package com.example.mindarkproxy.framework.proxy.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

public class ReceiverHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ReceiverHandler.class);

    private final Channel inboundChannel;

    public ReceiverHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        try {
            inboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    DispatcherHandler.closeOnFlush(ctx.channel(),future.channel());
                }

            });
        } catch (Exception e) {
            release((ByteBuf) msg);
            DispatcherHandler.closeOnFlush(ctx.channel(),inboundChannel);
        }


    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        DispatcherHandler.closeOnFlush(ctx.channel(),inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(" Receiver exceptionCaught:", cause);
        DispatcherHandler.closeOnFlush(ctx.channel(),inboundChannel);
    }

    private void release(ByteBuf msg) {
        if (msg == null) return;
        if (msg.refCnt() > 0) {
            msg.release(msg.refCnt());
        }
    }
}
