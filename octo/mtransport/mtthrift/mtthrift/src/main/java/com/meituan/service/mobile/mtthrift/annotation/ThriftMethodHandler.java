/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.meituan.service.mobile.mtthrift.annotation;

import com.facebook.swift.codec.ThriftCodec;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.internal.TProtocolReader;
import com.facebook.swift.codec.internal.TProtocolWriter;
import com.facebook.swift.codec.metadata.ThriftFieldMetadata;
import com.facebook.swift.codec.metadata.ThriftParameterInjection;
import com.facebook.swift.codec.metadata.ThriftType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.meituan.service.mobile.mtthrift.annotation.metadata.ThriftMethodMetadata;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.Map;

import static org.apache.thrift.TApplicationException.*;
import static org.apache.thrift.protocol.TMessageType.*;

@ThreadSafe
public class ThriftMethodHandler
{
    private final String name;
    private final String qualifiedName;
    private final List<ParameterHandler> parameterCodecs;
    private final ThriftCodec<Object> successCodec;
    private final Map<Short, ThriftCodec<Object>> exceptionCodecs;

    public ThriftMethodHandler(ThriftMethodMetadata methodMetadata, ThriftCodecManager codecManager)
    {
        name = methodMetadata.getName();
        qualifiedName = methodMetadata.getQualifiedName();

        // get the thrift codecs for the parameters
        ParameterHandler[] parameters = new ParameterHandler[methodMetadata.getParameters().size()];
        for (ThriftFieldMetadata fieldMetadata : methodMetadata.getParameters()) {
            ThriftParameterInjection parameter = (ThriftParameterInjection) fieldMetadata.getInjections().get(0);

            ParameterHandler handler = new ParameterHandler(
                    fieldMetadata.getId(),
                    fieldMetadata.getName(),
                    (ThriftCodec<Object>) codecManager.getCodec(fieldMetadata.getThriftType()));

            parameters[parameter.getParameterIndex()] = handler;
        }
        parameterCodecs = ImmutableList.copyOf(parameters);

        // get the thrift codecs for the exceptions
        ImmutableMap.Builder<Short, ThriftCodec<Object>> exceptions = ImmutableMap.builder();
        for (Map.Entry<Short, ThriftType> entry : methodMetadata.getExceptions().entrySet()) {
            exceptions.put(entry.getKey(), (ThriftCodec<Object>) codecManager.getCodec(entry.getValue()));
        }
        exceptionCodecs = exceptions.build();

        // get the thrift codec for the return value
        successCodec = (ThriftCodec<Object>) codecManager.getCodec(methodMetadata.getReturnType());
    }

    public String getName()
    {
        return name;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public Object invoke(
            final TProtocol inputProtocol,
            final TProtocol outputProtocol,
            final int sequenceId,
            final Object... args)
            throws Exception
    {
        Object results = null;

        // write request
        writeArguments(outputProtocol, sequenceId, args);
        // Don't need to copy the output buffer for sync case


        try {
            waitForResponse(inputProtocol, sequenceId);
            results = readResponse(inputProtocol);
        } catch (Exception e) {
            throw e;
        }


        return results;
    }

    public void send(
            final TProtocol outputProtocol,
            final int sequenceId,
            final Object... args)
            throws Exception
    {
        // write request
        writeArguments(outputProtocol, sequenceId, args);
        // Don't need to copy the output buffer for sync case
    }

    public Object receive(
            final TProtocol inputProtocol,
            final int sequenceId)
            throws Exception
    {
        Object results = null;

        try {
            waitForResponse(inputProtocol, sequenceId);
            results = readResponse(inputProtocol);
        } catch (Exception e) {
            throw e;
        }

        return results;
    }

    private Object readResponse(TProtocol in)
            throws Exception
    {
        TProtocolReader reader = new TProtocolReader(in);
        reader.readStructBegin();
        Object results = null;
        Exception exception = null;
        while (reader.nextField()) {
            if (reader.getFieldId() == 0) {
                results = reader.readField(successCodec);
            }
            else {
                ThriftCodec<Object> exceptionCodec = exceptionCodecs.get(reader.getFieldId());
                if (exceptionCodec != null) {
                    exception = (Exception) reader.readField(exceptionCodec);
                }
                else {
                    reader.skipFieldData();
                }
            }
        }
        reader.readStructEnd();
        in.readMessageEnd();

        if (exception != null) {
            throw exception;
        }

        if (successCodec.getType() == ThriftType.VOID) {
            // TODO: check for non-null return from a void function?
            return null;
        }

        if (results == null) {
            throw new TApplicationException(TApplicationException.MISSING_RESULT, name + " failed: unknown result");
        }
        return results;
    }

    private void writeArguments(TProtocol out, int sequenceId, Object[] args)
            throws Exception
    {
        // Note that though setting message type to ONEWAY can be helpful when looking at packet
        // captures, some clients always send CALL and so servers are forced to rely on the "oneway"
        // attribute on thrift method in the interface definition, rather than checking the message
        // type.
        out.writeMessageBegin(new TMessage(name, CALL, sequenceId));

        // write the parameters
        TProtocolWriter writer = new TProtocolWriter(out);
        writer.writeStructBegin(name + "_args");
        for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            ParameterHandler parameter = parameterCodecs.get(i);
            writer.writeField(parameter.getName(), parameter.getId(), parameter.getCodec(), value);
        }
        writer.writeStructEnd();

        out.writeMessageEnd();
        out.getTransport().flush();
    }

    private void waitForResponse(TProtocol in, int sequenceId)
            throws TException
    {
        TMessage message = in.readMessageBegin();
        if (message.type == EXCEPTION) {
            TApplicationException exception = TApplicationException.read(in);
            in.readMessageEnd();
            throw exception;
        }
        if (message.type != REPLY) {
            throw new TApplicationException(INVALID_MESSAGE_TYPE,
                    "Received invalid message type " + message.type + " from server");
        }
        if (!message.name.equals(this.name)) {
            throw new TApplicationException(WRONG_METHOD_NAME,
                    "Wrong method name in reply: expected " + this.name + " but received " + message.name);
        }
        if (message.seqid != sequenceId) {
            throw new TApplicationException(BAD_SEQUENCE_ID, name + " failed: out of sequence response");
        }
    }

    public ParameterHandler getParameterCodecs(int i) {
        return parameterCodecs.get(i);
    }

    public ThriftCodec<Object> getSuccessCodec() {
        return successCodec;
    }

    public ThriftCodec<Object> getExceptionCodec(short fieldId) {
        return exceptionCodecs.get(fieldId);
    }

    public static final class ParameterHandler
    {
        private final short id;
        private final String name;
        private final ThriftCodec<Object> codec;

        private ParameterHandler(short id, String name, ThriftCodec<Object> codec)
        {
            this.id = id;
            this.name = name;
            this.codec = codec;
        }

        public short getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public ThriftCodec<Object> getCodec()
        {
            return codec;
        }
    }
}
