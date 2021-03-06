package lzf.webserver.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;

/**
* @author 李子帆
* @version 1.0
* @date 2018年7月24日 下午10:42:35
* @Description ServletRegistration.Dynamics实现类，用于动态注册Servlet
*/
public class ApplicationServletRegistration implements ServletRegistration.Dynamic {
	
	private final StandardWrapper wrapper;
	
	ApplicationServletRegistration(StandardWrapper wrapper) {
		this.wrapper = wrapper;
	}

	/**
	 * @return 该Servlet名称，由web.xml配置文件决定
	 */
	@Override
	public String getName() {
		return wrapper.servletConfig.servletName;
	}

	/**
	 * @return 该Servlet的类名，该类名由web.xml配置文件决定，不一定存在该类
	 */
	@Override
	public String getClassName() {
		return wrapper.servletConfig.servletClass;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		
		Map<String, String> parameters = wrapper.servletConfig.parameterMap;
		
		if(parameters.containsKey(name)) {
			return false;
		}
		
		parameters.put(name, value);
		return true;
	}

	/**
	 * 该方法默认从ServletConfig中获取参数
	 * @param name 初始化参数名
	 * @return 参数值
	 */
	@Override
	public String getInitParameter(String name) {
		return wrapper.servletConfig.parameterMap.get(name);
	}

	/**
	 * 向Servlet添加初始化参数，如果键已存在则不更新
	 * @param initParameters 初始化参数Map
	 * @return 与原有的初始化参数相冲突的键名集合，即原有的Map和initParameters都存在的键集合,如果不存在冲突的键
	 * 			则返回null
	 */
	@Override
	public Set<String> setInitParameters(Map<String, String> initParameters) {
		
		if(initParameters == null) {
			return null;
		}
		
		Set<String> set = new HashSet<>();
		Map<String, String> sourceMap = wrapper.servletConfig.parameterMap;
		
		for(Map.Entry<String, String> entry : initParameters.entrySet()) {
			
			String key = entry.getKey();
			
			for(Map.Entry<String, String> entry0 : sourceMap.entrySet()) {
				
				if(entry0.getKey().equals(key)) {
					set.add(entry0.getKey());
					continue;
				}
				
				sourceMap.put(key, entry.getValue());
			}
		}
		
		if(set.isEmpty())
			return null;
		
		return set;
	}

	@Override
	public Map<String, String> getInitParameters() {
		return wrapper.servletConfig.parameterMap;
	}

	@Override
	public Set<String> addMapping(String... urlPatterns) {
		
		if(urlPatterns == null || urlPatterns.length == 0)
			return null;
		
		Set<String> set = new HashSet<>();
		List<String> list = wrapper.servletConfig.urlPatterns;
		
		for(String urlPattern : urlPatterns) {
			
			if(list.contains(urlPattern)) {
				set.add(urlPattern);
				continue;
			}
			
			list.add(urlPattern);
		}
		
		if(set.isEmpty())
			return null;
		
		return set;
	}

	@Override
	public Collection<String> getMappings() {
		return wrapper.servletConfig.urlPatterns;
	}

	@Override
	public String getRunAsRole() {
		return null;
	}

	@Override
	public void setAsyncSupported(boolean isAsyncSupported) {
		
	}

	@Override
	public void setLoadOnStartup(int loadOnStartup) {
		wrapper.setLoadOnStartup(loadOnStartup);
	}

	@Override
	public void setMultipartConfig(MultipartConfigElement multipartConfig) {
		
	}

	@Override
	public void setRunAsRole(String roleName) {
		
	}

	@Override
	public Set<String> setServletSecurity(ServletSecurityElement constraint) {
		// TODO Auto-generated method stub
		return null;
	}

}
