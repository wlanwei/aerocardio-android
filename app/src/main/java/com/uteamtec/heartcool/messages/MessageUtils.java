package com.uteamtec.heartcool.messages;

public class MessageUtils {

	public static byte[] demiIntToBytes(int val){
        byte data[] = new byte[3];
        for (int m = 0; m < 3; m ++) {
            data[m] = (byte) ((val>>>(8*m)) & 0x00ff);
        }
		return data;
	}

	public static int bytesToDemiInt(byte data[], int offset){
        int val = 0;
		int mask = 0xff;
		for (int m = offset; m < offset+3; m ++) {
			int num = data[m];
			num <<= (8*(m-offset));
			num &= mask;
            num <<= 8;
            num >>= 8;
			mask <<= 8;
			val += num;
		}
		return val;
	}

	public static byte[] shortToBytes (int val) {
		val <<= 16;
		val >>= 16;
		byte data[] = new byte[2];
		for (int m = 0; m < 2; m ++) {
			data[m] = (byte) ((val>>>(8*m)) & 0x00ff);
		}
		return data;
	}

	public static int bytesToShort(byte data[], int offset) {
		int val = 0;
		int mask = 0xff;
		int num;
		num = data[offset];
		num &= mask;
		mask <<= 8;
		val += num;

		num = data[offset+1];
		num <<= 24;
		num >>= 16;
		val += num;

		return val;
	}

	public static int bytesToUnsignedShort(byte data[], int offset) {
		int val = 0;
		int mask = 0xff;
		int num;
		num = data[offset];
		num &= mask;
		mask <<= 8;
		val += num;

		num = data[offset+1];
		num <<= 8;
		num &= mask;
		val += num;

		return val;
	}

	public static byte[] intToBytes(int val){
		byte data[] = new byte[4];
		for (int m = 0; m < 4; m ++) {
			data[m] = (byte) ((val>>>(8*m)) & 0x00ff);
		}
		return data;
	}

	public static int bytesToInt(byte[] data, int offset) {
		int val = 0;
		int mask = 0xff;
		for (int m = offset; m < offset+4; m ++) {
			int num = data[m];
			num <<= (8*(m-offset));
			num &= mask;
			mask <<= 8;
			val += num;
		}
		return val;
	}

	public static long bytesToLong(byte[] data, int offset) {

		long val = 0;
		long mask = 0xff;
		for (int m = offset; m < offset+8; m ++) {
			long num = data[m];
			num <<= (8*(m-offset));
			num &= mask;
			mask <<= 8;
			val += num;
		}
		return val;
	}

	public static byte[] longToBytes(long val) {
		byte data[] = new byte[8];
		for (int m = 0; m < 8; m ++) {
			data[m] = (byte) ((val>>>(8*m)) & 0x00ff);
		}
		return data;
	}
}
