/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.config.manager.impl.jmx;

import com.google.common.base.Preconditions;
import java.util.Collections;
import javax.management.InstanceAlreadyExistsException;
import javax.management.ObjectName;
import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.opendaylight.controller.config.api.jmx.ObjectNameUtil;
import org.opendaylight.controller.config.api.runtime.RootRuntimeBeanRegistrator;
import org.opendaylight.controller.config.api.runtime.RuntimeBean;

public class RootRuntimeBeanRegistratorImpl implements
        RootRuntimeBeanRegistrator {
    private final InternalJMXRegistrator internalJMXRegistrator;
    private final ModuleIdentifier moduleIdentifier;
    private final ObjectName defaultRuntimeON;

    RootRuntimeBeanRegistratorImpl(final InternalJMXRegistrator internalJMXRegistrator,
            final ModuleIdentifier moduleIdentifier) {
        this.internalJMXRegistrator = Preconditions.checkNotNull(internalJMXRegistrator);
        this.moduleIdentifier = moduleIdentifier;
        defaultRuntimeON = ObjectNameUtil.createRuntimeBeanName(
                moduleIdentifier.getFactoryName(),
                moduleIdentifier.getInstanceName(),
                Collections.<String, String> emptyMap());
    }

    @Override
    public HierarchicalRuntimeBeanRegistrationImpl registerRoot(final RuntimeBean mxBean) {
        try {
            internalJMXRegistrator.registerMBean(mxBean, defaultRuntimeON);
        } catch (final InstanceAlreadyExistsException e) {
            throw sanitize(e, moduleIdentifier, defaultRuntimeON);
        }
        return new HierarchicalRuntimeBeanRegistrationImpl(moduleIdentifier,
                internalJMXRegistrator, Collections.<String, String> emptyMap());
    }

    @Override
    public void close() {
        internalJMXRegistrator.close();
    }

    static IllegalStateException sanitize(final InstanceAlreadyExistsException e,
            final ModuleIdentifier moduleIdentifier, final ObjectName on) {
        throw new IllegalStateException("Could not register runtime bean in "
                + moduleIdentifier + " under name " + on, e);

    }
}
