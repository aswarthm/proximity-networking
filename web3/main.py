
import os
from web3 import Web3
from web3.middleware import geth_poa_middleware
import json

def webb3(c_id, d_id, c_url, d_url):
	alchemy_url = "https://eth-sepolia.g.alchemy.com/v2/YwHbuBOHyHFSAmNKgi5kd_pPSdmjjznO"
	# alchemy_url = "https://evm.ngd.network:32332/"
	w3 = Web3(Web3.HTTPProvider(alchemy_url))

	private_key = '94db62679605b62adb6bf044e5ac3d03a5b6723eeb1391c5c30c3a514155d0c3'
	w3.eth.account.from_key(private_key)
	w3.middleware_onion.inject(geth_poa_middleware, layer=0)

	# w3.eth.default_account = acct.address

	contract_address = '0x79CDb3834A865e38df3aE1d2f7034D547cFeBf09'

	with open('./abi.json') as f:
		abi = json.load(f)
	# print(abi)

	wallet_address = '0xfB12Ad056716430BE0477802faD0933040fbA76C'

	print(f'Making a call to contract at address: { contract_address }')
	Incrementer = w3.eth.contract(address=contract_address, abi=abi)

	nonce = w3.eth.get_transaction_count(wallet_address)
	increment_tx = Incrementer.functions.addMatch(c_id, d_id, c_url, d_url).build_transaction({
		"from": wallet_address,
		"gas": 1000000,
		"gasPrice": w3.to_wei("0.01", "gwei"),
		"nonce": nonce
	})
	signed = w3.eth.account.sign_transaction(increment_tx, private_key=private_key)
	tx_hash = w3.eth.send_raw_transaction(signed.rawTransaction)
	receipt = w3.eth.wait_for_transaction_receipt(tx_hash)
	print(tx_hash, receipt)

webb3("1", "2", "https://www.google.com", "https://www.youtube.com")