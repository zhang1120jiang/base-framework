package com.unisinsight.sprite.common.utils;//package com.unisinsight.ic.commons.utils;
//
//import java.io.ByteArrayOutputStream;
//import java.nio.ByteOrder;
//
//import org.apache.commons.lang3.ArrayUtils;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// * 数据转换工具类
// */
//@Slf4j
//public class BasicDataUtil {
//	private static char[] cs = new char[] {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
//
//	/**
//	 * 字节数组转16进制字符串
//	 * @param bs byte
//	 * @return String
//	 */
//	public static String toHexStr(byte...bs) {
//		if (bs == null || bs.length == 0) {
//			return null;
//		}
//		StringBuilder sb = new StringBuilder();
//		for (byte b : bs) {
//			sb.append(cs[b >> 4 & 0x0f]);
//			sb.append(cs[b & 0x0f]);
//		}
//		return sb.toString();
//	}
//
//	/**
//	 * 字节数组转ip地址，多个用逗号拼接
//	 * @param bs byte[]
//	 * @return String
//	 */
//	public static String bytes2IPStr(byte[] bs) {
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < bs.length; i++) {
//			if (i>0) {
//				if (i%4==0) {
//					sb.append(',');
//				} else {
//					sb.append('.');
//				}
//			}
//			sb.append(bs[i]& 0xFF);
//		}
//		return sb.toString();
//	}
//
//	/**
//	 * 十六进制字符串转字节数组
//	 * @param hexstr String
//	 * @return byte[]
//	 */
//	public static byte[] toBytes(String hexstr) {
//		byte[] b = new byte[hexstr.length() / 2];
//		int j = 0;
//		for (int i = 0; i < b.length; i++) {
//			char c0 = hexstr.charAt(j++);
//			char c1 = hexstr.charAt(j++);
//			b[i] = (byte) ((parse(c0) << 4) | parse(c1));
//		}
//		return b;
//	}
//
//	private static int parse(char c) {
//		if (c >= 'a')
//			return (c - 'a' + 10) & 0x0F;
//		if (c >= 'A')
//			return (c - 'A' + 10) & 0x0F;
//		return (c - '0') & 0x0F;
//	}
//
//
//	/**
//	 * int到byte[]
//	 *
//	 * @param i
//	 * @return
//	 */
//	public static byte[] toBytes(int number) {
//		  return toBytes(number,null);
//	}
//
//	public static byte[] toBytes(int number,ByteOrder order) {
//		 byte[] targets = new byte[4];
//		 if (ByteOrder.BIG_ENDIAN == order) {
//			 targets[3] = (byte) (number & 0xff);// 最低位
//			 targets[2] = (byte) ((number >> 8) & 0xff);// 次低位
//			 targets[1] = (byte) ((number >> 16) & 0xff);// 次高位
//			 targets[0] = (byte) (number >>> 24);// 最高位,无符号右移。
//		 } else {
//			 targets[0] = (byte) (number & 0xff);// 最低位
//			 targets[1] = (byte) ((number >> 8) & 0xff);// 次低位
//			 targets[2] = (byte) ((number >> 16) & 0xff);// 次高位
//			 targets[3] = (byte) (number >>> 24);// 最高位,无符号右移。
//		 }
//
//		  return targets;
//	}
//
//	public static byte[] toBytes(long number) {
//		  return toBytes(number,null);
//	}
//
//	public static byte[] toBytes(long number,ByteOrder order) {
//		 byte[] targets = new byte[8];
//
//		 if (ByteOrder.BIG_ENDIAN == order) {
//			 targets[7] = (byte) (number & 0xff);
//			 targets[6] = (byte) ((number >> 8) & 0xff);
//			 targets[5] = (byte) ((number >> 16) & 0xff);
//			 targets[4] = (byte) ((number >> 24) & 0xff);
//			 targets[3] = (byte) ((number >> 32) & 0xff);
//			 targets[2] = (byte) ((number >> 40) & 0xff);
//			 targets[1] = (byte) ((number >> 48) & 0xff);
//			 targets[0] = (byte) ((number >> 56) & 0xff);
//		 } else {
//			 targets[0] = (byte) (number & 0xff);
//			 targets[1] = (byte) ((number >> 8) & 0xff);
//			 targets[2] = (byte) ((number >> 16) & 0xff);
//			 targets[3] = (byte) ((number >> 24) & 0xff);
//			 targets[4] = (byte) ((number >> 32) & 0xff);
//			 targets[5] = (byte) ((number >> 40) & 0xff);
//			 targets[6] = (byte) ((number >> 48) & 0xff);
//			 targets[7] = (byte) ((number >> 56) & 0xff);
//		 }
//		  return targets;
//	}
//
//	public static int toInt(char c) {
//		return c-48;
//	}
//	public static int toInt(byte[] b) {
//		return toInt(b,null);
//	}
//	public static int toInt(byte[] b,ByteOrder order) {
//		if (ByteOrder.BIG_ENDIAN == order) {
//			return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
//					| (b[0] & 0xFF) << 24;
//		} else {
//			return b[0] & 0xFF | (b[1] & 0xFF) << 8 | (b[2] & 0xFF) << 16
//					| (b[3] & 0xFF) << 24;
//		}
//	}
//
//	public static long toLong(byte[] bs) {
//		return toLong(bs,null);
//	}
//	public static long toLong(byte[] bs,ByteOrder order) {
//		if (ByteOrder.BIG_ENDIAN==order) {
//			return ((((long) bs[7] & 0xff) << 56)
//		              | (((long) bs[6] & 0xff) << 48)
//		              | (((long) bs[5] & 0xff) << 40)
//		              | (((long) bs[4] & 0xff) << 32)
//		              | (((long) bs[3] & 0xff) << 24)
//		              | (((long) bs[2] & 0xff) << 16)
//		              | (((long) bs[1] & 0xff) << 8)
//		              | (((long) bs[0] & 0xff) << 0));
//		} else {
//			return ((((long) bs[ 0] & 0xff) << 56)
//		              | (((long) bs[ 1] & 0xff) << 48)
//		              | (((long) bs[ 2] & 0xff) << 40)
//		              | (((long) bs[ 3] & 0xff) << 32)
//		              | (((long) bs[ 4] & 0xff) << 24)
//		              | (((long) bs[ 5] & 0xff) << 16)
//		              | (((long) bs[ 6] & 0xff) << 8)
//		              | (((long) bs[ 7] & 0xff) << 0));
//		}
//	}
//
//
//	public static byte[] toBytes(short number) {
//		return toBytes(number, null);
//	}
//	public static byte[] toBytes(short number,ByteOrder order) {
//		byte[] bs = new byte[2];
//
//		if (ByteOrder.BIG_ENDIAN==order) {
//			bs[1] = (byte) (number& 0xff);
//			bs[0] = (byte) ((number >> 8) & 0xff);
//		}else {
//			bs[0] = (byte) (number& 0xff);
//			bs[1] = (byte) ((number >> 8) & 0xff);
//		}
//		return bs;
//	}
//
//	public static Short toShort(byte[] bs) {
//		return toShort(bs,null);
//	}
//	public static Short toShort(byte[] bs,ByteOrder order) {
//		if (ByteOrder.BIG_ENDIAN == order) {
//			return (short)(bs[1] & 0xFF | (bs[0] & 0xFF) << 8);
//		} else {
//			return (short) ((bs[0] & 0xFF) | ((bs[1] & 0xFF) << 8));
//		}
//	}
//
//
//	/**
//	 * 合并字节
//	 * @param bytes
//	 * @return
//	 */
//	public static byte[] mergeBytes(Object... bytes) {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		try {
//			for (Object obj : bytes) {
//				if (obj != null) {
//					if (obj instanceof byte[] || obj instanceof Byte[]) {
//						baos.write((byte[])obj);
//					} else if (obj instanceof Byte) {
//						baos.write((byte)obj);
//					} else if (obj instanceof Integer) {
//						baos.write((byte)((int)obj));
//					}
//				}
//			}
//			baos.flush();
//		} catch (Exception e) {
//			log.info(e.getMessage());
//		}
//		return baos.toByteArray();
//	}
//
//	/**
//	 * 截取数据
//	 * @param b 数据
//	 * @param start 开始下标
//	 * @param length 截取数据的长度
//	 * @return
//	 */
//	public static byte[] subByts(byte[] b, int start, int length) {
//		byte[] rbytes = new byte[length];
//		System.arraycopy(b, start, rbytes, 0, length);
//		return rbytes;
//	}
//
//	/**
//	 * 字节转二进制字符串
//	 * @param b byte
//	 * @return String
//	 */
//	public static String toBit(byte b) {
//        return ""
//                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
//                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
//                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
//                + (byte) ((b >> 1) & 0x1) + (byte) (b & 0x1);
//    }
//
//	/**
//	 * ip转字节数组
//	 * @param ips String
//	 * @return byte[]
//	 */
//	public static byte[] ipToBytes(String... ips) {
//		if (ArrayUtils.isEmpty(ips)) {
//			return new byte[0];
//		}
//		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
//			for (String ip : ips) {
//				if (ip.matches("^(\\d+.){3}\\d+$")) {
//					byte[] bs = new byte[4];
//					String[] ipArr = ip.split("\\.");
//					bs[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
//					bs[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
//					bs[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
//					bs[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);
//					bos.write(bs);
//				}
//			}
//			bos.flush();
//			return bos.toByteArray();
//		} catch (Exception e) {
//			log.info(e.getMessage());
//		}
//		return new byte[0];
//	}
//}
