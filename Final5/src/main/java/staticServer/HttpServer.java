package staticServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpServer {
    private final static int port=8082;//制定端口
    public static void main(String[]args) throws InterruptedException{
        //创建时间循环组BossEventLoop负责接收客户端的连接
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        //将Socket交给WorkerEventLoopGroup进行IO处理
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        try{
            //ServerBootstrap是服务器端启动
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            //链式调用
            serverBootstrap.group(bossGroup,workerGroup)
            //使用NioServerSocketChannel作为服务器的通道
                    .channel(NioServerSocketChannel.class)
                    //初始化服务端可连接队列
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    //初始化对象
                    .childHandler(new HttpServerInitializer());
            //通道处理器添加完毕后启动服务器
            ChannelFuture channelFuture=serverBootstrap.bind(port).sync();//异步绑定端口

            //监听关闭   阻塞
            channelFuture.channel().closeFuture().sync();//异步

        }finally {
            ///释放资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
