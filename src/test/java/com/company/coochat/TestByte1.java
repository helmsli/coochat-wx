package com.company.coochat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;

public class TestByte1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ls = "081e1208080110d49ad8c7021208080210d19bd8c70212080803108293d8c7021208080410e799cbc7021208080510e799cbc7021204080710001204080810001208080910b29bd8c7021204080a10001208080b108f9ad8c7021208080d10e799cbc7021208080e10e799cbc7021208081010e799cbc7021204081110001204086510001204086610001204086710001204086810001204086910001204086b10001204086d10001204086f1000120408701000120408721000120908c9011097dbcbe405120908cb0110a9e6cae405120508cc011000120508cd011000120908e80710c7a1cbe405120908e90710fba2cbe405";
		String lsC = hexStr2Str(ls);
		System.out.println(lsC);
		System.out.println(byteToHex(lsC.getBytes()));
		ByteArrayInputStream bArray = new ByteArrayInputStream(lsC.getBytes());
		try {
			ObjectInputStream objStream = new ObjectInputStream(bArray);
			Object object = objStream.readObject();
			System.out.println(object.toString());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private final static char[] mChars = "0123456789ABCDEF".toCharArray();
	private final static String mHexStr = "0123456789ABCDEF"; 

	public static String hexStr2Str(String hexStr){  
    	try {
			hexStr = hexStr.toString().trim().replace(" ", "").toUpperCase();
			char[] hexs = hexStr.toCharArray();  
			byte[] bytes = new byte[hexStr.length() / 2];  
			int iTmp = 0x00;;  
 
			for (int i = 0; i < bytes.length; i++){  
				iTmp = mHexStr.indexOf(hexs[2 * i]) << 4;  
				iTmp |= mHexStr.indexOf(hexs[2 * i + 1]);  
			    bytes[i] = (byte) (iTmp & 0xFF);  
			}  
			return new String(bytes,"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    	return "";
    }

	
	public static String byteToHex(byte[] b){  
	    String ls = "";
		for(int i=0;i<b.length;i++)
	    {
	    	ls = ls + byteToHex(b[i]);
	    }
		return ls;
	}  	

public static String byteToHex(byte b){  
    String hex = Integer.toHexString(b & 0xFF);  
    if(hex.length() < 2){  
        hex = "0" + hex;  
    }  
    return hex;  
}  


}
