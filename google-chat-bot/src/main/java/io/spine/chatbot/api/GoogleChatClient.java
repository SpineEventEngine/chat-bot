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
import com.google.api.services.chat.v1.model.Message;
import com.google.api.services.chat.v1.model.OnClick;
import com.google.api.services.chat.v1.model.OpenLink;
import com.google.api.services.chat.v1.model.Section;
import com.google.api.services.chat.v1.model.TextButton;
import com.google.api.services.chat.v1.model.TextParagraph;
import com.google.api.services.chat.v1.model.Thread;
import com.google.api.services.chat.v1.model.WidgetMarkup;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.spine.chatbot.github.repository.BuildState;
import io.spine.net.Url;
import io.spine.net.Urls;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public final class GoogleChatClient {

    private static final String BOT_NAME = "Spine Chat Bot";
    private static final String CHAT_BOT_SCOPE = "https://www.googleapis.com/auth/chat.bot";

    private GoogleChatClient() {
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
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
        chat.spaces()
            .list()
            .execute()
            .getSpaces()
            .forEach(System.out::println);
        var commit = BuildState.Commit
                .newBuilder()
                .setMessage("My test commit")
                .setSha("d97c603d5e855d0d211382f78916ad085ba04743")
                .setCompareUrl(
                        "https://github.com/SpineEventEngine/base/commit/d97c603d5e855d0d211382f78916ad085ba04743")
                .vBuild();
        var message = new Message()
                .setThread(new Thread().setName("spaces/AAAAnLxnh_o/threads/TPFMA4dK0_4"))
                .setCards(ImmutableList.of(
                        new Card()
                                .setHeader(new CardHeader().setTitle("SpineEventEngine/base"))
                                .setSections(ImmutableList.of(
                                        new Section()
                                                .setHeader("Build failed in master.")
                                                .setWidgets(ImmutableList.of(
                                                        new WidgetMarkup()
                                                                .setTextParagraph(
                                                                        new TextParagraph().setText(
                                                                                "First text paragraph")),
                                                        commitInfo(commit),
                                                        buttons(
                                                                linkButton("Build status",
                                                                           Urls.urlOfSpec(
                                                                                   "https://travis-ci.com/github/SpineEventEngine/base/builds/169043750")),
                                                                linkButton("Changeset",
                                                                           Urls.urlOfSpec(
                                                                                   "https://github.com/SpineEventEngine/base/compare/8a042f66b8c9...d97c603d5e85"
                                                                           )))
                                                            )
                                                )
                                             )
                                ))
                )
//                .setPreviewText("This is a test.")
                .setText(namedLink("https://google.com", "GOOGLE!"));
        System.out.println(chat.spaces()
                               .messages()
                               .create("spaces/AAAAnLxnh_o", message)
                               .execute());
    }

    private static CardHeader cardHeader(String title, String subTitle) {
        return new CardHeader().setTitle(title)
                               .setSubtitle(subTitle);
    }

    private static WidgetMarkup commitInfo(BuildState.Commit commit) {
        var message = commit.getMessage();
        var sha = commit.getSha();
        var url = commit.getCompareUrl();
        var paragraphMessage = new StringBuilder("Commit ")
                .append('`')
                .append(sha)
                .append('`')
                .append(" with message ")
                .append('`')
                .append(message)
                .append('`')
                .append(" has broken the build.")
                .append('\n')
                .append("The diff is available ")
                .append(namedLink(url, "here"))
                .append('.')
                .toString();
        return new WidgetMarkup().setTextParagraph(new TextParagraph().setText(paragraphMessage));
    }

    private static String namedLink(String url, String title) {
        return "<a href=\"" + url + "\">" + title + "</a>";
    }

    private static WidgetMarkup buttons(Button first, Button... rest) {
        return new WidgetMarkup().setButtons(Lists.asList(first, rest));
    }

    private static Button linkButton(String title, Url url) {
        return new Button().setTextButton(new TextButton().setText(title)
                                                          .setOnClick(openLink(url)));
    }

    private static OnClick openLink(Url url) {
        return new OnClick().setOpenLink(new OpenLink().setUrl(url.getSpec()));
    }
}
