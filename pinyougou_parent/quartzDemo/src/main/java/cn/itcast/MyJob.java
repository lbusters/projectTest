package cn.itcast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyJob {

	public void showTime() {
		System.out.println("当前时间:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
	}
}
