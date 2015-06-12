package uk.nhs.ciao.jms.dumper;

import uk.nhs.ciao.camel.CamelApplication;
import uk.nhs.ciao.camel.CamelApplicationRunner;
import uk.nhs.ciao.configuration.CIAOConfig;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * The main ciao-jms-dumper application
 */
public class JMSDumperApplication extends CamelApplication {
	/**
	 * Runs the CDA builder application
	 * 
	 * @see CIAOConfig#CIAOConfig(String[], String, String, java.util.Properties)
	 * @see CamelApplicationRunner
	 */
	public static void main(final String[] args) throws Exception {
		final CamelApplication application = new JMSDumperApplication(args);
		CamelApplicationRunner.runApplication(application);
	}
	
	public JMSDumperApplication(final String... args) throws CIAOConfigurationException {
		super("ciao-jms-dumper.properties", args);
	}
	
	public JMSDumperApplication(final CIAOConfig ciaoConfig, final String... args) {
		super(ciaoConfig, args);
	}
}
