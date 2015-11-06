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
package org.polymap.p4.imports.utils;

import static java.nio.charset.Charset.forName;

import java.nio.charset.Charset;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class CharsetPromptBuilder
        implements PromptUIBuilder {

    private ICharSetAware charSetProvider;


    public CharsetPromptBuilder( ICharSetAware charSetProvider ) {
        this.charSetProvider = charSetProvider;
    }

    /** Allowed charsets. */
    public static final Charset[] CHARSETS = { forName( "UTF-8" ), forName( "ISO-8859-1" ), forName( "IBM437" ) };

    private Charset               charset  = null;


    @Override
    public void submit( ImporterPrompt prompt ) {
        charSetProvider.setCharset( charset );
        prompt.ok.set( true );
        prompt.value.put( charset.displayName() );
    }


    @Override
    public void createContents( ImporterPrompt prompt, Composite parent ) {
        for (Charset cs : CHARSETS) {
            Button btn = new Button( parent, SWT.RADIO );
            btn.setText( cs.displayName() );
            btn.addSelectionListener( new SelectionAdapter() {

                @Override
                public void widgetSelected( SelectionEvent ev ) {
                    charset = cs;
                }
            } );
            if (charSetProvider.getCharset() == null) {
                btn.setSelection( cs == CHARSETS[0] );
            }
            else {
                btn.setSelection( cs == charSetProvider.getCharset() );
            }
        }
        charset = CHARSETS[0];
    }
}
