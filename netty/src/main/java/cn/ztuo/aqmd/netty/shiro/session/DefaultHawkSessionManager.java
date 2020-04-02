/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: DefaultHawkSessionManager.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019年7月25日
 * @version 1.0
 * History:
 * v1.0.0, , 2019年7月25日, Create
 */
package cn.ztuo.aqmd.netty.shiro.session;

import cn.ztuo.aqmd.core.entity.RequestPacket;
import cn.ztuo.aqmd.core.entity.ResponsePacket;
import cn.ztuo.aqmd.netty.shiro.listener.HSessionListener;
import cn.ztuo.aqmd.netty.shiro.mgt.HawkSessionKey;
import cn.ztuo.aqmd.netty.shiro.util.HawkUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Title: DefaultHawkSessionManager
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @author MrGao
 * @date 2019年7月25日
 */
public class DefaultHawkSessionManager extends DefaultSessionManager implements SessionManager {
	public DefaultHawkSessionManager(){
		List<SessionListener> listeners = new ArrayList<>();
		listeners.add(new HSessionListener());
		super.setSessionListeners(listeners);//添加会话监听
	}
	private Serializable getReferencedSessionId(RequestPacket request, ResponsePacket response) {
		return request.getSequenceId();
	}
	@Override
    public Serializable getSessionId(SessionKey key) {
        Serializable id = super.getSessionId(key);
        RequestPacket request = HawkUtils.getRequest(key);
        ResponsePacket response = HawkUtils.getResponse(key);
        id = getSessionId(request, response);
        return id;
    }
	protected Serializable getSessionId(RequestPacket request, ResponsePacket response) {
		return getReferencedSessionId(request, response);
	}

	@Override
	protected Session createExposedSession(Session session, SessionContext context) {

		RequestPacket request = HawkUtils.getRequest(context);
		ResponsePacket response = HawkUtils.getResponse(context);
		SessionKey key = new HawkSessionKey(session.getId(), request, response);
		return new DelegatingSession(this, key);
	}
	@Override
	protected Session createExposedSession(Session session, SessionKey key) {

		RequestPacket request = HawkUtils.getRequest(key);
		ResponsePacket response = HawkUtils.getResponse(key);
		SessionKey sessionKey = new HawkSessionKey(session.getId(), request, response);
		return new DelegatingSession(this, sessionKey);
	}
}
