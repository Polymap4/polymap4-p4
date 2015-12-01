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

import static org.polymap.rhei.batik.toolkit.md.dp.dp;

import java.util.EventObject;
import java.util.function.Consumer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.style.color.IColorInfo;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.form.batik.BatikFormContainer;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class FontPanel
        extends DefaultPanel {

    public static final PanelIdentifier ID = PanelIdentifier.parse( "font" );

    private final FontDAO               fontDao;

    private MdToolkit                   toolkit;

    @Scope(P4Plugin.Scope)
    private Context<IFontInfo>          fontInfo;

    @Scope(P4Plugin.Scope)
    private Context<IColorInfo>         colorInfo;

    private FontPage                    fontPage;

    private BatikFormContainer          fontPageContainer;

    private Label                       lblPreview;

    private final Consumer<Object>      callback;


    public FontPanel() {
        fontDao = new FontDAO();
        callback = ( Object object ) -> updatePreview( fontDao );
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.rhei.batik.IPanel#createContents(org.eclipse.swt.widgets.Composite
     * )
     */
    @Override
    public void createContents( Composite panelBody ) {
        getSite().setTitle( "Font Selection" );
        toolkit = (MdToolkit)getSite().toolkit();
        panelBody.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        prepareOpen( panelBody );
    }


    protected void prepareOpen( Composite parent ) {
        createControls( parent );
        updatePreview( fontDao );
    }


    private void createControls( Composite parent ) {
        fontPage = new FontPage( getContext(), getSite(), fontDao, colorInfo, callback );
        fontPageContainer = new BatikFormContainer( fontPage );
        Composite formContainer = toolkit.createComposite( parent, SWT.NONE );

        fontPageContainer.createContents( formContainer );
        Composite previewArea = createPreviewArea( parent );

        Button applyButton = createApplyButton( parent );

        FormDataFactory.on( formContainer ).left( 0 ).right( 100 );
        FormDataFactory.on( previewArea ).top( formContainer ).height( 300 ).left( 0 ).right( 100 );
        FormDataFactory.on( applyButton ).top( previewArea, dp( 30 ).pix() ).right( 100 );
    }


    private Composite createPreviewArea( Composite parent ) {
        Composite previewArea = toolkit.createComposite( parent, SWT.BORDER );
        previewArea.setLayout( new GridLayout() );
        lblPreview = toolkit.createLabel( previewArea, "Font preview", SWT.CENTER );
        lblPreview.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, true ) );
        Color bgColor = parent.getDisplay().getSystemColor( SWT.COLOR_LIST_BACKGROUND );
        previewArea.setBackground( bgColor );
        previewArea.setBackgroundMode( SWT.INHERIT_DEFAULT );
        return previewArea;
    }


    private void updatePreview( FontDAO fontDao ) {
        try {
            fontPageContainer.submit(new NullProgressMonitor());
            if (lblPreview != null) {
                Display display = lblPreview.getDisplay();
                Font font = new Font( display, fontDao.getFontData() );
                lblPreview.setFont( font );
                Color color = new Color( display, fontDao.getFontColor() );
                lblPreview.setForeground( color );
                lblPreview.getParent().layout( true );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Button createApplyButton( Composite comp ) {
        Button applyButton = toolkit.createButton( comp, "Apply selection", SWT.PUSH );
        applyButton.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    fontPageContainer.submit(new NullProgressMonitor());
                    fontInfo.get().setColor( fontDao.getFontColor() );
                    fontInfo.get().setFontData( new FontData[] { fontDao.getFontData() } );
                    PanelPath path = getSite().getPath();
                    getContext().closePanel( path );
                    EventManager.instance().publish( new EventObject( fontInfo.get() ) );
                }
                catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        } );
        return applyButton;
    }
}
