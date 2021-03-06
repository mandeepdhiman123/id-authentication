package io.mosip.authentication.common.service.websub.impl;

import static io.mosip.authentication.core.constant.IdAuthConfigKeyConstants.IDA_AUTH_PARTNER_ID;
import static io.mosip.authentication.core.constant.IdAuthConfigKeyConstants.IDA_WEBSUB_AUTHTYPE_CALLBACK_SECRET;
import static io.mosip.authentication.core.constant.IdAuthConfigKeyConstants.IDA_WEBSUB_AUTH_TYPE_CALLBACK_URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.authentication.core.constant.IdAuthCommonConstants;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.idrepository.core.constant.IDAEventType;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;

/**
 * The Class AuthTypeStatusEventsSubscriber.
 * @author Loganathan Sekar
 */
@Component
public class AuthTypeStatusEventsSubscriber extends BaseWebSubEventsSubscriber {
	
	/** The Constant logger. */
	private static final Logger logger = IdaLogger.getLogger(AuthTypeStatusEventsSubscriber.class);
	
	/** The Constant PARTNER_ID_PLACEHOLDER. */
	private static final String PARTNER_ID_PLACEHOLDER = "{partnerId}";
	
	/** The auth type callback URL. */
	@Value("${"+ IDA_WEBSUB_AUTH_TYPE_CALLBACK_URL +"}")
	private String authTypeCallbackURL;
	
	
	/** The autype callback secret. */
	@Value("${"+ IDA_WEBSUB_AUTHTYPE_CALLBACK_SECRET +"}")
	private String autypeCallbackSecret;
	
	/** The auth parther id. */
	@Value("${"+ IDA_AUTH_PARTNER_ID  +"}")
	private String authPartherId;

	/**
	 * Do initialize.
	 */
	@Override
	protected void doInitialize() {
		logger.info(IdAuthCommonConstants.SESSION_ID, "doInitialize",  this.getClass().getSimpleName(), "Initializing Internal Auth subscribptions..");
		String topicPrefix = authPartherId + "/";
		initAuthTypeEvent(topicPrefix);	
	}
	
	/**
	 * Try register topic for auth events.
	 *
	 * @param topicPrefix the topic prefix
	 */
	private void tryRegisterTopicForAuthEvents(String topicPrefix) {
		String topic = topicPrefix + IDAEventType.AUTH_TYPE_STATUS_UPDATE.name();
		try {
			logger.debug(IdAuthCommonConstants.SESSION_ID, "tryRegisterTopicForAuthEvents", "", "Trying to register topic: " + topic);
			publisher.registerTopic(topic, publisherUrl);	
			logger.info(IdAuthCommonConstants.SESSION_ID, "tryRegisterTopicForAuthEvents", "", "Registered topic: " + topic);
		} catch (Exception e) {
			logger.info(IdAuthCommonConstants.SESSION_ID, "tryRegisterTopicForAuthEvents",  e.getClass().toString(), "Error registering topic: "+ topic +"\n" + e.getMessage());
		}
	}

	/**
	 * Subscribe for auth type events.
	 *
	 * @param topicPrefix the topic prefix
	 */
	private void subscribeForAuthTypeEvents(String topicPrefix) {
		String topic = topicPrefix + IDAEventType.AUTH_TYPE_STATUS_UPDATE.name();
		try {
			SubscriptionChangeRequest subscriptionRequest = new SubscriptionChangeRequest();
			subscriptionRequest.setCallbackURL(authTypeCallbackURL.replace(PARTNER_ID_PLACEHOLDER, authPartherId));
			subscriptionRequest.setHubURL(hubURL);
			subscriptionRequest.setSecret(autypeCallbackSecret);
			subscriptionRequest.setTopic(topic);
			logger.debug(IdAuthCommonConstants.SESSION_ID, "subscribeForAuthTypeEvents", "", "Trying to subscribe to topic: " + topic + " callback-url: " + authTypeCallbackURL);
			subscribe.subscribe(subscriptionRequest);
			logger.info(IdAuthCommonConstants.SESSION_ID, "subscribeForAuthTypeEvents", "", "Subscribed to topic: " + topic);
		} catch (Exception e) {
			logger.info(IdAuthCommonConstants.SESSION_ID, "subscribeForAuthTypeEvents",  e.getClass().toString(), "Error subscribing topic: "+ topic +"\n" + e.getMessage());
			throw e;
		}
	}

	/**
	 * Inits the auth type event.
	 *
	 * @param topicPrefix the topic prefix
	 */
	private void initAuthTypeEvent(String topicPrefix) {
		tryRegisterTopicForAuthEvents(topicPrefix);
		subscribeForAuthTypeEvents(topicPrefix);
	}

}
