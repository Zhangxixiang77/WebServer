package staticServer;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import java.io.File;
import java.io.RandomAccessFile;

public class HttpServerHandleAdapter extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        //获取URI
        String uri=fullHttpRequest.uri();
        int not_found = 0;
        if(uri.equalsIgnoreCase("/") || uri.equalsIgnoreCase("/index.html")){
            uri="/index.html";//null 
        }else if(uri.equalsIgnoreCase("/shutdown")){
            System.exit(0);
        }else{
            uri="/error.html";
            not_found = 1;
        }

        String fileName=uri.substring(1);//文件地址

        //根据地址构建文件
        File file=new File(fileName);

        //创建http响应
        HttpResponse httpResponse=new DefaultHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.OK);

        //设置文件格式内容
        if(fileName.endsWith(".html")){
            httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        }
        if(file.exists()&&not_found!=1){
           httpResponse.setStatus(HttpResponseStatus.OK);
        }
        else{
            //如果文件不存在
            //file=ERROR;
            httpResponse.setStatus(HttpResponseStatus.NOT_FOUND);
        }
        RandomAccessFile randomAccessFile=new RandomAccessFile(file,"r");
        /*在异步框架中高效地写大块地数据：即将文件内容写出到网络
         * NIO零拷贝特性，消除了将文件的内容从文件系统移动到网络栈的复制过程。
         * 从FileInputStream创建一个DefaultFileRegion，并将其写入Channel，利用零拷贝特性来传输一个文件内容。
         */
        //设置http头部信息
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, randomAccessFile.length());
        httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);


        channelHandlerContext.write(httpResponse);//写回http报文
        channelHandlerContext.write(new DefaultFileRegion(randomAccessFile.getChannel(), 0, file.length()));//写回文件

        //写入文件尾部，必须有。
        channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        randomAccessFile.close();//关闭文件
    }
}
