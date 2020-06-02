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

import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.Repository;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.net.Urls.urlOfSpec;

@DisplayName("Repository should")
public class RepositoryAggregateTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return GitHubContext.newBuilder();
    }

    @Test
    void registerOrganization() {
        var id = RepositoryId
                .newBuilder()
                .setValue("SpineEventEngine/base")
                .vBuild();
        var registerRepository = RegisterRepository
                .newBuilder()
                .setId(id)
                .setGithubUrl(urlOfSpec("https://github.com/SpineEventEngine/base"))
                .setTravisCiUrl(urlOfSpec("https://travis-ci.com/github/SpineEventEngine/base"))
                .setName("Spine base")
                .vBuild();
        context().receivesCommand(registerRepository);

        var expectedState = Repository
                .newBuilder()
                .setId(id)
                .setGithubUrl(urlOfSpec("https://github.com/SpineEventEngine/base"))
                .setTravisCiUrl(urlOfSpec("https://travis-ci.com/github/SpineEventEngine/base"))
                .setName("Spine base")
                .vBuild();
        context().assertState(id, Repository.class)
                 .isEqualTo(expectedState);
    }
}
