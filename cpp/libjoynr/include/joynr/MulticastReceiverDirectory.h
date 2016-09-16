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
#ifndef MULTICASTRECEIVERDIRECTORY_H
#define MULTICASTRECEIVERDIRECTORY_H

#include <mutex>
#include <string>
#include <unordered_map>
#include <unordered_set>

#include "joynr/PrivateCopyAssign.h"
#include "joynr/Logger.h"

namespace joynr
{

class MulticastReceiverDirectory
{
public:
    MulticastReceiverDirectory() = default;

    virtual ~MulticastReceiverDirectory();

    void registerMulticastReceiver(const std::string& multicastId, const std::string& receiverId);

    bool unregisterMulticastReceiver(const std::string& multicastId, const std::string& receiverId);

    std::unordered_set<std::string> getReceivers(const std::string& multicastId);

    bool contains(const std::string& multicastId);

    bool contains(const std::string& multicastId, const std::string& receiverId);

private:
    DISALLOW_COPY_AND_ASSIGN(MulticastReceiverDirectory);
    ADD_LOGGER(MulticastReceiverDirectory);
    std::unordered_map<std::string, std::unordered_set<std::string>> multicastReceivers;
    std::recursive_mutex mutex;
};

} // namespace joynr

#endif // MULTICASTRECEIVERDIRECTORY_H
