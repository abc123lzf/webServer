package lzf.webserver.session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import javax.servlet.http.HttpSession;

import lzf.webserver.Context;
import lzf.webserver.LifecycleException;
import lzf.webserver.Session;
import lzf.webserver.log.Log;
import lzf.webserver.log.LogFactory;
import lzf.webserver.util.TimeUtil;

/**
* @author 李子帆
* @version 1.0
* @date 2018年7月15日 下午4:20:06
* @Description 会话管理类，每个Web应用对应一个会话管理类a
*/
public final class HttpSessionManager extends SessionManagerBase {
	
	private static final Log log = LogFactory.getLog(HttpSessionManager.class);
	
	//WebApp容器实例
	private final Context context;
	
	public HttpSessionManager(Context context) {
		this.context = context;
	}
	
	public HttpSessionManager(Context context, int defaultMaxSessionInactiveTime) {
		this(context);
		this.defaultMaxSessionInactiveTime = defaultMaxSessionInactiveTime;
	}
	
	@Override
	protected void initInternal() throws LifecycleException {
		log.info("HTTPSessionManager init");
	}

	@Override
	protected void startInternal() throws LifecycleException {
		log.info("HTTPSessionManager start");
		new Thread(processer).start();
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		//持久化
	}

	@Override
	protected void destoryInternal() throws LifecycleException {
		super.sessions.clear();
	}
	
	/**
	 * 根据Session Id获取HttpSession对象
	 * @param sessionId SessionId
	 * @param create true:如果没有找到该Session对象则创建一个新的Session对象并返回
	 * false:如果没有找到则返回null
	 * @return HttpSession实例
	 * @throws LifecycleException Session管理器未处于启动状态
	 */
	public HttpSession getHttpSession(String sessionId, boolean create) throws LifecycleException {
		HttpSession session = (HttpSession)getSession(sessionId, create);
		return session;
	}
	
	/**
	 * 获取该SessionManager所属的Context容器
	 * @return Context实例
	 */
	public Context getContext() {
		return context;
	}
	
	/**
	 * 创建一个标准HTTP会话类
	 * @see SessionManagerBase.newSession();
	 * @return 该SessionId
	 */
	@Override
	protected Session newSessionInternal() {
		
		StandardSession session = new StandardSession(this);
		context.getListenerContainer().runSessionInitializedEvent(session);
		return session;
	}
	
	@Override
	public void removeSession(String sessionId) throws LifecycleException {
		
		checkLifecycleState();
		HttpSession session = (HttpSession)sessions.get(sessionId);
		
		if(session != null) {
			
			synchronized(sessions) {
				sessions.remove(sessionId);
			}
		
			context.getListenerContainer().runSessionDestroyedEvent(session);
		}
	}
	
	/**
	 * 持久化Session
	 * @throws IOException 
	 */
	protected void persistSessions() throws IOException {
		File file = new File(System.getProperty("user.dir") + File.separator 
				+ TimeUtil.getTimeString() + ".ser");
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		for(Map.Entry<String, Session> entry : sessions.entrySet()) {
			oos.writeObject(entry);
		}
		oos.close();
	}
}
