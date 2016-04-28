/*******************************************************************************
 * Copyright (c) 2009 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributor:  Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.anyedit.compare;

import java.io.File;

import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IActionFilter;

import de.loskutov.anyedit.ui.editor.AbstractEditor;
import de.loskutov.anyedit.util.EclipseUtils;

/**
 * @author Andrey
 */
public class ContentWrapper implements IActionFilter {

    /** content type for compare */
    public static final String UNKNOWN_CONTENT_TYPE = ITypedElement.UNKNOWN_TYPE; // XXX ? ITypedElement.TEXT_TYPE;

    private final String name;
    private final String extension;
    private IFile ifile;
    private File file;
    private boolean modifiable;
    private ITextSelection selection;

    public ContentWrapper(String name, String fileExtension, AbstractEditor editor) {
        super();
        this.name = name;
        if (editor != null) {
            this.selection = editor.getSelection();
            this.ifile = editor.getIFile();
            this.file = editor.getFile();
        } else {
            this.selection = null;
        }
        if(selection != null && selection.getLength() == 0){
            selection = null;
        }
        this.extension = fileExtension == null? UNKNOWN_CONTENT_TYPE : fileExtension;
        this.modifiable = true;
    }

    public ContentWrapper recreate(){
        ContentWrapper cw = new ContentWrapper(name, extension, null);
        cw.ifile = ifile;
        cw.file = file;
        cw.modifiable = modifiable;
        cw.setSelection(selection);
        return cw;
    }

    private ContentWrapper(IPath path) {
        this(path.lastSegment(), path.getFileExtension(), null);
    }

    private ContentWrapper(IFile file) {
        this(file.getFullPath());
        this.ifile = file;
    }

    private ContentWrapper(File file) {
        this(new Path(file.getAbsolutePath()));
        this.file = file;
    }

    public File getFile() {
        if(file == null && ifile != null){
            IPath location = ifile.getLocation();
            if(location != null){
                return location.toFile();
            }
        }
        return file;
    }

    public IFile getIFile() {
        return ifile;
    }

    public String getName() {
        return name;
    }

    public String getFullName(){
        if(ifile != null){
            IPath path = ifile.getLocation();
            if(path != null){
                return path.toOSString();
            }
        } else if(file != null){
            return file.getAbsolutePath();
        }
        return getName();
    }

    public String getFileExtension() {
        return extension;
    }

    public static ContentWrapper create(AbstractEditor editor1) {
        IDocument document = editor1.getDocument();
        if (document != null) {
            String title = editor1.getTitle();
            String type = editor1.getContentType();
            return new ContentWrapper(title, type, editor1);
        }

        IFile ifile = editor1.getIFile();
        if (ifile != null) {
            if (ifile.getLocation() != null) {
                return new ContentWrapper(ifile);
            }
            return new ContentWrapper(ifile.getFullPath().toFile());
        }

        File file = editor1.getFile();
        if(file != null){
            return new ContentWrapper(file);
        }
        return null;
    }

    public static ContentWrapper create(Object element) {
        if (element instanceof IFile) {
            return new ContentWrapper((IFile) element);
        }
        if (element instanceof File) {
            return new ContentWrapper((File) element);
        }
        if (element instanceof ContentWrapper) {
            return (ContentWrapper) element;
        }
        if (element instanceof AbstractEditor) {
            return create((AbstractEditor) element);
        }
        IFile ifile = EclipseUtils.getIFile(element, true);
        if (ifile != null) {
            return new ContentWrapper(ifile);
        }
        File file = EclipseUtils.getFile(element, true);
        if (file != null) {
            return new ContentWrapper(file);
        }
        ContentWrapper content = EclipseUtils.getAdapter(element, ContentWrapper.class);
        return content;
    }

    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    @Override
    public boolean testAttribute(Object target, String attrName, String value) {
        if(!(target instanceof ContentWrapper)){
            return false;
        }
        if("isModifiable".equals(attrName)){
            ContentWrapper content = (ContentWrapper) target;
            return Boolean.valueOf(value).booleanValue() == content.isModifiable();
        }
        return false;
    }

    public ITextSelection getSelection() {
        return selection;
    }
    public void setSelection(ITextSelection sel) {
        selection = sel;
    }

}
