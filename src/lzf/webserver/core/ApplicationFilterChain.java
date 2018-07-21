package lzf.webserver.core;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author ���ӷ�
 * @version 1.0
 * @date 2018��7��20�� ����13:39:03
 * @Description FilterChainʵ���࣬����Filter�Ĺ�������
 */
public class ApplicationFilterChain implements FilterChain {

	public static final int INCR = 8;
	
	private ApplicationFilterConfig[] filters = new ApplicationFilterConfig[0];
	
	//��ǰFilterConfig������
	private int n = 0;
	
	/**
	 * �߳�˽�ж��󣬱�ʾ��ǰ�߳������ʵ�Filters�����±�
	 */
	private ThreadLocal<Integer> pos = new ThreadLocal<Integer>() {
		//��ʼ�������±�Ϊ0
		@Override
		protected Integer initialValue() {
			return 0;
		}
	};
	
	/**
	 * ������ת��������������һ��filter
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		if(pos.get() < n) {
			ApplicationFilterConfig filterConfig = filters[pos.get()];
			Filter filter = filterConfig.getFilter();
			filter.doFilter(request, response, this);
			pos.set(pos.get() + 1);
		} 
		return;
	}

	/**
	 * ����ApplicationFilterConfig
	 * @param filterConfig ApplicationFilterConfig����
	 */
	synchronized void addFilter(ApplicationFilterConfig filterConfig) {
		//����Ƿ��ظ�����
		for(ApplicationFilterConfig filter : filters)
			if(filter == filterConfig)
				return;
			
		if(filters.length == n) {
			ApplicationFilterConfig[] newFilters = new ApplicationFilterConfig[filters.length + INCR];
			int i = 0;
			for(ApplicationFilterConfig config : filters) {
				newFilters[i++] = config;
			}
			newFilters[i] = filterConfig;
			this.filters = newFilters;
		} else {
			filters[n + 1] = filterConfig;
		}
		n++;
	}
	
	/**
	 * �ͷ����е�FilterConfig
	 */
	synchronized void release() {
		for(int i = 0; i < n; i++) {
			filters[i] = null;
		}
		n = 0;
	}
}