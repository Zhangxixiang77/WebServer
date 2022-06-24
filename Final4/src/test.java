import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class test {
    public static void proxyHandler(InputStream input, OutputStream output) {
        try{
            while(true){
                //FileInputStream fis = new FileInputStream(fileName);
                BufferedInputStream bis=new BufferedInputStream(input);
                byte[]buffer=new byte[1024];
                int length;
                while((length=bis.read(buffer))!=-1){
                    output.write(buffer,0,length);
                    length=-1;
                }
                output.flush();
            }
        }catch (SocketTimeoutException e){
            try{
                input.close();
                output.close();
            }catch(IOException e2){
                e2.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                input.close();
                output.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    public static void handleRequest(Socket socket) {
        try {
            //socket.setSoTimeout(60*1000);//设置代理服务器与客户端的连接未活动超时时间毫秒
            String line = "";
            InputStream clientInput = socket.getInputStream();
            String tempHost="",host;
            int port =80;//默认
            String type=null;//请求方法
            OutputStream os = socket.getOutputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(clientInput));
            int flag=1;
            StringBuilder sb =new StringBuilder();
            while((line = br.readLine())!=null) {
                if(flag==1) {       //获取请求行中请求方法，默认是http
                    type = line.split(" ")[0];
                    //如果是https，type是CONNECT
                    if(type==null)continue;
                }
                flag++;
                String[] s1 = line.split(": ");
                if(line.isEmpty()) {
                    break;
                }
                for(int i=0;i<s1.length;i++) {
                    if(s1[i].equalsIgnoreCase("host")) {
                        //获取host
                        tempHost=s1[i+1];
                    }
                }
                sb.append(line).append("\r\n");
                //System.out.println(line);
                line=null;
            }
            sb.append("\r\n");

            if(tempHost.split(":").length>1) {
                port = Integer.parseInt(tempHost.split(":")[1]);
                //获取端口
            }
            host = tempHost.split(":")[0];
            //System.out.println(host);
            Socket proxySocket;//代理间通信的socket

            if(host!=null&&!host.equals("")) {
                //打开一个通向目标服务器的Socket 连接到目标服务器
                proxySocket = new Socket(host,port);
                //proxySocket.setSoTimeout(1000*60);//设置代理服务器与服务器端的连接未活动超时时间
                OutputStream proxyOs = proxySocket.getOutputStream();//代理的输出
                InputStream proxyIs = proxySocket.getInputStream();//代理的输入

                assert type != null;
                if(type.equalsIgnoreCase("connect")) {//是否为https请求的话
                    os.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                    os.flush();
                }else {//http请求则直接转发
                    proxyOs.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    proxyOs.flush();
                }
                //新开线程转发客户端请求至目标服务器
                ExecutorService Proxyexecutor=Executors.newCachedThreadPool();
                Proxyexecutor.submit(new Thread(()->proxyHandler(clientInput,proxyOs)));
                //转发目标服务器响应至客户端
                Proxyexecutor.submit(new Thread(()->proxyHandler(proxyIs,os)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        //等待来自客户的请求 启动新线程以处理客户连接请求
        ExecutorService Socketexecutor = Executors.newCachedThreadPool();//线程池
        ServerSocket ss = new ServerSocket(8080);//监听代理代理服务器端口
        while(!Thread.currentThread().isInterrupted()){
            Socket socket=ss.accept();
            Socketexecutor.submit(new Thread(()->handleRequest(socket)));//socket线程池
        }
    }
}





