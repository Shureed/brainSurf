import com.emotiv.Iedk.Edk;
import com.emotiv.Iedk.EdkErrorCode;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayDeque;

/**
 * Created by shureedkabir on 4/9/16.
 */
public class motion {
    public static void main(String[] args) throws Exception {
        Pointer eEvent = Edk.INSTANCE.IEE_EmoEngineEventCreate();
        Pointer eState = Edk.INSTANCE.IEE_EmoStateCreate();
        IntByReference userID = null;
        IntByReference nSamplesTaken = null;
        float secs = 1;
        int state = 0;

        boolean readytocollect = false;

        userID = new IntByReference(0);
        nSamplesTaken = new IntByReference(0);

        if (Edk.INSTANCE.IEE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK
                .ToInt()) {
            System.out.println("Emotiv Engine start up failed.");
            return;
        }

        Pointer hMotionData = Edk.INSTANCE.IEE_MotionDataCreate();
        Edk.INSTANCE.IEE_MotionDataSetBufferSizeInSec(secs);
        System.out.print("Buffer size in secs: ");
        System.out.println(secs);

        System.out.println("Start receiving Motion Data!");
        System.out.println("COUNTER, GYROX, GYROY, GYROZ, ACCX, ACCY, ACCZ, MAGX, "
                + "MAGY, MAGZ, TIMESTAMP");

        ArrayDeque<Double> que = new ArrayDeque<>(5);
        for (int i = 0; i < 5; i++) {
            que.add(8000d);
        }

        Surfer surfer = new Surfer();
        surfer.start();
        boolean spike = false;

        while (true) {
            state = Edk.INSTANCE.IEE_EngineGetNextEvent(eEvent);

            // New event needs to be handled
            if (state == EdkErrorCode.EDK_OK.ToInt()) {
                int eventType = Edk.INSTANCE.IEE_EmoEngineEventGetType(eEvent);
                Edk.INSTANCE.IEE_EmoEngineEventGetUserId(eEvent, userID);

                // Log the EmoState if it has been updated
                if (eventType == Edk.IEE_Event_t.IEE_UserAdded.ToInt())
                    if (userID != null) {
                        System.out.println("User added");
                        readytocollect = true;
                    }
            } else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
                System.out.println("Internal error in Emotiv Engine!");
                break;
            }

            if (readytocollect) {
                Edk.INSTANCE.IEE_MotionDataUpdateHandle(0, hMotionData);

                Edk.INSTANCE.IEE_MotionDataGetNumberOfSample(hMotionData, nSamplesTaken);

                if (nSamplesTaken != null) {
                    if (nSamplesTaken.getValue() != 0) {

                        StringBuilder sb = new StringBuilder();
                        sb.append("\r");
                        double[] data = new double[nSamplesTaken.getValue()];
                        for (int sampleIdx = 0; sampleIdx < nSamplesTaken
                                .getValue(); ++sampleIdx) { //1 - 4 for acceleration
                            //
                            for (int i = 1; i < 4; i++) { //there are 10 total i for each column of data

                                Edk.INSTANCE.IEE_MotionDataGet(hMotionData, i, data,
                                        nSamplesTaken.getValue());
                                sb.append(data[sampleIdx]);
                                sb.append(" ");

                                double diff = data[sampleIdx] - que.remove();
                                if (Math.abs(diff) > 1600){
                                    surfer.next();
                                    System.out.println("spike: "+diff);
                                    que.add(8000d);
                                    Thread.sleep(1000);
                                    spike = true;

                                } else {
                                    que.add(data[sampleIdx]);
                                }
                            }
                            System.out.print(sb.toString());
                            if (spike){
                                spike = false;
                                sampleIdx = nSamplesTaken.getValue();
                            }
                        }
                    }
                }
            }
        }

        Edk.INSTANCE.IEE_EngineDisconnect();
        Edk.INSTANCE.IEE_EmoStateFree(eState);
        Edk.INSTANCE.IEE_EmoEngineEventFree(eEvent);
        System.out.println("Disconnected!");
    }
}
