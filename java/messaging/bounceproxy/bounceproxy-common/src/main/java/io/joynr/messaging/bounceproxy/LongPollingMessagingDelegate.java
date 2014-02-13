package io.joynr.messaging.bounceproxy;

/*
 * #%L
 * joynr::java::messaging::bounceproxy::controlled-bounceproxy
 * %%
 * Copyright (C) 2011 - 2013 BMW Car IT GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.joynr.communications.exceptions.JoynrHttpException;
import io.joynr.messaging.datatypes.JoynrMessagingErrorCode;

import javax.ws.rs.core.Response.Status;

import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements long poll messaging routines to be used by bounce
 * proxies.
 * 
 * @author christina.strobel
 * 
 */
public class LongPollingMessagingDelegate {

    private static final Logger log = LoggerFactory.getLogger(LongPollingMessagingDelegate.class);

    /**
     * Creates a long polling channel.
     * 
     * @param ccid
     *            the identifier of the channel
     * @param atmosphereTrackingId
     *            the tracking ID of the channel
     * @return the path segment for the channel. The path, appended to the base
     *         URI of the channel service, can be used to post messages to the
     *         channel.
     */
    public String createChannel(String ccid, String atmosphereTrackingId) {

        throwExceptionIfTrackingIdnotSet(atmosphereTrackingId);

        log.info("CREATE channel for cluster controller: {} trackingId: {} ", ccid, atmosphereTrackingId);
        Broadcaster broadcaster = null;
        // look for an existing broadcaster

        BroadcasterFactory defaultBroadcasterFactory = BroadcasterFactory.getDefault();
        if (defaultBroadcasterFactory == null) {
            throw new JoynrHttpException(500, 10009, "broadcaster was null");
        }

        broadcaster = defaultBroadcasterFactory.lookup(Broadcaster.class, ccid, false);
        // create a new one if none already exists
        if (broadcaster == null) {
            broadcaster = defaultBroadcasterFactory.get(BounceProxyBroadcaster.class, ccid);

        }

        // avoids error where previous long poll from browser got message
        // destined for new long poll
        // especially as seen in js, where every second refresh caused a fail
        for (AtmosphereResource resource : broadcaster.getAtmosphereResources()) {
            if (resource.uuid() != null && resource.uuid().equals(atmosphereTrackingId)) {
                resource.resume();
            }
        }

        UUIDBroadcasterCache broadcasterCache = (UUIDBroadcasterCache) broadcaster.getBroadcasterConfig()
                                                                                  .getBroadcasterCache();
        broadcasterCache.activeClients().put(atmosphereTrackingId, System.currentTimeMillis());

        // BroadcasterCacheInspector is not implemented corrected in Atmosphere
        // 1.1.0RC4
        // broadcasterCache.inspector(new MessageExpirationInspector());

        return "/channels/" + ccid + "/";
    }

    private void throwExceptionIfTrackingIdnotSet(String atmosphereTrackingId) {
        if (atmosphereTrackingId == null || atmosphereTrackingId.isEmpty()) {
            log.error("atmosphereTrackingId NOT SET");
            throw new JoynrHttpException(Status.BAD_REQUEST,
                                         JoynrMessagingErrorCode.JOYNRMESSAGINGERROR_TRACKINGIDNOTSET);
        }
    }
}
