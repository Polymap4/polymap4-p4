/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.style.font;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.p4.style.color.ColorInfo;
import org.polymap.p4.style.color.ColorPanel;
import org.polymap.p4.style.color.IColorInfo;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.BeanPropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.ColorFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.SpinnerFormField;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class FontPage
        extends DefaultFormPage
        implements IFormFieldListener {

    /**
     * 
     */
    private static final RGB DEFAULT_FONT_COLOR = new RGB( 0, 0, 0 );

    private final IAppContext         context;

    private final IPanelSite          panelSite;

    private final FontDAO             fontDAO;

    private ColorFormField            fontColorField;

    private final Context<IColorInfo> colorInfoInContext;

    private final Consumer<Object>    callback;


    public FontPage( IAppContext context, IPanelSite panelSite, FontDAO fontDAO, Context<IColorInfo> colorInfoInContext,
            Consumer<Object> callback ) {
        this.context = context;
        this.panelSite = panelSite;
        this.fontDAO = fontDAO;
        this.colorInfoInContext = colorInfoInContext;
        this.callback = callback;
        
        ColorInfo colorInfo = new ColorInfo();
        colorInfoInContext.set( colorInfo );
        
        EventManager.instance().subscribe( colorInfo, ev -> ev.getSource() instanceof IColorInfo );
    }


    private IPanelSite getPanelSite() {
        return panelSite;
    }


    public FontDAO getFontDao() {
        return fontDAO;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.rhei.form.DefaultFormPage#createFormContents(org.polymap.rhei.
     * form.IFormPageSite)
     */
    @Override
    public void createFormContents( IFormPageSite site ) {
        super.createFormContents( site );

        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( getPanelSite().getLayoutPreference().getSpacing() / 2 ).create() );
        initializeDefaults( parent );

        List<String> availableFontNames = getAvailableFonts( parent );
        PicklistFormField picklistFormField = new PicklistFormField( availableFontNames );
        if (getFontDao().getFontFamily() == null && availableFontNames.size() > 0) {
            getFontDao().setFontFamily( availableFontNames.get( 0 ) );
        }
        site.newFormField( new BeanPropertyAdapter( getFontDao(), FontDAO.FONT_FAMILY ) ).label.put( "Font family" ).field
                .put( picklistFormField ).tooltip.put( "" ).create();

        SpinnerFormField fontSizeField = new SpinnerFormField( 0, 200, 12 );
        site.newFormField( new BeanPropertyAdapter( getFontDao(), FontDAO.FONT_SIZE ) ).label.put( "Font size" ).field
                .put( fontSizeField ).tooltip.put( "" ).create();

        CheckboxFormField fontBoldField = new CheckboxFormField();
        site.newFormField( new BeanPropertyAdapter( getFontDao(), FontDAO.FONT_BOLD ) ).label.put( "Font bold" ).field
                .put( fontBoldField ).tooltip.put( "" ).create();

        CheckboxFormField fontItalicField = new CheckboxFormField();
        site.newFormField( new BeanPropertyAdapter( getFontDao(), FontDAO.FONT_ITALIC ) ).label.put( "Font italic" ).field
                .put( fontItalicField ).tooltip.put( "" ).create();

        fontColorField = new ColorFormField() {
            /* (non-Javadoc)
             * @see org.polymap.rhei.field.ColorFormField#setValue(java.lang.Object)
             */
            @Override
            public IFormField setValue( Object value ) {
                IFormField field = super.setValue( value );
                callback.accept( value );
                return field;
            }
        };
        if (colorInfoInContext.get().getColor() != null) {
            fontColorField.setValue( colorInfoInContext.get().getColor() );
        } else if(getFontDao().getFontColor() != null) {
            fontColorField.setValue( getFontDao().getFontColor() );
        }
        site.newFormField( new BeanPropertyAdapter( getFontDao(), FontDAO.FONT_COLOR ) ).label.put( "Font color" ).field
                .put( fontColorField ).tooltip.put( "" ).create();

        site.addFieldListener( this );
    }


    private void initializeDefaults( Composite parent ) {
        if (getFontDao().getFontData() == null) {
            FontData systemFontData = parent.getDisplay().getSystemFont().getFontData()[0];
            String fontName = getFirstFontName( systemFontData.getName() );
            getFontDao().setFontFamily( fontName );
            int fontHeight = systemFontData.getHeight();
            getFontDao().setFontSize( fontHeight );
            int fontStyle = systemFontData.getStyle();
            if ((fontStyle & SWT.BOLD) != 0) {
                getFontDao().setFontBold( true );
            }
            if ((fontStyle & SWT.ITALIC) != 0) {
                getFontDao().setFontItalic( true );
            }
            getFontDao().setFontData( new FontData( fontName, fontHeight, fontStyle ) );
        }
        if (getFontDao().getFontColor() == null) {
            getFontDao().setFontColor( DEFAULT_FONT_COLOR );
        }
    }


    static String getFirstFontName( String fontName ) {
        String result = fontName;
        int index = result.indexOf( ',' );
        if (index != -1) {
            result = result.substring( 0, index );
        }
        result = result.trim();
        if (result.length() > 2) {
            char firstChar = result.charAt( 0 );
            char lastChar = result.charAt( result.length() - 1 );
            boolean isQuoted = (firstChar == '\'' && lastChar == '\'') || (firstChar == '"' && lastChar == '"');
            if (isQuoted) {
                result = result.substring( 1, result.length() - 1 );
            }
        }
        return result;
    }


    private List<String> getAvailableFonts( Composite parent ) {
        Collection<String> fontFamilies = new HashSet<String>();
        FontData[] fontList = parent.getDisplay().getFontList( null, true );
        if (fontList != null) {
            for (int i = 0; i < fontList.length; i++) {
                fontFamilies.add( fontList[i].getName() );
            }
        }
        String[] availableFontNames = fontFamilies.toArray( new String[fontFamilies.size()] );
        Arrays.sort( availableFontNames );
        return Arrays.asList( availableFontNames );
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.rhei.field.IFormFieldListener#fieldChange(org.polymap.rhei.field
     * .FormFieldEvent)
     */
    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == VALUE_CHANGE) {
            if (ev.getSource() == fontColorField) {
                colorInfoInContext.get().setFormField( fontColorField );
                colorInfoInContext.get().setColor( getFontDao().getFontColor() );
                context.openPanel( panelSite.getPath(), ColorPanel.ID );
            } else {
                callback.accept( ev.getSource() );
            }
        }
        // if (ev.getEventCode() == VALUE_CHANGE) {
        // Button okBtn = getButton( IDialogConstants.OK_ID );
        // if (okBtn != null) {
        // okBtn.setEnabled( pageContainer.isValid() && pageContainer.isValid() );
        // }
        // }
    }
}
