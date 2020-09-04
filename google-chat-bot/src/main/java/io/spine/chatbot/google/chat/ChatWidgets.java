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

package io.spine.chatbot.google.chat;

import com.google.api.services.chat.v1.model.Button;
import com.google.api.services.chat.v1.model.Card;
import com.google.api.services.chat.v1.model.CardHeader;
import com.google.api.services.chat.v1.model.OnClick;
import com.google.api.services.chat.v1.model.OpenLink;
import com.google.api.services.chat.v1.model.Section;
import com.google.api.services.chat.v1.model.TextButton;
import com.google.api.services.chat.v1.model.TextParagraph;
import com.google.api.services.chat.v1.model.WidgetMarkup;
import io.spine.net.Url;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Provides building blocks to empower the rich messages sent to Google Chat.
 *
 * @see <a href="https://developers.google.com/hangouts/chat/reference/message-formats/cards">
 *         Google Chat Cards</a>
 */
final class ChatWidgets {

    /**
     * Prevents instantiation of this utility class.
     */
    private ChatWidgets() {
    }

    /**
     * Creates a new button with an on-click open link action with the specified {@code title}
     * and {@code url} to open upon a click.
     */
    static Button linkButton(String title, Url url) {
        checkNotEmptyOrBlank(title);
        checkNotNull(url);
        var button = new TextButton()
                .setText(title)
                .setOnClick(openLink(url));
        return new Button().setTextButton(button);
    }

    private static OnClick openLink(Url url) {
        return new OnClick().setOpenLink(new OpenLink().setUrl(url.getSpec()));
    }

    /**
     * Creates a singleton card list with a new {@link Card} with a specified {@code header}
     * and {@code sections}.
     */
    static List<Card> cardWith(CardHeader header, List<Section> sections) {
        checkNotNull(header);
        checkNotNull(sections);
        var result = new Card()
                .setHeader(header)
                .setSections(sections);
        return newArrayList(result);
    }

    /**
     * Creates a new {@link Section} with a single {@code widget}.
     */
    static Section sectionWithWidget(WidgetMarkup widget) {
        checkNotNull(widget);
        return new Section().setWidgets(newArrayList(widget));
    }

    /**
     * Creates a new {@link TextParagraph} widget with the supplied {@code formattedText}.
     *
     * @see <a href="https://developers.google.com/hangouts/chat/reference/message-formats/cards#card_text_formatting">
     *         Card text formatting</a>
     */
    static WidgetMarkup textParagraph(String formattedText) {
        checkNotEmptyOrBlank(formattedText);
        return new WidgetMarkup().setTextParagraph(new TextParagraph().setText(formattedText));
    }
}
