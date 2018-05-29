package io.eblock.eos4j.ese;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.eblock.eos4j.utils.Base58;
import io.eblock.eos4j.utils.ByteUtils;
import io.eblock.eos4j.utils.EException;
import io.eblock.eos4j.utils.Ripemd160;

/**
 * DataParam
 * 
 * @author espritblock http://eblock.io
 *
 */
public class DataParam {

	public DataParam(String value, DataType type, Action action) {
		this.value = value;
		this.type = type;
		if (type == DataType.asset) {
			if (action == action.transfer) {
				String vs[] = value.split(" ");
				if (vs.length < 2) {
					throw new EException("error", "quantity error");
				}
				this.value = vs[0] + " " + action.getCode().replace("${quantity}", vs[1]);
			} else {
				this.value = value;
			}
		}
	}

	private String value;

	private DataType type;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public byte[] seria() {
		if (this.type == DataType.name) {
			return nameSeria();
		} else if (this.type == DataType.asset) {
			return assetSeria();
		} else if (this.type == DataType.unit32) {
			return unit32Seria();
		}else if (this.type == DataType.unit16) {
			return unit16Seria();
		}else if(this.type == DataType.key) {
			return keySeria();
		}else if(this.type == DataType.varint32) {
			return varint32();	
		} else {
			return stringSeria();
		}
	}

	/**
	 * assetSeria
	 * 
	 * @return
	 */
	private byte[] assetSeria() {
		String _value[] = this.value.split(" ");
		String amount = _value[0];
		String sym = _value[1];
		String precision = sym.split(",")[0];
		String symbol = sym.split(",")[1].split("@")[0];
		String[] part = amount.split("[.]");

		int pad = Integer.parseInt(precision);
		StringBuffer bf = new StringBuffer(part[0] + ".");
		if (part.length > 1) {
			pad = Integer.parseInt(precision) - part[1].length();
			bf.append(part[1]);
		}
		// ���Ȳ�0
		for (int i = 0; i < pad; i++) {
			bf.append("0");
		}
		String asset = precision + "," + symbol;
		// amount
		amount = bf.toString().replace(".", "");
		ByteBuffer ammount = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN)
				.putLong(Long.parseLong(amount));

		// asset
		StringBuffer padStr = new StringBuffer();
		for (int i = 0; i < (7 - symbol.length()); i++) {
			padStr.append("\0");
		}
		char c = (char) Integer.parseInt(precision);
		asset = c + symbol + padStr;
		ByteBuffer ba = ByteBuffer.wrap(asset.getBytes());
		return ByteUtils.concat(ammount.array(), ba.array());
	}

	/**
	 * stringSeria
	 * 
	 * @return
	 */
	private byte[] stringSeria() {
		long value = charCount();
		byte[] a = new byte[] {};
		value >>>= 0;
		while (value >= 0x80) {
			long b = (value & 0x7f) | 0x80;
			a = ByteUtils.concat(a, new byte[] { (byte) b });
			value >>>= 7;
		}
		a = ByteUtils.concat(a, new byte[] { (byte) value });
		for (char c : this.value.toCharArray()) {
			a = ByteUtils.concat(a, decodeChar(c));
		}
		return a;
	}

	/**
	 * charCount
	 * 
	 * @return
	 */
	private long charCount() {
		long c = 0;
		for (char cp : value.toCharArray()) {
			if (cp < 0x80) {
				c += 1;
			} else if (cp < 0x800) {
				c += 2;
			} else if (cp < 0x10000) {
				c += 3;
			} else {
				c += 4;
			}
		}
		return c;
	}

	/**
	 * decodeChar
	 * 
	 * @param ca
	 * @return
	 */
	private byte[] decodeChar(char ca) {
		long cp = (long) ca;
		if (cp < 0x80) {
			long a = cp & 0x7F;
			return new byte[] { (byte) a };
		} else if (cp < 0x800) {
			long a = ((cp >> 6) & 0x1F) | 0xC0;
			long b = (cp & 0x3F) | 0x80;
			return new byte[] { (byte) a, (byte) b };
		} else if (cp < 0x10000) {
			long a = ((cp >> 12) & 0x0F) | 0xE0;
			long b = ((cp >> 6) & 0x3F) | 0x80;
			long c = (cp & 0x3F) | 0x80;
			return new byte[] { (byte) a, (byte) b, (byte) c };
		} else {
			long a = ((cp >> 18) & 0x07) | 0xF0;
			long b = ((cp >> 12) & 0x3F) | 0x80;
			long c = ((cp >> 6) & 0x3F) | 0x80;
			long d = (cp & 0x3F) | 0x80;
			return new byte[] { (byte) a, (byte) b, (byte) c, (byte) d };
		}
	}

	/**
	 * nameSeria
	 * 
	 * @return
	 */
	private byte[] nameSeria() {
		StringBuffer bitstr = new StringBuffer();
		for (int i = 0; i <= 12; i++) {
			int c = i < value.length() ? ByteUtils.charidx(value.charAt(i)) : 0;
			int bitlen = i < 12 ? 5 : 4;
			String bits = Integer.toBinaryString(c);
			if (bits.length() > bitlen) {
				throw new EException("", "Invalid name " + value);
			}
			StringBuffer sb = new StringBuffer("");
			for (int j = 0; j < bitlen - bits.length(); j++) {
				sb.append("0");
			}
			bits = sb + bits;
			bitstr.append(bits);
		}
		BigInteger lv = new BigInteger(bitstr.toString(), 2);
		StringBuffer leHex = new StringBuffer();
		int bytes[] = ByteUtils.LongToBytes(lv.longValue());
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			String n = Integer.toHexString(b);
			leHex.append(n.length() == 1 ? "0" : "").append(n);
		}
		BigInteger ulName = new BigInteger(leHex.toString(), 16);
		return ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(ulName.longValue()).array();
	}

	/**
	 * lengthSeria
	 * 
	 * @return
	 */
	private byte[] lengthSeria() {
		long value = Long.parseLong(this.value);
		byte[] a = new byte[] {};
		value >>>= 0;
		while (value >= 0x80) {
			long b = (value & 0x7f) | 0x80;
			a = ByteUtils.concat(a, new byte[] { (byte) b });
			value >>>= 7;
		}
		return ByteUtils.concat(a, new byte[] { (byte) value });
	}

	private byte[] unit32Seria() {
		return ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(Integer.parseInt(this.value))
				.array();
	}
	
	private byte[] unit16Seria() {
		long value = Long.parseLong(this.value);
		return new byte[] {
			(byte)(value & 0x00FF),
			(byte) ((value & 0xFF00) >>> 8)
		};
	}
	
	
	private byte[] varint32() {
		long value = Long.parseLong(this.value);
		byte[] a = new byte[] {};
		value >>>= 0;
		while (value >= 0x80) {
			long b = (value & 0x7f) | 0x80;
			a = ByteUtils.concat(a, new byte[] { (byte) b });
			value >>>= 7;
		}
		a = ByteUtils.concat(a, new byte[] { (byte) value });
		return a;
	}

	/**
	 * keySeria
	 * 
	 * @return
	 */
	private byte[] keySeria() {

		this.value = this.value.replace("EOS", "");

		byte[] b = Base58.decode(this.value);
		
		b = ByteBuffer.allocate(b.length).order(ByteOrder.BIG_ENDIAN).put(b).array();

		byte[] checksum = ByteUtils.copy(b, b.length - 4, 4);

		byte[] key = ByteUtils.copy(b, 0, b.length - 4);

		byte[] rp = ByteUtils.copy(Ripemd160.from(key).bytes(), 0, 4);

		return key;
	}
}
