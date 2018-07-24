package lzf.webserver.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.dom4j.DocumentException;
import org.dom4j.Element;

import lzf.webserver.Context;
import lzf.webserver.LifecycleException;
import lzf.webserver.Loader;
import lzf.webserver.core.LifecycleBase;
import lzf.webserver.core.StandardWrapper;
import lzf.webserver.log.Log;
import lzf.webserver.log.LogFactory;
import lzf.webserver.util.XMLUtil;

/**
 * @author 李子帆
 * @version 1.0
 * @date 2018年7月21日 下午4:30:10
 * @Description web应用载入器，包括XML文件解析，静态资源文件的读取，类加载实现
 */
public class WebappLoader extends LifecycleBase implements Loader {

	public static final Log log = LogFactory.getLog(WebappLoader.class);

	// 该Web加载器所属的Context容器
	private Context context = null;

	//当需要热替换时，需要重新新建一个WebappClassLoader对象并替换旧的ClassLoader
	private volatile WebappClassLoader classLoader = null;
	
	//支持热替换吗
	private boolean reloadable = false;

	public WebappLoader(Context context) {
		this.context = context;
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(Context context) throws LifecycleException {
		if (context == null)
			return;

		if (getLifecycleState().isAvailable()) {
			throw new LifecycleException("The webAppLoader is running");
		}

		synchronized (this) {
			this.context = context;
			setReloadable(context.getReloadable());
		}
	}

	@Override
	public boolean getReloadable() {
		return reloadable;
	}

	@Override
	public void setReloadable(boolean reloadable) {
		this.reloadable = reloadable;
	}

	@Override
	protected void initInternal() throws Exception {
		classLoader = new WebappClassLoader(WebappClassLoader.class.getClassLoader(), context.getPath());
		resourceLoad(context.getPath());
	}

	@Override
	protected void startInternal() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void stopInternal() throws Exception {

	}

	@Override
	protected void destoryInternal() throws Exception {

	}

	/**
	 * @param path 单个Web应用主目录，该方法会尝试载入里面所有的文件
	 */
	private void resourceLoad(File file) {
	
		if (file.exists()) {
			File[] files = file.listFiles();
			
			if (files.length == 0) {
				return;
			} else {
				
				for (File file2 : files) {
					if (file2.isDirectory()) {
						if(file2.getName().equals("WEB-INF")) {
							loadWebXml();
							continue;
						}
						resourceLoad(file2);
						
					} else {
						
						byte[] b = loadFile(file2);
						if(b == null)
							return;
						
						String fileName = file2.getName();
						if(!(fileName.endsWith("class") || fileName.endsWith("jsp"))) {
							context.addChildContainer(StandardWrapper.getDefaultWrapper(context, file2, b));
						}
						
					}
				}
				
			}
		}
	}
	
	/**
	 * @param file 需要载入的资源文件File对象
	 * @return 资源文件二进制数据
	 */
	private byte[] loadFile(File file) {
		try {
			@SuppressWarnings("resource")
			FileInputStream fis = new FileInputStream(file);
			byte[] b = new byte[(int) file.length()];
			fis.read(b);
			return b;
		} catch (FileNotFoundException e) {
			log.error("找不到资源文件：" + file.getAbsolutePath(), e);
		} catch (IOException e) {
			log.error("无法读入资源文件：" + file.getAbsolutePath() , e);
		}
		return null;
	}
	
	/**
	 * 加载web.xml文件
	 * @param path web.xml文件路径
	 * @return 是否加载成功
	 * @throws DocumentException
	 */
	private boolean loadWebXml() {	
		
		File path = new File(context.getPath(), "WEB-INF" + File.separator + "web.xml");
		if(!path.exists())
			return false;
		
		Element root = null;
		try {
			root = XMLUtil.getXMLRoot(path);
		} catch (DocumentException e) {
			log.error("Context: " + context.getName() + " web.xml read error", e);
			return false;
		}
		
		//String displayName = root.element("display-name").getStringValue();
		/*
		for(Element contextParam : root.elements("context-param")) {
			String paramName = contextParam.element("param-name").getStringValue();
			String paramValue = contextParam.element("param-value").getStringValue();
		}
		
		for(Element filter : root.elements("filter")) {
			String filterName = filter.element("filter-name").getStringValue();
			String filterClass = filter.element("filter-class").getStringValue();
		}
		
		for(Element filterMapping : root.elements("filter-mapping")) {
			String filterName = filterMapping.element("filter-name").getStringValue();
			String urlPattern = filterMapping.element("url-pattern").getStringValue();
		}*/
		
		Map<String, String> servletMap = new LinkedHashMap<>();
		
		for(Element servlet : root.elements("servlet")) {
			String servletName = servlet.element("servlet-name").getStringValue();
			String servletClass = servlet.element("servlet-class").getStringValue();
			
			servletMap.put(servletName, servletClass);
		}
		
		for(Element servletMapping : root.elements("servlet-mapping")) {
			
			String servletName = servletMapping.element("servlet-name").getStringValue();
			String uriPattern = servletMapping.element("url-pattern").getStringValue();
			
			if(servletMap.containsKey(servletName)) {
				context.addChildContainer(StandardWrapper.getDynamicWrapper(context, servletName
						, servletMap.get(servletName), uriPattern));
			} else {
				log.warn("Can not find servlet name:" + servletName + " in web.xml");
			}
		}
		
		/*
		Element sessionConfig = root.element("session-config");
		
		//TODO mime-mapping
		
		for(Element welcomeFile : root.element("welcome-file-list").elements("welcome-file")) {
			String weclomeFileName = welcomeFile.getStringValue();
		}
		
		for(Element taglib : root.elements("taglib")) {
			String taglibUri = taglib.element("taglib-uri").getStringValue();
			String taglibLocation = taglib.element("taglib-location").getStringValue();
		}
		
		for(Element listener : root.element("listener").elements("listener-class")) {
			String listenerClass = listener.getStringValue();
		}*/
		
		return true;
	}
}
