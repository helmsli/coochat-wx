package com.company.cooChatWx.utils;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NumberUtil {
	// 小数位数精度
	private static int scale;

	@Value("${coojisu.demical.scale:1}")
	public void setScale(int scale) {
		NumberUtil.scale = scale;
	}

	/**
	 *  x+y	保留2位小数  四舍五入取整
	 * @param x
	 * @param y
	 * @return
	 */
	public static double add(double x, double y) {
		return add(x, y, scale);
	}

	/**
	 *   x+y	保留scale位小数  四舍五入取整
	 * @param x
	 * @param y
	 * @param scale
	 * @return
	 */
	public static double add(double x, double y, int scale) {
		BigDecimal b1 = new BigDecimal(Double.toString(x));
		BigDecimal b2 = new BigDecimal(Double.toString(y));
		return b1.add(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 *  x-y		保留2位小数  四舍五入取整
	 * @param x
	 * @param y
	 * @return
	 */
	public static double sub(double x, double y) {
		return sub(x, y, scale);
	}

	/**
	 *  x-y	保留scale位小数  四舍五入取整
	 * @param x
	 * @param y
	 * @param scale
	 * @return
	 */
	public static double sub(double x, double y, int scale) {
		BigDecimal b1 = new BigDecimal(Double.toString(x));
		BigDecimal b2 = new BigDecimal(Double.toString(y));
		return b1.subtract(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * x*y	保留2位小数  四舍五入取整
	 * @param x
	 * @param y
	 * @return
	 */
	public static double mul(double x, double y) {
		return mul(x, y, scale);
	}

	/**
	 * x*y	保留scale位小数  四舍五入取整
	 * @param x
	 * @param y
	 * @param scale
	 * @return
	 */
	public static double mul(double x, double y, int scale) {
		BigDecimal b1 = new BigDecimal(Double.toString(x));
		BigDecimal b2 = new BigDecimal(Double.toString(y));
		return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static long mulToLong(double x, double y, int scale) {
		BigDecimal b1 = new BigDecimal(Double.toString(x));
		BigDecimal b2 = new BigDecimal(Double.toString(y));
		return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).longValue();
	}

	/**
	 *  x除以y  保留2位小数  四舍五入取整
	 * @param x
	 * @param y
	 * @return
	 */
	public static double div(double x, double y) {
		return div(x, y, scale);
	}

	/**
	 *  x除以y  保留2位小数  向上取整
	 * @param x
	 * @param y
	 * @return
	 */
	public static double divCeiling(double x, double y) {
		return divCeiling(x, y, scale);
	}

	/**
	 *  x除以y 保留scale位小数  四舍五入取整
	 * @param x
	 * @param y
	 * @param scale
	 * @return
	 */
	public static double div(double x, double y, int scale) {
		BigDecimal b1 = new BigDecimal(Double.toString(x));
		BigDecimal b2 = new BigDecimal(Double.toString(y));
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 *  x除以y 保留scale位小数  向上取整
	 * @param x
	 * @param y
	 * @param scale
	 * @return
	 */
	public static double divCeiling(double x, double y, int scale) {
		BigDecimal b1 = new BigDecimal(Double.toString(x));
		BigDecimal b2 = new BigDecimal(Double.toString(y));
		return b1.divide(b2, scale, BigDecimal.ROUND_CEILING).doubleValue();
	}

	/**
	 * -1 x < y 
	 *  0 x = y
	 *  1 x > y
	 * @param x
	 * @param y
	 * @return
	 */
	public static int compare(double x, double y) {
		BigDecimal b1 = new BigDecimal(Double.toString(x));
		BigDecimal b2 = new BigDecimal(Double.toString(y));
		return b1.compareTo(b2);
	}

	public static void main(String[] args) {
		BigDecimal b1 = new BigDecimal("10");
		BigDecimal b2 = new BigDecimal("3");
		double x = b1.divide(b2, 2, BigDecimal.ROUND_CEILING).doubleValue();
		System.err.println(x);
		System.err.println(b2);
	}

}
