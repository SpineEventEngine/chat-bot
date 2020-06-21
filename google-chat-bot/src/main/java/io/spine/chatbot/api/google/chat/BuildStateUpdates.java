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

package io.spine.chatbot.api.google.chat;

import com.google.api.services.chat.v1.model.CardHeader;
import com.google.api.services.chat.v1.model.KeyValue;
import com.google.api.services.chat.v1.model.Message;
import com.google.api.services.chat.v1.model.Section;
import com.google.api.services.chat.v1.model.Thread;
import com.google.api.services.chat.v1.model.WidgetMarkup;
import com.google.common.collect.ImmutableList;
import io.spine.chatbot.github.repository.build.BuildState;
import io.spine.chatbot.google.chat.thread.ThreadResource;
import io.spine.chatbot.server.github.BuildStates;
import io.spine.protobuf.Messages;

import static io.spine.chatbot.api.google.chat.ChatWidgets.cardWith;
import static io.spine.chatbot.api.google.chat.ChatWidgets.linkButton;
import static io.spine.chatbot.api.google.chat.ChatWidgets.sectionWithWidget;
import static io.spine.chatbot.api.google.chat.ChatWidgets.textParagraph;
import static io.spine.validate.Validate.checkValid;

/**
 * A Google Chat utility class that creates {@link BuildState} update messages.
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
     * Creates a new {@link BuildState} update message from the supplied state and thread.
     *
     * <p>If the thread has no name set, assumes that the update message should be
     * sent to a new thread.
     */
    static Message buildStateMessage(BuildState buildState, ThreadResource thread) {
        checkValid(buildState);
        var headerIcon = BuildStates.isFailed(buildState) ? FAILURE_ICON : SUCCESS_ICON;
        var cardHeader = new CardHeader()
                .setTitle(buildState.getRepositorySlug())
                .setImageUrl(headerIcon);
        var sections = ImmutableList.of(
                buildStateSection(buildState),
                commitSection(buildState.getLastCommit()),
                actions(buildState)
        );
        var message = new Message().setCards(cardWith(cardHeader, sections));
        if (Messages.isNotDefault(thread)) {
            message.setThread(new Thread().setName(thread.getName()));
        }
        return message;
    }

    private static Section commitSection(BuildState.Commit commit) {
        var commitInfo = String.format(
                "Authored by <b>%s</b> at %s.", commit.getAuthoredBy(), commit.getCommittedAt()
        );
        var section = new Section()
                .setHeader("Commit " + commit.getSha())
                .setWidgets(ImmutableList.of(
                        textParagraph(commit.getMessage()),
                        textParagraph(commitInfo)
                ));
        return section;
    }

    private static Section actions(BuildState buildState) {
        BuildState.Commit commit = buildState.getLastCommit();
        WidgetMarkup actionButtons = new WidgetMarkup().setButtons(ImmutableList.of(
                linkButton("Open build", buildState.getTravisCiUrl()),
                linkButton("Open changeset", commit.getCompareUrl())
        ));
        return sectionWithWidget(actionButtons);
    }

    private static Section buildStateSection(BuildState buildState) {
        return sectionWithWidget(buildStateWidget(buildState));
    }

    private static WidgetMarkup buildStateWidget(BuildState buildState) {
        var keyValue = new KeyValue()
                .setTopLabel("Build No.")
                .setContent(buildState.getNumber())
                .setBottomLabel(capitalizeState(buildState.getState()));
        return new WidgetMarkup().setKeyValue(keyValue);
    }

    private static String capitalizeState(BuildState.State state) {
        var name = state.name();
        return name.charAt(0) + name.toLowerCase();
    }
}
