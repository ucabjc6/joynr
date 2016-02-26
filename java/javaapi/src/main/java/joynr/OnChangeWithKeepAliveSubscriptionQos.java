package joynr;

/*
 * #%L
 * %%
 * Copyright (C) 2011 - 2015 BMW Car IT GmbH
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

import io.joynr.pubsub.HeartbeatSubscriptionInformation;
import io.joynr.pubsub.SubscriptionQos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Class representing the quality of service settings for subscriptions
 * based on changes and time periods
 * <br>
 * This class stores quality of service settings used for subscriptions to
 * <b>attributes</b> in generated proxy objects. Using it for subscriptions to
 * broadcasts is theoretically possible because of inheritance but makes no
 * sense (in this case the additional members will be ignored).
 * <br>
 * Notifications will be sent if the subscribed value has changed or a time
 * interval without notifications has expired. The subscription will
 * automatically expire after the expiry date is reached. If no publications
 * were received for alertAfter Interval, publicationMissed will be called.
 * <br>
 * minInterval can be used to prevent too many messages being sent.
 */
public class OnChangeWithKeepAliveSubscriptionQos extends OnChangeSubscriptionQos implements
        HeartbeatSubscriptionInformation {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(OnChangeWithKeepAliveSubscriptionQos.class);

    private static final long MIN_MAX_INTERVAL_MS = 50L;
    private static final long MAX_MAX_INTERVAL_MS = 2592000000L; // 30 days
    private static final long DEFAULT_MAX_INTERVAL_MS = 60000L; // 1 minute
    private static final long NO_ALERT_AFTER_INTERVAL = 0;
    private static final long DEFAULT_ALERT_AFTER_INTERVAL_MS = NO_ALERT_AFTER_INTERVAL;
    private static final long MAX_ALERT_AFTER_INTERVAL_MS = 2592000000L; // 30 days

    private long maxIntervalMs = DEFAULT_MAX_INTERVAL_MS;
    private long alertAfterIntervalMs = NO_ALERT_AFTER_INTERVAL;

    /**
     * Default Constructor
     */
    public OnChangeWithKeepAliveSubscriptionQos() {
    }

    /**
     * @deprecated This constructor will be deleted by 2017-01-01.
     * Use the fluent interface instead.
     *
     * Constructor of OnChangeWithKeepAliveSubscriptionQos object with specified
     * minInterval, maxInterval, and expiry date.
     *
     * @param minIntervalMs
     *            defines how often an update may be sent
     * @param maxIntervalMs
     *            defines how long to wait before sending an update even if the
     *            value did not change
     * @param expiryDateMs
     *            how long is the subscription valid
     *
     * @see OnChangeSubscriptionQos#OnChangeSubscriptionQos(long, long, long)
     *            OnChangeSubscriptionQos.OnChangeSubscriptionQos(long, long, long)
     *            for more information about <b>minInterval</b>
     * @see #setMaxInterval(long)
     * @see SubscriptionQos#SubscriptionQos(long, long)
     *            SubscriptionQos.SubscriptionQos(long, long)
     *            for more information on <b>expiryDate</b> and <b>publicationTtl</b>
     *            (publicationTtl will be set to its default value)
     * @see #setAlertAfterInterval(long) setAlertAfterInterval(long)
     *            (alertAfterInterval will be set to its default value)
     */
    @Deprecated
    public OnChangeWithKeepAliveSubscriptionQos(long minIntervalMs, long maxIntervalMs, long expiryDateMs) {
        this(minIntervalMs,
             maxIntervalMs,
             expiryDateMs,
             DEFAULT_ALERT_AFTER_INTERVAL_MS,
             SubscriptionQos.DEFAULT_PUBLICATION_TTL_MS);
    }

    /**
     * @deprecated This constructor will be deleted by 2017-01-01.
     * Use the fluent interface instead.
     *
     * Constructor of OnChangeWithKeepAliveSubscriptionQos object with specified
     * minInterval, maxInterval, expiry date, and alertAfterInterval.
     *
     * @param minIntervalMs
     *            defines how often an update may be sent
     * @param maxIntervalMs
     *            defines how long to wait before sending an update even if the
     *            value did not change
     * @param expiryDateMs
     *            how long is the subscription valid
     * @param alertAfterIntervalMs
     *            defines how long to wait for an update before publicationMissed
     *            is called if no publications were received.
     *
     * @see OnChangeSubscriptionQos#OnChangeSubscriptionQos(long, long, long)
     *            OnChangeSubscriptionQos.OnChangeSubscriptionQos(long, long, long)
     *            for more information about <b>minInterval</b>
     * @see #setMaxInterval(long)
     * @see SubscriptionQos#SubscriptionQos(long, long)
     *            SubscriptionQos.SubscriptionQos(long, long)
     *            for more information on <b>expiryDate</b> and <b>publicationTtl</b>
     *            (publicationTtl will be set to its default value)
     * @see #setAlertAfterInterval(long)
     */
    @Deprecated
    public OnChangeWithKeepAliveSubscriptionQos(long minIntervalMs,
                                                long maxIntervalMs,
                                                long expiryDateMs,
                                                long alertAfterIntervalMs) {
        this(minIntervalMs,
             maxIntervalMs,
             expiryDateMs,
             alertAfterIntervalMs,
             SubscriptionQos.DEFAULT_PUBLICATION_TTL_MS);
    }

    /**
     * Constructor of OnChangeWithKeepAliveSubscriptionQos object with specified
     * minInterval, maxInterval, expiry date, alertAfterInterval, and
     * publicationTtl (full parameter set).
     *
     * @param minIntervalMs
     *            defines how often an update may be sent
     * @param maxIntervalMs
     *            defines how long to wait before sending an update even if the
     *            value did not change
     * @param expiryDateMs
     *            how long is the subscription valid
     * @param alertAfterIntervalMs
     *            defines how long to wait for an update before publicationMissed
     *            is called if no publications were received.
     * @param publicationTtlMs
     *            time to live for publication messages
     *
     * @see OnChangeSubscriptionQos#OnChangeSubscriptionQos(long, long, long)
     *            OnChangeSubscriptionQos.OnChangeSubscriptionQos(long, long, long)
     *            for more information about <b>minInterval</b>
     * @see #setMaxInterval(long)
     * @see SubscriptionQos#SubscriptionQos(long, long)
     *            SubscriptionQos.SubscriptionQos(long, long)
     *            for more information on <b>expiryDate</b> and <b>publicationTtl</b>
     * @see #setAlertAfterInterval(long)
     */
    @Deprecated
    public OnChangeWithKeepAliveSubscriptionQos(long minIntervalMs,
                                                long maxIntervalMs,
                                                long expiryDateMs,
                                                long alertAfterIntervalMs,
                                                long publicationTtlMs) {
        super(minIntervalMs, expiryDateMs, publicationTtlMs);
        setMaxInterval(maxIntervalMs);
        setAlertAfterInterval(alertAfterIntervalMs);
    }

    /**
     * Get the maximum interval in milliseconds.
     * <br>
     * The provider will send notifications every maximum interval in milliseconds,
     * even if the value didn't change. It will send notifications more often if
     * on-change notifications are enabled, the value changes more often, and the
     * minimum interval QoS does not prevent it. The maximum interval can thus
     * be seen as a sort of heart beat or keep alive interval, if no other
     * publication has been sent within that time.
     *
     * @return The maxInterval in milliseconds. The publisher will send a
     *            notification at least every maxInterval milliseconds.
     */
    public long getMaxInterval() {
        return maxIntervalMs;
    }

    /**
     * Set the maximum interval in milliseconds.
     * <br>
     * The provider will send publications every maximum interval in milliseconds,
     * even if the value didn't change. It will send notifications more often if
     * on-change notifications are enabled, the value changes more often, and the
     * minimum interval QoS does not prevent it. The maximum interval can thus
     * be seen as a sort of heart beat or keep alive interval, if no other
     * publication has been sent within that time.
     *
     * @param maxIntervalMs
     *            The publisher will send a notification at least every
     *            maxIntervalMs.<br>
     *            <br>
     *            <b>Minimum and Maximum Values</b>
     *            <ul>
     *            <li>The absolute <b>minimum</b> setting is
     *            {@value #MIN_MAX_INTERVAL_MS} milliseconds. <br>
     *            Any value less than this minimum will be treated at the absolute
     *            minimum setting of{@value #MIN_MAX_INTERVAL_MS} milliseconds.
     *            <li>The absolute <b>maximum</b> setting is
     *            {@value #MAX_MAX_INTERVAL_MS} milliseconds. <br>
     *            Any value bigger than this maximum will be treated as the absolute
     *            maximum setting of {@value #MAX_MAX_INTERVAL_MS} milliseconds.
     *            </ul>
     * @return the subscriptionQos (fluent interface)
     */
    public OnChangeWithKeepAliveSubscriptionQos setMaxInterval(long maxIntervalMs) {
        if (maxIntervalMs < MIN_MAX_INTERVAL_MS) {
            this.maxIntervalMs = MIN_MAX_INTERVAL_MS;
        } else if (maxIntervalMs > MAX_MAX_INTERVAL_MS) {
            this.maxIntervalMs = MAX_MAX_INTERVAL_MS;
        } else {
            this.maxIntervalMs = maxIntervalMs;
        }

        if (this.maxIntervalMs < this.getMinInterval()) {
            this.maxIntervalMs = this.getMinInterval();
        }

        if (alertAfterIntervalMs != 0 && alertAfterIntervalMs < this.maxIntervalMs) {
            alertAfterIntervalMs = this.maxIntervalMs;
        }
        return this;
    }

    /**
     * Get the alertAfterInterval in milliseconds.
     * <br>
     * If no notification was received within the last alert interval, a missed
     * publication notification will be raised.
     *
     * @return The alertAfterInterval in milliseconds. If more than
     *         alertAfterInterval milliseconds pass without receiving a message,
     *         the subscriptionManager will issue a publicationMissed. If set
     *         to 0 (NO_ALERT_AFTER_INTERVAL), never alert.
     */
    @Override
    public long getAlertAfterInterval() {
        return alertAfterIntervalMs;
    }

    /**
     * Set the alertAfterInterval in milliseconds.
     * <br>
     * If no notification was received within the last alert interval, a missed
     * publication notification will be raised.
     *
     * @param alertAfterIntervalMs
     *            the max time that can expire without receiving a publication
     *            before an alert will be generated. If more than alertIntervalMs
     *            pass without receiving a message, subscriptionManager will issue
     *            a publicationMissed.
     *            <ul>
     *            <li><b>Minimum</b> setting: The value cannot be set below the
     *            value of maxInterval <br>
     *            If a value is passed that is less than this minimum, maxInterval
     *            will be used instead.
     *            <li>The absolute <b>maximum</b> setting is 2.592.000.000
     *            milliseconds (30 days). <br>
     *            Any value bigger than this maximum will be treated as the
     *            absolute maximum setting of 2.592.000.000 milliseconds.
     *            </ul>
     * @return the subscriptionQos (fluent interface)
     */
    public OnChangeWithKeepAliveSubscriptionQos setAlertAfterInterval(final long alertAfterIntervalMs) {
        if (alertAfterIntervalMs > MAX_ALERT_AFTER_INTERVAL_MS) {
            this.alertAfterIntervalMs = MAX_ALERT_AFTER_INTERVAL_MS;
            logger.warn("alertAfterIntervalMs > maxInterval. Using MAX_ALERT_AFTER_INTERVAL_MS: {}",
                        MAX_ALERT_AFTER_INTERVAL_MS);
        } else {
            this.alertAfterIntervalMs = alertAfterIntervalMs;
        }

        if (this.alertAfterIntervalMs != 0 && this.alertAfterIntervalMs < maxIntervalMs) {
            this.alertAfterIntervalMs = maxIntervalMs;
            logger.warn("attempt to set alertAfterIntervalMs to a value smaller than maxInterval; setting to maxInterval instead");
        }
        return this;
    }

    @Override
    public OnChangeWithKeepAliveSubscriptionQos setExpiryDate(long expiryDateMs) {
        return (OnChangeWithKeepAliveSubscriptionQos) super.setExpiryDate(expiryDateMs);
    }

    @Override
    public OnChangeWithKeepAliveSubscriptionQos setMinInterval(long minIntervalMs) {
        super.setMinInterval(minIntervalMs);
        // adjust maxInterval to match new minInterval
        return setMaxInterval(maxIntervalMs);
    }

    @Override
    public OnChangeWithKeepAliveSubscriptionQos setPublicationTtl(long publicationTtlMs) {
        return (OnChangeWithKeepAliveSubscriptionQos) super.setPublicationTtl(publicationTtlMs);
    }

    @Override
    public OnChangeWithKeepAliveSubscriptionQos setValidityMs(long validityMs) {
        return (OnChangeWithKeepAliveSubscriptionQos) super.setValidityMs(validityMs);
    }

    public void clearAlertAfterInterval() {
        this.alertAfterIntervalMs = NO_ALERT_AFTER_INTERVAL;
    }

    @Override
    @JsonIgnore
    public long getHeartbeat() {
        return maxIntervalMs;
    }

    /**
     * Calculate code for hashing based on member contents
     *
     * @return The calculated hash code
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (alertAfterIntervalMs ^ (alertAfterIntervalMs >>> 32));
        result = prime * result + (int) (maxIntervalMs ^ (maxIntervalMs >>> 32));
        return result;
    }

    /**
     * Check for equality
     *
     * @param obj Reference to the object to compare to
     * @return true, if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OnChangeWithKeepAliveSubscriptionQos other = (OnChangeWithKeepAliveSubscriptionQos) obj;
        if (alertAfterIntervalMs != other.alertAfterIntervalMs) {
            return false;
        }
        if (maxIntervalMs != other.maxIntervalMs) {
            return false;
        }
        return true;
    }

}
