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

package io.spine.chatbot.api;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.chat.v1.HangoutsChat;
import com.google.api.services.chat.v1.model.Button;
import com.google.api.services.chat.v1.model.Card;
import com.google.api.services.chat.v1.model.CardHeader;
import com.google.api.services.chat.v1.model.KeyValue;
import com.google.api.services.chat.v1.model.Message;
import com.google.api.services.chat.v1.model.OnClick;
import com.google.api.services.chat.v1.model.OpenLink;
import com.google.api.services.chat.v1.model.Section;
import com.google.api.services.chat.v1.model.Space;
import com.google.api.services.chat.v1.model.TextButton;
import com.google.api.services.chat.v1.model.TextParagraph;
import com.google.api.services.chat.v1.model.Thread;
import com.google.api.services.chat.v1.model.WidgetMarkup;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.chatbot.github.repository.BuildState;
import io.spine.net.Url;
import io.spine.net.Urls;
import io.spine.validate.Validate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

public final class GoogleChatClient {

    private static final String BOT_NAME = "Spine Chat Bot";
    private static final String CHAT_BOT_SCOPE = "https://www.googleapis.com/auth/chat.bot";

    private GoogleChatClient() {
    }

    private static HangoutsChat hangoutsChat() {
        try {
            var keyStream = Files.newInputStream(Paths.get("spine-chat-bot-ea2e6c200084.json"));

            var credentials = GoogleCredentials.fromStream(keyStream)
                                               .createScoped(CHAT_BOT_SCOPE);
            var credentialsAdapter = new HttpCredentialsAdapter(credentials);
            var chat = new HangoutsChat.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credentialsAdapter)
                    .setApplicationName(BOT_NAME)
                    .build();
            return chat;
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Unable to create Hangouts Chat client", e);
        }
    }

    private static List<Space> listSpaces(HangoutsChat chat) {
        try {
            return chat.spaces()
                       .list()
                       .execute()
                       .getSpaces();
        } catch (IOException e) {
            throw new RuntimeException("Unable to retrieve available spaces.", e);
        }
    }

    @CanIgnoreReturnValue
    private static Message sendMessage(HangoutsChat chat, String space, Message message) {
        try {
            return chat.spaces()
                       .messages()
                       .create(space, message)
                       .execute();
        } catch (IOException e) {
            throw new RuntimeException("Unable to send message to space " + space, e);
        }
    }

    private static ImmutableList<Card> cardWith(CardHeader header, List<Section> sections) {
        return ImmutableList.of(new Card().setHeader(header)
                                          .setSections(sections));
    }

    private static Section sectionWithWidget(WidgetMarkup widget) {
        return new Section().setWidgets(List.of(widget));
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

    private static WidgetMarkup textParagraph(String formattedText) {
        return new WidgetMarkup().setTextParagraph(new TextParagraph().setText(formattedText));
    }

    private static Section buildStateSection(BuildState buildState) {
        return sectionWithWidget(buildStateWidget(buildState));
    }

    private static WidgetMarkup buildStateWidget(BuildState buildState) {
        var keyValue = new KeyValue()
                .setTopLabel("Build No.")
                .setContent(buildState.getNumber())
                .setBottomLabel(buildState.getState());
        return new WidgetMarkup().setKeyValue(keyValue);
    }

    private static Button linkButton(String title, Url url) {
        var button = new TextButton().setText(title)
                                     .setOnClick(openLink(url));
        return new Button().setTextButton(button);
    }

    private static OnClick openLink(Url url) {
        return new OnClick().setOpenLink(new OpenLink().setUrl(url.getSpec()));
    }

    private static Message buildStateMessage(BuildState buildState, @Nullable String threadName) {
        Validate.checkValid(buildState);
        var cardHeader = new CardHeader()
                .setTitle(buildState.getRepositorySlug())
                .setImageUrl("https://www.freeiconspng.com/uploads/failure-icon-2.png");
        var sections = ImmutableList.of(
                buildStateSection(buildState),
                commitSection(buildState.getLastCommit()),
                actions(buildState)
        );
        var message = new Message().setCards(cardWith(cardHeader, sections));
        if (!isNullOrEmpty(threadName)) {
            message.setThread(new Thread().setName(threadName));
        }
        return message;
    }

    public static void main(String[] args) {
        var chat = hangoutsChat();
        listSpaces(chat).forEach(System.out::println);
        var commit = BuildState.Commit
                .newBuilder()
                .setCommittedAt("2020-06-03T14:45:02Z")
                .setMessage(
                        "My test commit with a very long description and even\n\n Sub-description, cause it is very relevant and has additional reference to issue\n #749 and a lot of kind words!")
                .setSha("d97c603d5e855d0d211382f78916ad085ba04743")
                .setCompareUrl(Urls.urlOfSpec(
                        "https://github.com/SpineEventEngine/base/commit/d97c603d5e855d0d211382f78916ad085ba04743"))
                .setAuthoredBy("yuri-sergiichuk")
                .vBuild();
        BuildState buildState = BuildState
                .newBuilder()
                .setRepositorySlug("SpineEventEngine/base")
                .setNumber("5292")
                .setState("failed")
                .setLastCommit(commit)
                .setTravisCiUrl(Urls.urlOfSpec(
                        "https://travis-ci.com/github/SpineEventEngine/base/builds/166723382"))
                .setBranch("master")
                .setCreatedBy("yuri-sergiichuk")
                .vBuild();
        Message message = buildStateMessage(buildState, "spaces/AAAAnLxnh_o/threads/TPFMA4dK0_4");
        sendMessage(chat, "spaces/AAAAnLxnh_o", message);
    }
}
