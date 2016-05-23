/*
 * polymap.org Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.layer;

import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.polymap.core.runtime.UIThreadExecutor.async;
import static org.polymap.core.ui.FormDataFactory.on;
import static org.polymap.core.ui.SelectionAdapter.on;

import java.util.ArrayList;

import java.io.IOException;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsLike;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.polymap.core.data.unitofwork.CommitOperation;
import org.polymap.core.data.unitofwork.UnitOfWork;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionListenerAdapter;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelSite;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.batik.toolkit.Snackbar.Appearance;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.batik.BatikFormContainer;
import org.polymap.rhei.table.DefaultFeatureTableColumn;
import org.polymap.rhei.table.FeatureCollectionContentProvider.FeatureTableElement;
import org.polymap.rhei.table.FeatureTableViewer;

import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;

/**
 * Displays a {@link StandardFeatureForm} for the {@link FeatureSelection#clicked()}
 * feature.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeaturePanel
        extends P4Panel
        implements IFormFieldListener {

    private static Log                  log             = LogFactory.getLog( FeaturePanel.class );

    public static final PanelIdentifier ID              = PanelIdentifier.parse( "feature" );

    private FeatureStore                fs;

    private Feature                     feature;

    private UnitOfWork                  uow;

    private Button                      fab;

    private BatikFormContainer          form;

    private boolean                     previouslyValid = true;

    private FeatureSelection            associatedFeatureSelection;

    private FeatureTableViewer          viewer;

    private PanelSite                   site;


    @Override
    public void init() {
    }


    @Override
    public void createContents( Composite parent ) {
        try {
            fs = featureSelection.get().waitForFs().get();
            feature = featureSelection.get().clicked().get();
            
            String basePanelName = "Basis-Daten";
            boolean isSpecialCase = "organisationen".equals(feature.getType().getName().getLocalPart());
            if(isSpecialCase) {
                basePanelName = "Organisation";
            }
            
            IPanelSection baseSection = tk().createPanelSection( parent, basePanelName, SWT.BORDER );
            baseSection.addConstraint( new PriorityConstraint( 100 ) ).addConstraint( new MinWidthConstraint( 800, 0 ) );

            uow = new UnitOfWork( fs );
            uow.track( feature );
            form = new BatikFormContainer( new StandardFeatureForm() );
            form.createContents( baseSection.getBody() );

            form.addFieldListener( this );

            if(isSpecialCase) {
                FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
                PropertyIsLike filterRolleByUnternehmen = ff.like( ff.property( "Organisation" ), String.valueOf( feature.getProperty( "Name" ).getValue()) );
//            FeatureCollection rollen = createAssociationTable( parent, "Rollen", "rollen", filterRolleByUnternehmen );
                Name typeName = new NameImpl("rollen");
                FeatureSource featureSource = P4Plugin.localCatalog().localFeaturesStore().getFeatureSource( typeName );
                FeatureCollection rollen = featureSource.getFeatures( filterRolleByUnternehmen );
                
                Filter personByUnternehmen = null;
                Feature rolle = null;
                FeatureIterator iter = rollen.features();
                And filterPersonByNameAndVorname = null;
                while(iter.hasNext()) {
                    rolle = iter.next();
                    PropertyIsLike filterPersonByName = ff.like( ff.property( "Name" ), String.valueOf( rolle.getProperty( "Name" ).getValue()) );
                    PropertyIsLike filterPersonByVorname = ff.like( ff.property( "Vorname" ), String.valueOf( rolle.getProperty( "Vorname" ).getValue()) );
                    filterPersonByNameAndVorname = ff.and( filterPersonByName, filterPersonByVorname );
                    if(personByUnternehmen == null) {
                        personByUnternehmen = filterPersonByNameAndVorname;
                    } else {
                        personByUnternehmen = ff.or(personByUnternehmen, filterPersonByNameAndVorname);
                    }
                }
                createAssociationTable( parent, "Personen", "personen", personByUnternehmen );
            }

            fab = tk().createFab();
            fab.setToolTipText( "Save changes" );
            fab.setVisible( false );
            fab.addSelectionListener( new SelectionListenerAdapter( ev -> submit() ) );
        }
        catch (Exception e) {
            createErrorContents( parent, "Unable to display feature.", e );
        }
    }


    private FeatureCollection createAssociationTable( Composite parent, String associationPanelName, String association, Filter filter ) throws IOException {
        Name typeName = new NameImpl(association);
        FeatureSource featureSource = P4Plugin.localCatalog().localFeaturesStore().getFeatureSource( typeName );
        FeatureCollection features = featureSource.getFeatures( filter );
        
        IPanelSection labelSection = tk().createPanelSection( parent, associationPanelName, SWT.BORDER );
        labelSection.addConstraint( new PriorityConstraint( 99 ) ).addConstraint( new MinWidthConstraint( 400, 0 ) );

        Composite tableComposite = tk().createComposite( labelSection.getBody(), SWT.NONE );
        tableComposite.setLayout( FormLayoutFactory.defaults().create() );

        // topbar
        Composite topbar = on( tk().createComposite( tableComposite ) ).fill().noBottom().height( 30 ).control();
        topbar.setLayout( FormLayoutFactory.defaults().spacing( 3 ).create() );
        
        // search
        Pair<Control, Control> textAndButton = createTextSearch( features, topbar );
        on( textAndButton.getLeft() ).fill().noLeft().control();
        on( textAndButton.getRight() ).fill().right( textAndButton.getLeft() );

        // table viewer
        createTableViewer( features, tableComposite, association );
        on( viewer.getTable() ).left( 0 ).right( 100 ).height( 300 ).top( topbar );
        
        return features;
    }


    protected void createTableViewer( FeatureCollection features, Composite parent, String association ) throws IOException {
        viewer = new FeatureTableViewer( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER );

        // add columns
        for (PropertyDescriptor prop : features.getSchema().getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                viewer.addColumn( new DefaultFeatureTableColumn( prop ) );
            }
        }
        //
        viewer.setContent( features );

        // selection -> FeaturePanel
        viewer.addSelectionChangedListener( ( SelectionChangedEvent ev ) -> {
            FeatureTableElement elm = (FeatureTableElement)on( ev.getSelection() ).first().get();
            log.info( "selection: " + elm );
            associatedFeatureSelection.setClicked( elm.getFeature() );
            BatikApplication.instance().getContext().openPanel( site.path(), FeaturePanel.ID );
        } );
    }


    protected Pair<Control, Control> createTextSearch( FeatureCollection features, Composite topbar ) {
        Text searchText = tk().createText( topbar, null, SWT.BORDER );
        searchText.setToolTipText( "Beginning of a text to search for. At least 2 characters." );
        searchText.forceFocus();
        searchText.addKeyListener( new KeyAdapter() {

            @Override
            public void keyReleased( KeyEvent ev ) {
                if (ev.keyCode == SWT.Selection) {
                    search(features, searchText);
                }
            }
        } );

        Button searchBtn = tk().createButton( topbar, null, SWT.PUSH );
        searchBtn.setToolTipText( "Start search" );
        searchBtn.setImage( P4Plugin.images().svgImage( "magnify.svg", SvgImageRegistryHelper.WHITE24 ) );
        searchBtn.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent ev ) {
                search(features, searchText);
            }
        } );
        searchText.addModifyListener( new ModifyListener() {

            @Override
            public void modifyText( ModifyEvent ev ) {
                searchBtn.setEnabled( searchText.getText().length() > 1 );
            }
        } );
        return Pair.of( searchText, searchText );
    }


    protected void search(FeatureCollection features, Text searchText) {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
        Filter filter = Filter.INCLUDE;
        for (PropertyDescriptor prop : features.getSchema().getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                PropertyIsLike isLike = ff.like( ff.property( prop.getName() ), searchText.getText() + "*", "*", "?",
                        "\\" );
                filter = filter == Filter.INCLUDE ? isLike : ff.or( filter, isLike );
            }
        }
        log.info( "FILTER: " + filter );
        FeatureCollection filtered = features.subCollection( filter );
        log.info( "RESULT: " + filtered.size() );
        viewer.setContent( filtered );
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.hasEventCode( VALUE_CHANGE )) {
            boolean isDirty = form.isDirty();
            boolean isValid = form.isValid();

            fab.setVisible( isDirty );
            fab.setEnabled( isDirty && isValid );

            if (previouslyValid && !isValid) {
                tk().createSnackbar( Appearance.FadeIn, "There are invalid settings" );
            }
            if (!previouslyValid && isValid) {
                tk().createSnackbar( Appearance.FadeIn, "Settings are ok" );
            }
            previouslyValid = isValid;
        }
    }


    protected void submit() {
        try {
            // XXX doing this inside operation cause "Invalid thread access"
            form.submit( null );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "Unable to submit form.", e );
        }

        CommitOperation op = new CommitOperation().uow.put( uow );
        OperationSupport.instance().execute2( op, false, false, ev -> {
            async( ( ) -> {
                tk().createSnackbar( Appearance.FadeIn, ev.getResult().isOK()
                        ? "Saved" : abbreviate( "Unable to save: " + ev.getResult().getMessage(), 50 ) );
            } );
        } );
    }


    /**
     * 
     */
    class StandardFeatureForm
            extends DefaultFormPage {

        @Override
        public void createFormContents( IFormPageSite site ) {
            super.createFormContents( site );
            site.getPageBody().setLayout( ColumnLayoutFactory.defaults().columns(
                    1, 1 ).spacing( 3 ).create() );

            for (Property prop : FeaturePanel.this.feature.getProperties()) {
                if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                    // skip Geometry
                }
                else {
                    site.newFormField( prop ).create();
                }
            }
        }
    }

}
