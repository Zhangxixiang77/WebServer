import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class NettyProxyServerInitializer extends ChannelInitializer<Channel> {
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline channelPipeline=channel.pipeline();
        channelPipeline.addLast(new NettyProxyServerHandleAdapter());
    }
}
