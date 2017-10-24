package com.one.key.diagnosis.utils;

import android.text.TextUtils;

import com.one.key.diagnosis.BT.BTSocket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.one.key.diagnosis.BT.BarCodeConvert.byteToHexString;
import static java.lang.Integer.parseInt;

/**
 * Created by 1611281 on 2017/10/18.
 */

public class SendByteUtil {
    private static final String TAG = "SendByteUtil";

    public static final int BAUD_RATE_600 = 0x02;            //波特率600
    public static final int BAUD_RATE_2400 = 0x04;            //波特率2400
    public static final int BAUD_RATE_1200 = 0x03;
    private int sql = 70;

    private static final String[] TSLstrs = new String[]{"无需设置或使用默认的", "600", "1200", "2400", "4800", "7200", "9600", "19200"};

    private static SendByteUtil sendByteUtil = null;

    private static BTSocket btSocket;

    public static SendByteUtil getInstance() {
        if (sendByteUtil == null) {
            synchronized (SendByteUtil.class) {
                if (sendByteUtil == null) {
                    sendByteUtil = new SendByteUtil();
                    btSocket = BTSocket.getInstance();
                }
            }
        }
        return sendByteUtil;
    }

    /**
     * 拼接报文
     *
     * @param C    控制域
     * @param A    地址域
     * @param AFN  应用层功能码
     * @param DADT 数据单元标识
     * @return
     */
    public String splitMessage(String C, String A, String AFN, String DADT, String DataUnit) {
        StringBuffer buffer2 = new StringBuffer();
        buffer2.append(C);
        buffer2.append(A);
        buffer2.append(AFN);
        buffer2.append(returnSEQ());
        buffer2.append(DADT);
        buffer2.append(DataUnit);
        StringBuffer buffer = new StringBuffer();
        buffer.append("68");
        buffer.append(calculationLength(buffer2.toString()));
        buffer.append("00");
        buffer.append(calculationLength(buffer2.toString()));
        buffer.append("00");
        buffer.append("68");
        buffer.append(buffer2.toString());
        buffer.append(cmdTocs(buffer.toString()));
        buffer.append("16");
        return buffer.toString();
    }

    public String returnSEQ() {
        return String.valueOf(sql++);
    }

    /**
     * 根据报文计算长度
     *
     * @param cmd
     * @return
     */
    public String calculationLength(String cmd) {
        int lengthTnt = cmd.length() / 2;
        String len = Long.toBinaryString(lengthTnt);//十进制长度转二进制
        String s = len + "10";
        String strlen = Long.toHexString(Long.parseLong(s, 2));//二进制转十六进制
        return strlen;
    }

    /**
     * 请求数据
     *
     * @param cmd2
     */
    public boolean sendByte(String cmd2) {
        String cmd = "68 32 00 32 00 68 1B 00 00 00 00 00 00 60 00 00 01 00 7C 16";
        //等待校验成功
        if (send(cmd))
            //发出指令
            return send(cmd2);
        return false;
    }

    /**
     * 发送报文
     *
     * @param cmd
     */
    public boolean send(String cmd) {
        byte[] data = hexStringToByte(cmd.replaceAll(" ", ""));
        byte[] temp = bufferTransition(data,
                BAUD_RATE_1200);
        String str = bytesToHexString(data, " ");
        LogUtil.e(TAG, "发送报文:" + str);
        return btSocket.write(temp);
    }

    /**
     * 计算校验和
     *
     * @param data
     * @param start
     * @param len
     * @return
     */
    private int CalcCS(byte[] data, int start, int len) {
        int cs = 0;
        for (int i = start; i < start + len; i++) {
            cs += data[i];
        }
        cs = (cs % 256);
        return cs;
    }

    /**
     * 将报文转成校验和 发送
     *
     * @param cmd
     * @return
     */
    public String cmdTocs(String cmd) {
        byte[] data = hexStringToByte(cmd.replaceAll(" ", ""));
        int cs = CalcCS(data, 6, data.length - 6);
        String temp = Integer.toHexString(cs);
        if (temp.length() < 2) {
            temp = "0" + temp;
        }
        return temp;
    }

    /**
     * 将String端口号转成十六进制报文端口号 发送
     *
     * @return
     */
    public String getPort(String dk) {
        String port;
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(dk);
        if (!isNum.matches()) {
            return "";
        }
        String temp = Long.toBinaryString(parseInt(dk));
        port = Integer.toHexString(parseInt(temp, 2));
        if (TextUtils.isEmpty(port) || "0".equals(port))
            return "0000";
        port = port.substring(2) + port.substring(0, 2);
        return port;
    }

    /**
     * 拼接IP 将String类型IP转成十六进制报文IP进行拼接 发送
     *
     * @param ip
     * @return
     */
    public String SplicedIp(String ip) {
        String cmd = "";
        if (!TextUtils.isEmpty(ip)) {
            String[] cmds = ip.split("\\.");
            for (int i = 0; i < cmds.length; i++) {
                int temp = Integer.valueOf(cmds[i]);
                String str = Integer.toHexString(temp);
                if (str.length() < 2) {
                    str = "0" + str;
                }
                cmd = cmd + str;
            }
            return cmd;
        } else
            return null;
    }

    /**
     * 将Byte转成十六进制
     */
    private String genFrameProp2(int recieveData) {
        StringBuffer result = new StringBuffer();
        String temp = Integer.toHexString(recieveData & 255);
        if (temp.length() == 1) {
            temp = "0" + temp;
        }
        result.append(temp);
        return result.toString();
    }

    /**
     * 十六进制转成十进制
     *
     * @param recieveData
     * @return
     */
    public String tenTosix(int recieveData) {
        String result = genFrameProp2(recieveData);
        String str = String.valueOf(parseInt(result, 16));
        return str;
    }

    /**
     * 将byte数组转成IP
     *
     * @param bytes
     * @return
     */
    public String bytesToIp(byte[] bytes) {
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            if (i == bytes.length - 1) {
                hex.append(tenTosix(bytes[i]));
            } else {
                hex.append(tenTosix(bytes[i]) + ".");
            }
        }
        return hex.toString();
    }


    /**
     * 将bytes数组转成端口
     *
     * @param bytes
     * @return
     */
    public String bytestoTen(byte[] bytes) {
        long ii = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            ii += bytes[i] * Math.pow(256, i);
        }
        return String.valueOf(ii);
    }

    /**
     * 将字符串ascii码转成十六进制
     *
     * @param str
     * @return
     */
    public String convertStringToHex(String str) {

        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }

        return hex.toString();
    }

    /**
     * 将十六进制转成ascii码
     *
     * @param hex
     * @return
     */
    public String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < hex.length() - 1; i += 2) {
            // grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            // convert hex to decimal
            int decimal = parseInt(output, 16);
            // convert the decimal to character
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

    /**
     * 将byte数组转成apn
     *
     * @param apn
     * @return
     */
    public String bytetoAPn(byte[] apn) {
        StringBuilder spnstr = new StringBuilder();
        for (int i = 0; i < apn.length; i++) {
            if (0 == apn[i]) {
                break;
            }
            String temp = genFrameProp2(apn[i]);
            spnstr.append(convertHexToString(temp));
        }
        return spnstr.toString();
    }

    /**
     * 将字符串转成十六进制不足16位补齐
     *
     * @param str
     * @return
     */
    public String apntoSixteen(String str) {
        StringBuilder spnstr = new StringBuilder();
        spnstr.append(convertStringToHex(str));
        while (spnstr.length() < 32) {
            spnstr.append("00");
        }
        return spnstr.toString();
    }

    /**
     * 将byte转成日期
     * 数据格式A1
     *
     * @param bytes
     * @return
     */
    public String byteToStringDate(byte[] bytes) {
        String str = "20";
        String str2 = "";
        for (int i = bytes.length; i > 0; i--) {
            if (i == 5) {
                int a = bytes[i - 1];
                str2 = String.valueOf((a & 16) >> 4) + String.valueOf(a & 15);
            } else
                str2 = String.valueOf(BCDTo16byte(bytes[i - 1]));
            if (str2 != null && str2.length() > 0) {
                if (str2.length() == 1) {
                    str2 = "0" + str2;
                }
                if (i != 1) {
                    if (i > 4) {
                        str2 += "-";
                    } else if (i == 4) {
                        str2 += " ";
                    } else
                        str2 += ":";
                }
            }
            str = str + str2;
        }
        return str;
    }

    /**
     * 将byte转成日期
     * 数据格式A1
     *
     * @param bytes
     * @return
     */
    public String byteToStringDate3(byte[] bytes) {
        String str = "20";
        String str2 = "";
        for (int i = bytes.length; i > 0; i--) {
            if (i == 5) {
                int a = bytes[i - 1];
                str2 = String.valueOf((a & 16) >> 4) + String.valueOf(a & 15);
            } else
                str2 = String.valueOf(BCDTo16byte(bytes[i - 1]));
            if (str2 != null && str2.length() > 0) {
                if (str2.length() == 1) {
                    str2 = "0" + str2;
                }
                if (i != 1) {
                    if (i > 3) {
                        str2 += "-";
                    } else if (i == 3) {
                        str2 += " ";
                    } else
                        str2 += ":";
                }
            }
            str = str + str2;
        }
        return str;
    }

    /**
     * 将byte转成日期
     * 数据格式A15
     *
     * @param bytes
     * @return
     */
    public String byteToStringDate2(byte[] bytes) {
        String str = "20";
        String str2 = "";
        for (int i = bytes.length; i > 0; i--) {
            if (i == 5) {
                int a = bytes[i - 1];
                str2 = String.valueOf((a & 16) >> 4) + String.valueOf(a & 15);
            } else
                str2 = String.valueOf(BCDTo16byte(bytes[i - 1]));
            if (str2 != null && str2.length() > 0) {
                if (str2.length() == 1) {
                    str2 = "0" + str2;
                }
                if (i != 1) {
                    str2 += "-";
                }
            }
            str = str + str2;
        }
        return str;
    }

    /**
     * 数据格式A7,A22,A23
     *
     * @param bytes
     * @param sun   10代表有十分位，100代表有百分位,1000代表有千分位
     * @return
     */
    public String byteToStringDY(byte[] bytes, int sun) {
        String str = "";
        String str2 = "";
        for (int i = bytes.length; i > 0; i--) {
            str2 = String.valueOf(BCDTo16byte(bytes[i - 1]));
            if (i != bytes.length) {
                if (Integer.parseInt(str2) < 0) {
                    str2 = String.valueOf(Integer.parseInt(str2) / -1);
                }
            }
            if (str2 != null && str2.length() > 0) {
                if (str2.length() == 1) {
                    str2 = "0" + str2;
                }
            }
            str = str + str2;
        }
        if (Double.valueOf(str) < 0) {
            return "-";
        }
        return String.valueOf(Double.valueOf(str) / sun);
    }

    /**
     * 将byte转成通信地址
     *
     * @param bytes
     * @return
     */
    public String byteToStringAddress(byte[] bytes) {
        String str = "";
        String str2 = "";
        for (int i = bytes.length; i > 0; i--) {
            str2 = String.valueOf(BCDTo16byte(bytes[i - 1]));
            if (str2 != null && str2.length() > 0) {
                if (str2.length() == 1) {
                    str2 = "0" + str2;
                }
            }
            str = str + str2;
        }
        return str;
    }

    /**
     * BCD转成十六进制
     *
     * @param value
     * @return
     */
    public byte BCDTo16byte(byte value) {
        return (byte) (((value >> 4) % 10) * 10 + value % 16);
    }

    /**
     * 测量点信息长度
     *
     * @param count
     * @return
     */
    public String toLength(int count) {
        int length = count * 2 + 2;
        length = length + 12;
        return intTosexten(length);
    }

    /**
     * 根据int类型长度转成十六进制长度
     *
     * @param lengthTnt
     * @return
     */
    public String intTosexten(int lengthTnt) {
        String len = Long.toBinaryString(lengthTnt);//十进制长度转二进制
        String s = len + "10";
        String strlen = Long.toHexString(Long.parseLong(s, 2));//二进制转十六进制
        return strlen;
    }

    /**
     * 根据个数拼出测量点标识
     *
     * @param start,count
     * @return
     */
    public String toCld(int start, int count) {
        String str = Integer.toHexString(count);
        if (str.length() == 1) {
            str = "0" + str + " 00 ";
        }
        for (int i = start + 1; i <= start + count; i++) {
            String s = Integer.toHexString(i);
            if (s.length() == 1) {
                s = "0" + s + " ";
            }
            str = str + s + "00 ";
        }
        return str;
    }

    /**
     * 根据bin取出波特率和通信端口号
     *
     * @param ten
     * @return
     */
    public static String tenToBcdStr2(int ten) {
        String bcdStr = Integer.toBinaryString(ten);
        if (bcdStr != null && bcdStr.length() > 0) {
            while (bcdStr.length() < 8) {
                bcdStr = "0" + bcdStr;
            }
        }
        char[] chars = bcdStr.toCharArray();
        char[] chars1 = new char[3];
        char[] chars2 = new char[5];
        System.arraycopy(chars, 0, chars1, 0, 3);
        System.arraycopy(chars, 3, chars2, 0, 5);
        String a = TSLstrs[Integer.valueOf(String.valueOf(chars1), 2)];
        String b = Integer.valueOf(String.valueOf(chars2), 2).toString();
        return a + "," + b;
    }

    /**
     * byte转成测量点通信密码
     *
     * @param bytes
     * @return
     */
    public String tocldtxpass(byte[] bytes) {
        String pass = bytestoTen(bytes);
        while (pass.length() < 12) {
            pass = "0" + pass;
        }
        return pass;
    }

    /**
     * 计算数据域真实长度
     *
     * @param length1,length2
     * @return
     */
    public int byteTolength(byte length1, byte length2) {
        String len1 = Long.toBinaryString(length1);
        String len2 = Long.toBinaryString(length2);
        while (len1.length() < 8) {
            len1 = "0" + len1;
        }
        if (len1 != null && len1.length() > 0)
            len1 = len1.substring(0, len1.length() - 2);
        String strlen = len2 + len1;
        return Integer.valueOf(strlen, 2);
    }


    public static byte[] hexStringToByte(String hex) {
        hex = hex.toUpperCase();
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();

        for (int i = 0; i < len; ++i) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }

        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 将原始的数据格式转换成红外设备可识别的数据格式
     *
     * @param beforedata 原始数据
     * @param beforedata 波特率
     * @return
     */
    public static byte[] bufferTransition(byte[] beforedata, int baudRate) {
        byte[] data = null;
        try {
            if (beforedata == null || beforedata.length == 0) {
                return null;
            }
            data = new byte[beforedata.length + 6];
            data[0] = (byte) 0x86; // 数据开始标志
            data[1] = (byte) baudRate; // 波特率
            if (beforedata.length >= 128) {
                data[2] = 127;
                data[3] = (byte) (beforedata.length - 127);
            } else {
                data[2] = (byte) beforedata.length;
                data[3] = 0;
            }
            for (int i = 0; i < beforedata.length; i++) {
                data[i + 4] = beforedata[i];
            }
            byte cs = getCheckCode(data);
            data[data.length - 2] = cs;
            data[data.length - 1] = 0x61;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 获取校验码
     *
     * @param data 数据区域
     * @param data 帧类型
     * @return
     */
    public static byte getCheckCode(byte[] data) {
        int type = 0;
        for (int i = 0; i < data.length - 2; i++) { //
            type = type + data[i];
        }

        return (byte) (type & 0xff);
    }

    /**
     * 将字节数组转为十六进制字符串。
     *
     * @param value
     * @param separator 可选的分隔符。不能为null，可以使用空字符串。
     * @return
     */
    public static String bytesToHexString(byte[] value, String separator) {
        String strValue = "";

        if (value != null) {
            for (int intIndex = 0; intIndex < value.length; intIndex++) {
                strValue += separator + byteToHexString(value[intIndex]);
            }
            if (strValue.length() > 0 && separator != null && separator.length() > 0) {
                strValue = strValue.substring(separator.length());
            }
        }

        return strValue;
    }
}
