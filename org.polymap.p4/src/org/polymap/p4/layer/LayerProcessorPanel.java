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

import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.time.Duration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.pipeline.Param;
import org.polymap.core.data.pipeline.Param.UISupplier;
import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ILayer.KeyValue;
import org.polymap.core.project.ILayer.ProcessorConfig;
import org.polymap.core.project.ops.TwoPhaseCommitOperation;
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
import org.polymap.rhei.field.NumberValidator;

import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;

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
    @Mandatory
    @Scope(P4Plugin.Scope)
    protected Context<ILayer>               layer;

    /** Inbound: */
    @Mandatory
    @Scope(P4Plugin.Scope)
    protected Context<ProcessorExtension>   ext;

    private ProcessorConfig                 config;
    
    private Params                          params;

    private Button                          fab;
    

    @Override
    public void init() {
        super.init();
        site().title.set( "Processor" );
        
        this.params = new Params();
        for (ProcessorConfig candidate : layer.get().processorConfigs) {
            if (candidate.type.get().equals( ext.get().getProcessorType().getName() ) ) {
                config = candidate;
                config.params.forEach( param -> params.put( param.key.get(), param.value.get() ) );
                break;
            }
        }
    }
    
    
    @Override
    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().margins( 3, 10 ).create() );
        
        IPanelSection section = tk().createPanelSection( parent, ext.get().getName(), SWT.BORDER );
        FormDataFactory.on( section.getControl() ).fill().noBottom();
        section.getBody().setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).margins( 0, 5 ).spacing( 10 ).create() );

        Label label = tk().createLabel( section.getBody(), ext.get().getDescription().orElse( "No description." ), SWT.WRAP );
        label.setLayoutData( ColumnDataFactory.defaults().widthHint( 300 ).create() );
        label.setEnabled( false );
        
        AtomicBoolean isFirst = new AtomicBoolean( true );
        Class cl = ext.get().getProcessorType();
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
        
        // FAB
        fab = tk().createFab();
        fab.setVisible( false );
        fab.setEnabled( false );
        fab.addSelectionListener( UIUtils.selectionListener( ev -> submit( ev ) ) );
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
        return supplier.createContents( parent, param, params );
    }

    
    protected void notifyChange( Param.UISupplier supplier ) {
        fab.setVisible( true );
        fab.setEnabled( true );
    }


    protected void submit( SelectionEvent ev ) {
        TwoPhaseCommitOperation op = new TwoPhaseCommitOperation( "Submit layer" ) {
            @Override
            protected IStatus doWithCommit( IProgressMonitor monitor, IAdaptable info ) throws Exception {
                monitor.beginTask( getLabel(), 1 );
                register( layer.get().belongsTo() );
                
                if (config != null) {
                    config.params.clear();
                }
                else {
                    config = layer.get().processorConfigs.createElement( 
                            ProcessorConfig.defaults( "", ext.get().getProcessorType().getName() ) );
                }
                params.entrySet().forEach( param -> config.params.createElement( (KeyValue proto) -> {
                    proto.key.set( param.getKey() );
                    proto.value.set( (String)param.getValue() );
                    return proto;
                }));
                
                monitor.done();
                return Status.OK_STATUS;
            }
            @Override
            protected void onSuccess() {
                UIThreadExecutor.async( () -> {
                    tk().createSnackbar( Appearance.FadeIn, "Saved" );
                    fab.setEnabled( false );
                    //getContext().closePanel( site().path() );
                });
            }
            @Override
            protected void onError( Throwable e ) {
                UIThreadExecutor.async( () -> 
                        StatusDispatcher.handleError( "Unable to submit changes.", e ) );
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
        @SuppressWarnings( "hiding" )
        public Control createContents( Composite parent, Param<String> param, Params params ) {
            Optional<String> value = param.opt( params );
            Text control = tk().createText( parent, value.orElse( "" ), SWT.BORDER );
            control.addModifyListener( ev -> {
                param.put( params, control.getText() );
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

        public final Pattern PATTERN = Pattern.compile( 
                "((\\d{1,3})d(ay)*)*\\s*" +   // 123d(ay) 
                "((\\d{1,2})h(our)*)*\\s*" +  // 12h(our) 
                "((\\d{1,2})m(in)*)*\\s*" +   // 12m(in) 
                "((\\d{1,2})s(ec)*)*\\s*",    // 12s(ec) 
                Pattern.CASE_INSENSITIVE );
        
        public final MessageFormat FORMAT = new MessageFormat( "", Polymap.getSessionLocale() );

        @Override
        @SuppressWarnings( "hiding" )
        public Control createContents( Composite parent, Param<Duration> param, Params params ) {
            Optional<String> value = param.opt( params ).map( v -> {
                StringBuffer result = new StringBuffer( 16 );
                if (v.toDays() > 0) {
                    result.append( v.toDays() ).append( "d " );
                    v = v.minusDays( v.toDays() );
                }
                if (v.toHours() > 0) {
                    result.append( v.toHours() ).append( "h " );
                    v = v.minusHours( v.toHours() );
                }
                if (v.toMinutes() > 0) {
                    result.append( v.toMinutes() ).append( "m " );
                    v = v.minusMinutes( v.toMinutes() );
                }
                if (v.getSeconds() > 0) {
                    result.append( v.getSeconds() ).append( "s " );
                }
                return result.toString();
            });
            
            Text control = tk().createText( parent, value.orElse( "" ), SWT.BORDER );
            control.setToolTipText( "Duration: \"1d 2h 3m 4s\" or \"1hour 2min\"" );

            Color defaultForeground = control.getForeground();
            control.addModifyListener( ev -> {
                Matcher matcher = PATTERN.matcher( control.getText() );
                if (!matcher.matches()) {
                    control.setForeground( UISupplier.errorColor() );
                    control.setToolTipText( "Not valid. Example: \"1d 2h 3m 4s\" or \"1hour 2min\"" );
                }
                else {
                    control.setForeground( defaultForeground );
                    control.setToolTipText( null );
                    
                    Duration newValue = Duration.ofDays( parseLong( defaultIfBlank( matcher.group( 2 ), "0" ) ) )
                            .plus( Duration.ofHours( parseLong( defaultIfBlank( matcher.group( 5 ), "0" ) ) ) )
                            .plus( Duration.ofMinutes( parseLong( defaultIfBlank( matcher.group( 8 ), "0" ) ) ) )
                            .plus( Duration.ofSeconds( parseLong( defaultIfBlank( matcher.group( 11 ), "0" ) ) ) );
                    log.info( "Duration: " + newValue );
                    param.put( params, newValue );
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
        @SuppressWarnings( "hiding" )
        public Control createContents( Composite parent, Param<Number> param, Params params ) {
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

            Optional<String> value = param.opt( params ).map( v -> transform2Field( v ) );
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
                        param.put( params, newValue );
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
