/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.chatbot.delivery;

import io.spine.base.Environment;
import io.spine.server.delivery.Delivery;
import io.spine.server.storage.datastore.DatastoreStorageFactory;

/**
 * A utility class for configuring {@link Delivery} for environments.
 */
public interface DeliveryFactory {

    /**
     * Creates a new fully-configured delivery.
     */
    Delivery delivery();

    /**
     * Creates a new instance of a delivery factory for the specified {@code environment}
     * and using the {@code storageFactory} if required.
     */
    static DeliveryFactory instance(Environment environment,
                                    DatastoreStorageFactory storageFactory) {
        if (environment.isProduction()) {
            return DsDeliveryFactory.instance(storageFactory);
        } else {
            return LocalDeliveryFactory.instance;
        }
    }
}
