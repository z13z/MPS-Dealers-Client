package helpers;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.message.Message;

import java.util.List;

public class CXFLoggingInterceptorHelper {

	public static void addLoggingInterceptors(Client client) {
		List<Interceptor<? extends Message>> inInterceptors = client.getInInterceptors();
		if (!containsLoggingInInterceptor(inInterceptors)) {
			inInterceptors.add(new LoggingInInterceptor());
		}

		List<Interceptor<? extends Message>> outInterceptors = client.getOutInterceptors();
		if (!containsLoggingOutInterceptor(outInterceptors)) {
			outInterceptors.add(new LoggingOutInterceptor());
		}
	}

	private static boolean containsLoggingInInterceptor(List<Interceptor<? extends Message>> interceptors) {
		for (Interceptor interceptor : interceptors) {
			if (interceptor instanceof LoggingInInterceptor) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsLoggingOutInterceptor(List<Interceptor<? extends Message>> interceptors) {
		for (Interceptor interceptor : interceptors) {
			if (interceptor instanceof LoggingOutInterceptor) {
				return true;
			}
		}
		return false;
	}

}
