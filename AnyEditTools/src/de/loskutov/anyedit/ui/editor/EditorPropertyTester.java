/*******************************************************************************
 * Copyright (c) 2009 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributor:  Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.anyedit.ui.editor;

import java.lang.reflect.Method;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBookView;

import de.loskutov.anyedit.AnyEditToolsPlugin;
import de.loskutov.anyedit.IAnyEditConstants;
import de.loskutov.anyedit.util.EclipseUtils;

public class EditorPropertyTester extends PropertyTester {

    public EditorPropertyTester() {
        super();
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if(receiver instanceof IWorkbenchPart) {
            if ("hasModifiableDocument".equals(property)) {
                return hasModifiableDocument((IWorkbenchPart)receiver, args, expectedValue);
            }
            if ("hasDocument".equals(property)) {
                return hasDocument((IWorkbenchPart)receiver, args, expectedValue);
            }
            if ("showOpenType".equals(property)) {
                return showOpenType((IWorkbenchPart)receiver, args, expectedValue);
            }
        }
        return false;
    }

    private static boolean showOpenType(IWorkbenchPart receiver, Object[] args, Object expectedValue) {
        boolean hide = !EclipseUtils.hasJDT() || AnyEditToolsPlugin.getDefault().getPreferenceStore().getBoolean(
                IAnyEditConstants.HIDE_OPEN_TYPE_ACTION);
        return !hide;
    }

    private static boolean hasModifiableDocument(IWorkbenchPart part, Object[] args,
            Object expectedValue) {
        if(!(part instanceof IEditorPart)){
            return false;
        }
        AbstractEditor ae = new AbstractEditor((IEditorPart) part);
        return ae.isEditorInputModifiable() && ae.getDocument() != null;
    }

    private static boolean hasDocument(IWorkbenchPart part, Object[] args, Object expectedValue) {
        if(part instanceof IEditorPart){
            AbstractEditor ae = new AbstractEditor(part);
            return ae.getDocument() != null;
        }
        if(!(part instanceof IViewPart)){
            return false;
        }
        IViewPart vp =(IViewPart) part;
        if (vp instanceof PageBookView) {
            IPage page = ((PageBookView) vp).getCurrentPage();
            ITextViewer viewer = getViewer(page);
            return viewer != null && viewer.getDocument() != null;
        }
        TextViewer viewer = (TextViewer) vp.getAdapter(TextViewer.class);
        if(viewer != null){
            return viewer.getDocument() != null;
        }
        ISelectionProvider sp = vp.getViewSite().getSelectionProvider();
        if(sp instanceof ITextViewer){
            return ((ITextViewer) sp).getDocument() != null;
        }
        return false;
    }

    public static ITextViewer getViewer(IPage page) {
        if(page == null){
            return null;
        }
        if(page instanceof TextConsolePage) {
            return ((TextConsolePage)page).getViewer();
        }
        if(page.getClass().equals(MessagePage.class)){
            // empty page placeholder
            return null;
        }
        try {
            /*
             * org.eclipse.cdt.internal.ui.buildconsole.BuildConsolePage does not
             * extend TextConsolePage, so we get access to the viewer with dirty tricks
             */
            Method method = page.getClass().getDeclaredMethod("getViewer", null);
            method.setAccessible(true);
            if(ITextViewer.class.isAssignableFrom(method.getReturnType())) {
                return (ITextViewer) method.invoke(page, null);
            }
        } catch (Exception e) {
            // AnyEditToolsPlugin.logError("Can't get page viewer from the console page", e);
        }
        return null;
    }

}
