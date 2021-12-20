package UI;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AboutWindow extends JFrame{
    AboutWindow(){
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 12);
        // 读取About.txt内的说明信息
        String strAbout = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("Resource/About.txt"), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String tmp = "";
            while ((tmp = reader.readLine()) != null) {
                sb.append(tmp + "\n");
            }
            strAbout = sb.toString();
            reader.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        // 创建窗口
        setTitle("关于");
        JPanel panel = new JPanel(new BorderLayout());
        JTextPane text = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(text);
        panel.add(scrollPane, BorderLayout.CENTER);
        text.setBackground(new Color(70, 70, 70));
        text.setForeground(new Color(139, 150, 158));
        text.setFont(font);
        text.setEditable(false);
        // 逐字输出关于信息
        String finalStrAbout = strAbout;
        new Thread(() -> {
            for (int i = 0; i < finalStrAbout.length(); i++) {
                try {
                    Thread.sleep(100);
                    text.setText(text.getText() + finalStrAbout.charAt(i));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

        setContentPane(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(100, 100, 420, 500);
        setVisible(true);
    }

}
