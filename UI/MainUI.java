package UI;

import Core.Crypto;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainUI extends JFrame {
    private JPanel mainPanel; // 主Panel
    private JTextField textFilePath;// 文件路径文本框
    private JTextField textKey; // 密钥文本框
    private JButton btnEncryptDecrypt; // 加解密按钮
    private JProgressBar progressBar; // 进度条
    private JLabel lblStatus; // 状态Label
    private JButton btnGenerateRandomKey;
    private JPanel panelKey;

    private final Crypto crypto = new Crypto(); // Crypto类的唯一实例

    /**
     * MainUI构造函数，目前主要负责事件添加和设置一些初始值
     */
    public MainUI() {
        // Swing GUI编辑器生成代码
        $$$setupUI$$$();
        // 拖入文件的事件(对主要面板和输入框都设置以达到只要文件拖入窗口就能导入的效果)
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
                for (int i = 0; i < flavors.length; i++) {
                    if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
                        return true;
                    }
                }
                return false;
            }
        });
        // 设置进度条最大值
        progressBar.setMaximum(100);
        // 按钮“加密 / 解密”的点击事件
        btnEncryptDecrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnEncryptDecrypt.setEnabled(false);
                String a = textFilePath.getText(), b = textKey.getText();
                // 检测两个文本框到底有没有写东西
                if (a.isEmpty() || b.isEmpty()) {
                    // 没写就提示
                    if (lblStatus.getText().length() == 20)
                        return;
                    if (lblStatus.getText().startsWith("请先输入文件路径和密钥")) {
                        lblStatus.setText(lblStatus.getText().replace("。", "") + "！");
                        btnEncryptDecrypt.setEnabled(true);
                    }
                    else {
                        lblStatus.setText("请先输入文件路径和密钥。");
                        btnEncryptDecrypt.setEnabled(true);
                    }
                } else {
                    // 写了就开始运行加解密
                    new BackRunnerCrypt().start();
                }
                lblStatus.requestFocus();
            }
        });
        // 显示灰色提示消息
        textFilePath.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //获取焦点时，清空提示内容
                String temp = textFilePath.getText();
                if (temp.equals("支持拖拽文件至窗口内")) {
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
                    textFilePath.setText("支持拖拽文件至窗口内");
                }

            }
        });
        // 密钥输入框回车开始加解密
        textKey.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnEncryptDecrypt.doClick();
            }
        });
        // 按钮”随机“的点击事件
        btnGenerateRandomKey.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SecureRandom rd = new SecureRandom();
                String myAscii = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                StringBuilder keyStr = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    keyStr.append(myAscii.charAt(rd.nextInt(62)));
                }
                textKey.setText(keyStr.toString());
            }
        });
        // 边框设置
        LineBorder border = new TextBorderUtlis(new Color(70, 70, 70), 2, false);
        textFilePath.setBorder(border);
        textKey.setBorder(border);
        btnGenerateRandomKey.setBorder(border);
        progressBar.setBorder(border);
        btnEncryptDecrypt.setBorder(new TextBorderUtlis(new Color(70, 70, 70), 5, false));

        // 修改窗口风格为当前系统对应的风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        // 显示窗口
        setVisible(true);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setTitle("Crypto v0.1.2 by LZHJK");
        pack();
        // 设置焦点位置
        textKey.dispatchEvent(new FocusEvent(textKey, FocusEvent.FOCUS_GAINED, true));
        textKey.requestFocusInWindow();
    }

    class BackRunnerCrypt implements Runnable {
        private Thread t;

        public BackRunnerCrypt() {
        }

        public void start() {
            t = new Thread(this);
            t.start();
        }

        public void run() {
            boolean isError = false;
            lblStatus.setText("运行中...");
            // 再运行一个进程控制进度条
            new BackRunnerShowProgress().start();
            try {
                crypto.Crypt(textFilePath.getText(), textKey.getText());
            } catch (NullPointerException e) {
                lblStatus.setText("错误：找不到文件，请检查路径拼写");
                isError = true;
            } catch (Exception e) {
                e.printStackTrace();
                lblStatus.setText("错误：" + e.getMessage());
                isError = true;
            }
            finally {
                if (!isError)
                    lblStatus.setText("成功。");
                btnEncryptDecrypt.setEnabled(true);
                t.stop();
            }

        }

        class BackRunnerShowProgress implements Runnable {
            private Thread t;

            public void start() {
                t = new Thread(this);
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
                            t.stop();
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
        textFilePath.setText("");
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

    private void createUIComponents() {
        // TODO: place custom component creation code here
        mainPanel = new JPanel() {
            Image image = new ImageIcon("Curve.png").getImage();

            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, mainPanel.getWidth(), mainPanel.getHeight(), mainPanel);
            }
        };
        panelKey = new JPanel() {
            Image image = new ImageIcon("Curve.png").getImage();

            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, panelKey.getWidth(), panelKey.getHeight(), panelKey);
            }
        };
    }
}