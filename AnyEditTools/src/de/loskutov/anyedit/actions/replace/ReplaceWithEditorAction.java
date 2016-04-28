/*******************************************************************************
 * Copyright (c) 2009 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributor:  Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.anyedit.actions.replace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.dialogs.ListDialog;

import de.loskutov.anyedit.AnyEditToolsPlugin;
import de.loskutov.anyedit.actions.compare.CompareWithEditorAction;
import de.loskutov.anyedit.actions.compare.CompareWithEditorAction.EditorsContentProvider;
import de.loskutov.anyedit.ui.editor.AbstractEditor;

/**
 * @author Andrey
 *
 */
public class ReplaceWithEditorAction extends ReplaceWithAction {

    public ReplaceWithEditorAction() {
        super();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // restore disabled state and let us re-test enablement
        action.setEnabled(true);
        super.selectionChanged(action, selection);
        if (action.isEnabled()) {
            Object[] elements = new EditorsContentProvider(editor, selectedContent).getElements(null);
            action.setEnabled(elements.length > 0);
        }
    }

    @Override
    protected InputStream createInputStream() {

        ListDialog dialog = CompareWithEditorAction.createSelectionDialog(editor,
                selectedContent, "Select an opened editor to get content from:");

        int result = dialog.open();
        if (result == Window.OK) {
            Object[] objects = dialog.getResult();
            if (objects != null && objects.length == 1
                    && objects[0] instanceof IEditorReference) {
                IEditorReference reference = (IEditorReference) objects[0];
                IEditorPart editorPart = reference.getEditor(true);
                AbstractEditor editor1 = new AbstractEditor(editorPart);
                IFile file = editor1.getIFile();
                if (file != null) {
                    if (file.getLocation() != null) {
                        try {
                            return file.getContents();
                        } catch (CoreException e) {
                            AnyEditToolsPlugin.logError(
                                    "Can't get file content: " + file, e);
                        }
                    }
                    try {
                        return new FileInputStream(file.getFullPath().toFile());
                    } catch (FileNotFoundException e) {
                        AnyEditToolsPlugin.logError("File not found: " + file, e);
                    }
                }

                File localFile = editor1.getFile();
                if (localFile != null) {
                    try {
                        return new FileInputStream(localFile);
                    } catch (FileNotFoundException e) {
                        AnyEditToolsPlugin.logError("File not found: "
                                + localFile, e);
                    }
                } else {
                    IDocument document = editor1.getDocument();
                    if(document != null) {
                        String content = document.get();
                        if (content != null) {
                            try {
                                return new ByteArrayInputStream(content.getBytes(editor1
                                        .computeEncoding()));
                            } catch (UnsupportedEncodingException e) {
                                return new ByteArrayInputStream(content.getBytes());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


}
