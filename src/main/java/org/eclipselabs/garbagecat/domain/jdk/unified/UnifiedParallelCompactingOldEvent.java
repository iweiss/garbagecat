/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceCollection;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.ParallelCollector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.github.joa.domain.GarbageCollector;

/**
 * <p>
 * UNIFIED_PARALLEL_COMPACTING_OLD
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelCompactingOldEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Timestamp is end of gc (e.g. no `gc,start` details):
 * </p>
 * 
 * <pre>
 * [0.017s][info][gc       ] GC(0) Pause Young (Allocation Failure) 0M-&gt;0M(1M) 0.521ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * 2) Timestamp is beginning of gc (e.g. preprocessed with `gc,start` details):
 * </p>
 * 
 * <p>
 * JDK11
 * </p>
 * 
 * <pre>
 * [0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K-&gt;496K(1536K) ParOldGen: 472K-&gt;432K(2048K) Metaspace: 701K-&gt;701K(1056768K) 0M-&gt;0M(3M) 4.336ms User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 * 
 * <p>
 * JDK17
 * </p>
 * 
 * <pre>
 * [0.058s][info][gc,start    ] GC(3) Pause Full (Ergonomics) PSYoungGen: 499K(1536K)-&gt;497K(1536K) ParOldGen: 400K(512K)-&gt;366K(2048K) Metaspace: 666K(832K)-&gt;666K(832K) 0M-&gt;0M(3M) 2.095ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedParallelCompactingOldEvent extends ParallelCollector
        implements UnifiedLogging, BlockingEvent, OldCollection, PermMetaspaceCollection, ParallelEvent, YoungData,
        OldData, PermMetaspaceData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.ERGONOMICS.getRegex() + "|" + GcTrigger.HEAP_DUMP_INITIATED_GC.getRegex() + "|"
            + GcTrigger.METADATE_GC_CLEAR_SOFT_REFERENCES.getRegex() + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex()
            + ")";

    /**
     * Regular expression defining the logging.
     */
    private static final String _REGEX = "" + UnifiedRegEx.DECORATOR + " Pause Full \\(" + __TRIGGER
            + "\\) PSYoungGen: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) ParOldGen: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\) Metaspace: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + TimesData.REGEX_JDK9 + "[ ]*$";

    private static Pattern pattern = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return pattern.matcher(logLine).matches();
    }

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long eventTime;
    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;
    /**
     * Old generation size at beginning of GC event.
     */
    private Memory old;

    /**
     * Space allocated to old generation.
     */
    private Memory oldAllocation;

    /**
     * Old generation size at end of GC event.
     */
    private Memory oldEnd;

    /**
     * Permanent generation size at beginning of GC event.
     */
    private Memory permGen;
    /**
     * Space allocated to permanent generation.
     */
    private Memory permGenAllocation;
    /**
     * Permanent generation size at end of GC event.
     */
    private Memory permGenEnd;

    /**
     * The wall (clock) time in centiseconds.
     */
    private int timeReal = TimesData.NO_DATA;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The time of all system (kernel) threads added together in centiseconds.
     */
    private int timeSys = TimesData.NO_DATA;

    /**
     * The time of all user (non-kernel) threads added together in centiseconds.
     */
    private int timeUser = TimesData.NO_DATA;

    /**
     * The trigger for the GC event.
     */
    private GcTrigger trigger;

    /**
     * Young generation size at beginning of GC event.
     */
    private Memory young;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngAvailable;

    /**
     * Young generation size at end of GC event.
     */
    private Memory youngEnd;

    /**
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedParallelCompactingOldEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            eventTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 50)).intValue();
            long time = UnifiedUtil.calculateTime(matcher);
            if (!isEndstamp()) {
                timestamp = time;
            } else {
                timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
            }
            trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
            young = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4).charAt(0)).convertTo(KILOBYTES);
            youngEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 9),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 11).charAt(0)).convertTo(KILOBYTES);
            youngAvailable = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 12),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 14).charAt(0)).convertTo(KILOBYTES);
            old = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 15),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 17).charAt(0)).convertTo(KILOBYTES);
            oldEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 22),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 24).charAt(0)).convertTo(KILOBYTES);
            oldAllocation = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 25),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 27).charAt(0)).convertTo(KILOBYTES);
            permGen = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 28),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 30).charAt(0)).convertTo(KILOBYTES);
            permGenEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 35),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 37).charAt(0)).convertTo(KILOBYTES);
            permGenAllocation = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 38),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 40).charAt(0)).convertTo(KILOBYTES);
            timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 52)).intValue();
            timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 53)).intValue();
            timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 54)).intValue();
        }
    }

    /**
     * Alternate constructor. Create serial logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public UnifiedParallelCompactingOldEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.eventTime = duration;
    }

    public long getDuration() {
        return eventTime;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.PARALLEL_OLD;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString();
    }

    public Memory getOldOccupancyEnd() {
        return oldEnd;
    }

    public Memory getOldOccupancyInit() {
        return old;
    }

    public Memory getOldSpace() {
        return oldAllocation;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

    public Memory getPermOccupancyEnd() {
        return permGenEnd;
    }

    public Memory getPermOccupancyInit() {
        return permGen;
    }

    public Memory getPermSpace() {
        return permGenAllocation;
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public int getTimeReal() {
        return timeReal;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTimeSys() {
        return timeSys;
    }

    public int getTimeUser() {
        return timeUser;
    }

    public GcTrigger getTrigger() {
        return trigger;
    }

    public Memory getYoungOccupancyEnd() {
        return youngEnd;
    }

    public Memory getYoungOccupancyInit() {
        return young;
    }

    public Memory getYoungSpace() {
        return youngAvailable;
    }

    public boolean isEndstamp() {
        // default assumes gc,start not logged (e.g. not preprocessed)
        boolean isEndStamp = true;
        isEndStamp = !logEntry.matches(UnifiedRegEx.TAG_GC_START);
        return isEndStamp;
    }

    protected void setPermOccupancyEnd(Memory permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    protected void setPermOccupancyInit(Memory permGen) {
        this.permGen = permGen;
    }

    protected void setPermSpace(Memory permGenAllocation) {
        this.permGenAllocation = permGenAllocation;
    }

    protected void setTrigger(GcTrigger trigger) {
        this.trigger = trigger;
    }
}
