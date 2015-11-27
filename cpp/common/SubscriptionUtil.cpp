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
#include "joynr/SubscriptionUtil.h"

#include "joynr/QtOnChangeSubscriptionQos.h"
#include "joynr/QtOnChangeWithKeepAliveSubscriptionQos.h"
#include "joynr/QtPeriodicSubscriptionQos.h"
#include "joynr/exceptions/JoynrException.h"

namespace joynr
{

bool SubscriptionUtil::isOnChangeSubscription(QtSubscriptionQos* qos)
{
    return qos->inherits(QtOnChangeSubscriptionQos::staticMetaObject.className()) ||
           qos->inherits(QtOnChangeWithKeepAliveSubscriptionQos::staticMetaObject.className());
}

bool SubscriptionUtil::isOnChangeSubscription(const Variant& qos)
{
    return qos.is<OnChangeWithKeepAliveSubscriptionQos>() || qos.is<OnChangeSubscriptionQos>();
}

qint64 SubscriptionUtil::getAlertInterval(QtSubscriptionQos* qos)
{
    if (qos->inherits(QtPeriodicSubscriptionQos::staticMetaObject.className())) {
        return (qobject_cast<QtPeriodicSubscriptionQos*>(qos))->getAlertAfterInterval();
    }
    if (qos->inherits(QtOnChangeWithKeepAliveSubscriptionQos::staticMetaObject.className())) {
        return (qobject_cast<QtOnChangeWithKeepAliveSubscriptionQos*>(qos))
                ->getAlertAfterInterval();
    }
    return -1;
}

int64_t SubscriptionUtil::getAlertInterval(const Variant& qos)
{
    if (qos.is<OnChangeWithKeepAliveSubscriptionQos>()) {
        const OnChangeWithKeepAliveSubscriptionQos* subscriptionQosPtr =
                &qos.get<OnChangeWithKeepAliveSubscriptionQos>();
        return subscriptionQosPtr->getAlertAfterInterval();
    }
    if (qos.is<PeriodicSubscriptionQos>()) {
        const PeriodicSubscriptionQos* subscriptionQosPtr = &qos.get<PeriodicSubscriptionQos>();
        return subscriptionQosPtr->getAlertAfterInterval();
    }
    return -1;
}

qint64 SubscriptionUtil::getMinInterval(QtSubscriptionQos* qos)
{
    if (qos->inherits(QtOnChangeSubscriptionQos::staticMetaObject.className())) {
        return (qobject_cast<QtOnChangeSubscriptionQos*>(qos))->getMinInterval();
    }
    if (qos->inherits(QtOnChangeWithKeepAliveSubscriptionQos::staticMetaObject.className())) {
        return (qobject_cast<QtOnChangeWithKeepAliveSubscriptionQos*>(qos))->getMinInterval();
    }
    return -1;
}

int64_t SubscriptionUtil::getMinInterval(const Variant& qos)
{
    if (qos.is<OnChangeWithKeepAliveSubscriptionQos>()) {
        const OnChangeWithKeepAliveSubscriptionQos* subscriptionQosPtr =
                &qos.get<OnChangeWithKeepAliveSubscriptionQos>();
        return subscriptionQosPtr->getMinInterval();
    }
    if (qos.is<OnChangeSubscriptionQos>()) {
        const OnChangeSubscriptionQos* subscriptionQosPtr = &qos.get<OnChangeSubscriptionQos>();
        return subscriptionQosPtr->getMinInterval();
    }
    return -1;
}

qint64 SubscriptionUtil::getPeriodicPublicationInterval(QtSubscriptionQos* qos)
{
    if (qos->inherits(QtOnChangeWithKeepAliveSubscriptionQos::staticMetaObject.className())) {
        return (qobject_cast<QtOnChangeWithKeepAliveSubscriptionQos*>(qos))->getMaxInterval();
    }
    if (qos->inherits(QtPeriodicSubscriptionQos::staticMetaObject.className())) {
        return (qobject_cast<QtPeriodicSubscriptionQos*>(qos))->getPeriod();
    }
    return -1;
}

int64_t SubscriptionUtil::getPeriodicPublicationInterval(const Variant& qos)
{
    if (qos.is<OnChangeWithKeepAliveSubscriptionQos>()) {
        const OnChangeWithKeepAliveSubscriptionQos* subscriptionQosPtr =
                &qos.get<OnChangeWithKeepAliveSubscriptionQos>();
        return subscriptionQosPtr->getMaxInterval();
    }
    if (qos.is<PeriodicSubscriptionQos>()) {
        const PeriodicSubscriptionQos* subscriptionQosPtr = &qos.get<PeriodicSubscriptionQos>();
        return subscriptionQosPtr->getPeriod();
    }
    return -1;
}

Variant SubscriptionUtil::getVariant(const SubscriptionQos& qos)
{
    if (dynamic_cast<const OnChangeWithKeepAliveSubscriptionQos*>(&qos) != nullptr) {
        return Variant::make<OnChangeWithKeepAliveSubscriptionQos>(
                static_cast<const OnChangeWithKeepAliveSubscriptionQos&>(qos));
    } else if (dynamic_cast<const OnChangeSubscriptionQos*>(&qos) != nullptr) {
        return Variant::make<OnChangeSubscriptionQos>(
                static_cast<const OnChangeSubscriptionQos&>(qos));
    } else if (dynamic_cast<const PeriodicSubscriptionQos*>(&qos) != nullptr) {
        return Variant::make<PeriodicSubscriptionQos>(
                static_cast<const PeriodicSubscriptionQos&>(qos));
    }

    assert(false);
    throw exceptions::JoynrRuntimeException(
            "Exception in SubscriptionUtil: reference to unknown SubscriptionQos has been sent");
}
}
