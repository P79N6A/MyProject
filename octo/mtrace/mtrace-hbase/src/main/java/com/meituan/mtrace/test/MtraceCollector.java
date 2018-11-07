/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.meituan.mtrace.test;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MtraceCollector {

  public interface Iface {

    public boolean collect(ByteBuffer log) throws org.apache.thrift.TException;

  }

  public interface AsyncIface {

    public void collect(ByteBuffer log, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.collect_call> resultHandler) throws org.apache.thrift.TException;

  }

  public static class Client extends org.apache.thrift.TServiceClient implements Iface {
    public static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {
      public Factory() {}
      public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
        return new Client(prot);
      }
      public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
        return new Client(iprot, oprot);
      }
    }

    public Client(org.apache.thrift.protocol.TProtocol prot)
    {
      super(prot, prot);
    }

    public Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
      super(iprot, oprot);
    }

    public boolean collect(ByteBuffer log) throws org.apache.thrift.TException
    {
      send_collect(log);
      return recv_collect();
    }

    public void send_collect(ByteBuffer log) throws org.apache.thrift.TException
    {
      collect_args args = new collect_args();
      args.setLog(log);
      sendBase("collect", args);
    }

    public boolean recv_collect() throws org.apache.thrift.TException
    {
      collect_result result = new collect_result();
      receiveBase(result, "collect");
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "collect failed: unknown result");
    }

  }
  public static class AsyncClient extends org.apache.thrift.async.TAsyncClient implements AsyncIface {
    public static class Factory implements org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {
      private org.apache.thrift.async.TAsyncClientManager clientManager;
      private org.apache.thrift.protocol.TProtocolFactory protocolFactory;
      public Factory(org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
        this.clientManager = clientManager;
        this.protocolFactory = protocolFactory;
      }
      public AsyncClient getAsyncClient(org.apache.thrift.transport.TNonblockingTransport transport) {
        return new AsyncClient(protocolFactory, clientManager, transport);
      }
    }

    public AsyncClient(org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.transport.TNonblockingTransport transport) {
      super(protocolFactory, clientManager, transport);
    }

    public void collect(ByteBuffer log, org.apache.thrift.async.AsyncMethodCallback<collect_call> resultHandler) throws org.apache.thrift.TException {
      checkReady();
      collect_call method_call = new collect_call(log, resultHandler, this, ___protocolFactory, ___transport);
      this.___currentMethod = method_call;
      ___manager.call(method_call);
    }

    public static class collect_call extends org.apache.thrift.async.TAsyncMethodCall {
      private ByteBuffer log;
      public collect_call(ByteBuffer log, org.apache.thrift.async.AsyncMethodCallback<collect_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.log = log;
      }

      public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("collect", org.apache.thrift.protocol.TMessageType.CALL, 0));
        collect_args args = new collect_args();
        args.setLog(log);
        args.write(prot);
        prot.writeMessageEnd();
      }

      public boolean getResult() throws org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_collect();
      }
    }

  }

  public static class Processor<I extends Iface> extends org.apache.thrift.TBaseProcessor<I> implements org.apache.thrift.TProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());
    public Processor(I iface) {
      super(iface, getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
    }

    protected Processor(I iface, Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      super(iface, getProcessMap(processMap));
    }

    private static <I extends Iface> Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> getProcessMap(Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      processMap.put("collect", new collect());
      return processMap;
    }

    private static class collect<I extends Iface> extends org.apache.thrift.ProcessFunction<I, collect_args> {
      public collect() {
        super("collect");
      }

      protected collect_args getEmptyArgsInstance() {
        return new collect_args();
      }

      protected collect_result getResult(I iface, collect_args args) throws org.apache.thrift.TException {
        collect_result result = new collect_result();
        result.success = iface.collect(args.log);
        result.setSuccessIsSet(true);
        return result;
      }
    }

  }

  public static class collect_args implements org.apache.thrift.TBase<collect_args, collect_args._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("collect_args");

    private static final org.apache.thrift.protocol.TField LOG_FIELD_DESC = new org.apache.thrift.protocol.TField("log", org.apache.thrift.protocol.TType.STRING, (short)1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new collect_argsStandardSchemeFactory());
      schemes.put(TupleScheme.class, new collect_argsTupleSchemeFactory());
    }

    public ByteBuffer log; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      LOG((short)1, "log");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: // LOG
            return LOG;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments
    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.LOG, new org.apache.thrift.meta_data.FieldMetaData("log", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING          , true)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(collect_args.class, metaDataMap);
    }

    public collect_args() {
    }

    public collect_args(
      ByteBuffer log)
    {
      this();
      this.log = log;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public collect_args(collect_args other) {
      if (other.isSetLog()) {
        this.log = org.apache.thrift.TBaseHelper.copyBinary(other.log);
;
      }
    }

    public collect_args deepCopy() {
      return new collect_args(this);
    }

    @Override
    public void clear() {
      this.log = null;
    }

    public byte[] getLog() {
      setLog(org.apache.thrift.TBaseHelper.rightSize(log));
      return log == null ? null : log.array();
    }

    public ByteBuffer bufferForLog() {
      return log;
    }

    public collect_args setLog(byte[] log) {
      setLog(log == null ? (ByteBuffer)null : ByteBuffer.wrap(log));
      return this;
    }

    public collect_args setLog(ByteBuffer log) {
      this.log = log;
      return this;
    }

    public void unsetLog() {
      this.log = null;
    }

    /** Returns true if field log is set (has been assigned a value) and false otherwise */
    public boolean isSetLog() {
      return this.log != null;
    }

    public void setLogIsSet(boolean value) {
      if (!value) {
        this.log = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case LOG:
        if (value == null) {
          unsetLog();
        } else {
          setLog((ByteBuffer)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case LOG:
        return getLog();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case LOG:
        return isSetLog();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof collect_args)
        return this.equals((collect_args)that);
      return false;
    }

    public boolean equals(collect_args that) {
      if (that == null)
        return false;

      boolean this_present_log = true && this.isSetLog();
      boolean that_present_log = true && that.isSetLog();
      if (this_present_log || that_present_log) {
        if (!(this_present_log && that_present_log))
          return false;
        if (!this.log.equals(that.log))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(collect_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      collect_args typedOther = (collect_args)other;

      lastComparison = Boolean.valueOf(isSetLog()).compareTo(typedOther.isSetLog());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetLog()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.log, typedOther.log);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("collect_args(");
      boolean first = true;

      sb.append("log:");
      if (this.log == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.log, sb);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private static class collect_argsStandardSchemeFactory implements SchemeFactory {
      public collect_argsStandardScheme getScheme() {
        return new collect_argsStandardScheme();
      }
    }

    private static class collect_argsStandardScheme extends StandardScheme<collect_args> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, collect_args struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 1: // LOG
              if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                struct.log = iprot.readBinary();
                struct.setLogIsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            default:
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
          }
          iprot.readFieldEnd();
        }
        iprot.readStructEnd();

        // check for required fields of primitive type, which can't be checked in the validate method
        struct.validate();
      }

      public void write(org.apache.thrift.protocol.TProtocol oprot, collect_args struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.log != null) {
          oprot.writeFieldBegin(LOG_FIELD_DESC);
          oprot.writeBinary(struct.log);
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class collect_argsTupleSchemeFactory implements SchemeFactory {
      public collect_argsTupleScheme getScheme() {
        return new collect_argsTupleScheme();
      }
    }

    private static class collect_argsTupleScheme extends TupleScheme<collect_args> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, collect_args struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetLog()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetLog()) {
          oprot.writeBinary(struct.log);
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, collect_args struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          struct.log = iprot.readBinary();
          struct.setLogIsSet(true);
        }
      }
    }

  }

  public static class collect_result implements org.apache.thrift.TBase<collect_result, collect_result._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("collect_result");

    private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.BOOL, (short)0);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new collect_resultStandardSchemeFactory());
      schemes.put(TupleScheme.class, new collect_resultTupleSchemeFactory());
    }

    public boolean success; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      SUCCESS((short)0, "success");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: // SUCCESS
            return SUCCESS;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments
    private static final int __SUCCESS_ISSET_ID = 0;
    private BitSet __isset_bit_vector = new BitSet(1);
    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(collect_result.class, metaDataMap);
    }

    public collect_result() {
    }

    public collect_result(
      boolean success)
    {
      this();
      this.success = success;
      setSuccessIsSet(true);
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public collect_result(collect_result other) {
      __isset_bit_vector.clear();
      __isset_bit_vector.or(other.__isset_bit_vector);
      this.success = other.success;
    }

    public collect_result deepCopy() {
      return new collect_result(this);
    }

    @Override
    public void clear() {
      setSuccessIsSet(false);
      this.success = false;
    }

    public boolean isSuccess() {
      return this.success;
    }

    public collect_result setSuccess(boolean success) {
      this.success = success;
      setSuccessIsSet(true);
      return this;
    }

    public void unsetSuccess() {
      __isset_bit_vector.clear(__SUCCESS_ISSET_ID);
    }

    /** Returns true if field success is set (has been assigned a value) and false otherwise */
    public boolean isSetSuccess() {
      return __isset_bit_vector.get(__SUCCESS_ISSET_ID);
    }

    public void setSuccessIsSet(boolean value) {
      __isset_bit_vector.set(__SUCCESS_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((Boolean)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return Boolean.valueOf(isSuccess());

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof collect_result)
        return this.equals((collect_result)that);
      return false;
    }

    public boolean equals(collect_result that) {
      if (that == null)
        return false;

      boolean this_present_success = true;
      boolean that_present_success = true;
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (this.success != that.success)
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(collect_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      collect_result typedOther = (collect_result)other;

      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
      }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("collect_result(");
      boolean first = true;

      sb.append("success:");
      sb.append(this.success);
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private static class collect_resultStandardSchemeFactory implements SchemeFactory {
      public collect_resultStandardScheme getScheme() {
        return new collect_resultStandardScheme();
      }
    }

    private static class collect_resultStandardScheme extends StandardScheme<collect_result> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, collect_result struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 0: // SUCCESS
              if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                struct.success = iprot.readBool();
                struct.setSuccessIsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            default:
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
          }
          iprot.readFieldEnd();
        }
        iprot.readStructEnd();

        // check for required fields of primitive type, which can't be checked in the validate method
        struct.validate();
      }

      public void write(org.apache.thrift.protocol.TProtocol oprot, collect_result struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeBool(struct.success);
        oprot.writeFieldEnd();
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class collect_resultTupleSchemeFactory implements SchemeFactory {
      public collect_resultTupleScheme getScheme() {
        return new collect_resultTupleScheme();
      }
    }

    private static class collect_resultTupleScheme extends TupleScheme<collect_result> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, collect_result struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetSuccess()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetSuccess()) {
          oprot.writeBool(struct.success);
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, collect_result struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          struct.success = iprot.readBool();
          struct.setSuccessIsSet(true);
        }
      }
    }

  }

}