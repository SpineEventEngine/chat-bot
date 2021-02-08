/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.chatbot.google.chat;

import com.google.api.services.chat.v1.model.CardHeader;
import com.google.api.services.chat.v1.model.KeyValue;
import com.google.api.services.chat.v1.model.Message;
import com.google.api.services.chat.v1.model.Section;
import com.google.api.services.chat.v1.model.Thread;
import com.google.api.services.chat.v1.model.WidgetMarkup;
import io.spine.chatbot.github.repository.build.Build;
import io.spine.chatbot.google.chat.thread.ThreadResource;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static io.spine.chatbot.google.chat.ChatWidgets.cardWith;
import static io.spine.chatbot.google.chat.ChatWidgets.linkButton;
import static io.spine.chatbot.google.chat.ChatWidgets.sectionWithWidget;
import static io.spine.chatbot.google.chat.ChatWidgets.textParagraph;
import static io.spine.protobuf.Messages.isNotDefault;
import static io.spine.validate.Validate.checkValid;

/**
 * A utility class that creates {@link Build} update messages.
 *
 * @implNote Chat entities rely on the {@code clone()} functionality internally, so we're
 *         intentionally using mutable collections while building up Chat messages.
 */
final class BuildStateUpdates {

    private static final String FAILURE_ICON =
            "https://storage.googleapis.com/spine-chat-bot.appspot.com/failure-icon.png";
    private static final String SUCCESS_ICON =
            "https://storage.googleapis.com/spine-chat-bot.appspot.com/success-icon.png";

    /**
     * Prevents instantiation of this utility class.
     */
    private BuildStateUpdates() {
    }

    /**
     * Creates a new {@link Build} update message from the supplied {@code build}
     * and {@code thread}.
     *
     * <p>If the thread has no name set, assumes that the update message should be
     * sent to a new thread.
     */
    static Message buildStateMessage(Build build, ThreadResource thread) {
        checkValid(build);
        checkNotNull(thread);
        var headerIcon = build.failed() ? FAILURE_ICON : SUCCESS_ICON;
        var cardHeader = new CardHeader()
                .setTitle(build.getRepository()
                               .value())
                .setImageUrl(headerIcon);
        var sections = newArrayList(
                buildStateSection(build),
                commitSection(build),
                actions(build)
        );
        var message = new Message().setCards(cardWith(cardHeader, sections));
        if (isNotDefault(thread)) {
            message.setThread(new Thread().setName(thread.getName()));
        }
        return message;
    }

    private static Section commitSection(Build build) {
        var commit = build.getLastCommit();
        var commitInfo = String.format(
                "Authored by <b>%s</b> at %s.", commit.getAuthoredBy(), commit.getCommittedAt()
        );
        var section = new Section()
                .setHeader("Commit " + commit.getSha())
                .setWidgets(newArrayList(
                        textParagraph(commit.getMessage()),
                        textParagraph(commitInfo)
                ));
        return section;
    }

    private static Section actions(Build build) {
        var commit = build.getLastCommit();
        var actionButtons = new WidgetMarkup().setButtons(newArrayList(
                linkButton("Build", build.getTravisCiUrl()),
                linkButton("Changeset", commit.getCompareUrl())
        ));
        return sectionWithWidget(actionButtons);
    }

    private static Section buildStateSection(Build build) {
        return sectionWithWidget(buildStateWidget(build));
    }

    private static WidgetMarkup buildStateWidget(Build build) {
        var keyValue = new KeyValue()
                .setTopLabel("Build No.")
                .setContent(build.getNumber())
                .setBottomLabel(build.stateLabel());
        return new WidgetMarkup().setKeyValue(keyValue);
    }
}
