/*
 * Copyright 2022 Haulmont.
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
import '@vaadin/button/src/vaadin-button.js';
import '@vaadin/dialog/src/vaadin-dialog.js';

import {Upload} from '@vaadin/upload/src/vaadin-upload.js';

class JmixUpload extends Upload {

    static get is() {
        return 'jmix-upload';
    }

    static get properties() {
        return {
            readOnly: {
                type: Boolean,
                value: false,
            },
            enabled: {
                type: Boolean,
                value: true,
            },
            file: Object,
            jmixI18n: {
                type: Object,
                value: function () {
                    return {
                        uploadDialog: {
                            title: "Uploading",
                            cancelButtonLabel: "Cancel",
                        },
                    }
                }
            }
        };
    }

    ready() {
        super.ready();

        this.$.fileList.hidden = true;

        this.addEventListener("upload-progress", this._onUploadProgressEvent.bind(this));

        this._initUploadDialog();
    }

    static get observers() {
        return [
            '_onEnabledChanged(enabled)',
        ]
    }

    /**
     * @param e
     * @private
     * @override
     */
    _onAddFilesTouchEnd(e) {
        // Don't open add file dialog if component is readOnly or disabled
        if (this.readOnly || !this.enabled) {
            e.stopPropagation();
            e.preventDefault();
            return
        }
        super._onAddFilesTouchEnd(e)
    }

    /**
     * @param e
     * @private
     * @override
     */
    _onAddFilesClick(e) {
        // Don't open add file dialog if component is readOnly or disabled
        if (this.readOnly || !this.enabled) {
            e.stopPropagation();
            e.preventDefault();
            return
        }
        super._onAddFilesClick(e)
    }

    // todo rp add our listeners instead?
    /**
     * @param event
     * @private
     * #override
     */
    _onUploadStart(event) {
        super._onUploadStart(event);
        this._setUploadDialogOpened(true);
    }

    /**
     * @param event
     * @private
     * #override
     */
    _onUploadSuccess(event) {
        super._onUploadSuccess(event);
        this._setUploadDialogOpened(false);
    }

    /**
     * @param event
     * @private
     * #override
     */
    _onUploadError(event) {
        super._onUploadError(event);
        this._setUploadDialogOpened(false);
    }

    /**
     * @param event
     * @private
     * #override
     */
    _onFileReject(event) {
        super._onFileReject(event);
        this._setUploadDialogOpened(false);
    }

    /**
     * @param event
     * @private
     * #override
     */
    _onFileAbort(event) {
        super._onFileAbort(event);
        this._setUploadDialogOpened(false);
    }

    _initUploadDialog() {
        const uploadDialog = document.createElement("vaadin-dialog");
        uploadDialog.id = "jmixUploadDialog";
        uploadDialog.headerTitle = this.jmixI18n.uploadDialog.title;
        uploadDialog.noCloseOnOutsideClick = true;
        uploadDialog.noCloseOnEsc = true;
        uploadDialog.className = "jmix-upload-dialog";
        uploadDialog.renderer = this._uploadDialogRenderer();

        this.appendChild(uploadDialog);
    }

    // todo rp aria label
    _uploadDialogRenderer() {
        const uploadContext = this;
        return function (root, dialog) {
            if (root.children && root.children.length > 0) {
                const uploadFileElements = root.getElementsByTagName("vaadin-upload-file");
                if (uploadFileElements.length === 0) {
                    return;
                }
                const uploadFile = uploadFileElements[0];
                // hide control buttons
                const uploadFileButtons = uploadFile.shadowRoot.querySelectorAll("button");
                if (uploadFileButtons.length > 0) {
                    for (const btn of uploadFileButtons) {
                        btn.hidden = true;
                    }
                }

                // 'vaadin-upload-file' does automatically update bounded elements,
                // therefore firstly unset the file, then set new one.
                uploadFile.file = {};
                uploadFile.file = uploadContext.file;
            } else {
                const content = document.createElement("div");
                content.className = "jmix-upload-dialog-content"

                const uploadFile = document.createElement("vaadin-upload-file");
                const cancelBtn = uploadContext._createUploadDialogCancelButton();

                uploadFile.file = uploadContext.file;

                content.appendChild(uploadFile);
                content.appendChild(cancelBtn);

                root.appendChild(content);
            }
        }
    }

    _createUploadDialogCancelButton() {
        const cancelBtn = document.createElement("vaadin-button");
        cancelBtn.className = "jmix-upload-dialog-cancel-button";
        cancelBtn.innerText = this.jmixI18n.uploadDialog.cancelButtonLabel;
        cancelBtn.addEventListener("click", this._onUploadDialogCancelButtonClick.bind(this));
        return cancelBtn;
    }

    _onUploadDialogCancelButtonClick(event) {
        this.dispatchEvent(new CustomEvent('file-abort', {detail: {file: this.file, xhr: this.file.xhr}}));
    }

    _onEnabledChanged(enabled) {
        // disable upload component
        const uploadComponent = this.shadowRoot.querySelector('slot').children[0];
        if (uploadComponent) {
            if (enabled) {
                uploadComponent.removeAttribute("disabled")
            } else {
                uploadComponent.setAttribute("disabled", "");
            }
        }
    }

    _onUploadProgressEvent(e) {
        this.file = e.detail.file;
        const dialog = this._getUploadDialog();
        if (dialog) {
            dialog.requestContentUpdate();
        }
    }

    _setUploadDialogOpened(opened) {
        const dialog = this._getUploadDialog();
        if (dialog) {
            dialog.opened = opened;
        }
    }

    _getUploadDialog() {
        const dialogs = this.getElementsByTagName("vaadin-dialog")
        if (dialogs.length <= 0) {
            return;
        }
        const dialog = Array.from(dialogs).filter((dialog) => {
            return dialog.id === "jmixUploadDialog";
        });
        return dialog.length > 0 ? dialog[0] : null;
    }
}

customElements.define(JmixUpload.is, JmixUpload);

export {JmixUpload};