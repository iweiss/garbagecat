/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

/**
 * Analysis constants.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class Analysis {

    /**
     * Property file.
     */
    public static final String PROPERTY_FILE = "analysis";
    
    /**
     * Property key for explicit garbage collection.
     */
    public static final String KEY_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED = "first.timestamp.threshold.exceeded";
    
    /**
     * Property key for explicit gc with concurrent collectors (CMS, G1).
     */
    public static final String KEY_EXPLICIT_GC_UNECESSARY_CMS_G1 = "explicit.gc.unnecessary.cms.g1";
    
    /**
     * Property key for explicit gc with non concurrent collectors (Serial, ParallelSerial).
     */
    public static final String KEY_EXPLICIT_GC_UNNECESSARY = "explicit.gc.unnecessary";
    
    /**
     * Property key for explicit garbage collection by a serial collector.
     */
    public static final String KEY_EXPLICIT_GC_SERIAL = "explicit.gc.serial";
    
    /**
     * Property key for explicit garbage collection disabled.
     */
    public static final String KEY_EXPLICIT_GC_DISABLED = "explicit.gc.disabled";
    
    /**
     * Property key for -XX:+PrintGCApplicationStoppedTime missing.
     */
    public static final String KEY_APPLICATION_STOPPED_TIME_MISSING = "application.stopped.time.missing";
    
    /**
     * Property key for the ratio of gc time vs. stopped time showing a significant amount of stopped time (>20%) is
     * not GC related.
     */
    public static final String KEY_GC_STOPPED_RATIO = "gc.stopped.ratio";
    
    /**
     * Property key for thread stack size not set.
     */
    public static final String KEY_THREAD_STACK_SIZE_NOT_SET = "thread.stack.size.not.set";  
    
    /**
     * Property key for thread stack size is large.
     */
    public static final String KEY_THREAD_STACK_SIZE_LARGE = "thread.stack.size.large";     
    
    /**
     * Property key for min heap not equal to max heap.
     */
    public static final String KEY_MIN_HEAP_NOT_EQUAL_MAX_HEAP = "min.heap.not.equal.max.heap"; 
    
    /**
     * Property key for perm gen or metaspace size not explicitly set.
     */
    public static final String KEY_PERM_METASPACE_NOT_SET = "perm.metaspace.not.set";  
    
    /**
     * Property key for min perm not equal to max perm.
     */
    public static final String KEY_MIN_PERM_NOT_EQUAL_MAX_PERM = "min.perm.not.equal.max.perm";  
    
    /**
     * Property key for metaspace not equal to max metaspace.
     */
    public static final String KEY_MIN_METASPACE_NOT_EQUAL_MAX_METASPACE = "min.metaspace.not.equal.max.metaspace";     
    
    /**
     * Property key for the Throughput collector invoking a serial collection.
     */
    public static final String KEY_THROUGHPUT_SERIAL_GC = "throughput.serial.gc";  
    
    /**
     * Property key for the CMS collector invoking a serial collection.
     */
    public static final String KEY_CMS_SERIAL_GC = "cms.serial.gc";
    
    /**
     * Property key for the G1 collector invoking a serial collection.
     */
    public static final String KEY_G1_SERIAL_GC = "g1.serial.gc";  

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private Analysis() {

    }
}