# -JavaSchoolWork-Crypto
My Java school work

这是我的Java课程设计作业

一个文件加解密程序

异或算法

动画效果最少持续4s

加密算法：
    密钥字符串的hashCode作为Random类的种子
    对文件的每一个字节，取五次随机值对其进行异或运算
    利用Random的伪随机数特性和异或运算的可逆性
    缺陷是
    简单的异或运算以及字符串生成哈希值可能的重复问题
