package lzf.webserver.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import lzf.webserver.Context;
import lzf.webserver.Wrapper;
import lzf.webserver.connector.Request;
import lzf.webserver.connector.Response;

/**
* @author ���ӷ�
* @version 1.0
* @date 2018��7��21�� ����10:38:27
* @Description Context��������
*/
public class StandardContextValve extends ValveBase {
	
	StandardContextValve() {
		super();
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		
		Wrapper wrapper = request.getWrapper();
		if(wrapper == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

}