设置环境变量
vi ~/.bash_profile

	export VOICE_APP_HOME=/home/voice
	export VOICE_FRW_HOME=/home/voice/frw
	export VOICE_FRW_CONF=/home/voice/conf

	PATH=$PATH:$VOICE_FRW_HOME/sbin/core

	export PATH
	
	. setenv
	
source ~/.bash_profile 使环境变量生效

常用命令 voice <command> <app>
	例：
		启动auth组件服务: voice start auth
		停止auth组件服务: voice stop auth
		重启auth组件服务: voice restart auth
		查看auth组件状态: voice status auth
		

