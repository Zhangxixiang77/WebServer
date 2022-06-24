package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
//多线程
public class WebServer {
    public static void main(String[] args) {
        try{
            //使用serversocket创建服务对象
            ServerSocket server = new ServerSocket(8081);
            while(true){
                try {
                    //使用服务器对象的输入流获取客户端发来的数据
                    Socket socket = server.accept();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                InputStream is = socket.getInputStream();
                                //将字节流转换为字符流 效率低
                                InputStreamReader isr = new InputStreamReader(is);
                                //将字符流包装成缓冲流
                                BufferedReader br = new BufferedReader(isr);
                                String line = br.readLine();
                                System.out.println(line);
                                int flag=1;
                                //从line中解析文件名
                                String[] linesArr = line.split(" ");
                                //String fileName = linesArr[1].substring(1);
		                        //GET /index.html HTTP/1.1
                                String fileName2 = linesArr[1];// /index.html
                                System.out.println(fileName2);
                                String[] fileName1 = fileName2.split("/");
                                String fileName=fileName1[fileName1.length-1];
                                System.out.println(fileName);
                                if(fileName.equals("shutdown")){
                                    System.exit(0);
                                }
                                File file = new File(fileName);
                                if(file.exists()==false){
                                    fileName = "error.html";
                                    flag = 0;
                                }
                                //从磁盘读取文件
                                FileInputStream fis = new FileInputStream(fileName);
                                BufferedInputStream bis = new BufferedInputStream(fis);
                                byte[] data = new byte[1024];
                                int length = 0;
                                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                                //先返回响应头信息
                                if(flag == 0){
                                    bos.write("HTTP/1.1 404 NOT_FOUND \r\n".getBytes());
                                }else{
                                    bos.write("HTTP/1.1 200 OK \r\n".getBytes());
                                }
                                bos.write("content-type: text/html; charset=utf-8 \r\n".getBytes());

                                //响应头和数据之间有一个空行
                                bos.write("\r\n".getBytes());

                                //再返回数据
                                while((length = bis.read(data))!=-1){
                                    //将读取的文件输出到网络
                                    bos.write(data,0,length);
                                }
                                bos.close();
                                bis.close();
                                br.close();
                                socket.close();
                            }catch (Exception e){
                            }
                        }
                    }).start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
