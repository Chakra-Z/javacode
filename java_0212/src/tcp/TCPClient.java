package tcp;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9999;

    public static void main(String[] args) throws IOException {
        // 建立了客户端到服务端的一个TCP连接
        Socket socket = new Socket(HOST, PORT);
        InputStream is = socket.getInputStream();// 输入字节流
        BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));

        OutputStream os = socket.getOutputStream();// 输出字节流
//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        PrintWriter pw = new PrintWriter(os,true);
//        pw.println("hello,我来了");
//        bw.write("我来了");
//        bw.flush();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // 发送数据到服务器
            pw.println(line);
            // 接收服务器
            String response = br.readLine();
            System.out.println("接收到服务端响应:" +response);
        }
    }
}
