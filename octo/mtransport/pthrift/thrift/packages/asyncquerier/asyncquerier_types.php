<?php
/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
include_once $GLOBALS['THRIFT_ROOT'].'/Thrift.php';

include_once $GLOBALS['THRIFT_ROOT'].'/packages/fb303/fb303_types.php';

$GLOBALS['E_FIELDTYPES'] = array(
  'NULL' => 0,
  'INT' => 1,
  'FLOAT' => 2,
  'STRING' => 3,
);

final class FIELDTYPES {
  const NULL = 0;
  const INT = 1;
  const FLOAT = 2;
  const STRING = 3;
  static public $__names = array(
    0 => 'NULL',
    1 => 'INT',
    2 => 'FLOAT',
    3 => 'STRING',
  );
}

$GLOBALS['E_DATASTYLE'] = array(
  'STRING' => 1,
  'TABLE' => 2,
  'MULTIDIMENSION' => 3,
  'STRING_WITH_HEADER' => 4,
);

final class DATASTYLE {
  const STRING = 1;
  const TABLE = 2;
  const MULTIDIMENSION = 3;
  const STRING_WITH_HEADER = 4;
  static public $__names = array(
    1 => 'STRING',
    2 => 'TABLE',
    3 => 'MULTIDIMENSION',
    4 => 'STRING_WITH_HEADER',
  );
}

$GLOBALS['E_FILTERTYPE'] = array(
  'SENSITIVES' => 1,
  'TABLE_READY' => 2,
  'SQL_PERFORMANCE' => 4,
  'AUTH' => 8,
);

final class FILTERTYPE {
  const SENSITIVES = 1;
  const TABLE_READY = 2;
  const SQL_PERFORMANCE = 4;
  const AUTH = 8;
  static public $__names = array(
    1 => 'SENSITIVES',
    2 => 'TABLE_READY',
    4 => 'SQL_PERFORMANCE',
    8 => 'AUTH',
  );
}

$GLOBALS['E_QUERYSTATUS'] = array(
  'NEW_QUERY' => 0,
  'IN_QUEUE' => 1,
  'PROCESSING' => 2,
  'FINISH' => 3,
  'TOOBIG' => 4,
  'ERROR' => 5,
  'DBERROR' => 6,
);

final class QUERYSTATUS {
  const NEW_QUERY = 0;
  const IN_QUEUE = 1;
  const PROCESSING = 2;
  const FINISH = 3;
  const TOOBIG = 4;
  const ERROR = 5;
  const DBERROR = 6;
  static public $__names = array(
    0 => 'NEW_QUERY',
    1 => 'IN_QUEUE',
    2 => 'PROCESSING',
    3 => 'FINISH',
    4 => 'TOOBIG',
    5 => 'ERROR',
    6 => 'DBERROR',
  );
}

class FieldDescription {
  static $_TSPEC;

  public $name = null;
  public $type = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'name',
          'type' => TType::STRING,
          ),
        2 => array(
          'var' => 'type',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['name'])) {
        $this->name = $vals['name'];
      }
      if (isset($vals['type'])) {
        $this->type = $vals['type'];
      }
    }
  }

  public function getName() {
    return 'FieldDescription';
  }

  public function read($input)
  {
    $xfer = 0;
    $fname = null;
    $ftype = 0;
    $fid = 0;
    $xfer += $input->readStructBegin($fname);
    while (true)
    {
      $xfer += $input->readFieldBegin($fname, $ftype, $fid);
      if ($ftype == TType::STOP) {
        break;
      }
      switch ($fid)
      {
        case 1:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->name);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->type);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        default:
          $xfer += $input->skip($ftype);
          break;
      }
      $xfer += $input->readFieldEnd();
    }
    $xfer += $input->readStructEnd();
    return $xfer;
  }

  public function write($output) {
    $xfer = 0;
    $xfer += $output->writeStructBegin('FieldDescription');
    if ($this->name !== null) {
      $xfer += $output->writeFieldBegin('name', TType::STRING, 1);
      $xfer += $output->writeString($this->name);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->type !== null) {
      $xfer += $output->writeFieldBegin('type', TType::I32, 2);
      $xfer += $output->writeI32($this->type);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class DataSet {
  static $_TSPEC;

  public $style = null;
  public $description = null;
  public $data = null;
  public $rowcount = null;
  public $datainfo = "";

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'style',
          'type' => TType::I32,
          ),
        2 => array(
          'var' => 'description',
          'type' => TType::LST,
          'etype' => TType::STRUCT,
          'elem' => array(
            'type' => TType::STRUCT,
            'class' => 'FieldDescription',
            ),
          ),
        3 => array(
          'var' => 'data',
          'type' => TType::STRING,
          ),
        4 => array(
          'var' => 'rowcount',
          'type' => TType::I32,
          ),
        5 => array(
          'var' => 'datainfo',
          'type' => TType::STRING,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['style'])) {
        $this->style = $vals['style'];
      }
      if (isset($vals['description'])) {
        $this->description = $vals['description'];
      }
      if (isset($vals['data'])) {
        $this->data = $vals['data'];
      }
      if (isset($vals['rowcount'])) {
        $this->rowcount = $vals['rowcount'];
      }
      if (isset($vals['datainfo'])) {
        $this->datainfo = $vals['datainfo'];
      }
    }
  }

  public function getName() {
    return 'DataSet';
  }

  public function read($input)
  {
    $xfer = 0;
    $fname = null;
    $ftype = 0;
    $fid = 0;
    $xfer += $input->readStructBegin($fname);
    while (true)
    {
      $xfer += $input->readFieldBegin($fname, $ftype, $fid);
      if ($ftype == TType::STOP) {
        break;
      }
      switch ($fid)
      {
        case 1:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->style);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::LST) {
            $this->description = array();
            $_size0 = 0;
            $_etype3 = 0;
            $xfer += $input->readListBegin($_etype3, $_size0);
            for ($_i4 = 0; $_i4 < $_size0; ++$_i4)
            {
              $elem5 = null;
              $elem5 = new FieldDescription();
              $xfer += $elem5->read($input);
              $this->description []= $elem5;
            }
            $xfer += $input->readListEnd();
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->data);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->rowcount);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 5:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->datainfo);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        default:
          $xfer += $input->skip($ftype);
          break;
      }
      $xfer += $input->readFieldEnd();
    }
    $xfer += $input->readStructEnd();
    return $xfer;
  }

  public function write($output) {
    $xfer = 0;
    $xfer += $output->writeStructBegin('DataSet');
    if ($this->style !== null) {
      $xfer += $output->writeFieldBegin('style', TType::I32, 1);
      $xfer += $output->writeI32($this->style);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->description !== null) {
      if (!is_array($this->description)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('description', TType::LST, 2);
      {
        $output->writeListBegin(TType::STRUCT, count($this->description));
        {
          foreach ($this->description as $iter6)
          {
            $xfer += $iter6->write($output);
          }
        }
        $output->writeListEnd();
      }
      $xfer += $output->writeFieldEnd();
    }
    if ($this->data !== null) {
      $xfer += $output->writeFieldBegin('data', TType::STRING, 3);
      $xfer += $output->writeString($this->data);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->rowcount !== null) {
      $xfer += $output->writeFieldBegin('rowcount', TType::I32, 4);
      $xfer += $output->writeI32($this->rowcount);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->datainfo !== null) {
      $xfer += $output->writeFieldBegin('datainfo', TType::STRING, 5);
      $xfer += $output->writeString($this->datainfo);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class QueryInfo {
  static $_TSPEC;

  public $queryid = null;
  public $ts = null;
  public $lastcachetime = null;
  public $exptime = null;
  public $statuscode = null;
  public $isfinished = null;
  public $message = "";
  public $timetaken = 0;
  public $connuri = null;
  public $expr = null;
  public $context = null;
  public $arrive_ts = null;
  public $realconnuri = null;
  public $servercontext = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'queryid',
          'type' => TType::STRING,
          ),
        2 => array(
          'var' => 'ts',
          'type' => TType::I64,
          ),
        3 => array(
          'var' => 'lastcachetime',
          'type' => TType::I64,
          ),
        4 => array(
          'var' => 'exptime',
          'type' => TType::I64,
          ),
        5 => array(
          'var' => 'statuscode',
          'type' => TType::I32,
          ),
        6 => array(
          'var' => 'isfinished',
          'type' => TType::BOOL,
          ),
        10 => array(
          'var' => 'message',
          'type' => TType::STRING,
          ),
        16 => array(
          'var' => 'timetaken',
          'type' => TType::I64,
          ),
        18 => array(
          'var' => 'connuri',
          'type' => TType::STRING,
          ),
        19 => array(
          'var' => 'expr',
          'type' => TType::STRING,
          ),
        20 => array(
          'var' => 'context',
          'type' => TType::MAP,
          'ktype' => TType::STRING,
          'vtype' => TType::STRING,
          'key' => array(
            'type' => TType::STRING,
          ),
          'val' => array(
            'type' => TType::STRING,
            ),
          ),
        21 => array(
          'var' => 'arrive_ts',
          'type' => TType::I64,
          ),
        22 => array(
          'var' => 'realconnuri',
          'type' => TType::STRING,
          ),
        23 => array(
          'var' => 'servercontext',
          'type' => TType::MAP,
          'ktype' => TType::STRING,
          'vtype' => TType::STRING,
          'key' => array(
            'type' => TType::STRING,
          ),
          'val' => array(
            'type' => TType::STRING,
            ),
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['queryid'])) {
        $this->queryid = $vals['queryid'];
      }
      if (isset($vals['ts'])) {
        $this->ts = $vals['ts'];
      }
      if (isset($vals['lastcachetime'])) {
        $this->lastcachetime = $vals['lastcachetime'];
      }
      if (isset($vals['exptime'])) {
        $this->exptime = $vals['exptime'];
      }
      if (isset($vals['statuscode'])) {
        $this->statuscode = $vals['statuscode'];
      }
      if (isset($vals['isfinished'])) {
        $this->isfinished = $vals['isfinished'];
      }
      if (isset($vals['message'])) {
        $this->message = $vals['message'];
      }
      if (isset($vals['timetaken'])) {
        $this->timetaken = $vals['timetaken'];
      }
      if (isset($vals['connuri'])) {
        $this->connuri = $vals['connuri'];
      }
      if (isset($vals['expr'])) {
        $this->expr = $vals['expr'];
      }
      if (isset($vals['context'])) {
        $this->context = $vals['context'];
      }
      if (isset($vals['arrive_ts'])) {
        $this->arrive_ts = $vals['arrive_ts'];
      }
      if (isset($vals['realconnuri'])) {
        $this->realconnuri = $vals['realconnuri'];
      }
      if (isset($vals['servercontext'])) {
        $this->servercontext = $vals['servercontext'];
      }
    }
  }

  public function getName() {
    return 'QueryInfo';
  }

  public function read($input)
  {
    $xfer = 0;
    $fname = null;
    $ftype = 0;
    $fid = 0;
    $xfer += $input->readStructBegin($fname);
    while (true)
    {
      $xfer += $input->readFieldBegin($fname, $ftype, $fid);
      if ($ftype == TType::STOP) {
        break;
      }
      switch ($fid)
      {
        case 1:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->queryid);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->ts);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->lastcachetime);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->exptime);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 5:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->statuscode);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 6:
          if ($ftype == TType::BOOL) {
            $xfer += $input->readBool($this->isfinished);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 10:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->message);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 16:
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->timetaken);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 18:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->connuri);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 19:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->expr);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 20:
          if ($ftype == TType::MAP) {
            $this->context = array();
            $_size7 = 0;
            $_ktype8 = 0;
            $_vtype9 = 0;
            $xfer += $input->readMapBegin($_ktype8, $_vtype9, $_size7);
            for ($_i11 = 0; $_i11 < $_size7; ++$_i11)
            {
              $key12 = '';
              $val13 = '';
              $xfer += $input->readString($key12);
              $xfer += $input->readString($val13);
              $this->context[$key12] = $val13;
            }
            $xfer += $input->readMapEnd();
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 21:
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->arrive_ts);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 22:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->realconnuri);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 23:
          if ($ftype == TType::MAP) {
            $this->servercontext = array();
            $_size14 = 0;
            $_ktype15 = 0;
            $_vtype16 = 0;
            $xfer += $input->readMapBegin($_ktype15, $_vtype16, $_size14);
            for ($_i18 = 0; $_i18 < $_size14; ++$_i18)
            {
              $key19 = '';
              $val20 = '';
              $xfer += $input->readString($key19);
              $xfer += $input->readString($val20);
              $this->servercontext[$key19] = $val20;
            }
            $xfer += $input->readMapEnd();
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        default:
          $xfer += $input->skip($ftype);
          break;
      }
      $xfer += $input->readFieldEnd();
    }
    $xfer += $input->readStructEnd();
    return $xfer;
  }

  public function write($output) {
    $xfer = 0;
    $xfer += $output->writeStructBegin('QueryInfo');
    if ($this->queryid !== null) {
      $xfer += $output->writeFieldBegin('queryid', TType::STRING, 1);
      $xfer += $output->writeString($this->queryid);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->ts !== null) {
      $xfer += $output->writeFieldBegin('ts', TType::I64, 2);
      $xfer += $output->writeI64($this->ts);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->lastcachetime !== null) {
      $xfer += $output->writeFieldBegin('lastcachetime', TType::I64, 3);
      $xfer += $output->writeI64($this->lastcachetime);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->exptime !== null) {
      $xfer += $output->writeFieldBegin('exptime', TType::I64, 4);
      $xfer += $output->writeI64($this->exptime);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->statuscode !== null) {
      $xfer += $output->writeFieldBegin('statuscode', TType::I32, 5);
      $xfer += $output->writeI32($this->statuscode);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->isfinished !== null) {
      $xfer += $output->writeFieldBegin('isfinished', TType::BOOL, 6);
      $xfer += $output->writeBool($this->isfinished);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->message !== null) {
      $xfer += $output->writeFieldBegin('message', TType::STRING, 10);
      $xfer += $output->writeString($this->message);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->timetaken !== null) {
      $xfer += $output->writeFieldBegin('timetaken', TType::I64, 16);
      $xfer += $output->writeI64($this->timetaken);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->connuri !== null) {
      $xfer += $output->writeFieldBegin('connuri', TType::STRING, 18);
      $xfer += $output->writeString($this->connuri);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->expr !== null) {
      $xfer += $output->writeFieldBegin('expr', TType::STRING, 19);
      $xfer += $output->writeString($this->expr);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->context !== null) {
      if (!is_array($this->context)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('context', TType::MAP, 20);
      {
        $output->writeMapBegin(TType::STRING, TType::STRING, count($this->context));
        {
          foreach ($this->context as $kiter21 => $viter22)
          {
            $xfer += $output->writeString($kiter21);
            $xfer += $output->writeString($viter22);
          }
        }
        $output->writeMapEnd();
      }
      $xfer += $output->writeFieldEnd();
    }
    if ($this->arrive_ts !== null) {
      $xfer += $output->writeFieldBegin('arrive_ts', TType::I64, 21);
      $xfer += $output->writeI64($this->arrive_ts);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->realconnuri !== null) {
      $xfer += $output->writeFieldBegin('realconnuri', TType::STRING, 22);
      $xfer += $output->writeString($this->realconnuri);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->servercontext !== null) {
      if (!is_array($this->servercontext)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('servercontext', TType::MAP, 23);
      {
        $output->writeMapBegin(TType::STRING, TType::STRING, count($this->servercontext));
        {
          foreach ($this->servercontext as $kiter23 => $viter24)
          {
            $xfer += $output->writeString($kiter23);
            $xfer += $output->writeString($viter24);
          }
        }
        $output->writeMapEnd();
      }
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class Result {
  static $_TSPEC;

  public $dataset = null;
  public $info = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'dataset',
          'type' => TType::STRUCT,
          'class' => 'DataSet',
          ),
        2 => array(
          'var' => 'info',
          'type' => TType::STRUCT,
          'class' => 'QueryInfo',
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['dataset'])) {
        $this->dataset = $vals['dataset'];
      }
      if (isset($vals['info'])) {
        $this->info = $vals['info'];
      }
    }
  }

  public function getName() {
    return 'Result';
  }

  public function read($input)
  {
    $xfer = 0;
    $fname = null;
    $ftype = 0;
    $fid = 0;
    $xfer += $input->readStructBegin($fname);
    while (true)
    {
      $xfer += $input->readFieldBegin($fname, $ftype, $fid);
      if ($ftype == TType::STOP) {
        break;
      }
      switch ($fid)
      {
        case 1:
          if ($ftype == TType::STRUCT) {
            $this->dataset = new DataSet();
            $xfer += $this->dataset->read($input);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::STRUCT) {
            $this->info = new QueryInfo();
            $xfer += $this->info->read($input);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        default:
          $xfer += $input->skip($ftype);
          break;
      }
      $xfer += $input->readFieldEnd();
    }
    $xfer += $input->readStructEnd();
    return $xfer;
  }

  public function write($output) {
    $xfer = 0;
    $xfer += $output->writeStructBegin('Result');
    if ($this->dataset !== null) {
      if (!is_object($this->dataset)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('dataset', TType::STRUCT, 1);
      $xfer += $this->dataset->write($output);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->info !== null) {
      if (!is_object($this->info)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('info', TType::STRUCT, 2);
      $xfer += $this->info->write($output);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class ServerNotAliveException extends TException {
  static $_TSPEC;

  public $message = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'message',
          'type' => TType::STRING,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['message'])) {
        $this->message = $vals['message'];
      }
    }
  }

  public function getName() {
    return 'ServerNotAliveException';
  }

  public function read($input)
  {
    $xfer = 0;
    $fname = null;
    $ftype = 0;
    $fid = 0;
    $xfer += $input->readStructBegin($fname);
    while (true)
    {
      $xfer += $input->readFieldBegin($fname, $ftype, $fid);
      if ($ftype == TType::STOP) {
        break;
      }
      switch ($fid)
      {
        case 1:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->message);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        default:
          $xfer += $input->skip($ftype);
          break;
      }
      $xfer += $input->readFieldEnd();
    }
    $xfer += $input->readStructEnd();
    return $xfer;
  }

  public function write($output) {
    $xfer = 0;
    $xfer += $output->writeStructBegin('ServerNotAliveException');
    if ($this->message !== null) {
      $xfer += $output->writeFieldBegin('message', TType::STRING, 1);
      $xfer += $output->writeString($this->message);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class QueryException extends TException {
  static $_TSPEC;

  public $code = null;
  public $message = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'code',
          'type' => TType::I32,
          ),
        2 => array(
          'var' => 'message',
          'type' => TType::STRING,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['code'])) {
        $this->code = $vals['code'];
      }
      if (isset($vals['message'])) {
        $this->message = $vals['message'];
      }
    }
  }

  public function getName() {
    return 'QueryException';
  }

  public function read($input)
  {
    $xfer = 0;
    $fname = null;
    $ftype = 0;
    $fid = 0;
    $xfer += $input->readStructBegin($fname);
    while (true)
    {
      $xfer += $input->readFieldBegin($fname, $ftype, $fid);
      if ($ftype == TType::STOP) {
        break;
      }
      switch ($fid)
      {
        case 1:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->code);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->message);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        default:
          $xfer += $input->skip($ftype);
          break;
      }
      $xfer += $input->readFieldEnd();
    }
    $xfer += $input->readStructEnd();
    return $xfer;
  }

  public function write($output) {
    $xfer = 0;
    $xfer += $output->writeStructBegin('QueryException');
    if ($this->code !== null) {
      $xfer += $output->writeFieldBegin('code', TType::I32, 1);
      $xfer += $output->writeI32($this->code);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->message !== null) {
      $xfer += $output->writeFieldBegin('message', TType::STRING, 2);
      $xfer += $output->writeString($this->message);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

?>
