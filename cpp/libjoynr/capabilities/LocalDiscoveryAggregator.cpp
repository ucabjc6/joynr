/*
 * #%L
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
#include "joynr/LocalDiscoveryAggregator.h"

#include <vector>

#include "joynr/IRequestCallerDirectory.h"
#include "joynr/SystemServicesSettings.h"
#include "joynr/RequestStatus.h"
#include "joynr/RequestStatusCode.h"

#include "joynr/types/DiscoveryEntry.h"
#include "joynr/types/StdDiscoveryEntry.h"
#include "joynr/types/StdDiscoveryQos.h"
#include "joynr/system/IRouting.h"
#include "joynr/system/IDiscovery.h"
#include "joynr/system/DiscoveryProxy.h"
#include "joynr/TypeUtil.h"

namespace joynr
{

LocalDiscoveryAggregator::LocalDiscoveryAggregator(
        IRequestCallerDirectory& requestCallerDirectory,
        const SystemServicesSettings& systemServicesSettings)
        : discoveryProxy(NULL),
          hasOwnershipOfDiscoveryProxy(false),
          requestCallerDirectory(requestCallerDirectory),
          provisionedDiscoveryEntries(),
          systemServicesSettings(systemServicesSettings)
{
    QList<joynr::types::CommunicationMiddleware::Enum> connections;
    connections << joynr::types::CommunicationMiddleware::JOYNR;
    joynr::types::DiscoveryEntry routingProviderDiscoveryEntry(
            systemServicesSettings.getDomain(),
            TypeUtil::toQt(joynr::system::IRouting::INTERFACE_NAME()),
            systemServicesSettings.getCcRoutingProviderParticipantId(),
            joynr::types::ProviderQos(),
            connections);
    provisionedDiscoveryEntries.insert(
            routingProviderDiscoveryEntry.getParticipantId().toStdString(),
            routingProviderDiscoveryEntry);
    joynr::types::DiscoveryEntry discoveryProviderDiscoveryEntry(
            systemServicesSettings.getDomain(),
            TypeUtil::toQt(joynr::system::IDiscovery::INTERFACE_NAME()),
            systemServicesSettings.getCcDiscoveryProviderParticipantId(),
            joynr::types::ProviderQos(),
            connections);
    provisionedDiscoveryEntries.insert(
            discoveryProviderDiscoveryEntry.getParticipantId().toStdString(),
            discoveryProviderDiscoveryEntry);
}

LocalDiscoveryAggregator::~LocalDiscoveryAggregator()
{
    if (hasOwnershipOfDiscoveryProxy) {
        delete discoveryProxy;
    }
}

void LocalDiscoveryAggregator::setDiscoveryProxy(joynr::system::IDiscoverySync* discoveryProxy)
{
    this->discoveryProxy = discoveryProxy;
    hasOwnershipOfDiscoveryProxy = true;
}

// inherited from joynr::system::IDiscoverySync
void LocalDiscoveryAggregator::add(joynr::RequestStatus& joynrInternalStatus,
                                   const joynr::types::StdDiscoveryEntry& discoveryEntry)
{
    if (discoveryProxy == NULL) {
        joynrInternalStatus.setCode(RequestStatusCode::ERROR);
        joynrInternalStatus.addDescription(
                QString("LocalDiscoveryAggregator: discoveryProxy not set. Couldn't reach "
                        "local capabilitites directory."));
        return;
    }

    discoveryProxy->add(joynrInternalStatus, discoveryEntry);
}

void LocalDiscoveryAggregator::checkForLocalAvailabilityAndAddInProcessConnection(
        joynr::types::StdDiscoveryEntry& discoveryEntry)
{
    std::string participantId(discoveryEntry.getParticipantId());
    if (requestCallerDirectory.containsRequestCaller(participantId)) {
        std::vector<joynr::types::StdCommunicationMiddleware::Enum> connections(
                discoveryEntry.getConnections());
        connections.insert(
                connections.begin(), joynr::types::StdCommunicationMiddleware::IN_PROCESS);
        discoveryEntry.setConnections(connections);
    }
}

// inherited from joynr::system::IDiscoverySync
void LocalDiscoveryAggregator::lookup(joynr::RequestStatus& joynrInternalStatus,
                                      std::vector<joynr::types::StdDiscoveryEntry>& result,
                                      const std::string& domain,
                                      const std::string& interfaceName,
                                      const joynr::types::StdDiscoveryQos& discoveryQos)
{
    if (discoveryProxy == NULL) {
        joynrInternalStatus.setCode(RequestStatusCode::ERROR);
        joynrInternalStatus.addDescription(
                QString("LocalDiscoveryAggregator: discoveryProxy not set. Couldn't reach "
                        "local capabilitites directory."));
        return;
    }
    discoveryProxy->lookup(joynrInternalStatus, result, domain, interfaceName, discoveryQos);

    for (joynr::types::StdDiscoveryEntry& discoveryEntry : result) {
        checkForLocalAvailabilityAndAddInProcessConnection(discoveryEntry);
    }
}

// inherited from joynr::system::IDiscoverySync
void LocalDiscoveryAggregator::lookup(joynr::RequestStatus& joynrInternalStatus,
                                      joynr::types::StdDiscoveryEntry& result,
                                      const std::string& participantId)
{
    if (provisionedDiscoveryEntries.contains(participantId)) {
        joynrInternalStatus.setCode(RequestStatusCode::OK);
        result = joynr::types::DiscoveryEntry::createStd(
                provisionedDiscoveryEntries.value(participantId));
    } else {
        if (discoveryProxy == NULL) {
            joynrInternalStatus.setCode(RequestStatusCode::ERROR);
            joynrInternalStatus.addDescription(
                    QString("LocalDiscoveryAggregator: discoveryProxy not set. Couldn't reach "
                            "local capabilitites directory."));
            return;
        }
        discoveryProxy->lookup(joynrInternalStatus, result, participantId);
    }
    checkForLocalAvailabilityAndAddInProcessConnection(result);
}

// inherited from joynr::system::IDiscoverySync
void LocalDiscoveryAggregator::remove(joynr::RequestStatus& joynrInternalStatus,
                                      const std::string& participantId)
{
    if (discoveryProxy == NULL) {
        joynrInternalStatus.setCode(RequestStatusCode::ERROR);
        joynrInternalStatus.addDescription(
                QString("LocalDiscoveryAggregator: discoveryProxy not set. Couldn't reach "
                        "local capabilitites directory."));
        return;
    }
    discoveryProxy->remove(joynrInternalStatus, participantId);
}

} // namespace joynr
