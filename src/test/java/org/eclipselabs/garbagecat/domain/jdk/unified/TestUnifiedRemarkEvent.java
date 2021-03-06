/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
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
public class TestUnifiedRemarkEvent {

    @Test
    public void testLogLine() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertTrue(UnifiedRemarkEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".");
        UnifiedRemarkEvent event = new UnifiedRemarkEvent(logLine);
        assertEquals((long) (7944 - 1),event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(1767,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_REMARK,JdkUtil.identifyEventType(logLine),JdkUtil.LogEventType.UNIFIED_REMARK + "not identified.");
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedRemarkEvent, JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not parsed.");
    }

    @Test
    public void testIsBlocking() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as blocking.");
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_REMARK), JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as reportable.");
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_REMARK);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes), JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as unified.");
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms           ";
        assertTrue(UnifiedRemarkEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".");
    }

    @Test
    public void testLogLinePreprocessedWithTimesData() {
        String logLine = "[16.053s][info][gc            ] GC(969) Pause Remark 29M->29M(46M) 2.328ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedRemarkEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".");
        UnifiedRemarkEvent event = new UnifiedRemarkEvent(logLine);
        assertEquals((long) (16053 - 2),event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(2328,event.getDuration(),"Duration not parsed correctly.");
        assertEquals(1,event.getTimeUser(),"User time not parsed correctly.");
        assertEquals(0,event.getTimeReal(),"Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE,event.getParallelism(),"Parallelism not calculated correctly.");
    }

    @Test
    public void testLogLinePreprocessedWithTimesData12SpacesAfterGc() {
        String logLine = "[0.091s][info][gc           ] GC(3) Pause Remark 0M->0M(2M) 0.414ms User=0.00s "
                + "Sys=0.00s Real=0.00s";
        assertTrue(UnifiedRemarkEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".");
    }

}
