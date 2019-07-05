import com.azry.mps.dealers.service.MPSDealersService;
import com.azry.mps.dealers.service.MPSServiceNotAvailableException_Exception;
import com.azry.mps.dealers.service.MPSTBCDealersService;
import helpers.CXFLoggingInterceptorHelper;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.BindingProvider;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

public class SimpleClient {

	private static final String TLS_VERSION = "TLSv1.2";

	private String keystorePath;

	private String serviceUrl;

	private String keystorePassword;

	private String truststorePassword;

	private String truststorePath;

	public static void main(String[] argv) {
		if (argv.length == 5) {
			SimpleClient instance = new SimpleClient(argv[0], argv[1], argv[2], argv[3], argv[4]);
			try {
				MPSTBCDealersService dealersService = instance.getDealersService();
				dealersService.getServices();
				System.out.println("get services called successfully");
			} catch (MPSServiceNotAvailableException_Exception e) {
				System.out.println("error while calling dealers service, getServices method");
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("keystore path, keystore password, truststore path, truststore password and dealers service url are required");
			System.exit(-1);
		}
	}


	public SimpleClient(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword, String serviceUrl) {
		this.truststorePath = truststorePath;
		this.truststorePassword = truststorePassword;
		this.keystorePath = keystorePath;
		this.keystorePassword = keystorePassword;
		this.serviceUrl = serviceUrl;
	}

	private MPSTBCDealersService getDealersService(){
		MPSTBCDealersService dealersService = new MPSDealersService().getMPSDealersServicePort();

		Map<String, Object> requestContext = ((BindingProvider) dealersService).getRequestContext();
		requestContext.put("set-jaxb-validation-event-handler", "false");
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceUrl);

		Client client = ClientProxy.getClient(dealersService);
		CXFLoggingInterceptorHelper.addLoggingInterceptors(client);
		HTTPConduit conduit = (HTTPConduit) client.getConduit();
		TLSClientParameters tlsClientParameters = conduit.getTlsClientParameters();
		if (tlsClientParameters == null) {
			tlsClientParameters = new TLSClientParameters();
		}
		configureTLSClientParameters(tlsClientParameters);
		conduit.setTlsClientParameters(tlsClientParameters);

		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(36000);
		httpClientPolicy.setAllowChunking(false);
		httpClientPolicy.setReceiveTimeout(32000);
		httpClientPolicy.setAutoRedirect(true);
		conduit.setClient(httpClientPolicy);

		return dealersService;
	}

	private void configureTLSClientParameters(TLSClientParameters tlsClientParameters) {
		try (InputStream keyInput = new FileInputStream(keystorePath)) {
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(keyInput, keystorePassword.toCharArray());
			keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

			KeyStore ksCACert = KeyStore.getInstance(KeyStore.getDefaultType());
			ksCACert.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(ksCACert);

			SSLContext context = SSLContext.getInstance(TLS_VERSION);
			context.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
			tlsClientParameters.setSSLSocketFactory(context.getSocketFactory());
			tlsClientParameters.setDisableCNCheck(true);
		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
			System.out.println("Error while adding tls parameters for Gateway service client");
			e.printStackTrace();
		}
	}
}