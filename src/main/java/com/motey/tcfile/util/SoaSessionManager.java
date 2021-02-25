package com.motey.tcfile.util;

import java.util.HashMap;
import java.util.Map;

public class SoaSessionManager {
	
	private static Map<String, SoaSession> sessions = new HashMap<>();
	
//	public static SoaSession getSession(String userId, String password) throws Exception {
//		return getSession(null, null, userId, password);
//	}
	
	public static SoaSession getSession(String address, String port, String userId, String password) throws Exception {
		
		if(sessions.containsKey(userId)) {
			SoaSession soaSession = sessions.get(userId);
//			User user = soaSession.getUser();
			return soaSession;
		}
		
		SoaSession soaSession = new SoaSession();
		
		soaSession.connect(address, port);
		
		soaSession.login(userId, password);

		sessions.put(userId, soaSession);
		
		return soaSession;
		
	}

}
