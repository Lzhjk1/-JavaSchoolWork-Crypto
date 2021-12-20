package UI;

import Core.Crypto;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * 动画效果窗口
 */
public class FX extends JFrame {
    private JPanel mainPanel, minorPanel; // 主Panel 和 副Panel
    private JLabel lblStatus; // 表示状态的Label
    private JTextField[][] textNodes; // 每个格子都是一个TextField，这是个TextFiled数组
    private Crypto crypto; // 存放Crypto的实例
    private int timeRatio = 50; // 决定动画持续时间的系数，50约为12s

    // 预先定义防止反复创建变量，优化性能
    private Color colorRed = new Color(150,32,32); // 红色
    private int redWaveRange = 80; // 红色颜色波动范围
    private Color colorGreen = new Color(54, 150, 48); // 绿色
    private int greenWaveRange = 80; // 绿色颜色波动范围

    /**
     * FX的主函数，仅供单独打开动画调试用<br>
     * @param args 命令行参数
     */
    public static void main(String[] args){
        // 若需仅打开动画，便启动这个主函数，并设置上面的timeRatio以控制持续时间
        new FX(null);
    }

    /**
     * 从时间秒数到timeRatio系数的转换函数<br>
     * 利用函数拟合器获得，基本为线性，相关度0.99996<br>
     * @param secs 秒数
     * @return 返回timeRatio系数
     */
    private int secsToRatio(float secs){
        if(secs<5)
            return 1;

        double A,B,C,D,X=secs;
        A = 1.01563088882032E+15;
        B = -1.00541928494497;
        C = 176451864312395d;
        D = -20.3014929981133;
        return (int)((A-D)/(1+Math.pow(X/C,B))+D);
    }

    /**
     * 构造函数<br>
     * 导入Crypto实例后 判断是否为单独运行动画<br>
     * 不是：运行{@link FX#SpeedTest()}函数 ({@link FX#SpeedTest()}之后会自动调用下面两个函数)<br>
     * 是：跳过{@link FX#SpeedTest()}函数，直接运行{@link FX#Initialize()}函数和{@link FX#Start()}函数<br>
     * @param _crypto 即将运行的{@link Core.Crypto}实例
     */
    FX(Crypto _crypto) {
        crypto = _crypto;
        if(crypto==null){
            Initialize();
            Start();
        }
        else{
            SpeedTest();
        }

    }

    /**
     * 初始化界面元素<br>
     */
    private void Initialize() {
        // 基本布局：JFrame(mainPanel:Border(North->minorPanel:Grid(textField[15][8])))
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(36, 36, 36));
        minorPanel = new JPanel(new GridLayout(15, 8,1,1));
        minorPanel.setBackground(new Color(36, 36, 36));
        lblStatus = new JLabel("File Encrypting...");
        lblStatus.setForeground(new Color(139, 150, 158));
        mainPanel.add(lblStatus, BorderLayout.NORTH);
        mainPanel.add(minorPanel, BorderLayout.CENTER);
        // 初始化textNodes数组
        textNodes = new JTextField[15][8];
        Font font = new Font("微软雅黑",Font.BOLD,12);
        Color colorRed = new Color(150,32,32);
        Color colorBg = new Color(180,180,180);
        TextBorderUtlis border = new TextBorderUtlis(new Color(36, 36, 36), 0, false);
        // 设置每一个格子
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 8; col++) {
                JTextField textField = textNodes[row][col];
                textNodes[row][col] = new JTextField("255", 3);
                textNodes[row][col].setForeground(colorBg);
                textNodes[row][col].setBackground(colorRed);
                textNodes[row][col].setHorizontalAlignment(JTextField.CENTER);
                textNodes[row][col].setBorder(border);
                textNodes[row][col].setVisible(false);
                textNodes[row][col].setEditable(false);
                textNodes[row][col].setFont(font);
                minorPanel.add(textNodes[row][col]);
            }
        }
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
        setBounds(50,50,300,300);
    }

    /**
     * 测试当前系统加解密速度，以确定动画持续市时长<br>
     */
    private void SpeedTest() {
        // 新的线程，用于测速：用时1s
        new Thread(() -> {
            long start, end;
            float cryStart, cryEnd, speed = 0;
            for (int i = 0; i < 10; i++) {
                start = System.currentTimeMillis();
                cryStart = (int) crypto.getCurrentProgress();
                // 等待
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //
                end = System.currentTimeMillis();
                cryEnd = (int) crypto.getCurrentProgress();
                speed += (int) ((cryEnd - cryStart) / (end - start));
            }
            // 测试完后调用
            if(speed<100){ // 若速度明显过慢，则是文件太小，完成很快
                System.out.println("速度过慢，认为是文件过小");
                timeRatio=1;
                Initialize();
                Start();
            }
            else {
                SpeedTestComplete((int) speed / 10);
            }
        }).start();
    }

    /**
     * 测速完成后调用的函数<br>
     * 在此处计算动画持续时间<br>
     * 以及调用{@link FX#Initialize()}和{@link FX#Start()}<br>
     * @param speed 前面测速所得的速度，单位bytes/ms<br>
     */
    private void SpeedTestComplete(int speed) {
        System.out.println(speed);
        int secs = (int) (crypto.getFileLength()/(speed*1000));
        System.out.printf("需要%d秒\n",secs);
        timeRatio=secsToRatio(secs);
        Initialize();
        Start();
    }

    /**
     * 开始动画<br>
     */
    private void Start() {
        // counts二维数组用于给每一个格子确定变化次数
        int counts[][] = new int[15][8];
        Random rd = new Random();
        // 填充数据以控制时间
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 8; col++) {
                counts[row][col] = rd.nextInt(timeRatio) + 4;// 加4防止动画过短造成视觉效果不佳
            }
        }
        // 开始时格子逐渐显现的过程(逐个设置可见；颜色亮度波动)
        // 遍历每一个格子并对齐调用nodeActionStart()使颜色变亮一会
        new Thread(() -> {
            for (int row = 0; row < 15; row++) {
                for (int col = 0; col < 8; col++) {
                    try {
                        Thread.sleep(5);
                        nodeActionStart(textNodes[row][col]);
                        textNodes[row][col].setVisible(true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        // 格子数字变换的效果
        // 遍历每一个格子，每遍历一次就将其剩余变换次数减1，并随机变换数字或不变
        new Thread(() -> {
            while (true) {
                boolean isComplete = true;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int row = 0; row < 15; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (counts[row][col] > 0) { // 还未完成
                            // 随机变或不变
                            if(rd.nextInt(2)>0)
                                textNodes[row][col].setText((rd.nextInt(254) + 1) + "");
                            counts[row][col]--;
                            isComplete = false;
                        } else if (counts[row][col] == 0) { // 已完成
                            // 完成的格子就变成绿色，字改为0，然后调用nodeActionComplete()使颜色变亮一会
                            counts[row][col]--;
                            textNodes[row][col].setText("0");
                            textNodes[row][col].setBackground(colorGreen);
                            nodeActionComplete(textNodes[row][col]);
                        }
                    }
                }
                // 如果有任意一个格子还没完成就不会执行
                if (isComplete) {
                    // 全部完成时，遍历每一个格子并调用nodeActionComplete()使颜色变亮一会
                    new Thread(() -> {
                        for (int row = 0; row < 15; row++) {
                            for (int col = 0; col < 8; col++) {
                                try {
                                    Thread.sleep(5);
                                    nodeActionComplete(textNodes[row][col]);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        // 等待1s后调用frameActionShrink()关闭动画
                        new Thread(()->{
                            try {
                                Thread.sleep(1000);
                                frameActionShrink();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }).start();
                    lblStatus.setText("File Encrypted");
                    break;
                }
            }
        }).start();
    }

    /**
     * 格子开始时的动作 (红色：亮度上升再回落)<br>
     * @param text TextFiled格子实例
     */
    private void nodeActionStart(JTextField text) {
        new Thread(() -> {
            for (int i = 0; i < 80; i += 8) {
                try {
                    Thread.sleep(20);
                    text.setBackground(new Color(150 + i, 32, 32));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < 80; i += 8) {
                try {
                    Thread.sleep(20);
                    text.setBackground(new Color(230 - i, 32, 32));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     * 格子完成时的动作 (绿色：亮度上升再回落)<br>
     * @param text TextFiled格子实例
     */
    private void nodeActionComplete(JTextField text) {
        new Thread(() -> {
            for (int i = 0; i < 80; i += 8) {
                try {
                    Thread.sleep(20);
                    text.setBackground(new Color(54, 150 + i, 48));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < 80; i += 8) {
                try {
                    Thread.sleep(20);
                    text.setBackground(new Color(54, 230 - i, 48));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     * 动画完成时的动作 (窗口向上收缩并最后关闭)<br>
     */
    private void frameActionShrink(){
        new Thread(() -> {
            for (int i = 0; i < getHeight(); i ++) {
                try {
                    Thread.sleep(10);
                    setSize(getWidth(),getHeight()-(int)Math.sqrt(i));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dispose();
        }).start();
    }

}
