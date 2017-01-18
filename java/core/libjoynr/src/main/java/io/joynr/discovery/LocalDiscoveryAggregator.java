package io.joynr.discovery;

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

import static io.joynr.util.VersionUtil.getVersionFromAnnotation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.joynr.exceptions.JoynrRuntimeException;

import io.joynr.provider.ProviderAnnotations;
import io.joynr.proxy.Callback;
import io.joynr.proxy.Future;
import io.joynr.runtime.SystemServicesSettings;
import joynr.system.DiscoveryAsync;
import joynr.system.DiscoveryProvider;
import joynr.system.DiscoveryProxy;
import joynr.system.Routing;
import joynr.system.RoutingProvider;
import joynr.types.DiscoveryEntry;
import joynr.types.DiscoveryQos;
import joynr.types.ProviderQos;
import joynr.types.ProviderScope;

public class LocalDiscoveryAggregator implements DiscoveryAsync {

    private static final Logger logger = LoggerFactory.getLogger(LocalDiscoveryAggregator.class);

    private static final long NO_EXPIRY = Long.MAX_VALUE;
    private HashMap<String, DiscoveryEntry> provisionedDiscoveryEntries = new HashMap<>();
    private DiscoveryProxy discoveryProxy;

    @Inject
    public LocalDiscoveryAggregator(@Named(SystemServicesSettings.PROPERTY_SYSTEM_SERVICES_DOMAIN) String systemServicesDomain,
                                    @Named(SystemServicesSettings.PROPERTY_CC_DISCOVERY_PROVIDER_PARTICIPANT_ID) String discoveryProviderParticipantId,
                                    @Named(SystemServicesSettings.PROPERTY_CC_ROUTING_PROVIDER_PARTICIPANT_ID) String routingProviderParticipantId) {
        ProviderQos providerQos = new ProviderQos();
        providerQos.setScope(ProviderScope.LOCAL);
        String defaultPublicKeyId = "";
        provisionedDiscoveryEntries.put(systemServicesDomain
                + ProviderAnnotations.getInterfaceName(DiscoveryProvider.class),
                                        new DiscoveryEntry(getVersionFromAnnotation(DiscoveryProvider.class),
                                                           systemServicesDomain,
                                                           ProviderAnnotations.getInterfaceName(DiscoveryProvider.class),
                                                           discoveryProviderParticipantId,
                                                           providerQos,
                                                           System.currentTimeMillis(),
                                                           NO_EXPIRY,
                                                           defaultPublicKeyId));
        // provision routing provider to prevent lookup via discovery proxy during startup.
        provisionedDiscoveryEntries.put(systemServicesDomain + Routing.INTERFACE_NAME,
                                        new DiscoveryEntry(getVersionFromAnnotation(RoutingProvider.class),
                                                           systemServicesDomain,
                                                           Routing.INTERFACE_NAME,
                                                           routingProviderParticipantId,
                                                           providerQos,
                                                           System.currentTimeMillis(),
                                                           NO_EXPIRY,
                                                           defaultPublicKeyId));
    }

    public void setDiscoveryProxy(DiscoveryProxy discoveryProxy) {
        this.discoveryProxy = discoveryProxy;
    }

    @Override
    public Future<Void> add(Callback<Void> callback, DiscoveryEntry discoveryEntry) {
        if (discoveryProxy == null) {
            throw new JoynrRuntimeException("LocalDiscoveryAggregator: discoveryProxy not set. Couldn't reach "
                    + "local capabilitites directory.");
        }
        return discoveryProxy.add(callback, discoveryEntry);
    }

    @Override
    public Future<DiscoveryEntry[]> lookup(final Callback<DiscoveryEntry[]> callback,
                                           String[] domains,
                                           String interfaceName,
                                           DiscoveryQos discoveryQos) {
        final Set<DiscoveryEntry> discoveryEntries = new HashSet<>();
        Set<String> missingDomains = new HashSet<>();
        for (String domain : domains) {
            if (provisionedDiscoveryEntries.containsKey(domain + interfaceName)) {
                DiscoveryEntry discoveryEntry = provisionedDiscoveryEntries.get(domain + interfaceName);
                discoveryEntries.add(discoveryEntry);
            } else {
                missingDomains.add(domain);
            }
        }
        logger.trace("Found locally provisioned discovery entries: {}", discoveryEntries);
        final Future<DiscoveryEntry[]> discoveryEntryFuture = new Future<>();
        if (!missingDomains.isEmpty()) {
            logger.trace("Did not find entries for the following domains: {}", missingDomains);
            if (discoveryProxy == null) {
                throw new JoynrRuntimeException("LocalDiscoveryAggregator: discoveryProxy not set. Couldn't reach "
                        + "local capabilitites directory.");
            }
            Callback<DiscoveryEntry[]> newCallback = new Callback<DiscoveryEntry[]>() {

                @Override
                public void onFailure(JoynrRuntimeException error) {
                    callback.onFailure(error);
                    discoveryEntryFuture.onFailure(error);
                }

                @Override
                public void onSuccess(DiscoveryEntry[] entries) {
                    assert entries != null : "Entries must not be null.";
                    logger.trace("Globally found entries for missing domains: {}", entries);
                    Collections.addAll(discoveryEntries, entries);
                    resolveDiscoveryEntriesFutureWithEntries(discoveryEntryFuture, discoveryEntries, callback);
                }
            };
            String[] missingDomainsArray = new String[missingDomains.size()];
            missingDomains.toArray(missingDomainsArray);
            discoveryProxy.lookup(newCallback, missingDomainsArray, interfaceName, discoveryQos);
        } else {
            resolveDiscoveryEntriesFutureWithEntries(discoveryEntryFuture, discoveryEntries, callback);
        }
        return discoveryEntryFuture;
    }

    private void resolveDiscoveryEntriesFutureWithEntries(Future<DiscoveryEntry[]> future,
                                                          Set<DiscoveryEntry> discoveryEntries,
                                                          Callback<DiscoveryEntry[]> callback) {
        DiscoveryEntry[] discoveryEntriesArray = new DiscoveryEntry[discoveryEntries.size()];
        discoveryEntries.toArray(discoveryEntriesArray);
        future.resolve((Object) discoveryEntriesArray);
        callback.resolve((Object) discoveryEntriesArray);
    }

    @Override
    public Future<DiscoveryEntry> lookup(Callback<DiscoveryEntry> callback, String participantId) {

        if (discoveryProxy == null) {
            throw new JoynrRuntimeException("LocalDiscoveryAggregator: discoveryProxy not set. Couldn't reach "
                    + "local capabilitites directory.");
        }
        return discoveryProxy.lookup(callback, participantId);

    }

    @Override
    public Future<Void> remove(Callback<Void> callback, String participantId) {
        if (discoveryProxy == null) {
            throw new JoynrRuntimeException("LocalDiscoveryAggregator: discoveryProxy not set. Couldn't reach "
                    + "local capabilitites directory.");
        }
        return discoveryProxy.remove(callback, participantId);
    }
}
