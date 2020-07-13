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

package io.spine.chatbot.server.github;

import com.google.common.collect.Sets;
import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.organization.OrganizationRepositories;
import io.spine.chatbot.github.organization.event.OrganizationRegistered;
import io.spine.chatbot.github.repository.event.RepositoryRegistered;
import io.spine.core.Subscribe;
import io.spine.server.projection.Projection;

/**
 * Organization repositories projection.
 *
 * <p>Repositories are only referenced by their identifiers.
 * See {@link io.spine.chatbot.github.repository.Repository Repository} for the details
 * on each repository.
 */
final class OrgReposProjection
        extends Projection<OrganizationId, OrganizationRepositories, OrganizationRepositories.Builder> {

    /**
     * Registers the organization to watch the repositories for.
     */
    @Subscribe
    void on(OrganizationRegistered e) {
        builder().setOrganization(e.getOrganization());
    }

    /**
     * Registers the organization repository.
     */
    @Subscribe
    void on(RepositoryRegistered e) {
        var repositories = Sets.newHashSet(builder().getRepositoryList());
        repositories.add(e.getRepository());
        builder().clearRepository()
                 .addAllRepository(repositories);
    }
}
