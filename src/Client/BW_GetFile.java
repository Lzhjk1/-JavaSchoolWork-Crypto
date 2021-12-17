package Client;

import DataPack.DataPack;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class BW_GetFile implements Runnable {
    private Thread t;
    private int port;
    private String addres;
    private ClientUI ui;

    BW_GetFile(String _addres, int _port, ClientUI _ui) {
        port = _port;
        addres = _addres;
        ui = _ui;
        start();
    }

    public void start() {
        t = new Thread(this);
        t.start();
    }

    public void run() {
        try {
            DataPack dataPack = null;
            // 标记连接是否失败
            boolean isError = false;
            // 与服务器建立连接
            Socket socket = null;
            try {
                socket = new Socket(addres, port);
            } catch (ConnectException e) {
                isError = true;
                ui.preWordAppendToPanel("\n连接失败:" + e.toString(), Color.RED);
                e.printStackTrace();
            }
            if (!isError) {
                // 反序列化数据包
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                dataPack = (DataPack) objectInputStream.readObject();
                // 关闭流
                objectInputStream.close();
            }
            // 关闭连接
            ui.GetFileComplete(dataPack, isError);
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
