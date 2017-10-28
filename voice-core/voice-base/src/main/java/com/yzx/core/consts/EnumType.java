package com.yzx.core.consts;

public class EnumType {
	public static enum SysErrorCode {
		S_000000("100000", "业务成功"), S_200000("200000", "指定的接口服务不存在"), S_900000("999999", "系统错误");

		private String errCode;
		private String errMsg;

		SysErrorCode(String errCode, String errMsg) {
			this.errCode = errCode;
			this.errMsg = errMsg;
		}

		public String getErrCode() {
			return errCode;
		}

		public void setErrCode(String errCode) {
			this.errCode = errCode;
		}

		public String getErrMsg() {
			return errMsg;
		}

		public void setErrMsg(String errMsg) {
			this.errMsg = errMsg;
		}
	}

	public static enum BusiErrorCode {
			  B_000000("100000", "业务成功")
			, B_100001("100001", "主账户ID为空")
			, B_100002("100002", "主账户Token为空")
			, B_100003("100003", "主账户不存在")
			, B_100004("100004", "主账户已禁用")
			, B_100005("100005", "主账户Token不正确")
			, B_100006("100006", "未开通此业务")
			, B_100007("100007", "业务被禁用")
			, B_100008("100008", "IP不在服务器白名单中")
			, B_100009("100009", "请求包头Authorization参数为空")
			, B_100010("100010", "Authorization参数Base64解码失败")
			, B_100011("100011", "Authorization参数解码后Token为空")
			, B_100012("100012", "Authorization参数解码后时间戳为空")
			, B_100015("100015", "Authorization参数中账户token不正确")
			, B_100016("100016", "主账号存在非法字符")
			, B_100017("100017", "Authorization参数解码后时间格式有误")
			, B_100018("100018", "Authorization参数解码后时间戳过期")
			, B_100019("100019", "账户余额不足")
			, B_100020("100020", "电话号码为空")
			, B_100021("100021", "号码不是正确的电话号码")
			, B_100022("100022", "城市ID为空")
			, B_100023("100023", "中间号码已经存在绑定关系")
			, B_100024("100024", "中间号码不属于此业务或中间号码不可用")
			, B_100025("100025", "申请绑定关系失败")
			, B_100026("100026", "绑定ID为空")
			, B_100027("100027", "绑定关系不存在")
			, B_100028("100028", "绑定关系解绑失败")
			, B_100029("100029", "模板ID不能为空")
			, B_100030("100030", "Content字段不能为空")
			, B_100031("100031", "Content字段长度不能超过200")
			, B_100032("100032", "captchaCode字段不能为空")
			, B_100033("100033", "captchaCode字段长度为4或6位")
			, B_100034("100034", "captchaCode内容为0-9之间数字")
			, B_100035("100035", "语音验证码请求失败")
			
			
			, B_900000("900000", "系统错误")
			;

		private String errCode;
		private String errMsg;

		BusiErrorCode(String errCode, String errMsg) {
			this.errCode = errCode;
			this.errMsg = errMsg;
		}
		
		@Override
		public String toString() {
			return this.errCode;
		}

		public String getErrCode() {
			return errCode;
		}

		public void setErrCode(String errCode) {
			this.errCode = errCode;
		}

		public String getErrMsg() {
			return errMsg;
		}

		public void setErrMsg(String errMsg) {
			this.errMsg = errMsg;
		}
	}
}
