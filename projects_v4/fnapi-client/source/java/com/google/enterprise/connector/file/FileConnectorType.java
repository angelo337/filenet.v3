package com.google.enterprise.connector.file;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.filenet.api.exception.EngineRuntimeException;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 *Represents FileNet connector type information. Contains methods for creating and validating user form. 
 *@author amit_kagrawal
 **/
public class FileConnectorType implements ConnectorType {

	private static final String HIDDEN = "hidden";
	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String TEXT = "text";
	private static final String TYPE = "type";
	private static final String INPUT = "input";
	private static final String CLOSE_ELEMENT = "/>";
	private static final String OPEN_ELEMENT = "<";
	private static final String PASSWORD = "password";
	private static final String PASSWORD_KEY = "Password";
	private static final String USERNAME = "username";
	private static final String OBJECT_STORE = "object_store";
	private static final String WORKPLACE_URL = "workplace_display_url";
	private static final String CONTENT_ENGINE_URL = "content_engine_url";
	private static final String DIV_START_LABEL = "<div style='";
	private static final String DIV_END = "</div>\r\n";
	private static final String TR_END = "</tr>\r\n";
	private static final String TD_END = "</td>\r\n";
	private static final String TD_START = "<td>";
	private static final String TD_START_LABEL = "<td style='";
	private static final String TD_END_START_LABEL = "'>";
	private static final String TD_WHITE_SPACE = "white-space: nowrap";
	private static final String TD_DELIMITER = ";";
	private static final String TD_FONT_WEIGHT = "font-weight: bold";
	private static final String ASTERISK = "*";
	private static final String TD_FONT_COLOR = "color: red";
	private static final String TD_TEXT_ALIGN_RIGHT = "text-align: right";
	private static final String TD_FLOAT_LEFT = "float: left";
	private static final String TD_START_COLSPAN = "<td colspan='2'>";
	private static final String TR_START = "<tr>\r\n";
	private static final String TR_START_HIDDEN = "<tr style='display: none'>\r\n";
	private static final String FNCLASS = "object_factory";
	private static final String FILEPATH = "path_to_WcmApiConfig";
	private static final String AUTHENTICATIONTYPE = "authentication_type";
	private static final String WHERECLAUSE = "additional_where_clause";
	private static final String ISPUBLIC = "is_public";
	private static final String CHECKBOX = "checkbox";
	private static final String CHECKED = "checked='checked'";
	private static Logger logger = null;
	private List keys = null;
	private Set keySet = null;
	private String initialConfigForm = null;
	private ResourceBundle resource;
	private String validation = "";

	static {
		logger = Logger.getLogger(FileConnectorType.class.getName());
	}

	/**
	 * Set the keys that are required for configuration. One of the overloadings
	 * of this method must be called exactly once before the SPI methods are
	 * used.
	 * 
	 * @param keys
	 *            A list of String keys
	 */
	public void setConfigKeys(List keys) {
		if (this.keys != null) {
			throw new IllegalStateException();
		}
		this.keys = keys;
		this.keySet = new HashSet(keys);
	}

	/**
	 * Set the keys that are required for configuration. One of the overloadings
	 * of this method must be called exactly once before the SPI methods are
	 * used.
	 * 
	 * @param keys
	 *            An array of String keys
	 */
	public void setConfigKeys(String[] keys) {
		setConfigKeys(Arrays.asList(keys));
	}

	/**
	 * Sets the form to be used by this configurer. This is optional. If this
	 * method is used, it must be called before the SPI methods are used.
	 * 
	 * @param formSnippet
	 *            A String snippet of html - see the Configurer interface
	 */
	public void setInitialConfigForm(String formSnippet) {
		if (this.initialConfigForm != null) {
			throw new IllegalStateException();
		}
		this.initialConfigForm = formSnippet;
	}

	public ConfigureResponse getConfigForm(Locale language) {

		try {
			logger.info("language used " + language.getLanguage());
			resource = ResourceBundle.getBundle("FileConnectorResources",
					language);
		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle("FileConnectorResources");
		}
		if (initialConfigForm != null) {
			return new ConfigureResponse("", initialConfigForm);
		}
		if (keys == null) {
			throw new IllegalStateException();
		}
		this.initialConfigForm = makeConfigForm(null,this.validation);
		return new ConfigureResponse("", initialConfigForm);
	}

	public ConfigureResponse getPopulatedConfigForm(Map configMap,
			Locale language) {
		try {
			logger.info("language used " + language.getLanguage());
			resource = ResourceBundle.getBundle("FileConnectorResources",language);

		} catch (MissingResourceException e) {
			resource = ResourceBundle.getBundle("FileConnectorResources");
		}
		ConfigureResponse response = new ConfigureResponse("",makeConfigForm(configMap,this.validation));
		return response;
	}

	/**
	 * Loops on keys and return a key name only if it finds one with a null or
	 * blank value, unless key.equals(FNCLASS) or key.equals(AUTHENTICATIONTYPE)
	 * or key.equals(WHERECLAUSE) or key.equals(FILEURI) or key.equals(ISPUBLIC)
	 */
	private String validateConfigMap(Map configData) {
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = (String) configData.get(key);
			//TODO remove unrelevant FILEURI
			if (!key.equals(FNCLASS) && !key.equals(AUTHENTICATIONTYPE)
					&& !key.equals(WHERECLAUSE) //&& !key.equals(FILEURI)
					&& !key.equals(ISPUBLIC)
					&& (val == null || val.length() == 0)) {

				return key;
			}
		}
		return "";
	}

	public ConfigureResponse validateConfig(Map configData, Locale language,ConnectorFactory connectorFactory) {
		try {
			logger.info("language used " + language.getLanguage());
			resource = ResourceBundle.getBundle("FileConnectorResources",language);
		} catch (MissingResourceException e) {
			logger.log(Level.SEVERE, "Unable to find the resource bundle file for language "+language,e);
			resource = ResourceBundle.getBundle("FileConnectorResources");
		}

		if(configData==null){
			logger.severe("No configuration information is available");
			return null;
		}
		
		String form = null;
		
		logger.info("validating the configuration data...");
		String validation = validateConfigMap(configData);
		this.validation = validation;
		
		logger.info("Configuration data validation.. succeeded");

		FileSession session =null;
		if (validation.equals("")) {
			try {
				Properties p = new Properties();
				p.putAll(configData);
				String isPublic = (String) configData.get(ISPUBLIC);
				if (isPublic == null) {
					p.put(ISPUBLIC, "false");
					logger.config("isPulic is set to false, documents will not be seen using public searches");
				}
				
				/*Amit: removed the hard coded filenet instantiation*/
				
				/*Resource res = new ClassPathResource("config/connectorInstance.xml");
				XmlBeanFactory factory = new XmlBeanFactory(res);
				PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
				cfg.setProperties(p);
				cfg.postProcessBeanFactory(factory);
				FileConnector conn = (FileConnector) factory.getBean("FileConnectorInstance");*/

				logger.info("Attempting to create FileNet4 connector instance");
				FileConnector conn =(FileConnector) connectorFactory.makeConnector(configData);
				if(null==conn){
					logger.severe("Unable to establish connection with FileNet server");
					return null;
				}
				
				logger.info("FileNet4 connector instance creation succeeded. Trying to Login into FileNet server.");
				session = (FileSession) conn.login();
				
				if(session != null){
					logger.log(Level.INFO, "Connection to Content Engine URL is Successful");
					session.getTraversalManager();//test on the objectStore name
					logger.log(Level.INFO, "Connection to Object Store " +  (String) configData.get("object_store") + " is Successful");
				}else{
					logger.log(Level.INFO, "Connection to Content Engine URL Failed");
				}

				testWorkplaceUrl((String) configData.get("workplace_display_url"));
			} catch (EngineRuntimeException e){
				String errorKey = e.getExceptionCode().getKey();
				String bundleMessage;
				try{
					if(errorKey.equalsIgnoreCase("E_NULL_OR_INVALID_PARAM_VALUE")){
						bundleMessage = resource.getString("content_engine_url_invalid") +" "+ e.getLocalizedMessage();
						logger.log(Level.SEVERE,bundleMessage,e);
					}else{
//						bundleMessage = resource.getString("required_field_error") +" "+ e.getLocalizedMessage();
						bundleMessage = e.getLocalizedMessage();
						logger.log(Level.SEVERE,bundleMessage,e);
					}
				}catch(MissingResourceException mre){
//					bundleMessage = resource.getString("required_field_error") +" "+ e.getLocalizedMessage();
					bundleMessage = e.getLocalizedMessage();
					logger.log(Level.SEVERE,bundleMessage,mre);
				}
				form = makeConfigForm(configData, validation);
				return new ConfigureResponse( bundleMessage, form);
//				}catch (RepositoryException e) {
			}catch (Throwable e) {
				String extractErrorMessage = null;
				String bundleMessage;
				try {
					extractErrorMessage = e.getCause().getClass().getName();
					if(extractErrorMessage.equalsIgnoreCase("com.filenet.api.exception.EngineRuntimeException")){
						EngineRuntimeException ere = (EngineRuntimeException)e.getCause();
						String errorKey = ere.getExceptionCode().getKey();
						if(errorKey.equalsIgnoreCase("E_OBJECT_NOT_FOUND")){
							bundleMessage = resource.getString("object_store_invalid")+" "+ere.getLocalizedMessage();
						}else if(errorKey.equalsIgnoreCase("E_NOT_AUTHENTICATED")){
							bundleMessage = resource.getString("invalid_credentials_error")+" "+ere.getLocalizedMessage();
						}else if(errorKey.equalsIgnoreCase("E_UNEXPECTED_EXCEPTION")){
							String errorMsg = ere.getCause().getClass().getName();
							if(errorMsg.equalsIgnoreCase("java.lang.NoClassDefFoundError")){
								NoClassDefFoundError ncdf = (NoClassDefFoundError)ere.getCause();
								errorMsg = ncdf.getMessage();
								if(errorMsg.indexOf("activation") != -1){
									bundleMessage = "Connector dependency file activation.jar is corrupted or its classpath has been reset.";
								}else{
									bundleMessage = resource.getString("content_engine_url_invalid");
								}
							}else if(errorMsg.equalsIgnoreCase("org.idoox.util.RuntimeWrappedException")){
								errorMsg = ere.getCause().getMessage();
								if(errorMsg.indexOf("Jetty") != -1){
									bundleMessage = "Connector dependency file jetty.jar is corrupted or its classpath has been reset.";
								}else{
									bundleMessage = "Connector dependency file builtin_serialization.jar is corrupted or its classpath has been reset.";
								}
							}else if(errorMsg.equalsIgnoreCase("java.lang.ExceptionInInitializerError")){
								bundleMessage = "Connector dependency file jaxrpc.jar is corrupted or its classpath has been reset.";
							}else if(errorMsg.equalsIgnoreCase("org.idoox.wasp.UnknownProtocolException")){
								bundleMessage = "Connector dependency file saaj.jar is corrupted or its classpath has been reset.";
							}else{
								bundleMessage = resource.getString("content_engine_url_invalid");
							}
							//e.printStackTrace();
							
						}else if(errorKey.equalsIgnoreCase("API_INVALID_URI")){
							bundleMessage = resource.getString("content_engine_url_invalid");
						}else if(errorKey.equalsIgnoreCase("TRANSPORT_WSI_LOOKUP_FAILURE")){
							bundleMessage = "Connector dependency file wsdl_api.jar is corrupted or its classpath has been reset.";
						}else{
							bundleMessage = resource.getString("required_field_error") +" "+ e.getLocalizedMessage();
						}
					}else{
						bundleMessage = resource.getString(extractErrorMessage);
					}
					logger.log(Level.SEVERE,bundleMessage,e);
				} catch (MissingResourceException mre) {
					bundleMessage = resource.getString("required_field_error") +" "+ e.getLocalizedMessage();
					//logger.severe(bundleMessage);
					logger.log(Level.SEVERE,bundleMessage,mre);
				}catch(NullPointerException npe){
//					bundleMessage = resource.getString("required_field_error") +" "+ e.getMessage();
					bundleMessage = npe.getLocalizedMessage();
					logger.log(Level.SEVERE,"Unable to connect to FileNet server. Got exception: ",npe);
				}catch(Throwable th){
					bundleMessage = th.getLocalizedMessage();
					logger.log(Level.SEVERE,"Unable to connect to FileNet server. Got exception: ",th);
				}
				
				logger.info("request to make configuration form..");
				form = makeConfigForm(configData, validation);
				return new ConfigureResponse( bundleMessage, form);
			} 
			return null;
		}
		form = makeConfigForm(configData, validation);
		return new ConfigureResponse(resource.getString(validation + "_error"), form);
	}

	private void testWorkplaceUrl(String workplaceServerUrl)
	throws RepositoryException {
		//Added by Pankaj on 04/05/2009 to remove the dependency of Httpclient.jar file
		try {
			new FileUrlValidator().validate(workplaceServerUrl);
			logger.log(Level.INFO, "Connection to Workplace URL is Successful");
		} catch (FileUrlValidatorException e) {
			logger.log(Level.WARNING, resource.getString("workplace_url_error"));
			throw new RepositoryException(resource.getString("workplace_url_error"));
		} catch (Throwable t) {
			logger.log(Level.WARNING, resource.getString("workplace_url_error"));
			throw new RepositoryException(resource.getString("workplace_url_error"));
		}
	}

	/**
	 * Make a config form snippet using the keys (in the supplied order) and, if
	 * passed a non-null config map, pre-filling values in from that map
	 * 
	 * @param configMap
	 * @return config form snippet
	 */
	private String makeConfigForm(Map configMap, String validate) {
		StringBuffer buf = new StringBuffer(2048);
		String value = "";
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (configMap != null) {
				value = (String) configMap.get(key);
			}
			if (key.equals(ISPUBLIC)) {
				appendCheckBox(buf, key, resource.getString(key), value);
				appendStartHiddenRow(buf);
				buf.append(OPEN_ELEMENT);
				buf.append(INPUT);
				appendAttribute(buf, TYPE, HIDDEN);
				appendAttribute(buf, VALUE, "false");
				appendAttribute(buf, NAME, key);
				buf.append(CLOSE_ELEMENT);
				appendEndRow(buf);
				value = "";
			} else {
				if (!key.equals(FNCLASS) && !key.equals(AUTHENTICATIONTYPE)
						/*&& !key.equals(WHERECLAUSE)*/ && !key.equals(FILEPATH)) {
					if(validate.equals(key)){
						appendStartRow(buf, key, validate);
					}else{
						appendStartRow(buf, key, "");
					}

				} else {
					appendStartHiddenRow(buf);
				}

				buf.append(OPEN_ELEMENT);
				buf.append(INPUT);
				if (key.equalsIgnoreCase(PASSWORD_KEY)) {
					appendAttribute(buf, TYPE, PASSWORD);
				} else if (key.equals(FNCLASS)
						|| key.equals(AUTHENTICATIONTYPE)
						//|| key.equals(WHERECLAUSE)
						// || key.equals(FILEURI)
				) {
					appendAttribute(buf, TYPE, HIDDEN);
				} else {
					appendAttribute(buf, TYPE, TEXT);
				}

				appendAttribute(buf, NAME, key);
				appendAttribute(buf, VALUE, value);
				buf.append(CLOSE_ELEMENT);
				appendEndRow(buf);
				value = "";
			}
		}
		if (configMap != null) {
			Iterator i = new TreeSet(configMap.keySet()).iterator();
			while (i.hasNext()) {
				String key = (String) i.next();
				if (!keySet.contains(key)) {
					// add another hidden field to preserve this data
					String val = (String) configMap.get(key);
					buf.append("<input type=\"hidden\" value=\"");
					buf.append(val);
					buf.append("\" name=\"");
					buf.append(key);
					buf.append("\"/>\r\n");
				}
			}
		}
		return buf.toString();
	}

	private void appendStartHiddenRow(StringBuffer buf) {
//		buf.append(TR_START);
		buf.append(TR_START_HIDDEN);
		buf.append(TD_START);

	}

	private void appendStartRow(StringBuffer buf, String key, String validate) {
		buf.append(TR_START);
//		buf.append(TD_START);
		buf.append(TD_START_LABEL);
		buf.append(TD_WHITE_SPACE);
		if(isRequired(key)){

			buf.append(TD_END_START_LABEL);
			buf.append(DIV_START_LABEL);
			buf.append(TD_FLOAT_LEFT);
			buf.append(TD_DELIMITER);
			if(!validate.equals("")){
				buf.append(TD_FONT_COLOR);
				buf.append(TD_DELIMITER);
			}
			buf.append(TD_FONT_WEIGHT);
			buf.append(TD_END_START_LABEL);
			buf.append(resource.getString(key));
			buf.append(DIV_END);

			buf.append(DIV_START_LABEL);
			buf.append(TD_TEXT_ALIGN_RIGHT);
			buf.append(TD_DELIMITER);
			buf.append(TD_FONT_WEIGHT);
			buf.append(TD_DELIMITER);
			buf.append(TD_FONT_COLOR);			
			buf.append(TD_END_START_LABEL);
			buf.append(ASTERISK);
			buf.append(DIV_END);
			buf.append(TD_END);
		}
		else{
			buf.append(TD_END_START_LABEL);
			buf.append(resource.getString(key));
			buf.append(TD_END);
		}
		buf.append(TD_START);
	}

	private void appendEndRow(StringBuffer buf) {
//		buf.append(CLOSE_ELEMENT);
		buf.append(TD_END);
		buf.append(TR_END);
	}

	private void appendAttribute(StringBuffer buf, String attrName,
			String attrValue) {
		buf.append(" ");
		buf.append(attrName);
		buf.append("=\"");
		buf.append(attrValue);
		buf.append("\"");
		if (attrName == TYPE && attrValue == TEXT) {
			buf.append(" size=\"50\"");
		}
	}

	private void appendCheckBox(StringBuffer buf, String key, String label,
			String value) {
		buf.append(TR_START);
//		buf.append(TD_START);
		buf.append(TD_START_COLSPAN);
		buf.append(OPEN_ELEMENT);
		buf.append(INPUT);
		buf.append(" " + TYPE + "=\"" +CHECKBOX+'"');
		buf.append(" " + NAME + "=\"" + key + "\" ");
		if (value != null && value.equals("on")) {
			buf.append(CHECKED);
		}
		buf.append(CLOSE_ELEMENT);
		buf.append(label + TD_END);

		buf.append(TR_END);

	}

	private boolean isRequired(final String configKey){
		final boolean bValue = false;
		if(configKey.equals(OBJECT_STORE) || configKey.equals(WORKPLACE_URL) || configKey.equals(PASSWORD_KEY) || configKey.equals(USERNAME) || configKey.equals(CONTENT_ENGINE_URL)){
			return true;
		}		
		return bValue;
	}

}
