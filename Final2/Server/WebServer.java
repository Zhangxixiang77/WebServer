package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebServer {
    public static void main(String[] args) {
        //构造线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(8, 50, 400, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(2000));
        try{
            //使用serversocket创建服务对象
            ServerSocket server = new ServerSocket(8081);
            while(true){
                try {
                    //使用服务器对象的输入流获取客户端发来的数据
                    Socket socket = server.accept();
                    executor.execute(new ThreadPoolTask( socket ));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}



class ThreadPoolTask implements Runnable{
    private Socket socket;

    ThreadPoolTask(Socket socket1){
    this.socket=socket1;
    }

    public void run() {
        try {
            InputStream is = null;
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //将字节流转换为字符流
            InputStreamReader isr = new InputStreamReader(is);
            //将字符流包装成缓冲流
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(line);
            //从line中解析文件名
            String[] linesArr = line.split(" ");
            String fileName = linesArr[1].substring(1);
            System.out.println(fileName);
            if (fileName.equals("shutdown")) {
                System.exit(0);
            }
            File file = new File(fileName);
            if (file.exists() == false) {
                fileName = "error.html";
            }
            //从磁盘读取文件
            FileInputStream fis = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] data = new byte[1024];
            int length = 0;
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            //先返回响应头信息
            bos.write("HTTP/1.1 200 OK \r\n".getBytes());
            bos.write("content-type: text/html; charset=utf-8 \r\n".getBytes());
            //响应头和数据之间有一个空行
            bos.write("\r\n".getBytes());
            //再返回数据
            while ((length = bis.read(data)) != -1) {
                //将读取的文件输出到网络
                bos.write(data, 0, length);
            }
            bos.close();
            bis.close();
            br.close();
            socket.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
