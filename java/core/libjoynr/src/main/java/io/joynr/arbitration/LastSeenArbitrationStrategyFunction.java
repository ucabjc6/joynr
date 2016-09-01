package io.joynr.arbitration;

import java.util.Arrays;

/*
 * #%L
 * %%
 * Copyright (C) 2011 - 2016 BMW Car IT GmbH
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

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joynr.types.DiscoveryEntry;

/**
 * Arbitrator using the lastSeenDateMs. The provider with the latest lastSeenDateMs value is
 * chosen.
 */
public class LastSeenArbitrationStrategyFunction extends ArbitrationStrategyFunction {
    private static final Logger logger = LoggerFactory.getLogger(LastSeenArbitrationStrategyFunction.class);

    @Override
    public final Collection<DiscoveryEntry> select(Map<String, String> parameters,
                                                   final Collection<DiscoveryEntry> capabilities) {
        logger.trace("starting select Provider by lastSeenDateMs");
        DiscoveryEntry latestSeenCapability = null;
        long latestSeenDateMs = -1L;
        for (DiscoveryEntry discoveryEntry : capabilities) {
            // Search for the provider with the highest lastSeenDateMs
            Long lastSeenDateMs = discoveryEntry.getLastSeenDateMs();
            logger.trace("Looking at capability with lastSeenDateMs " + lastSeenDateMs);
            if (latestSeenDateMs < lastSeenDateMs) {
                latestSeenDateMs = lastSeenDateMs;
                latestSeenCapability = discoveryEntry;
            }
        }
        logger.trace("capability with lastSeenMs: " + latestSeenDateMs + "\r\n" + latestSeenCapability);

        return latestSeenCapability == null ? null : Arrays.asList(latestSeenCapability);
    }
}