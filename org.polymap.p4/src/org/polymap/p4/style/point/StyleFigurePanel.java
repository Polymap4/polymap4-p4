/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
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
package org.polymap.p4.style.point;

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
import org.polymap.p4.style.color.IColorInfo;
import org.polymap.p4.style.entities.StyleFigure;
import org.polymap.p4.style.icon.FigureLibraryInitializer;
import org.polymap.p4.style.icon.IImageInfo;
import org.polymap.p4.style.ui.point.StyleFigureUI;
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
public class StyleFigurePanel
        extends DefaultPanel {

    public static final PanelIdentifier ID                   = PanelIdentifier.parse( "style_figure" );

    private MdToolkit                   toolkit;

    @Scope(P4Plugin.Scope)
    private Context<IStylePointInfo>    stylePointInfo;

    @Scope(P4Plugin.Scope)
    private Context<IImageInfo>         imageInfo;

    @Scope(P4Plugin.Scope)
    private Context<IColorInfo>         colorInfo;

    private DefaultFormPage             styleFigurePage;

    private BatikFormContainer          styleFigurePageContainer;


    @Override
    public void createContents( Composite panelBody ) {
        getSite().setTitle( "Marker Figure" );
        toolkit = (MdToolkit)getSite().toolkit();
        panelBody.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        createControls( panelBody );
    }


    private void createControls( Composite parent ) {
        FigureLibraryInitializer figureLibraryInitializer = new FigureLibraryInitializer();
        StyleFigureUI ui = new StyleFigureUI( getContext(), getSite(), imageInfo,
                colorInfo, figureLibraryInitializer );
        styleFigurePage = new DefaultFormPage() {

            @Override
            public void createFormContents( IFormPageSite formSite ) {
                IStylePointInfo info = stylePointInfo.get();
                Property<StyleFigure> markerFigureProp = info.getStylePoint().markerFigure;
                StyleFigure styleFigure = markerFigureProp.get();
                if (styleFigure == null) {
                    styleFigure = markerFigureProp.createValue( null );
                }
                ui.setModel( styleFigure );
                ui.createContents( formSite );
            }
        };
        styleFigurePageContainer = new BatikFormContainer( styleFigurePage );
        Composite formContainer = toolkit.createComposite( parent, SWT.NONE );

        styleFigurePageContainer.createContents( formContainer );

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
                    styleFigurePageContainer.submit( new NullProgressMonitor() );
                    PanelPath path = getSite().getPath();
                    getContext().closePanel( path );
                    EventManager.instance().publish( new EventObject( stylePointInfo.get() ) );
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
        IStylePointInfo info = stylePointInfo.get();
        if (info.getUnitOfWork() != null && info.getUnitOfWork().isOpen()) {
            info.getUnitOfWork().close();
        }
    }
}
