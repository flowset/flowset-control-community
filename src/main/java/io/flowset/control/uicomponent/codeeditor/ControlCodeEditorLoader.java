/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.uicomponent.codeeditor;

import io.jmix.core.Messages;
import io.jmix.flowui.xml.layout.loader.component.CodeEditorLoader;

/**
 * Extends the functionality of CodeEditorLoader by adding a default placeholder for the code editor.
 */
public class ControlCodeEditorLoader extends CodeEditorLoader {
    protected Messages messages;

    @Override
    public void loadComponent() {
        super.loadComponent();
        setDefaultPlaceholder();
    }

    protected void setDefaultPlaceholder() {
        String noValuePlaceholder = getMessages().getMessage("codeEditor.defaultPlaceholder");
        resultComponent.getElement().executeJs("this._editor.setOptions({placeholder: '%s'})".formatted(noValuePlaceholder));
    }

    protected Messages getMessages() {
        if (messages == null) {
            messages = applicationContext.getBean(Messages.class);
        }
        return messages;
    }
}
