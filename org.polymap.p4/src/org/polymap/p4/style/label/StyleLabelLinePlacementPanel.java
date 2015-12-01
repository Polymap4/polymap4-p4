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
package org.polymap.p4.style.label;

import static org.polymap.rhei.batik.toolkit.md.dp.dp;

import java.util.EventObject;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.model2.Property;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.style.entities.StyleLabelLinePlacement;
import org.polymap.p4.style.ui.label.StyleLabelLinePlacementUI;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.batik.BatikFormContainer;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelLinePlacementPanel
        extends DefaultPanel {

    public static final PanelIdentifier ID = PanelIdentifier.parse( "line_placement" );

    private MdToolkit                   toolkit;

    @Scope(P4Plugin.Scope)
    private Context<IStyleLabelInfo>    styleLabelInfo;

    private DefaultFormPage             linePlacementPage;

    private BatikFormContainer          linePlacementPageContainer;


    @Override
    public void createContents( Composite panelBody ) {
        getSite().setTitle( "Style Label Line Placement" );
        toolkit = (MdToolkit)getSite().toolkit();
        panelBody.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        createControls( panelBody );
    }


    private void createControls( Composite parent ) {
        StyleLabelLinePlacementUI ui = new StyleLabelLinePlacementUI( getContext(), getSite() );
        linePlacementPage = new DefaultFormPage() {

            @Override
            public void createFormContents( IFormPageSite formSite ) {
                IStyleLabelInfo info = styleLabelInfo.get();
                Property<StyleLabelLinePlacement> linePlacementProp = info.getStyleLabel().linePlacement;
                StyleLabelLinePlacement linePlacement = null;
                if(linePlacementProp.get() == null) {
                    linePlacement = linePlacementProp.createValue( null );
                }
                ui.setModel( linePlacement );
                ui.createContents( formSite );
            }
        };
        linePlacementPageContainer = new BatikFormContainer( linePlacementPage );
        Composite formContainer = toolkit.createComposite( parent, SWT.NONE );

        linePlacementPageContainer.createContents( formContainer );

        Button applyButton = createApplyButton( parent );

        FormDataFactory.on( formContainer ).left( 0 ).right( 100 );
        FormDataFactory.on( applyButton ).top( formContainer, dp( 30 ).pix() ).right( 100 );
    }


    private Button createApplyButton( Composite comp ) {
        Button applyButton = toolkit.createButton( comp, "Apply selection", SWT.PUSH );
        applyButton.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    linePlacementPageContainer.submit(new NullProgressMonitor());
                    PanelPath path = getSite().getPath();
                    getContext().closePanel( path );
                    EventManager.instance().publish( new EventObject( styleLabelInfo.get() ) );
                }
                catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        } );
        return applyButton;
    }
    
    @Override
    public void dispose() {
        IStyleLabelInfo info = styleLabelInfo.get();
        if (info.getUnitOfWork() != null && info.getUnitOfWork().isOpen()) {
            info.getUnitOfWork().close();
        }
    }    
}
