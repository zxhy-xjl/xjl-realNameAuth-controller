package com.zxhy.xjl.rna.controller.model;
/**
 * 刷脸数据模型
 * @author leasonlive
 *
 */
public class CheckFaceModel {
	private String phone;
	//身份证号码
	private String idCode;
	/**
	 * 拍照图片存储的url地址，这个参数根据实际情况进行修改
	 */
	private byte[] face;
	private String faceUrl;
	private String  base64Face;
	
	public String getIdCode() {
		return idCode;
	}
	public void setIdCode(String idCode) {
		this.idCode = idCode;
	}
	public byte[] getFace() {
		return face;
	}
	public void setFace(byte[] face) {
		this.face = face;
	}
	
	public String getFaceUrl() {
		return faceUrl;
	}
	public void setFaceUrl(String faceUrl) {
		this.faceUrl = faceUrl;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getBase64Face() {
		return base64Face;
	}
	public void setBase64Face(String base64Face) {
		this.base64Face = base64Face;
	}
}
