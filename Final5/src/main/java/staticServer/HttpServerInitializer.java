package staticServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline channelPipeline=socketChannel.pipeline();//pipeline里添加handler
        //对http进行编解码 将请求和应答消息编码或解码为HTTP消息
        channelPipeline.addLast(new HttpServerCodec());
        //添加自定义处理器
        channelPipeline.addLast(new HttpObjectAggregator(65536));//64*1024
        /*用POST方式请求服务器的时候，对应的参数信息是保存在message body中的,如果只是单纯的用HttpServerCodec
        是无法完全的解析Http POST请求的，因为HttpServerCodec只能获取uri中参数，所以需要加上HttpObjectAggregator
         */
        channelPipeline.addLast(new ChunkedWriteHandler());//ChunkedWriteHandler进行大规模文件传输。
        channelPipeline.addLast(new HttpServerHandleAdapter());//FileSystem业务工程处理器。
    }
}
