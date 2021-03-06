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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestTenuringDistributionEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    public void testReportable() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertTrue(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " incorrectly indentified as not reportable.");
    }

    @Test
    public void testIdentifyEventType() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertTrue(JdkUtil.identifyEventType(logLine).equals(LogEventType.TENURING_DISTRIBUTION), JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " not indentified.");
    }

    @Test
    public void testParseLogLine() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof TenuringDistributionEvent, JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " not indentified.");
    }

    @Test
    public void testDesiredSurvivorSizeLine() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertTrue(TenuringDistributionEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + ".");
    }

    @Test
    public void testAgeLine() {
        String logLine = "- age 1: 3177664 bytes, 3177664 total";
        assertTrue(TenuringDistributionEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + ".");
    }

}
