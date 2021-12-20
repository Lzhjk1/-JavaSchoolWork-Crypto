package Core;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 加解密类
 */
public class Crypto {
    private String filePath; // 文件路径
    private int depth = 5; // 加密深度(指对每个字节进行多少次操作)
    private Random rd; // 随机数生成器
    private String encryptedSuffix; // 加上crypt后的后缀名
    private String sourceSuffix; // 原始后缀名
    private boolean isEncrypt = true; // 当前要进行的是什么操作，true为加密，false为解密
    // 用于进度显示
    private double fileLength; // 文件总字节数
    private double currentProgress; // 当前已处理的字节数

    public double getFileLength() {
        return fileLength;
    }
    public double getCurrentProgress() {
        return currentProgress;
    }

    /**
     * 导入参数：filePath:String
     * <p>导入后立刻进行处理<br>
     * 1、文件是否存在<br>
     * 2、提取后缀名<br></p>
     * @param _filePath 需要加解密的文件路径
     */
    private void filePath(String _filePath) {
        filePath = _filePath;
        // 检查文件是否存在
        File file = new File(_filePath);
        if (!file.exists()) {
            //JOptionPane.showMessageDialog(null, String.format("文件%s不存在,请重新设置",_filePath),"文件不存在", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 解析后缀名
        Pattern pattern = Pattern.compile("\\.+\\w+");
        Matcher matcher = pattern.matcher(filePath);
        String tmpSuffix = null;
        while (matcher.find()) {
            String tmp = matcher.group();
            tmpSuffix = tmp.substring(1);
        }
        if (tmpSuffix.startsWith("crypt")) {
            isEncrypt = false;
            encryptedSuffix = tmpSuffix;
            sourceSuffix = tmpSuffix.substring(5);
        } else {
            isEncrypt = true;
            sourceSuffix = tmpSuffix;
            encryptedSuffix = "crypt" + tmpSuffix;
        }
    }

    /**
     * 导入参数 key:String
     * <p>导入后立刻进行处理<br>
     * 1、将字符串key的哈希值作为种子初始化随机数生成器rd<br></p>
     *
     * @param keyStr 作为密钥的字符串
     */
    private void key(String keyStr) {
        // 用字符串的Hash值做Random种子
        rd = new Random(keyStr.hashCode());
    }

    /**
     * 提供给外界以获取当前加解密处理的进度
     *
     * @return 0~1的float数值
     */
    public float ReturnProgress() {
        return (float) (currentProgress / fileLength);
    }



    /**
     * Crypto类主要函数，传入参数并开始加解密操作
     * @param _filePath 文件路径
     * @param keyStr    作为密钥的字符串
     * @throws Exception 可能有Null,FileNotFound
     */
    public void Crypt(String _filePath, String keyStr) throws Exception{
        // 录入参数并进行相关处理
        filePath(_filePath);
        key(keyStr);
        //
        File sourceFile = new File(filePath);
        if(!sourceFile.exists())
            throw new NullPointerException();
        File outputFile = null;
        // 确认输出文件的后缀名
        if (isEncrypt) {
            outputFile = new File(filePath.replace(sourceSuffix, "crypt" + sourceSuffix));
        } else {
            outputFile = new File(filePath.replace(encryptedSuffix, sourceSuffix));
        }
        // 创建各种流
        InputStream in = null;
        OutputStream out = null;
        if (!outputFile.exists())
            outputFile.createNewFile();
        in = new FileInputStream(sourceFile);
        out = new FileOutputStream(outputFile);
        // 用于计算进度
        fileLength = in.available();
        currentProgress = 0;
        // 文件读取相关参数
        int bufferlength = 1024;
        byte[] bytes = new byte[bufferlength];
        int bytesReadied = 0;
        while ((bytesReadied = in.read(bytes)) != -1) {
            // 处理 -------------------
            for (int i = 0; i < bytesReadied; i++) {
                for (int j = 0; j < depth; j++)
                    bytes[i] ^= rd.nextInt(1000);
                currentProgress++;
            }
            //------------------------
            out.write(bytes, 0, bytesReadied);
        }
        out.close();
        in.close();
    }
}
