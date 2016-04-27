package com.zxhy.xjl.rna.controller.model;

/**
 * 管理员登陆时候的数据模型
 * @author   yangzaixiong
 * 
 */
public class AdminLogonModel {
	private String accountNumber;
	private String passwd;
	
	
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	
}
