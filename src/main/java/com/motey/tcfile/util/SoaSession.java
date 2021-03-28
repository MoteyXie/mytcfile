package com.motey.tcfile.util;

import com.teamcenter.clientx.AppXCredentialManager;
import com.teamcenter.clientx.AppXExceptionHandler;
import com.teamcenter.clientx.AppXModelEventListener;
import com.teamcenter.clientx.AppXPartialErrorListener;
import com.teamcenter.clientx.AppXRequestListener;
import com.teamcenter.schemas.soa._2006_03.exceptions.InvalidCredentialsException;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.services.strong.core._2006_03.Session.LoginResponse;
import com.teamcenter.soa.SoaConstants;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.strong.User;

public class SoaSession {

	private Connection connection;
	private AppXCredentialManager credentialManager;
	private User user;
	private SoaPropertyManager propertyManager;;
    private SessionService sessionService;


    //	public Connection defaultConnect() {
//		return connect("tcserver", "7001");
//	}
//	
	public Connection connect(String address, String port) {
		return connect(String.format("http://%s:%s/tc", address, port));
	}
	
	public Connection connect(String host) {
		
		credentialManager = new AppXCredentialManager();

        String protocol=null;
        String envNameTccs = null;
        if ( host.startsWith("http") )
        {
            protocol   = SoaConstants.HTTP;
        }
        else if ( host.startsWith("tccs") )
        {
            protocol   = SoaConstants.TCCS;
            host = host.trim();
            int envNameStart = host.indexOf('/') + 2;
            envNameTccs = host.substring( envNameStart, host.length() );
            host = "";
        }
        else
        {
            protocol   = SoaConstants.IIOP;
        }
        
        connection = new Connection(host, credentialManager, SoaConstants.REST, protocol);

        if( protocol == SoaConstants.TCCS )
        {
           connection.setOption(  Connection.TCCS_ENV_NAME, envNameTccs );
        }

        connection.setExceptionHandler(new AppXExceptionHandler());
        connection.getModelManager().addPartialErrorListener(new AppXPartialErrorListener());
        connection.getModelManager().addModelEventListener(new AppXModelEventListener());
        Connection.addRequestListener( new AppXRequestListener() );
        return connection;
	}
	
	public User login(String id, String password) throws InvalidCredentialsException {
		sessionService = SessionService.getService(connection);
    	LoginResponse out = sessionService.login(id, password, "", "","", "SoaAppX");
    	sessionService.refreshPOMCachePerRequest(true);
    	this.user = out.user;

        return user;
	}


	public void refreshCache(){
        sessionService.refreshPOMCachePerRequest(true);
    }
	
	public User getUser() {
		return user;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void logout() throws ServiceException
    {
        SessionService sessionService = SessionService.getService(connection);
        sessionService.logout();
    }
	
	public SoaPropertyManager getPropertyManager() {
		if(propertyManager == null) {
			propertyManager = new SoaPropertyManager(connection);
		}
		return propertyManager;
	}
}
