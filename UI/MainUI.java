package UI;

import Core.Crypto;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;


public class MainUI extends JFrame {
    private JPanel mainPanel; // 主Panel
    private JPanel panelKey; // 密钥输入框所属Panel
    private JLabel lblStatus; // 状态Label
    private JButton btnEncryptDecrypt; // 加解密按钮
    private JButton btnGenerateRandomKey;
    private JTextField textFilePath;// 文件路径文本框
    private JTextField textKey; // 密钥文本框
    private JProgressBar progressBar; // 进度条

    private Crypto crypto = new Crypto(); // Crypto类的唯一实例

    public static void main(String[] args) {
        new MainUI();
    }

    /**
     * 用于分散构造函数：给窗口添加菜单栏（包含一个关于按钮）
     */
    private void addAboutMenu() {
        // 添加一个顶部菜单栏
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Color fontColor = new Color(139, 150, 158);
        Color backColor = new Color(50, 50, 50);
        TextBorderUtlis border = new TextBorderUtlis(backColor, 2, false);
        // 顶部菜单栏
        JMenuBar menuBar = new JMenuBar();
        JMenu menuAbout = new JMenu("关于");
        JMenuItem menuItemAbout = new JMenuItem("关于");

        menuAbout.setFont(font);
        menuAbout.setForeground(fontColor);
        menuAbout.add(menuItemAbout);
        menuAbout.setBackground(backColor);
        menuAbout.setBorder(border);

        menuItemAbout.setFont(font);
        menuItemAbout.setForeground(fontColor);
        menuItemAbout.setBackground(backColor);
        menuItemAbout.setBorder(border);

        menuBar.add(menuAbout);
        menuBar.setBackground(backColor);
        menuBar.setBorder(border);
        setJMenuBar(menuBar);
        // 设置菜单栏”关于“事件
        menuItemAbout.addActionListener(e -> {
            new AboutWindow();
        });
    }

    /**
     * 用于分散构造函数：设置组件边框
     */
    private void setBorder() {
        LineBorder border2 = new TextBorderUtlis(new Color(70, 70, 70), 2, false);
        textFilePath.setBorder(border2);
        textKey.setBorder(border2);
        btnGenerateRandomKey.setBorder(border2);
        progressBar.setBorder(border2);
        btnEncryptDecrypt.setBorder(new TextBorderUtlis(new Color(70, 70, 70), 5, false));
    }

    /**
     * MainUI构造函数，目前主要负责事件添加和设置一些初始值
     */
    public MainUI() {
        // Swing GUI编辑器生成代码
        $$$setupUI$$$();
        // 添加菜单栏
        addAboutMenu();
        // 设置进度条最大值
        progressBar.setMaximum(100);
        // 拖入文件的事件
        textFilePath.setTransferHandler(new TransferHandler() {
            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    Object o = t.getTransferData(DataFlavor.javaFileListFlavor);// 获取文件路径

                    String filepath = o.toString();
                    if (filepath.startsWith("[")) {
                        filepath = filepath.substring(1);
                    }
                    if (filepath.endsWith("]")) {
                        filepath = filepath.substring(0, filepath.length() - 1);
                    }
                    textFilePath.setText(filepath);
                    textFilePath.setForeground(new Color(139, 150, 158));
                    lblStatus.setText("文件路径已录入，等待开始...");
                    progressBar.setValue(0);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                for (DataFlavor flavor : flavors) {
                    if (DataFlavor.javaFileListFlavor.equals(flavor)) {
                        return true;
                    }
                }
                return false;
            }
        });
        // 按钮“加密 / 解密”的点击事件
        btnEncryptDecrypt.addActionListener(e -> {
            btnEncryptDecrypt.setEnabled(false);
            String a = textFilePath.getText(), b = textKey.getText();
            // 检测两个文本框到底有没有写东西
            if (a.equals("支持拖拽文件至此") || b.isEmpty()) {
                // 没写就提示
                if (lblStatus.getText().length() == 20) {
                    btnEncryptDecrypt.setEnabled(true);
                } else if (lblStatus.getText().startsWith("请先输入文件路径和密钥")) {
                    btnEncryptDecrypt.setEnabled(true);
                    lblStatus.setText(lblStatus.getText().replace("。", "") + "！");
                } else {
                    btnEncryptDecrypt.setEnabled(true);
                    lblStatus.setText("请先输入文件路径和密钥。");
                }
            } else {
                // 写了就开始运行加解密
                new FX(crypto);
                new BW_Crypt().start();
            }
            lblStatus.requestFocus();
        });
        // 显示灰色提示消息
        textFilePath.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //获取焦点时，清空提示内容
                String temp = textFilePath.getText();
                if (temp.equals("支持拖拽文件至此")) {
                    textFilePath.setText("");
                    textFilePath.setForeground(new Color(139, 150, 158));
                }

            }

            @Override
            public void focusLost(FocusEvent e) {
                //失去焦点时，没有输入内容，显示提示内容
                String temp = textFilePath.getText();
                if (temp.equals("")) {
                    textFilePath.setForeground(Color.GRAY);
                    textFilePath.setText("支持拖拽文件至此");
                }

            }
        });
        // 密钥输入框回车开始加解密
        textKey.addActionListener(e -> btnEncryptDecrypt.doClick());
        // 按钮”随机“的点击事件
        btnGenerateRandomKey.addActionListener(e -> {
            SecureRandom rd = new SecureRandom();
            String myAscii = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            StringBuilder keyStr = new StringBuilder();
            for (int i = 0; i < 32; i++) {
                keyStr.append(myAscii.charAt(rd.nextInt(62)));
            }
            textKey.setText(keyStr.toString());
        });
        // 边框设置
        setBorder();
        // 修改窗口风格为当前系统对应的风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        // 显示窗口
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setTitle("Crypto");
        setVisible(true);
        setLocation(300, 100);
        pack();
        // 设置焦点位置
        textKey.requestFocus();
    }

    /**
     * 用于后台运行加解密
     */
    class BW_Crypt implements Runnable {
        public void start() {
            Thread t = new Thread(this);
            t.start();
        }

        public void run() {
            boolean isError = false;
            lblStatus.setText("运行中...");
            // 再运行一个进程控制进度条
            new BW_ShowProgress().start();
            try {
                crypto.Crypt(textFilePath.getText(), textKey.getText());
            } catch (NullPointerException e) {
                lblStatus.setText("错误：找不到文件，请检查路径拼写");
                isError = true;
            } catch (Exception e) {
                e.printStackTrace();
                lblStatus.setText("错误：" + e.getMessage());
                isError = true;
            } finally {
                if (!isError)
                    lblStatus.setText("成功。");
                btnEncryptDecrypt.setEnabled(true);
                crypto = new Crypto();
            }

        }

        /**
         * 控制进度条的显示
         */
        class BW_ShowProgress implements Runnable {
            public void start() {
                Thread t = new Thread(this);
                t.start();
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    float progress = crypto.ReturnProgress();
                    progressBar.setValue((int) (progress * 100));
                    // 好了就退回0
                    if (progress == 1f) {
                        try {
                            Thread.sleep(500);
                            break;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

// -----------------------------------------以下为GUI编辑器自动生成-----------------------------------------------

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), -1, -1));
        textFilePath = new JTextField();
        textFilePath.setBackground(new Color(-13487566));
        textFilePath.setForeground(new Color(-7629154));
        textFilePath.setHorizontalAlignment(10);
        textFilePath.setText("支持拖拽文件至此");
        mainPanel.add(textFilePath, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(300, 30), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setForeground(new Color(-7629154));
        label1.setText("文件路径 :");
        mainPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setForeground(new Color(-7629154));
        label2.setText("密钥 :");
        mainPanel.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEncryptDecrypt = new JButton();
        btnEncryptDecrypt.setBackground(new Color(-13487566));
        btnEncryptDecrypt.setEnabled(true);
        Font btnEncryptDecryptFont = this.$$$getFont$$$(null, -1, 20, btnEncryptDecrypt.getFont());
        if (btnEncryptDecryptFont != null) btnEncryptDecrypt.setFont(btnEncryptDecryptFont);
        btnEncryptDecrypt.setForeground(new Color(-7629154));
        btnEncryptDecrypt.setHideActionText(false);
        btnEncryptDecrypt.setText("加密  /  解密");
        mainPanel.add(btnEncryptDecrypt, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(315, 30), null, 0, false));
        progressBar = new JProgressBar();
        progressBar.setBackground(new Color(-13487566));
        progressBar.setBorderPainted(false);
        progressBar.setForeground(new Color(-13605760));
        mainPanel.add(progressBar, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(315, 4), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("---------");
        mainPanel.add(label3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("");
        mainPanel.add(label4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(315, 17), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setForeground(new Color(-7629154));
        label5.setText("密钥输入完成之后回车也可开始加解密          输出文件将以.cryptxxx的后缀名输出到源文件相同目录下");
        mainPanel.add(label5, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(315, 17), null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(315, 14), null, 0, false));
        panelKey.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panelKey, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(315, 34), null, 0, false));
        textKey = new JTextField();
        textKey.setBackground(new Color(-13487566));
        textKey.setForeground(new Color(-7629154));
        textKey.setToolTipText("");
        panelKey.add(textKey, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(300, 30), null, 0, false));
        btnGenerateRandomKey = new JButton();
        btnGenerateRandomKey.setBackground(new Color(-13487566));
        btnGenerateRandomKey.setForeground(new Color(-7629154));
        btnGenerateRandomKey.setText("随机");
        panelKey.add(btnGenerateRandomKey, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblStatus = new JLabel();
        Font lblStatusFont = this.$$$getFont$$$(null, -1, 20, lblStatus.getFont());
        if (lblStatusFont != null) lblStatus.setFont(lblStatusFont);
        lblStatus.setForeground(new Color(-7629154));
        lblStatus.setText("等待命令...");
        mainPanel.add(lblStatus, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    /**
     * Swing GUI设计器生成代码：自定义组件初始化，我这里用于设置背景图
     */
    private void createUIComponents() {
        Image image = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("Resource/Bg.png"))).getImage();
        mainPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, mainPanel.getWidth(), mainPanel.getHeight(), mainPanel);
            }
        };
        panelKey = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, panelKey.getWidth(), panelKey.getHeight(), panelKey);
            }
        };
    }
}