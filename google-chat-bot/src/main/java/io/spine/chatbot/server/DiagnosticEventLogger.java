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

package io.spine.chatbot.server;

import io.spine.base.Identifier;
import io.spine.core.Subscribe;
import io.spine.logging.Logging;
import io.spine.server.event.AbstractEventSubscriber;
import io.spine.system.server.CannotDispatchDuplicateCommand;
import io.spine.system.server.CannotDispatchDuplicateEvent;
import io.spine.system.server.ConstraintViolated;
import io.spine.system.server.HandlerFailedUnexpectedly;
import io.spine.system.server.RoutingFailed;
import io.spine.validate.diags.ViolationText;

import java.util.stream.Collectors;

/**
 * Logs internal diagnostic events to ease applications management.
 */
public final class DiagnosticEventLogger extends AbstractEventSubscriber implements Logging {

    /**
     * Logs entity constraint violation rejection.
     */
    @Subscribe
    void on(ConstraintViolated e) {
        var entity = e.getEntity();
        var violations = e.getViolationList()
                          .stream()
                          .map(ViolationText::of)
                          .map(ViolationText::toString)
                          .collect(Collectors.joining());
        _error().log(
                "Entity `%s` with value `%s` validation constraints are violated. The violations are:%n%s",
                entity.getTypeUrl(), Identifier.toString(entity.id()), violations
        );
    }

    /**
     * Logs duplicate command delivery rejection.
     */
    @Subscribe
    void on(CannotDispatchDuplicateCommand e) {
        var command = e.getDuplicateCommand();
        var entity = e.getEntity();
        _warn().log(
                "Duplicate delivery of the command `%s` with ID `%s` to the entity `%s` with ID `%s` prevented.",
                command.getTypeUrl(), Identifier.toString(command.getId()),
                entity.getTypeUrl(), Identifier.toString(entity.id())
        );
    }

    /**
     * Logs duplicate event delivery rejection.
     */
    @Subscribe
    void on(CannotDispatchDuplicateEvent e) {
        var event = e.getDuplicateEvent();
        var entity = e.getEntity();
        _warn().log(
                "Duplicate delivery of the event `%s` with ID `%s` to the entity `%s` with ID `%s` prevented.",
                event.getTypeUrl(), Identifier.toString(entity.getId()),
                entity.getTypeUrl(), Identifier.toString(entity.id())
        );
    }

    /**
     * Logs unexpected signal handler exception.
     */
    @Subscribe
    void on(HandlerFailedUnexpectedly e) {
        var signal = e.getHandledSignal();
        var entity = e.getEntity();
        var error = e.getError();
        _error().log(
                "Signal `%s` with ID `%s` handler of the entity `%s` with ID `%s` failed with error `%s`.%n%s",
                signal.getTypeUrl(), Identifier.toString(signal.id()),
                entity.getTypeUrl(), Identifier.toString(entity.id()),
                error.getMessage(), error.getStacktrace()
        );
    }

    /**
     * Logs routing failures.
     */
    @Subscribe
    void on(RoutingFailed e) {
        var entityType = e.getEntityType()
                          .getJavaClassName();
        var signal = e.getHandledSignal();
        var error = e.getError();
        _error().log(
                "Signal `%s` with ID `%s` routing to the entity `%s` failed with the error `%s`.%n%s",
                signal.getTypeUrl(), Identifier.toString(signal.id()),
                entityType, error.getMessage(), error.getStacktrace()
        );
    }
}
