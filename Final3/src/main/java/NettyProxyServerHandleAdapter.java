import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;
import java.util.Map;

public class NettyProxyServerHandleAdapter extends ChannelInboundHandlerAdapter {

    private Map<Channel,Channel> channelMap = new HashMap<>();
    private Map<Channel, ByteBuf> msgMap = new HashMap<Channel,ByteBuf>();
    NioEventLoopGroup toServerGroup = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();

    @SuppressWarnings("rawtypes")
    public NettyProxyServerHandleAdapter() {
        bootstrap.group(toServerGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast("toServer handler", new ToServerHandler(channelMap));
                    }
                });
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if(channelMap.containsKey(ctx.channel())) {
            channelMap.remove(ctx.channel());
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if(channelMap.containsKey(channel)&&channelMap.get(channel) !=null) {
            Channel toChannel = channelMap.get(channel);
            toChannel.writeAndFlush(msg);
        }else {
            ByteBuf buffer = null;
            if(msgMap.containsKey(channel)&&msgMap.get(channel)!=null) {
                buffer = msgMap.get(channel);
            }else {
                buffer = ctx.alloc().buffer(1024*2);
            }
            buffer.writeBytes((ByteBuf) msg);
            buffer.retain();
            msgMap.put(channel, buffer);
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        final Channel channel = ctx.channel();
        if(!(channelMap.containsKey(channel)&&channelMap.get(channel)!=null)) {
	//?????????????????????????????????????????????host???port????????????
            if(msgMap.get(channel)!=null) {
                byte[] b =new byte[msgMap.get(channel).readableBytes()];
                msgMap.get(channel).getBytes(0, b);
                String header = new String(b);
                String[] lineStrs = header.split("\\n");
                String host="";
                int port = 80;
                int type=0;                   //?????????http??????
                String hostTemp="";
                for(int i=0 ; i<lineStrs.length ; i++) {         //???????????????
                    System.out.println(lineStrs[i]);
                    if(i==0) {
                        type = (lineStrs[i].split(" ")[0].equalsIgnoreCase("CONNECT") ? 1 : 0);
                    }else {
                        String[] hostLine = lineStrs[i].split(": ");
                        if(hostLine[0].equalsIgnoreCase("host")) {
                            hostTemp = hostLine[1];
                        }
                    }
                }
                if(hostTemp.split(":").length>1) {
                    host = hostTemp.split(":")[0];
                    port = Integer.valueOf(hostTemp.split(":")[1].split("\\r")[0]);
                }else {
                    host = hostTemp.split(":")[0].split("\\r")[0];
                }

                final int requestType = type;
                ChannelFuture future = bootstrap.connect(host, port).sync();
                if(future.isSuccess()) {         //????????????????????????????????????????????????????????????????????????map?????????????????????
                    channelMap.put(channel, future.channel());
                    channelMap.put(future.channel(), channel);
                    if(requestType==1) {      //https???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        ByteBuf buffer = channel.alloc().buffer("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes().length);
                        buffer.writeBytes("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                        channel.writeAndFlush(buffer);
                        msgMap.get(channel).release();
                    }else {
                        future.channel().writeAndFlush(msgMap.get(channel));
                        msgMap.get(channel).release();
                    }
                }else {
                    System.out.println("Connect Failing");
                }
            }
        }
    }
}