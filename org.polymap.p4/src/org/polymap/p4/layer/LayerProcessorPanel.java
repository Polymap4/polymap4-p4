/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.action.Action;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.pipeline.Param;
import org.polymap.core.data.pipeline.Param.UISupplier;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ILayer.ProcessorConfig;
import org.polymap.core.project.ops.TwoPhaseCommitOperation;
import org.polymap.core.runtime.DurationFormat;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.ColumnDataFactory;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.Snackbar.Appearance;
import org.polymap.rhei.batik.toolkit.md.MdActionbar;
import org.polymap.rhei.field.NumberValidator;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.AllResolver;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class LayerProcessorPanel
        extends P4Panel {

    private static final Log log = LogFactory.getLog( LayerProcessorPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layerProcessor" );

    /** Inbound: */
    @Scope(P4Plugin.Scope)
    protected Context<ILayer>               layer;

    /** Inbound: The config to modify. Its Entity must have been loaded from a nested {@link UnitOfWork}. */
    @Mandatory
    @Scope(P4Plugin.Scope)
    protected Context<ProcessorConfig>      config;
    
    private ProcessorConfig                 _config;
    
    private UnitOfWork                      uow;

    private ProcessorExtension              ext;
    
    private Params                          params;

    private Action                          submit;


    @Override
    public void init() {
        super.init();
        site().title.set( "Processor" );

        this._config = config.get();
        this.uow = _config.belongsTo();
        assert uow.parent().isPresent() : "We need a nested UnitOfWork to work properly.";
        this.ext = _config.ext.get().orElseThrow( () -> new RuntimeException( "The plugin for this processor is not installed: " + config.get().type.get() ) );
        this.params = _config.params();
    }
    
    
    @Override
    public void dispose() {
        super.dispose();
//        if (uow.isOpen()) {
//            uow.close();
//        }
    }


    @Override
    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().margins( 3, 10 ).create() );
        
        IPanelSection section = tk().createPanelSection( parent, ext.getName(), SWT.BORDER );
        FormDataFactory.on( section.getControl() ).fill().noBottom();
        section.getBody().setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).margins( 0, 5 ).spacing( 10 ).create() );

        Label label = tk().createLabel( section.getBody(), ext.getDescription().orElse( "No description." ), SWT.WRAP );
        label.setLayoutData( ColumnDataFactory.defaults().widthHint( 300 ).create() );
        label.setEnabled( false );
        
        AtomicBoolean isFirst = new AtomicBoolean( true );
        Class cl = ext.getProcessorType();
        for (Field f : cl.getDeclaredFields()) {
            Param.UI a = f.getAnnotation( Param.UI.class );
            if (a != null) {
                try {
                    if (!Modifier.isStatic( f.getModifiers() )) {
                        throw new IllegalStateException( "PipelineProcessor params should be static!" );
                    }
                    // separator
                    if (!isFirst.getAndSet( false )) {
                        Label sep = new Label( section.getBody(), SWT.SEPARATOR|SWT.HORIZONTAL );
                        UIUtils.setVariant( sep, DefaultToolkit.CSS_SECTION_SEPARATOR );  // XXX
                    }
                    // field
                    createParamField( section.getBody(), f, a );
                }
                catch (Exception e) {
                    StatusDispatcher.handleError( "Unable to create input field.", e );
                }
            }
        }
        
        // submit
        MdActionbar ab = tk().createFloatingActionbar();
        submit = ab.addSubmit( a -> submit() );
        submit.setEnabled( false );
    }
    
    
    protected Control createParamField( Composite parent, Field f, Param.UI ui ) throws Exception {
        Composite container = tk().createComposite( parent );
        container.setLayout( FormLayoutFactory.defaults().spacing( 2 ).create() );
        
        Param param = (Param)f.get( null );
        Label l = tk().createLabel( container, ui.description() );
        
        Control input = createInput( container, param, ui );
        
        // layout
        FormDataFactory.on( l ).fill().noBottom();
        FormDataFactory.on( input ).fill().top( l );
        return container;
    }
    
    
    protected Control createInput( Composite parent, Param param, Param.UI ui ) throws Exception {
        UISupplier supplier = null;

        // custom UI
        if (ui.custom() != Param.DEFAULT_UI.class) {
            supplier = ui.custom().newInstance();
        }
        // defaults
        else if (String.class.isAssignableFrom( param.type() )) {
            supplier = new StringSupplier();
        }
        else if (Number.class.isAssignableFrom( param.type() )) {
            supplier = new NumberSupplier();
        }
        else if (Duration.class.isAssignableFrom( param.type() )) {
            supplier = new DurationSupplier();
        }
        else {
            throw new RuntimeException( "Unsupported Param type: " + param.type() );
        }
        
        PipelineProcessorSite procSite = new PipelineProcessorSite( params );
        procSite.layerId.set( layer.get().id() );
        procSite.dsd.set( AllResolver.instance().connectLayer( layer.get(), new NullProgressMonitor() ).get() );
        return supplier.createContents( parent, param, procSite );
    }

    
    protected void notifyChange( Param.UISupplier supplier ) {
        submit.setEnabled( true );
    }


    protected void submit() {
        TwoPhaseCommitOperation op = new TwoPhaseCommitOperation( "Submit layer" ) {
            @Override
            protected IStatus doWithCommit( IProgressMonitor monitor, IAdaptable info ) throws Exception {
                monitor.beginTask( getLabel(), 1 );
                register( uow );
                register( uow.parent().get() );
                _config.updateParams( params );
                monitor.done();
                return Status.OK_STATUS;
            }
            @Override
            protected void onSuccess() {
                uow.close();
                UIThreadExecutor.async( () -> {
                    tk().createSnackbar( Appearance.FadeIn, "Saved" );
                    submit.setEnabled( false );
                    UIUtils.sessionDisplay().timerExec( 2500, () -> getContext().closePanel( site().path() ) );
                });
            }
            @Override
            protected void onError( Throwable e ) {
                uow.close();
                UIThreadExecutor.async( () -> {
                    getContext().closePanel( site().path() );
                    StatusDispatcher.handleError( "Unable to submit changes.", e );
                });
            }
        };
        OperationSupport.instance().execute2( op, true, false );
    }


    /**
     * 
     */
    class StringSupplier
            implements Param.UISupplier<String> {

        @Override
        public Control createContents( Composite parent, Param<String> param, PipelineProcessorSite site ) {
            Optional<String> value = param.opt( site.params() );
            Text control = tk().createText( parent, value.orElse( "" ), SWT.BORDER );
            control.addModifyListener( ev -> {
                param.put( site.params(), control.getText() );
                notifyChange( this );
            });
            return control;
        }
    }
    

    /**
     * 
     */
    class DurationSupplier
            implements Param.UISupplier<Duration> {

        public final DurationFormat FORMAT = DurationFormat.getInstance( Polymap.getSessionLocale() );

        @Override
        public Control createContents( Composite parent, Param<Duration> param, PipelineProcessorSite site ) {
            Optional<String> value = param.opt( site.params() ).map( v -> FORMAT.format( v ) );
            
            Text control = tk().createText( parent, value.orElse( "" ), SWT.BORDER );
            control.setToolTipText( "Duration: \"1d 2h 3m 4s\" or \"1hour 2min\"" );

            Color defaultForeground = control.getForeground();
            control.addModifyListener( ev -> {
                Optional<Duration> parsed = FORMAT.parse( control.getText() );
                if (!parsed.isPresent()) {
                    control.setForeground( UISupplier.errorColor() );
                    control.setToolTipText( "Not valid. Example: \"1d 2h 3m 4s\" or \"1hour 2min\"" );
                }
                else {
                    control.setForeground( defaultForeground );
                    control.setToolTipText( null );
                    
                    Duration newValue = parsed.get();
                    log.info( "Duration: " + newValue );
                    param.put( site.params(), newValue );
                }
                notifyChange( this );
            });
            return control;
        }
    }
    
    
    /**
     * 
     */
    class NumberSupplier 
            implements Param.UISupplier<Number> {

        private NumberValidator         validator;
        
        @Override
        public Control createContents( Composite parent, Param<Number> param, PipelineProcessorSite site ) {
            if (Integer.class.isAssignableFrom( param.type() ) || Integer.TYPE.equals( param.type() )) {
                validator = new NumberValidator( Integer.class, Polymap.getSessionLocale(), 10, 0, 1, 0 );
            }
            else if (Long.class.isAssignableFrom( param.type() ) || Long.TYPE.equals( param.type() )) {
                validator = new NumberValidator( Long.class, Polymap.getSessionLocale(), 10, 0, 1, 0 );
            }
            else if (Double.class.isAssignableFrom( param.type() ) || Double.TYPE.equals( param.type() )) {
                validator = new NumberValidator( Double.class, Polymap.getSessionLocale(), 10, 5, 1, 1 );
            }
            else if (Float.class.isAssignableFrom( param.type() ) || Float.TYPE.equals( param.type() )) {
                validator = new NumberValidator( Float.class, Polymap.getSessionLocale(), 10, 5, 1, 1 );
            }
            else {
                throw new RuntimeException( "Unsupported Number type: " + param.type() );
            }

            Optional<String> value = param.opt( site.params() ).map( v -> transform2Field( v ) );
            Text control = tk().createText( parent, value.orElse( "" ), SWT.BORDER );
            Color defaultForeground = control.getForeground();
            control.addModifyListener( ev -> {
                // validate
                String msg = validator.validate( control.getText() );
                if (msg != null) {
                    control.setForeground( UISupplier.errorColor() );
                    control.setToolTipText( msg );
                }
                else {
                    control.setForeground( defaultForeground );
                    control.setToolTipText( null );
                    try {
                        // set value
                        Number newValue = validator.transform2Model( control.getText() );
                        param.put( site.params(), newValue );
                        notifyChange( this );
                    }
                    catch (Exception e) {
                        StatusDispatcher.handleError( "Value was not set properly.", e );
                    }
                }
            });
            return control;
        }
        
        protected String transform2Field( Number value ) {
            try {
                return validator.transform2Field( value );
            }
            catch (Exception e) {
                log.warn( "", e );
                return null;
            }            
        }
    }
    
}
