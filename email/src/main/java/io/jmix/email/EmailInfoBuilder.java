/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.email;

import java.io.Serializable;
import java.util.*;

/**
 * EmailInfo builder.
 * <p>
 * Use setters to provide parameters and then invoke the build method to obtain the EmailInfo instance.<br>
 * <p>
 * Sample usage:
 * <pre>
 * EmailInfo emailInfo = EmailInfoBuilder.create()
 *               .setAddresses(addresses)
 *               .setCaption("Email subject")
 *               .setBody("Some email body")
 *               .build();</pre>
 */
public class EmailInfoBuilder {
    private String addresses;
    private String cc;
    private String bcc;
    private String caption;
    private String from;
    private String templatePath;
    private Map<String, Serializable> templateParameters;
    private String body;
    private String bodyContentType;
    private List<EmailAttachment> attachments;
    private List<EmailHeader> headers;

    /**
     * <pre>{@code
     *     EmailInfo emailInfo = EmailInfo.create()
     *          .setAddresses("john.doe@company.com,jane.roe@company.com")
     *          .setCaption("Company news")
     *          .setBody("Some content")
     *          .build();
     * }</pre>
     *
     */
    public static EmailInfoBuilder create() {
        return new EmailInfoBuilder();
    }

    /**
     * <pre>{@code
     *     EmailInfo emailInfo = EmailInfo.create("john.doe@company.com,jane.roe@company.com", "Company news", "Some content").build();
     * }</pre>
     *
     * @param addresses       comma or semicolon separated list of addresses
     * @param caption         email subject
     * @param body            email body
     */
    public static EmailInfoBuilder create(String addresses, String caption, String body) {
        return new EmailInfoBuilder(addresses, caption, body);
    }

    /**
     * INTERNAL
     */
    EmailInfoBuilder(String addresses, String caption, String body) {
        this.addresses = addresses;
        this.caption = caption;
        this.body = body;
    }

    /**
     * INTERNAL
     */
    EmailInfoBuilder() {

    }

    /**
     * @param addresses Recipient email addresses separated with "," or ";" symbol.
     */
    public EmailInfoBuilder setAddresses(String addresses) {
        this.addresses = addresses;
        return this;
    }

    public String getAddresses() {
        return addresses;
    }

    public String getCc() {
        return cc;
    }

    public EmailInfoBuilder setCc(String cc) {
        this.cc = cc;
        return this;
    }

    public String getBcc() {
        return bcc;
    }

    public EmailInfoBuilder setBcc(String bcc) {
        this.bcc = bcc;
        return this;
    }

    public String getCaption() {
        return caption;
    }

    /**
     * @param caption email subject
     */
    public EmailInfoBuilder setCaption(String caption) {
        this.caption = caption;
        return this;
    }

    public String getFrom() {
        return from;
    }

    /**
     * @param from "from" address. If null, a default provided by {@code cuba.email.fromAddress} app property is used.
     */
    public EmailInfoBuilder setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * @param templatePath path to a Freemarker template which is used to create the message body. The template
     *                     is loaded through {@link } in the <b>core</b> module.
     */
    public EmailInfoBuilder setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
        return this;
    }

    public Map<String, Serializable> getTemplateParameters() {
        return templateParameters;
    }

    /**
     * @param templateParameters map of parameters to be passed to the template
     */
    public EmailInfoBuilder setTemplateParameters(Map<String, Serializable> templateParameters) {
        this.templateParameters = templateParameters;
        return this;
    }

    public EmailInfoBuilder addTemplateParameter(String name, Serializable value) {
        if (templateParameters == null) {
            templateParameters = new HashMap<>();
        }
        templateParameters.put(name, value);
        return this;
    }

    public String getBody() {
        return body;
    }

    /**
     * @param body email body
     */
    public EmailInfoBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    public String getBodyContentType() {
        return bodyContentType;
    }

    /**
     * @param bodyContentType email body like "text/plain; charset=UTF-8" or "text/html; charset=UTF-8", etc
     */
    public EmailInfoBuilder setBodyContentType(String bodyContentType) {
        this.bodyContentType = bodyContentType;
        return this;
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    /**
     * @param attachments email attachments
     */
    public EmailInfoBuilder setAttachments(EmailAttachment... attachments) {
        this.attachments = Arrays.asList(attachments);
        return this;
    }

    public EmailInfoBuilder setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public EmailInfoBuilder addAttachment(EmailAttachment attachment) {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }

        attachments.add(attachment);
        return this;
    }

    public List<EmailHeader> getHeaders() {
        return headers;
    }

    public EmailInfoBuilder setHeaders(List<EmailHeader> headers) {
        this.headers = headers;
        return this;
    }

    public EmailInfoBuilder addHeader(EmailHeader header) {
        if (headers == null) {
            headers = new ArrayList<>();
        }
        headers.add(header);
        return this;
    }

    public EmailInfo build() {
        return new EmailInfo(addresses, cc, bcc, caption, from, templatePath, templateParameters,
                body, bodyContentType, headers, attachments);
    }
}
