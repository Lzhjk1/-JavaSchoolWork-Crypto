package DataPack;

import java.io.Serializable;

public class DataPack implements Serializable {
    public byte[] fileBytes; // 文件的所有字节
    public int fileSize; // 文件字节数
    public String fileName;
    public void calSize(){
        fileSize=fileBytes.length;
    }
}