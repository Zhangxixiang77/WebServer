import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.Map;

public class ToServerHandler extends ChannelInboundHandlerAdapter {

    private Map<Channel,Channel> map = null;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if(map.containsKey(ctx.channel())) {
            map.remove(ctx.channel());
        }
    }

    public ToServerHandler(Map<Channel, Channel> map) {
        this.map = map;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        Channel toChannel = map.get(channel);
        toChannel.writeAndFlush(msg);
    }
}