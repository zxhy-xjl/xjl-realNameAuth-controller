package com.zxhy.xjl.rna.controller;
/**
 * 实名认证配置参数管理
 * @author leasonlive
 *
 */
public class RealNameAuthConfig {
	//是不是使用模拟短信
	private boolean useSMSSendVerifyCode;
	//如果不适用短信发送验证码，默认验证码
	private String defaultVerifyCode;
	//使用人口库身份证认证
	private boolean usePeopleIDCheck;
	//使用人脸认证
	private boolean useFaceCheck;
	
	
	public boolean isUseSMSSendVerifyCode() {
		return useSMSSendVerifyCode;
	}
	public void setUseSMSSendVerifyCode(boolean useSMSSendVerifyCode) {
		this.useSMSSendVerifyCode = useSMSSendVerifyCode;
	}
	
	public String getDefaultVerifyCode() {
		return defaultVerifyCode;
	}
	public void setDefaultVerifyCode(String defaultVerifyCode) {
		this.defaultVerifyCode = defaultVerifyCode;
	}
	public boolean isUsePeopleIDCheck() {
		return usePeopleIDCheck;
	}
	public void setUsePeopleIDCheck(boolean usePeopleIDCheck) {
		this.usePeopleIDCheck = usePeopleIDCheck;
	}
	public boolean isUseFaceCheck() {
		return useFaceCheck;
	}
	public void setUseFaceCheck(boolean useFaceCheck) {
		this.useFaceCheck = useFaceCheck;
	}
	
}
