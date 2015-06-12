package uk.nhs.ciao.jms.dumper;

import org.apache.camel.LoggingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.CIPRoutes;
import uk.nhs.ciao.configuration.CIAOConfig;

/**
 * Configures multiple camel CDA builder routes determined by properties specified
 * in the applications registered {@link CIAOConfig}.
 */
public class JMSDumperRoutes extends CIPRoutes {
	private static final Logger LOGGER = LoggerFactory.getLogger(JMSDumperRoutes.class);
		
	/**
	 * Creates multiple document parser routes
	 * 
	 * @throws RuntimeException If required CIAO-config properties are missing
	 */
	@Override
	public void configure() {
		super.configure();
		
		from("jms:queue:{{inputQueue}}")
			.id("jms-dumper-{{inputQueue}}")
			.log(LoggingLevel.INFO, LOGGER, "Dumping document from JMS")
			.to("file:./{{inputQueue}}");
	}
}
