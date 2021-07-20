package com.example.hearingtestlibrary;

public class SinWave {
    /** 正弦波的高度 **/
    public static int HEIGHT = 127; //8位的采样率，2的8次方就应该是256，所以正弦波的波峰就应该是127
//    public static final int HEIGHT = 16;
    /** 2PI **/
    public static final double TWOPI = 2 * 3.1415;

    /**
     * 生成正弦波
     * @param wave
     * @param waveLen 每段正弦波的长度
     * @param length 总长度
     * @return
     */
    public static byte[] sin(byte[] wave, int waveLen, int length) {
        for (int i = 0; i < length; i++) {
            wave[i] = (byte) (HEIGHT * (1 - Math.sin(TWOPI
                    * ((i % waveLen) * 1.00 / waveLen))));
        }
        return wave;
    }

    public static void updateDB(int f,int db){
        double temp =0.0;
        switch (f){
            case 250:
                temp = 25.5;
                break;
            case 500:
                temp = 11.5;
                break;
            case 1000:
                temp = 7.0;
                break;
            case 2000:
                temp = 9.0;
                break;
            case 4000:
                temp = 9.5;
                break;
            case 8000:
                temp =13.0;
                break;
            default:
                break;
        }
        HEIGHT = (int) Math.pow(10.0, (db - temp) / 20);

    }
}
