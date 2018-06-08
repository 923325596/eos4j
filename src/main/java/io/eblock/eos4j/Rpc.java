package io.eblock.eos4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.eblock.eos4j.api.service.RpcService;
import io.eblock.eos4j.api.utils.Generator;
import io.eblock.eos4j.api.vo.Block;
import io.eblock.eos4j.api.vo.ChainInfo;
import io.eblock.eos4j.api.vo.account.Account;
import io.eblock.eos4j.api.vo.transaction.Transaction;
import io.eblock.eos4j.api.vo.transaction.push.Tx;
import io.eblock.eos4j.api.vo.transaction.push.TxAction;
import io.eblock.eos4j.api.vo.transaction.push.TxRequest;
import io.eblock.eos4j.api.vo.transaction.push.TxSign;
import io.eblock.eos4j.ese.Action;
import io.eblock.eos4j.ese.DataParam;
import io.eblock.eos4j.ese.DataType;
import io.eblock.eos4j.ese.Ese;

public class Rpc {

	private final RpcService rpcService;

	SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public Rpc(String baseUrl) {
		dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		rpcService = Generator.createService(RpcService.class, baseUrl);
	}

	/**
	 * �������Ϣ
	 * 
	 * @return
	 */
	public ChainInfo getChainInfo() {
		return Generator.executeSync(rpcService.getChainInfo());
	}

	/**
	 * ���������Ϣ
	 * 
	 * @param blockNumberOrId
	 *            ����ID���߸߶�
	 * @return
	 */
	public Block getBlock(String blockNumberOrId) {
		return Generator.executeSync(rpcService.getBlock(Collections.singletonMap("block_num_or_id", blockNumberOrId)));
	}

	/**
	 * ����˻���Ϣ
	 * 
	 * @param account
	 *            �˻�����
	 * @return
	 */
	public Account getAccount(String account) {
		return Generator.executeSync(rpcService.getAccount(Collections.singletonMap("account_name", account)));
	}

	/**
	 * ��������
	 * 
	 * @param compression
	 *            ѹ��
	 * @param pushTransaction
	 *            ����
	 * @param signatures
	 *            ǩ��
	 * @return
	 * @throws Exception
	 */
	public Transaction pushTransaction(String compression, Tx pushTransaction, String[] signatures) throws Exception {
		// ObjectMapper mapper = new ObjectMapper();
		// String mapJakcson = mapper.writeValueAsString(new TxRequest(compression,
		// pushTransaction, signatures));
		// System.out.println(mapJakcson);
		return Generator
				.executeSync(rpcService.pushTransaction(new TxRequest(compression, pushTransaction, signatures)));
	}

	/**
	 * ת��
	 * 
	 * @param pk
	 *            ˽Կ
	 * @param contractAccount
	 *            ��Լ�˻�
	 * @param from
	 *            ��
	 * @param to
	 *            ��
	 * @param quantity
	 *            ���ֽ��
	 * @param memo
	 *            ����
	 * @return
	 * @throws Exception
	 */
	public Transaction transfer(String pk, String contractAccount, String from, String to, String quantity, String memo)
			throws Exception {
		// get chain info
		ChainInfo info = getChainInfo();
		// get block info
		Block block = getBlock(info.getLastIrreversibleBlockNum().toString());
		// tx
		Tx tx = new Tx();
		tx.setExpiration(info.getHeadBlockTime().getTime() / 1000 + 60);
		tx.setRef_block_num(info.getLastIrreversibleBlockNum());
		tx.setRef_block_prefix(block.getRefBlockPrefix());
		tx.setNet_usage_words(0l);
		tx.setMax_cpu_usage_ms(0l);
		tx.setDelay_sec(0l);
		// actions
		List<TxAction> actions = new ArrayList<>();
		// data
		Map<String, Object> dataMap = new LinkedHashMap<>();
		dataMap.put("from", from);
		dataMap.put("to", to);
		dataMap.put("quantity", new DataParam(quantity, DataType.asset, Action.transfer).getValue());
		dataMap.put("memo", memo);
		// action
		TxAction action = new TxAction(from, contractAccount, "transfer", dataMap);
		actions.add(action);
		tx.setActions(actions);
		// sgin
		String sign = Ecc.signTransaction(pk, new TxSign(info.getChainId(), tx));
		// data parse
		String data = Ecc.parseTransferData(from, to, quantity, memo);
		// reset data
		action.setData(data);
		// reset expiration
		tx.setExpiration(dateFormatter.format(new Date(1000 * Long.parseLong(tx.getExpiration().toString()))));
		return pushTransaction("none", tx, new String[] { sign });
	}

	/**
	 * �����˻�
	 * 
	 * @param pk
	 *            ˽Կ
	 * @param creator
	 *            ������
	 * @param newAccount
	 *            ���˻�
	 * @param owner
	 *            ��Կ
	 * @param active
	 *            ��Կ
	 * @param buyRam
	 *            ����ռ�����
	 * @param stakeNetQuantity
	 *            �����Ѻ
	 * @param stakeCpuQuantity
	 *            cpu��Ѻ
	 * @param transfer
	 *            ��Ѻ�ʲ��Ƿ�ת�͸��Է���0�Լ����У�1�Է�����
	 * @return
	 * @throws Exception
	 */
	public Transaction createAccount(String pk, String creator, String newAccount, String owner, String active,
			Long buyRam, String stakeNetQuantity, String stakeCpuQuantity, Long transfer) throws Exception {
		// get chain info
		ChainInfo info = getChainInfo();
		// info.setChainId("cf057bbfb72640471fd910bcb67639c22df9f92470936cddc1ade0e2f2e7dc4f");
		// info.setLastIrreversibleBlockNum(22117l);
		// get block info
		Block block = getBlock(info.getLastIrreversibleBlockNum().toString());
		// block.setRefBlockPrefix(3920078619l);
		// tx
		Tx tx = new Tx();
		tx.setExpiration(info.getHeadBlockTime().getTime() / 1000 + 60);
		// tx.setExpiration(1528436078);
		tx.setRef_block_num(info.getLastIrreversibleBlockNum());
		tx.setRef_block_prefix(block.getRefBlockPrefix());
		tx.setNet_usage_words(0l);
		tx.setMax_cpu_usage_ms(0l);
		tx.setDelay_sec(0l);
		// actions
		List<TxAction> actions = new ArrayList<>();
		tx.setActions(actions);
		// create
		Map<String, Object> createMap = new LinkedHashMap<>();
		createMap.put("creator", creator);
		createMap.put("name", newAccount);
		createMap.put("owner", owner);
		createMap.put("active", active);
		TxAction createAction = new TxAction(creator, creator, "newaccount", createMap);
		actions.add(createAction);
		// buyrap
		Map<String, Object> buyMap = new LinkedHashMap<>();
		buyMap.put("payer", creator);
		buyMap.put("receiver", newAccount);
		buyMap.put("bytes", buyRam);
		TxAction buyAction = new TxAction(creator, creator, "buyrambytes", buyMap);
		actions.add(buyAction);
		// buyrap
		Map<String, Object> delMap = new LinkedHashMap<>();
		delMap.put("from", creator);
		delMap.put("receiver", newAccount);
		delMap.put("stake_net_quantity", new DataParam(stakeNetQuantity, DataType.asset, Action.delegate).getValue());
		delMap.put("stake_cpu_quantity", new DataParam(stakeCpuQuantity, DataType.asset, Action.delegate).getValue());
		delMap.put("transfer", transfer);
		TxAction delAction = new TxAction(creator, creator, "delegatebw", delMap);
		actions.add(delAction);
		// // sgin
		String sign = Ecc.signTransaction(pk, new TxSign(info.getChainId(), tx));
		// data parse
		String accountData = Ese.parseAccountData(creator, newAccount, owner, active);
		createAction.setData(accountData);
		// data parse
		String ramData = Ese.parseBuyRamData(creator, newAccount, buyRam);
		buyAction.setData(ramData);
		// data parse
		String delData = Ese.parseDelegateData(creator, newAccount, stakeNetQuantity, stakeCpuQuantity,
				transfer.intValue());
		delAction.setData(delData);
		// reset expiration
		tx.setExpiration(dateFormatter.format(new Date(1000 * Long.parseLong(tx.getExpiration().toString()))));
		return pushTransaction("none", tx, new String[] { sign });
	}
}
