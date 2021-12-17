package Client;

import DataPack.DataPack;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientUI {
    private JPanel mainPanel;
    private JTextPane textMessage;
    private JButton btnGet;
    private JTextField textIP;
    private JTextField textPort;
    private JButton btnOpen;
    private JTextField textSavePath;
    private JButton btnSave;
    private JButton btnSelectPath;

    private DataPack dataPack;

    private ClientUI self = this;
    private boolean isTextMessageBusy = false;

    ClientUI() {
        // 设置文本框背景色
        textMessage.setBackground(Color.DARK_GRAY);
        // 按钮"获取"的动作
        btnGet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnGet.setEnabled(false);
                preWordAppendToPanel("\n获取数据包中...", Color.YELLOW);
                new BW_GetFile(textIP.getText(), Integer.valueOf(textPort.getText()), self);
            }
        });
        // 按钮"保存"的动作
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveToFile();
                    preWordAppendToPanel("保存完毕\n", new Color(66, 153, 254));
                } catch (IOException ex) {
                    System.out.println("文件保存出现问题");
                    ex.printStackTrace();
                }
            }
        });
        // 按钮"打开"的动作
//        btnOpen.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                boolean[] isKeyPressing = new boolean[4];
//                for(boolean element:isKeyPressing){
//                    element=false;
//                }
//                JFrame frame = new JFrame();
//                JPanel panel = new JPanel(null);
//                frame.setContentPane(panel);
//                JLabel label = new JLabel();
//                ImageIcon imageIcon = new ImageIcon(new ImageIcon("bg1.png").getImage().getScaledInstance(50, 20, Image.SCALE_DEFAULT));
//                label.setIcon(imageIcon);
//                label.setBounds(0,0,200,100);
//                panel.add(label);
//                frame.setSize(500,300);
//                frame.setVisible(true);
//                frame.addKeyListener(new KeyListener() {
//                    @Override
//                    public void keyTyped(KeyEvent e) {
//                    }
//
//                    @Override
//                    public void keyPressed(KeyEvent e) {
//                        if(e.getKeyChar()=='w')
//                            isKeyPressing[0]=true;
//                        if(e.getKeyChar()=='s')
//                            isKeyPressing[1]=true;
//                        if(e.getKeyChar()=='a')
//                            isKeyPressing[2]=true;
//                        if(e.getKeyChar()=='d')
//                            isKeyPressing[3]=true;
//                    }
//
//                    @Override
//                    public void keyReleased(KeyEvent e) {
//                        if(e.getKeyChar()=='w')
//                            isKeyPressing[0]=false;
//                        if(e.getKeyChar()=='s')
//                            isKeyPressing[1]=false;
//                        if(e.getKeyChar()=='a')
//                            isKeyPressing[2]=false;
//                        if(e.getKeyChar()=='d')
//                            isKeyPressing[3]=false;
//                    }
//                });
//                new Thread(new Runnable(){
//                    @Override
//                    public void run() {
//                        for(;;){
//                            try {
//                                int speed = 5;
//                                Thread.sleep(50);
//                                Point location = label.getLocation();
//                                if(isKeyPressing[0]==true)
//                                    label.setLocation(location.x,location.y-speed);
//                                if(isKeyPressing[1]==true)
//                                    label.setLocation(location.x,location.y+speed);
//                                if(isKeyPressing[2]==true)
//                                    label.setLocation(location.x-speed,location.y);
//                                if(isKeyPressing[3]==true)
//                                    label.setLocation(location.x+speed,location.y);
//                            } catch (InterruptedException ex) {
//                                ex.printStackTrace();
//                            }
//                        }
//                    }
//                }).start();
//            }
//        });
        btnOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame(dataPack.fileName);
                ImageIcon icon = new ImageIcon(dataPack.fileBytes);
                Image image = icon.getImage();
                // 重写渲染方法，使图片和窗口一样大
                JPanel panel = new JPanel() {
                    public void paintComponent(Graphics g) {
                        // 图片随窗体大小而变化
                        g.drawImage(image, 0, 0, frame.getSize().width, frame.getSize().height, frame);
                    }
                };
                frame.add(panel);
                frame.setResizable(false);
                // 计算比例
                int imageWidth = image.getWidth(frame);
                int imageHeight = image.getHeight(frame);
                float ratio = ((float) imageHeight) / ((float) imageWidth);
                // 初始大小为图片分辨率
                frame.setBounds(50, 50, imageWidth, imageHeight);
                // 当窗口大小变动时，以宽为基准，保持固定比例
                frame.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        int frameWidth = frame.getWidth();
                        frame.setBounds(frame.getX(), frame.getY(), frameWidth, (int) (frameWidth * ratio));
                    }
                });
                frame.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(e.getButton()==MouseEvent.BUTTON3){
                            System.out.println("sss");
                            frame.dispose();
                        }
                    }
                });
                // 滚轮放大缩小
                frame.addMouseWheelListener(new MouseWheelListener() {
                    @Override
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        if (e.getWheelRotation() == 1) {
                            frame.setSize(frame.getWidth() - 50, frame.getHeight());
                        }
                        if (e.getWheelRotation() == -1) {
                            frame.setSize(frame.getWidth() + 50, frame.getHeight());
                        }

                    }
                });
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setVisible(true);
            }
        });
        // 按钮"浏览"的动作
        btnSelectPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = fileChooser.showOpenDialog(btnSelectPath);
                if (ret == JFileChooser.APPROVE_OPTION)
                    textSavePath.setText(fileChooser.getSelectedFile().getPath() + "\\");
            }
        });
    }

    // 保存数据包中的文件到计算机
    private void saveToFile() throws IOException {
        Path fullPath = Path.of(textSavePath.getText() + dataPack.fileName);
        Files.write(fullPath, dataPack.fileBytes);
    }

    // 逐字输出
    public boolean preWordAppendToPanel(String msg, Color color) {
        new Runnable() {
            private Thread t;

            public void start() {
                t = new Thread(this);
                t.start();
            }

            @Override
            public void run() {
                for (int i = 0; i < msg.length(); i++) {
                    appendToPanel(msg.charAt(i) + "", color);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        return true;
    }

    // 输出彩色字到TextPanel
    public void appendToPanel(String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = textMessage.getDocument().getLength();
        textMessage.setCaretPosition(len);
        textMessage.setCharacterAttributes(aset, false);
        textMessage.replaceSelection(msg);
    }

    // 后台获取数据包完成后将调用此方法以完成本次工作
    public void GetFileComplete(DataPack _dataPack, boolean isError) {
        dataPack = _dataPack;
        if (!isError) {
            preWordAppendToPanel("\n获取数据包成功:\n    文件名:" + dataPack.fileName + " 文件大小:" + dataPack.fileSize / 1024 + "KB", Color.CYAN);
        }
        else {
            preWordAppendToPanel("\n获取失败", Color.RED);
        }
        btnGet.setEnabled(true);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ClientUI");
        frame.setContentPane(new ClientUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(50, 50, 500, 500);
        frame.setVisible(true);
    }

}
