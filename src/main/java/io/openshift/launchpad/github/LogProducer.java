package io.openshift.launchpad.github;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LogProducer {

	@Produces
	Logger createLogger(final InjectionPoint ip) {
		return LoggerFactory.getLogger(ip.getMember().getDeclaringClass());
	}
}