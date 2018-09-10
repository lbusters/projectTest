package cn.itcast;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Demo {

	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("classpath:applicationContext_job.xml");
		
	}

}
