/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liulishuo.filedownloader.message;

import com.liulishuo.filedownloader.util.FileDownloadLog;

/**
 * The internal message snapshot station.
 * <p>
 * Making message snapshots keep flowing in order.
 */
public class MessageSnapshotFlow {

    private volatile MessageSnapshotThreadPool flowThreadPool;
    private volatile MessageReceiver receiver;

    public static final class HolderClass {
        private static final MessageSnapshotFlow INSTANCE = new MessageSnapshotFlow();
    }

    public static MessageSnapshotFlow getImpl() {
        return HolderClass.INSTANCE;
    }

    public void setReceiver(MessageReceiver receiver) {
        this.receiver = receiver;
        if (receiver == null) {
            this.flowThreadPool = null;
        } else {
            this.flowThreadPool = new MessageSnapshotThreadPool(5, receiver);
        }
    }

//    调用两次，有两次消息流动
    public void inflow(final MessageSnapshot snapshot) {
        if (snapshot instanceof IFlowDirectly) {
            if (receiver != null) {
//                FileDownloadLog.e(this,"inflow = %s",receiver.toString());
                receiver.receive(snapshot);
            }
        } else {
            if (flowThreadPool != null) {
                flowThreadPool.execute(snapshot);
            }
        }

    }

// 消息接收者有两个
    public interface MessageReceiver {
        void receive(MessageSnapshot snapshot);
    }
}
