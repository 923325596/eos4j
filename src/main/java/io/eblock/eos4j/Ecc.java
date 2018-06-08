package io.eblock.eos4j;

import io.eblock.eos4j.api.vo.transaction.push.TxSign;
import io.eblock.eos4j.ecc.EccTool;
import io.eblock.eos4j.ese.Ese;

/**
 * Ecc,�û����ɹ�˽Կ��ǩ�����������л�
 * 
 * @author espritblock http://eblock.io
 *
 */
public class Ecc {

	/**
	 * ͨ����������˽Կ
	 * 
	 * @param seed
	 *            ����
	 * @return
	 */
	public static String seedPrivate(String seed) {
		return EccTool.seedPrivate(seed);
	}

	/**
	 * ͨ��˽Կ���ɹ�Կ
	 * 
	 * @param privateKey
	 *            ˽Կ
	 * @return
	 */
	public static String privateToPublic(String privateKey) {
		return EccTool.privateToPublic(privateKey);
	}

	/**
	 * ��ͨ����ǩ��
	 * 
	 * @param privateKey
	 *            ˽Կ
	 * @param data
	 *            ��Ҫǩ��������
	 * @return
	 */
	public static String sign(String privateKey, String data) {
		return EccTool.sign(privateKey, data);
	}

	/**
	 * ����ǩ��
	 * 
	 * @param privateKey
	 *            ˽Կ
	 * @param data
	 *            ��Ҫǩ���Ķ���
	 * @return
	 */
	public static String signTransaction(String privateKey, TxSign sign) {
		return EccTool.signTransaction(privateKey, sign);
	}

	/**
	 * ת���������л�
	 * 
	 * @param from
	 *            ��
	 * @param to
	 *            ��
	 * @param quantity
	 *            ת�˽��ͱ���
	 * @param memo
	 *            ��ע����
	 * @return
	 */
	public static String parseTransferData(String from, String to, String quantity, String memo) {
		return Ese.parseTransferData(from, to, quantity, memo);
	}

	/**
	 * �����˻��������л�
	 * 
	 * @param creator
	 *            ������
	 * @param name
	 *            �˻���
	 * @param onwe
	 *            onwer��Կ
	 * @param active
	 *            active��Կ
	 * @return
	 */
	public static String parseAccountData(String creator, String name, String onwer, String active) {
		return Ese.parseAccountData(creator, name, onwer, active);
	}

	/**
	 * ����ram�������л�
	 * 
	 * @param payer
	 *            �����˻�
	 * @param receiver
	 *            �����˻�
	 * @param bytes
	 *            �����ֽ�����
	 * @return
	 */
	public static String parseBuyRamData(String payer, String receiver, Long bytes) {
		return Ese.parseBuyRamData(payer, receiver, bytes);
	}

	/**
	 * ��Ѻ�������л�
	 * 
	 * @param from
	 *            ��Ѻ�˻�
	 * @param receiver
	 *            �����˻�
	 * @param stakeNetQuantity
	 *            �����Ѻ�����ͱ���
	 * @param stakeCpuQuantity
	 *            CPU��Ѻ�����ͱ���
	 * @param transfer
	 *            �Ƿ񽲵�Ѻ�ʲ�ת�͸��Է���0�Լ����У�1�Է�����
	 * @return
	 */
	public static String parseBuyRamData(String from, String receiver, String stakeNetQuantity, String stakeCpuQuantity,
			int transfer) {
		return Ese.parseDelegateData(from, receiver, stakeNetQuantity, stakeCpuQuantity, transfer);
	}
}
