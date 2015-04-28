package io.joynr.capabilities.directory;

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

import io.joynr.runtime.AbstractJoynrApplication;
import io.joynr.runtime.JoynrApplication;
import io.joynr.runtime.JoynrApplicationModule;
import io.joynr.runtime.JoynrInjectorFactory;

import java.util.Properties;

import joynr.infrastructure.GlobalCapabilitiesDirectoryAbstractProvider;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class CapabilitiesDirectoryLauncher extends AbstractJoynrApplication {

    private static final String AUTH_TOKEN = "CapabilitiesDirectoryLauncher";
    private static JoynrInjectorFactory injectorFactory;
    @Inject
    private GlobalCapabilitiesDirectoryAbstractProvider capabilitiesDirectoryProvider;

    public static void main(String[] args) {

        start(new Properties());
    }

    public static void start(Properties joynrConfig) {

        // LongPollingMessagingModule is only added in main(), since the servletMessagingModule will be used otherwise
        injectorFactory = new JoynrInjectorFactory(joynrConfig, new CapabilitiesDirectoryModule());

        JoynrApplication capabilitiesDirectoryLauncher = injectorFactory.createApplication(new JoynrApplicationModule("capabilitiesDirectoryLauncher",
                                                                                                                      CapabilitiesDirectoryLauncher.class));
        capabilitiesDirectoryLauncher.run();
    }

    public CapabilitiesDirectoryLauncher() {
    }

    @Override
    public void run() {
        runtime.registerCapability(localDomain, capabilitiesDirectoryProvider, AUTH_TOKEN);
    }

    @Override
    public void shutdown() {
        // no need to send unregister capabilites request to itself

    }

    static Injector getInjector() {
        if (injectorFactory == null) {
            throw new IllegalStateException("start the launcher first by calling start()");
        }

        return injectorFactory.getInjector();
    }
}
