/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.layer;

import static org.polymap.core.project.ui.ProjectNodeLabelProvider.PropType.Label;
import static org.polymap.core.runtime.event.TypeEventFilter.ifType;
import static org.polymap.core.ui.UIUtils.selectionListener;
import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerDropAdapter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectNode.ProjectNodeCommittedEvent;
import org.polymap.core.project.ops.TwoPhaseCommitOperation;
import org.polymap.core.project.ui.ProjectNodeContentProvider;
import org.polymap.core.project.ui.ProjectNodeLabelProvider;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.StatusDispatcher.Style;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PropertyAccessEvent;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.CheckboxActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.RadioboxActionProvider;

import org.polymap.model2.Entities;
import org.polymap.p4.Messages;
import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class LayersPanel
        extends P4Panel {

    private static final Log log = LogFactory.getLog( LayersPanel.class );

    protected static final IMessages    i18n = Messages.forPrefix( "LayersPanel" );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layers" );

    @Mandatory
    @Scope(P4Plugin.Scope)
    protected Context<IMap>             map;
    
    /** Set before opening {@link LayerInfoPanel}. */
    @Scope(P4Plugin.Scope)
    protected Context<ILayer>           selected;
    
    private MdListViewer                list;

    private DropTarget                  dropTarget;

    private DragSource                  dragSource;
    
    private LocalSelectionTransfer      transfer = LocalSelectionTransfer.getTransfer();
    
    
    public MdListViewer getViewer() {
        return list;
    }


    @Override
    public boolean beforeInit() {
        if (parentPanel().orElse( null ) instanceof ProjectMapPanel) {
            site().icon.set( P4Plugin.images().svgImage( "layers.svg", P4Plugin.HEADER_ICON_CONFIG ) );
            site().title.set( "" );
            return true;
        }
        return false;
    }


    @Override
    public void dispose() {
        if (dropTarget != null) {
            dropTarget.dispose();
            dragSource.dispose();
        }
        super.dispose();
        EventManager.instance().unsubscribe( this );
    }


    @Override
    public void createContents( Composite parent ) {
        site().title.set( i18n.get( "title" ) );
        parent.setLayout( FormLayoutFactory.defaults().create() );
        
        list = tk().createListViewer( parent, SWT.SINGLE, SWT.FULL_SELECTION );
        list.setContentProvider( new ProjectNodeContentProvider() );

        list.firstLineLabelProvider.set( new ProjectNodeLabelProvider( Label ).abbreviate.put( 35 ) );
        //viewer.secondLineLabelProvider.set( new ProjectNodeLabelProvider( Description ).abbreviate.put( 35 ) );
        list.iconProvider.set( new LayerIconProvider() );
        
        list.secondSecondaryActionProvider.set( new LayerVisibleAction());
        list.thirdSecondaryActionProvider.set( new LayerActiveAction() );
        //viewer.firstSecondaryActionProvider.set( new LayerUpDownAction() );
        list.firstSecondaryActionProvider.set( new ActionProvider() {
            @Override public void update( ViewerCell cell ) {
                cell.setImage( P4Plugin.images().svgImage( "chevron-right.svg", SvgImageRegistryHelper.NORMAL24 ) );
            }
            @Override public void perform( MdListViewer viewer, Object elm ) {
                selected.set( (ILayer)elm );
                getContext().openPanel( site().path(), LayerInfoPanel.ID );
            }
        });

        list.addOpenListener( new IOpenListener() {
            @Override public void open( OpenEvent ev ) {
                SelectionAdapter.on( ev.getSelection() ).forEach( elm -> {
                    selected.set( (ILayer)elm );
                    getContext().openPanel( site().path(), LayerInfoPanel.ID );
                });
            }
        });

        // DnD
        //int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT | DND.DROP_LINK;
        dropTarget = UIUtils.addDropSupport( list.getControl(), transfer, new DropListener() );
        dragSource = UIUtils.addDragSupport( list.getControl(), transfer, new DragListener() );
        
        list.setInput( map.get() );

        // noBottom: avoid empty rows and lines
        list.getTree().setLayoutData( FormDataFactory.filled()/*.noBottom()*/.create() );
        
        // listen to ILayer changes
        EventManager.instance().subscribe( this, ifType( ProjectNodeCommittedEvent.class, ev -> 
                ev.getSource() instanceof ILayer ) );
    }

    
    @EventHandler( display=true, delay=100, scope=Event.Scope.JVM )
    protected void projectChanged( List<ProjectNodeCommittedEvent> evs ) {
        if (list == null || list.getControl().isDisposed()) {
            EventManager.instance().unsubscribe( LayersPanel.this );            
        }
        else {
            list.refresh();
        }
    }

    
    /**
     * 
     */
    protected class DropListener
            extends ViewerDropAdapter {

        protected DropListener() {
            super( list );
            setSelectionFeedbackEnabled( false );
        }

        @Override 
        public boolean validateDrop( Object target, int op, TransferData type ) {
            // XXX check source type
            return transfer.isSupportedType( type ) || target == null || target instanceof ILayer;
        }
        
        @Override 
        public boolean performDrop( Object data ) {
            ILayer source = UIUtils.selection( transfer.getSelection() ).first( ILayer.class ).orElse( null );
            ILayer target = (ILayer)getCurrentTarget();
            switch (getCurrentLocation()) {
                case LOCATION_BEFORE:
                case LOCATION_ON: {
                    if (source != target) {
                        List<ILayer> ordered = source.orderedLayers();
                        ordered.remove( source );
                        ordered.add( ordered.indexOf( target ) + 1, source );
                        source.updateOrderKeys( ordered );
                    }
                    break;
                }
                case LOCATION_NONE: {
                    source.orderKey.set( source.minOrderKey() - 1 );
                    break;
                }
                default: {
                    log.warn( "Not implemented: location=" + getCurrentLocation() );
                }
            }
            source.belongsTo().commit();
//            list.refresh();
            return true;
        }
    }

    
    protected class DragListener
            extends DragSourceAdapter {

        @Override
        public void dragSetData( DragSourceEvent event ) {
            transfer.setSelection( list.getSelection() );
        }
    }
    
    
    /**
     * 
     */
    protected final class LayerIconProvider
            extends CellLabelProvider {

        private Map<Object,Image> legendGraphics = new HashMap();

        @Override
        public void update( ViewerCell cell ) {
            ILayer layer = (ILayer)cell.getElement();
            cell.setImage( legendGraphics.containsKey( layer.id() )
                    ? legendGraphics.get( layer.id() )
                    : P4Plugin.images().svgImage( "layers.svg", NORMAL24 ) );
            
//                new UIJob( "Legend graphic" ) {
//                    @Override
//                    protected void runWithException( IProgressMonitor monitor ) throws Exception {
//                        Thread.sleep( 3000 );
//                        UIThreadExecutor.async( () -> {
//                            legendGraphics.put( layer.id(), P4Plugin.instance().imageForName( "resources/icons/map.png" ) );
//                            viewer.update( layer, null );
//                        }, UIThreadExecutor.runtimeException() );
//                    }
//                }.scheduleWithUIUpdate();
        }
    }


    /**
     * 
     */
    protected final class LayerVisibleAction
            extends CheckboxActionProvider {
    
        public LayerVisibleAction() {
            super( P4Plugin.images().svgImage( "eye.svg", SvgImageRegistryHelper.NORMAL24 ),
                    BatikPlugin.images().svgImage( "checkbox-blank-outline.svg", NORMAL24 ) );
        }

        @Override
        protected boolean initSelection( MdListViewer _viewer, Object elm ) {
//            try {
//                // check: feature layer without geom
//                Optional<FeatureLayer> fl = FeatureLayer.of( (ILayer)elm ).get( 3, SECONDS );
//                if (fl.isPresent()) {
//                    SimpleFeatureType schema = fl.get().featureSource().getSchema();
//                    if (schema.getGeometryDescriptor() == null) {
//                        return false;
//                    }
//                }
//            }
//            catch (Exception e) {
//                log.warn( "", e );
//                return false;
//            }
            
            return ((ILayer)elm).userSettings.get().visible.get();
        }

        @Override
        public void perform( MdListViewer _viewer, Object elm ) {
            FeatureLayer.of( (ILayer)elm ).thenAccept( fl -> {
                UIThreadExecutor.async( () -> {
                    if (fl.isPresent() && fl.get().featureSource().getSchema().getGeometryDescriptor() == null) {
                        StatusDispatcher.handle( new Status( IStatus.INFO, P4Plugin.ID, i18n.get( "invisible" ) ), Style.SHOW, Style.LOG );
                        setSelected( elm, Boolean.FALSE );
                        list.update( elm, null );
                        onSelection( list, elm, isSelected( elm ) );
                        return;
                    }
                    super.perform( _viewer, elm );
                });
            })
            .exceptionally( e -> {
                StatusDispatcher.handleError( "Unable to set visibility of layer.", e );
                return null;
            });
        }
        
        @Override
        protected void onSelection( MdListViewer _viewer, Object elm, @SuppressWarnings( "hiding" ) boolean selected ) {
            ((ILayer)elm).userSettings.get().visible.set( selected );
        }
    }


    /**
     * 
     */
    protected final class LayerActiveAction
            extends RadioboxActionProvider {
    
        private Map<ILayer,ViewerCell>  cells = new HashMap();
        
        private ViewerCell              active;
        
        public LayerActiveAction() {
            featureLayer.addListener( this, ev -> ev.getType() == PropertyAccessEvent.TYPE.SET );
        }

        @Override
        public void dispose() {
            super.dispose();
            featureLayer.removeListener( this );
        }

        /**
         * {@link #updateSelection(MdListViewer, Object, boolean) Update selection}
         * after featureLayer is set to null when {@link FeatureSelectionTable} was
         * closed.
         */
        @EventHandler( display=true )
        protected void onFeatureLayerChange( PropertyAccessEvent ev ) {
            if (ev.getOldValue() != null) {
                updateSelection( list, ((FeatureLayer)ev.getOldValue()).layer(), false );
            }
        }
        
        @Override
        protected Object initSelection( MdListViewer viewer ) {
            return featureLayer.isPresent() ? featureLayer.get().layer() : null;
        }

        @Override
        public void update( ViewerCell cell ) {
            try {
                // ommit non-feature layers
                cell.setImage( null );
                
                // sync wait for FeatureLayer
                ILayer layer = (ILayer)cell.getElement();
                FeatureLayer.of( layer ).get().ifPresent( fl -> LayerActiveAction.super.update( cell ) );

                // async implementation; does not work because ViewerCell does not seem to expect
                // to be called after this update() call :(
                
//                ViewerRow viewerRow = cell.getViewerRow();
//                int columnIndex = cell.getColumnIndex();
//                FeatureLayer.of( layer ).thenAccept( fl -> {
//                    if (fl.isPresent()) {
//                        UIThreadExecutor.async( () -> {
//                            Image image = isSelected( layer ) ? selectedImage.get() : unselectedImage.get();
//                            viewerRow.setImage( columnIndex, image );
//                        });
//                    }
//                });
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
        
        @Override
        @SuppressWarnings( "hiding" ) 
        protected void onSelection( MdListViewer viewer, Object elm, boolean selected ) {
            if (!selected) {
                assert featureLayer.get().layer() == elm;
                featureLayer.set( null );
            }
            else {
                FeatureLayer.of( (ILayer)elm ).thenAccept( fl -> {
                    if (fl.isPresent()) {
                        featureLayer.set( fl.get() );
                        //((ILayer)elm).userSettings.get().visible.set( true );
                    }
                });
            }
        }
    }
    
    
    /**
     * 
     */
    protected class LayerUpDownAction
            extends ActionProvider {

        private ILayer  layer;
        
        private Button  up, down;

        @Override
        public void update( ViewerCell cell ) {
            cell.setImage( P4Plugin.images().svgImage( "unfold-more.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
        }

        @Override
        public void perform( MdListViewer viewer, Object elm ) {
            layer = (ILayer)elm;
            
            EventManager.instance().subscribe( this, ifType( ProjectNodeCommittedEvent.class, ev -> 
                    ev.getSource() instanceof ILayer && Entities.equal( ev.getSource(), layer ) ) );
            
            tk().createSimpleDialog( "Layer display priority" )
                    .title.put( "Layer display priority" )
                    .setContents( parent -> {
                        parent.setLayout( FormLayoutFactory.defaults().margins( 6 ).spacing( 6 ).create() );
                        Label label = tk().createLabel( parent, "<b>" + layer.label.get() + "</b>" );
                        FormDataFactory.on( label ).fill().noBottom();
                        
                        up = tk().createButton( parent, "Up", SWT.PUSH );
                        up.setImage( P4Plugin.images().svgImage( "arrow-up.svg", SvgImageRegistryHelper.WHITE24 ) );
                        FormDataFactory.on( up ).left( 0 ).top( label ).width( 100 );
                        up.addSelectionListener( selectionListener( ev -> 
                                doPerform( true ) ) );

                        down = tk().createButton( parent, "Down", SWT.PUSH );
                        down.setImage( P4Plugin.images().svgImage( "arrow-down.svg", SvgImageRegistryHelper.WHITE24 ) );
                        FormDataFactory.on( down ).left( up ).top( label ).width( 100 );
                        down.addSelectionListener( selectionListener( ev -> 
                                doPerform( false ) ) );
                        
                        updateEnabled( null );
                    })
                    .addCancelAction( "Close" )
                    .open();
   
            layer = null;
            up = down = null;
            EventManager.instance().unsubscribe( this );
        }
        
        @EventHandler( display=true, delay=100, scope=Event.Scope.JVM )
        protected void updateEnabled( List<ProjectNodeCommittedEvent> evs ) {
            if (layer != null) {
                up.setEnabled( !layer.orderKey.get().equals( layer.maxOrderKey() ) );
                down.setEnabled( !layer.orderKey.get().equals( layer.minOrderKey() ) );
            }
        }
        
        protected void doPerform( @SuppressWarnings( "hiding" ) boolean up ) {
            TwoPhaseCommitOperation op = new TwoPhaseCommitOperation( "Layer up" ) {
                @Override
                protected IStatus doWithCommit( IProgressMonitor monitor, IAdaptable info ) throws Exception {
                    register( layer.belongsTo() );
                    if (up && !layer.orderKey.get().equals( layer.maxOrderKey() )) {
                        layer.orderUp( monitor );
                    }
                    if (!up && !layer.orderKey.get().equals( layer.minOrderKey() )) {                    
                        layer.orderDown( monitor );                        
                    }
                    return Status.OK_STATUS;
                }
            };
            OperationSupport.instance().execute( op, false, false );                            
        }
    }
    
}
