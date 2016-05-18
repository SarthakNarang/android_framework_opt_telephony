/* Copyright (c) 2016, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.android.internal.telephony.uicc;

import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.android.internal.telephony.TelephonyTest;

import com.android.internal.telephony.CommandsInterface;
import static com.android.internal.telephony.TelephonyTestUtils.waitForMs;
import android.content.Context;
import android.os.AsyncResult;
import android.os.HandlerThread;
import android.os.Message;

public class IccRecordsTest extends TelephonyTest {

    @Mock
    private CommandsInterface mMockCI;

    private IccRecords mIccRecords;

    private SIMFileHandler mSIMFileHandler;

    private class IccRecordsTestHandler extends HandlerThread {
        private IccRecordsTestHandler(String name) {
            super(name);
        }

        @Override
        public void onLooperPrepared() {
            mIccRecords = new SIMRecords(mUiccCardApplication3gpp, mContext, mMockCI);
            mSIMFileHandler = new SIMFileHandler(mUiccCardApplication3gpp, null, mMockCI);
            setReady(true);
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp(this.getClass().getSimpleName());
        mMockCI = mock(CommandsInterface.class);
        new IccRecordsTestHandler(TAG).start();
        waitUntilReady();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        mIccRecords.dispose();
    }

    @Test
    public void testDisposeCallsUnregisterForIccRefresh() {
        // verify called below when IccRecords object is created
        verify(mMockCI).registerForIccRefresh(any(IccRecords.class),
                    eq(IccRecords.EVENT_REFRESH), isNull());
        mIccRecords.dispose();
        // verify called within dispose
        verify(mMockCI).unregisterForIccRefresh(any(IccRecords.class));

    }

   @Test
    public void testgetPLMNWACTFilePath() {
        String filePath = mSIMFileHandler.getEFPath(IccConstants.EF_PLMNWACT);
        String expectedfilePath = IccConstants.MF_SIM + IccConstants.DF_GSM;
        assertEquals(expectedfilePath, filePath);
    }

    @Test
    public void testGetSmsCapacityOnIcc() {
        // set the number of records to 500
        int[] records = new int[3];
        records[2] = 500;
        Message fetchCapacityDone = mIccRecords.obtainMessage(IccRecords.EVENT_GET_SMS_RECORD_SIZE_DONE);
        AsyncResult.forMessage(fetchCapacityDone, records, null);
        fetchCapacityDone.sendToTarget();

        // verify whether the count is 500
        waitForMs(200);
        assertEquals(mIccRecords.getSmsCapacityOnIcc(), 500);
    }

}