package com.zxhy.xjl.rna.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.zxhy.xjl.face.FaceService;
import com.zxhy.xjl.notification.sms.SMS;
import com.zxhy.xjl.notification.verifyCode.VerifyCode;
import com.zxhy.xjl.people.PeopleService;
import com.zxhy.xjl.rna.business.RealNameAuthBusiness;
import com.zxhy.xjl.rna.business.RealNameAuthTask;
import com.zxhy.xjl.rna.controller.model.AdminLogonModel;
import com.zxhy.xjl.rna.controller.model.CheckFaceModel;
import com.zxhy.xjl.rna.controller.model.CheckIDModel;
import com.zxhy.xjl.rna.controller.model.LogonModel;
import com.zxhy.xjl.rna.controller.model.RegisterModel;
import com.zxhy.xjl.rna.fileService.RealNameAuthFileService;
import com.zxhy.xjl.rna.model.RealNameAuth;
import com.zxhy.xjl.rna.service.RealNameAuthService;
/**
 * 实名注册控制器
 * 1、发送验证码
 * 2、手机注册
 * 3、核名
 * 4、刷脸
 * 5、登陆
 * 6、发送重置密码的验证码
 * 7、重置密码
 * 8、得到当前认证状态
 */
@Controller
@RequestMapping("/realNameAuth")
public class RealNameAuthController {
	private static final Log log = LogFactory.getLog(RealNameAuthController.class);
	@Autowired
	private SMS sms ;//短信接口
	@Autowired
	private VerifyCode verifyCode;//验证码接口
	@Autowired
	private RealNameAuthBusiness realNameAuthBusiness;//实名认证操作类
	@Autowired
	private RealNameAuthFileService realNameAuthFileService;//文件管理类
	@Autowired
	private RealNameAuthService realNameAuthService;
	@Autowired
	private PeopleService peopleService;
	@Autowired
	private FaceService faceService;
	@Autowired 
	private RealNameAuthConfig realNameAuthConfig;
	/**
	 * 1、发送验证码
	 * @param phone 手机号码
	 * @return 字符串类型验证码
	 */
	@ResponseBody
	@RequestMapping(value="/sendCode/{phone}",method=RequestMethod.POST)
	public void sendCode(@PathVariable String phone){
		log.debug("sendCode phone:" + phone);
		 RealNameAuth realNameAuth = this.realNameAuthService.findByPhone(phone);
		//判断注册手机号码是否存在
		 if(null==realNameAuth){
			 String code = this.realNameAuthConfig.getDefaultVerifyCode();
			 if (this.realNameAuthConfig.isUseSMSSendVerifyCode()){
				 log.debug("使用短信发送验证码，这里随机生成一个验证码");
				 code=this.verifyCode.generate(phone,2);//产生随机四位验证码
			 } else {
				 log.debug("不使用短信发送验证码，默认验证码为:" + this.realNameAuthConfig.getDefaultVerifyCode());
			 }
			 String content="门户网站，" + code + "是您本次身份校验码，" + 2 + "分钟内有效．审批局工作人员绝不会向您索取此校验码，切勿告知他人．";
			 if (this.realNameAuthConfig.isUseSMSSendVerifyCode()){
				 this.sms.send(phone,content);//通过手机发送验证码; 
			 } else {
				 //不用短信发送验证码，则什么事情都不需要做
			 }
		 }else{
			 throw new RuntimeException("手机已经存在，不能发送验证码");
		 }
	}

	/**
	 * 2、手机注册
	 * @param realNameAuthTask 实名认证实体类
	 * @return true:注册验证成功,false：注册验证失败
	 */
	@ResponseBody
	@RequestMapping(value="/register",method=RequestMethod.POST,consumes = "application/json")
	public boolean register(@RequestBody RegisterModel registerModel){
		 boolean verifyCodeflag = false;
		 if (this.realNameAuthConfig.isUseSMSSendVerifyCode()){
			 verifyCodeflag =  this.verifyCode.check(registerModel.getPhone(),registerModel.getCode());//验证验证码是否正确
		 }  else {
			 verifyCodeflag = StringUtils.equals(this.realNameAuthConfig.getDefaultVerifyCode(), registerModel.getCode());
		 }
		 //判断验证码是否正确
		 if(verifyCodeflag){
			 //执行入库操作
			 this.realNameAuthBusiness.register(registerModel.getPhone(),registerModel.getCode());
		 }else{
			 throw new RuntimeException("验证码已经过期，请重新输入");
		 }
		 return verifyCodeflag;
	}
	/**
	 * 3、核名
	 */
	@ResponseBody
	@RequestMapping(value="/checkID",method=RequestMethod.POST,consumes = "application/json")
	public void checkID(@RequestBody CheckIDModel checkIDModel){
		boolean checkIdFlag=this.peopleService.checkID(checkIDModel.getId(), checkIDModel.getName());
		if (checkIdFlag){
			 com.zxhy.xjl.rna.business.RealNameAuthTask task  = this.realNameAuthBusiness.getRealNameAuthTask(checkIDModel.getPhone());//获取taskID
			 this.realNameAuthBusiness.checkRealName(checkIDModel.getPhone(),checkIDModel.getId(),checkIDModel.getName(),task.getTaskId());//执行核名操作
		} else {
			throw new RuntimeException("身份证号码和姓名核实失败");
		}
	}
	/**
	 * 4、刷脸
	 */
	@ResponseBody
	@RequestMapping(value="/checkFace",method=RequestMethod.POST,consumes = "application/json")
	public void checkFace(@RequestBody CheckFaceModel checkFaceModel){
		boolean checkFaceFlag=this.faceService.checkFace(checkFaceModel.getIdCode(), checkFaceModel.getFace());
		//把拍照图片存储在某个url中
		String faceURL = null;
		checkFaceModel.setFaceUrl(faceURL);
		if (checkFaceFlag){
			 com.zxhy.xjl.rna.business.RealNameAuthTask task  = this.realNameAuthBusiness.getRealNameAuthTask(checkFaceModel.getPhone());//获取taskID
			 this.realNameAuthBusiness.checkFace(checkFaceModel.getPhone(), checkFaceModel.getFaceUrl(), task.getTaskId());
		} else {
			throw new RuntimeException("人脸识别识别");
		}
	}
	/**
	 * 5、登陆
	 * @param realNameAuthTask实名认证实体类
	 */
	@ResponseBody
	@RequestMapping(value="/logon",method=RequestMethod.POST,consumes = "application/json")
	public RealNameAuthTask logon(@RequestBody LogonModel logonModel){
		log.debug("logon phone:" + logonModel.getPhone() + " passwd:" + logonModel.getPasswd());
		boolean logonFlag = this.realNameAuthBusiness.logon(logonModel.getPhone(), logonModel.getPasswd());
		if (logonFlag){
			RealNameAuthTask task =new RealNameAuthTask();
			task.setPhone(logonModel.getPhone());
			task.setProcessName("实名认证");
			task.setTaskId("1");
			task.setTaskName("核名");
			return task;
		} else {
			return new RealNameAuthTask();
		}
	}
	
	/**
	 * 6、发送重置密码的验证码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/sendCodeForUpdatePassword/{phone}",method=RequestMethod.POST,consumes = "application/json")
	public void sendCodeForUpdatePassword(@PathVariable String phone){
		RealNameAuth realNameAuth = this.realNameAuthService.findByPhone(phone);//根据phone获取账号信息
		//判断是否存在
		if(null!=realNameAuth){
			//参考上面的发送验证码方法进行做处理 todo
			 String code=this.verifyCode.generate(phone,2);//产生随机四位验证码
			 String content="门户网站，" + code + "是您本次身份校验码，" + 2 + "分钟内有效．审批局工作人员绝不会向您索取此校验码，切勿告知他人．";
			 this.sms.send(phone,content);//通过手机发送验证码;
		}else{
			throw new RuntimeException("手机号码不存在");
		}
	}
	/**
	 * 7、重置密码
	 */
	@ResponseBody
	@RequestMapping(value="/updatePassword",method=RequestMethod.POST,consumes = "application/json")
	public void updatePassword(@RequestBody RegisterModel registerModel){
		//判断验证码是否正确,参考上面的验证码处理方式，todo
		boolean flag = this.verifyCode.check(registerModel.getPhone(),registerModel.getCode());//验证验证码是否正确
		if(flag){
			
			log.debug("phone:"+registerModel.getPhone()+"password:"+registerModel.getPasswd()+"code:"+registerModel.getCode());
		}
	}
	/**
	 * 8、得到当前认证状态
	 */
	@ResponseBody
	@RequestMapping(value="/realNameAuth/{phone}",method=RequestMethod.GET)
	public RealNameAuthTask getRealNameStatus(@PathVariable String phone){
		return this.realNameAuthBusiness.getRealNameAuthTask(phone);
	}
	/**
	 * 上传文件
	 */
	@RequestMapping(value="/doUploadFile")
	public void doUploadFile(@RequestParam(value = "file", required = false) MultipartFile file,@RequestParam(name="phone") String phone,HttpServletRequest request,HttpServletResponse response){
		 String outPath = request.getSession().getServletContext().getRealPath("/");// 文件保存文件夹，也可自定为绝对路径
		 if(null!=file){
			 this.realNameAuthFileService.doUploadImage(file, outPath, phone+"sfz");//身份证照片
		 }else{
			 throw new RuntimeException("上传文件为空");
		 }
	}
	/**
	 * 9、管理员登陆
	 * 
	 */
	@ResponseBody
	@RequestMapping(value="/adminLogon",method=RequestMethod.POST,consumes = "application/json")
	public AdminLogonModel adminLogon(@RequestBody AdminLogonModel adminLogonModel){
		log.debug("logon phone:" + adminLogonModel.getAccountNumber()+ " passwd:" + adminLogonModel.getPasswd());
		boolean logonFlag = this.realNameAuthBusiness.adminLogon(adminLogonModel.getAccountNumber(), adminLogonModel.getPasswd());
		if (logonFlag){
			AdminLogonModel task =new AdminLogonModel();
			task.setAccountNumber(adminLogonModel.getAccountNumber());
			return task;
		} else {
			return new AdminLogonModel();
		}
	}
	/**
	 * 10、人工审核
	 * 
	 */
}
