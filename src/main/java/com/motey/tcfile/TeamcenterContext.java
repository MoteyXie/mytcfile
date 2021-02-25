package com.motey.tcfile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource(value="classpath:custom.properties", encoding="UTF-8")
@Component
public class TeamcenterContext {

	@Value("${tc.address}")
	private String tc_address;
	@Value("${tc.port}")
	private String tc_port;
	@Value("${tc.user}")
	private String user;
	@Value("${tc.pass}")
	private String pass;
	
	public String getTc_address() {
		return tc_address;
	}

	public String getTc_port() {
		return tc_port;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}


	
}
