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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestZMarkEndOldEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.213s][info][gc,phases   ] GC(2) O: Pause Mark End 0.005ms";
        assertEquals(JdkUtil.LogEventType.Z_MARK_END_OLD, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.Z_MARK_END_OLD + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.213s][info][gc,phases   ] GC(2) O: Pause Mark End 0.005ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.Z_MARK_END_OLD.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.213s][info][gc,phases   ] GC(2) O: Pause Mark End 0.005ms";
        assertTrue(ZMarkEndOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_MARK_END_OLD.toString() + ".");
        ZMarkEndOldEvent event = new ZMarkEndOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.Z_MARK_END_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (213 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(5, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.213s][info][gc,phases   ] GC(2) O: Pause Mark End 0.005ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ZMarkEndOldEvent,
                JdkUtil.LogEventType.Z_MARK_END_OLD.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.Z_MARK_END_OLD),
                JdkUtil.LogEventType.Z_MARK_END_OLD.toString() + " not indentified as reportable.");
    }

    /**
     * Test with time, uptime decorator.
     */
    @Test
    void testTimestampTimeUptime() {
        String logLine = "[2021-03-09T14:45:02.441-0300][0.213s][info][gc,phases   ] GC(2) O: Pause Mark End 0.005ms";
        assertTrue(ZMarkEndOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_MARK_END_OLD.toString() + ".");
        ZMarkEndOldEvent event = new ZMarkEndOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.Z_MARK_END_OLD.toString(), event.getName(), "Event name incorrect.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.Z_MARK_END_OLD);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.Z_MARK_END_OLD.toString() + " not indentified as unified.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[0.213s][info][gc,phases   ] GC(2) O: Pause Mark End 0.005ms   ";
        assertTrue(ZMarkEndOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_MARK_END_OLD.toString() + ".");
    }
}
