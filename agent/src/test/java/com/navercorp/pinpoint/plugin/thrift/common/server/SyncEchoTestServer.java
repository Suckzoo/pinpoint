/*
 * Copyright 2015 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.thrift.common.server;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.thrift.TBaseProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.BlockType;
import com.navercorp.pinpoint.plugin.thrift.common.client.AsyncEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.client.SyncEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;

/**
 * @author HyunGil Jeong
 */
public abstract class SyncEchoTestServer<T extends TServer> extends EchoTestServer<T> {

    protected SyncEchoTestServer(T server) throws TTransportException {
        super(server);
    }
    
    @Override
    public void verifyServerTraces(PluginTestVerifier verifier) throws Exception {
        Method process = TBaseProcessor.class.getDeclaredMethod("process", TProtocol.class, TProtocol.class);
        // SpanEvent - TBaseProcessor.process
        verifier.verifyTraceBlock(BlockType.EVENT, "THRIFT_SERVER_INTERNAL", process, null, null, null, null);
        // RootSpan - Thrift Server Invocation
        verifier.verifyTraceBlock(
                BlockType.ROOT, // BlockType,
                "THRIFT_SERVER", // ServiceType,
                "Thrift Server Invocation", // Method
                "com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo", // rpc
                SERVER_ADDRESS.getHostName() + ":" + SERVER_ADDRESS.getPort(), // endPoint
                SERVER_ADDRESS.getHostName(), // remoteAddress
                null // destinationId
        );
    }
    
    public static class SyncEchoTestServerFactory {
        
        private static TProcessor getProcessor() {
            return new EchoService.Processor<EchoService.Iface>(new EchoServiceHandler());
        }
        
        public static SyncEchoTestServer<TSimpleServer> simpleServer() throws TTransportException {
            TSimpleServer server = new TSimpleServer(new TSimpleServer.Args(new TServerSocket(SERVER_PORT))
                    .processor(getProcessor())
                    .inputProtocolFactory(PROTOCOL_FACTORY)
                    .outputProtocolFactory(PROTOCOL_FACTORY));
            return new SyncEchoTestServer<TSimpleServer>(server) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.Client();
                }
                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client();
                }
            };
        }
        
        public static SyncEchoTestServer<TThreadPoolServer> threadedPoolServer() throws TTransportException {
            TThreadPoolServer server = new TThreadPoolServer(new TThreadPoolServer.Args(new TServerSocket(SERVER_PORT))
                    .processor(getProcessor())
                    .inputProtocolFactory(PROTOCOL_FACTORY)
                    .outputProtocolFactory(PROTOCOL_FACTORY));
            return new SyncEchoTestServer<TThreadPoolServer>(server) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.Client();
                }
                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client();
                }
            };
        }
        
        public static SyncEchoTestServer<TThreadedSelectorServer> threadedSelectorServer() throws TTransportException {
            TThreadedSelectorServer server = new TThreadedSelectorServer(new TThreadedSelectorServer.Args(new TNonblockingServerSocket(SERVER_PORT))
                    .processor(getProcessor())
                    .inputProtocolFactory(PROTOCOL_FACTORY)
                    .outputProtocolFactory(PROTOCOL_FACTORY));
            return new SyncEchoTestServer<TThreadedSelectorServer>(server) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.ClientForNonblockingServer();
                }
                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client();
                }
            };
        }
        
        public static SyncEchoTestServer<TNonblockingServer> nonblockingServer() throws TTransportException {
            TNonblockingServer server = new TNonblockingServer(new TNonblockingServer.Args(new TNonblockingServerSocket(SERVER_PORT))
                    .processor(getProcessor())
                    .inputProtocolFactory(PROTOCOL_FACTORY)
                    .outputProtocolFactory(PROTOCOL_FACTORY));
            return new SyncEchoTestServer<TNonblockingServer>(server) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.ClientForNonblockingServer();
                }
                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client();
                }
            };
        }
        
        public static SyncEchoTestServer<THsHaServer> halfSyncHalfAsyncServer() throws TTransportException {
            THsHaServer server = new THsHaServer(new THsHaServer.Args(new TNonblockingServerSocket(SERVER_PORT))
                    .processor(getProcessor())
                    .inputProtocolFactory(PROTOCOL_FACTORY)
                    .outputProtocolFactory(PROTOCOL_FACTORY));
            return new SyncEchoTestServer<THsHaServer>(server) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.ClientForNonblockingServer();
                }
                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client();
                }
            };
        }
    }


}
