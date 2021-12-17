package Server;

import DataPack.DataPack;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

public class ServerUI {
    private JPanel mainPanel;
    private JTextPane textMessage;
    private JTextField textDirectoryPath;
    private JTextField textPort;
    private JButton btnStart;
    private JButton btnSelectPath;

    private boolean isServerStarted;
    private BW_ReturnFile bw_returnFile;

    private ServerUI self = this;

    ServerUI(){
        // 设置文本框背景色
        textMessage.setBackground(Color.DARK_GRAY);
        // 按钮"浏览"的动作
        btnSelectPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = fileChooser.showOpenDialog(btnSelectPath);
                if(ret==JFileChooser.APPROVE_OPTION)
                    textDirectoryPath.setText(fileChooser.getSelectedFile().getPath()+"\\");
            }
        });
        // 按钮"开始"的动作
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isServerStarted)
                    bw_returnFile.stop();
                isServerStarted =true;
                bw_returnFile = new BW_ReturnFile(Integer.valueOf(textPort.getText()),textDirectoryPath.getText(),self);
            }
        });
    }

    // 输出彩色字到TextPanel
    public void appendToPane(String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = textMessage.getDocument().getLength();
        textMessage.setCaretPosition(len);
        textMessage.setCharacterAttributes(aset, false);
        textMessage.replaceSelection(msg);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ServerUI");
        frame.setContentPane(new ServerUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(50, 50, 600, 500);
        frame.setVisible(true);
    }
}

class BW_ReturnFile implements Runnable {
    private Thread t;
    private int port;
    private ArrayList<String> fileList;
    private Random rd = new Random();
    private ServerUI ui;

    BW_ReturnFile(int _port, String directoryPath, ServerUI _ui) {
        port = _port;
        fileList = new ArrayList<>();
        fileList = getAllFilePaths(directoryPath,fileList);
        ui=_ui;
        ui.appendToPane("加载了"+fileList.size()+"个文件",Color.cyan);
        start();
    }

    private static ArrayList<String> getAllFilePaths(String DirPath, ArrayList<String> AllFilePaths ) {
        File dirfile = new File( DirPath );//根据DirPath实例化一个File对象
        File[] files = dirfile.listFiles();//listFiles():以相对路径返回该目录下所有的文件名的一个File对象数组
        if ( files == null ) {
            return AllFilePaths;//[]
        }
        //遍历目录-1
//            for ( File file : files ) {
//                // isDirectory()是检查一个对象是否是文件夹,如果是则返回true，否则返回false
//                if ( file.isDirectory() ) {
//                    getAllFilePaths( file.getAbsolutePath(), AllFilePaths );// getAbsolutePath(): 返回的是定义时的路径对应的相对路径
//                } else {
//                    AllFilePaths.add( file.getPath() );
//                }
//            }
        //遍历目录-2
        for (int i = 0; i < files.length; i++) {
            // isDirectory()是检查一个对象是否是文件夹,如果是则返回true，否则返回false
            File file = files[i];
            if ( file.isDirectory() ) {
                getAllFilePaths( file.getAbsolutePath(), AllFilePaths );// getAbsolutePath(): 返回的是定义时的路径对应的相对路径
            } else {
                AllFilePaths.add( file.getPath() );
            }
        }
        return AllFilePaths;
    }

    public void start() {
        t = new Thread(this);
        t.start();
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                new RequestHandler_SendDataPack(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop(){
        t.stop();
    }

    class RequestHandler_SendDataPack implements Runnable {
        Thread t;
        private Socket socClient;

        RequestHandler_SendDataPack(Socket _socClient) {
            socClient = _socClient;
            start();
        }

        public void start() {
            t = new Thread(this);
            t.start();
        }

        public void run() {
            try {
                ui.appendToPane("\n收到请求:\n    IP:"+socClient.getInetAddress(),Color.cyan);
                // 初始化一个数据包
                DataPack dataPack = new DataPack();
                Path filePath = Path.of(fileList.get(rd.nextInt(fileList.size())));
                dataPack.fileName = filePath.getFileName().toString();
                dataPack.fileBytes= Files.readAllBytes(filePath);
                dataPack.calSize();
                // 显示信息
                ui.appendToPane("\n数据包打包成功:\n    文件名:" + dataPack.fileName + " 文件大小:" + dataPack.fileSize / 1024 + "KB", Color.CYAN);
                // 序列化数据包为字节
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(dataPack);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                // 发送
                socClient.getOutputStream().write(bytes);
                socClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}