import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyProxyServer {
    private final static int port=8083;
    public static void main(String[] s) throws InterruptedException {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(8);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(8);
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .childHandler(new NettyProxyServerInitializer());
            ChannelFuture channelFuture=serverBootstrap.bind(port).sync();//异步
            channelFuture.channel().closeFuture().sync();//异步
        }finally{
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
