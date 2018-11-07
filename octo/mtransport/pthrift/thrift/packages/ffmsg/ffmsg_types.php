<?php
/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
include_once $GLOBALS['THRIFT_ROOT'].'/Thrift.php';


class ffmsg_HomeMsgReq {
  static $_TSPEC;

  public $senders = null;
  public $reqid = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'senders',
          'type' => TType::LST,
          'etype' => TType::I32,
          'elem' => array(
            'type' => TType::I32,
            ),
          ),
        2 => array(
          'var' => 'reqid',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['senders'])) {
        $this->senders = $vals['senders'];
      }
      if (isset($vals['reqid'])) {
        $this->reqid = $vals['reqid'];
      }
    }
  }

  public function getName() {
    return 'HomeMsgReq';
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
          if ($ftype == TType::LST) {
            $this->senders = array();
            $_size0 = 0;
            $_etype3 = 0;
            $xfer += $input->readListBegin($_etype3, $_size0);
            for ($_i4 = 0; $_i4 < $_size0; ++$_i4)
            {
              $elem5 = null;
              $xfer += $input->readI32($elem5);
              $this->senders []= $elem5;
            }
            $xfer += $input->readListEnd();
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->reqid);
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
    $xfer += $output->writeStructBegin('HomeMsgReq');
    if ($this->senders !== null) {
      if (!is_array($this->senders)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('senders', TType::LST, 1);
      {
        $output->writeListBegin(TType::I32, count($this->senders));
        {
          foreach ($this->senders as $iter6)
          {
            $xfer += $output->writeI32($iter6);
          }
        }
        $output->writeListEnd();
      }
      $xfer += $output->writeFieldEnd();
    }
    if ($this->reqid !== null) {
      $xfer += $output->writeFieldBegin('reqid', TType::I32, 2);
      $xfer += $output->writeI32($this->reqid);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class ffmsg_HomeMsgRsp {
  static $_TSPEC;

  public $results = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'results',
          'type' => TType::MAP,
          'ktype' => TType::I32,
          'vtype' => TType::STRING,
          'key' => array(
            'type' => TType::I32,
          ),
          'val' => array(
            'type' => TType::STRING,
            ),
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['results'])) {
        $this->results = $vals['results'];
      }
    }
  }

  public function getName() {
    return 'HomeMsgRsp';
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
          if ($ftype == TType::MAP) {
            $this->results = array();
            $_size7 = 0;
            $_ktype8 = 0;
            $_vtype9 = 0;
            $xfer += $input->readMapBegin($_ktype8, $_vtype9, $_size7);
            for ($_i11 = 0; $_i11 < $_size7; ++$_i11)
            {
              $key12 = 0;
              $val13 = '';
              $xfer += $input->readI32($key12);
              $xfer += $input->readString($val13);
              $this->results[$key12] = $val13;
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
    $xfer += $output->writeStructBegin('HomeMsgRsp');
    if ($this->results !== null) {
      if (!is_array($this->results)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('results', TType::MAP, 1);
      {
        $output->writeMapBegin(TType::I32, TType::STRING, count($this->results));
        {
          foreach ($this->results as $kiter14 => $viter15)
          {
            $xfer += $output->writeI32($kiter14);
            $xfer += $output->writeString($viter15);
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

class ffmsg_MsgQueryReq {
  static $_TSPEC;

  public $query = null;
  public $offset = null;
  public $count = 20;
  public $sinceID = 0;
  public $maxID = 2000000000;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'query',
          'type' => TType::STRING,
          ),
        2 => array(
          'var' => 'offset',
          'type' => TType::I32,
          ),
        3 => array(
          'var' => 'count',
          'type' => TType::I32,
          ),
        4 => array(
          'var' => 'sinceID',
          'type' => TType::I64,
          ),
        5 => array(
          'var' => 'maxID',
          'type' => TType::I64,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['query'])) {
        $this->query = $vals['query'];
      }
      if (isset($vals['offset'])) {
        $this->offset = $vals['offset'];
      }
      if (isset($vals['count'])) {
        $this->count = $vals['count'];
      }
      if (isset($vals['sinceID'])) {
        $this->sinceID = $vals['sinceID'];
      }
      if (isset($vals['maxID'])) {
        $this->maxID = $vals['maxID'];
      }
    }
  }

  public function getName() {
    return 'MsgQueryReq';
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
            $xfer += $input->readString($this->query);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->offset);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->count);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->sinceID);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 5:
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->maxID);
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
    $xfer += $output->writeStructBegin('MsgQueryReq');
    if ($this->query !== null) {
      $xfer += $output->writeFieldBegin('query', TType::STRING, 1);
      $xfer += $output->writeString($this->query);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->offset !== null) {
      $xfer += $output->writeFieldBegin('offset', TType::I32, 2);
      $xfer += $output->writeI32($this->offset);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->count !== null) {
      $xfer += $output->writeFieldBegin('count', TType::I32, 3);
      $xfer += $output->writeI32($this->count);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->sinceID !== null) {
      $xfer += $output->writeFieldBegin('sinceID', TType::I64, 4);
      $xfer += $output->writeI64($this->sinceID);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->maxID !== null) {
      $xfer += $output->writeFieldBegin('maxID', TType::I64, 5);
      $xfer += $output->writeI64($this->maxID);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class ffmsg_MsgQueryRsp {
  static $_TSPEC;

  public $ids = null;
  public $num = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'ids',
          'type' => TType::LST,
          'etype' => TType::I64,
          'elem' => array(
            'type' => TType::I64,
            ),
          ),
        2 => array(
          'var' => 'num',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['ids'])) {
        $this->ids = $vals['ids'];
      }
      if (isset($vals['num'])) {
        $this->num = $vals['num'];
      }
    }
  }

  public function getName() {
    return 'MsgQueryRsp';
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
          if ($ftype == TType::LST) {
            $this->ids = array();
            $_size16 = 0;
            $_etype19 = 0;
            $xfer += $input->readListBegin($_etype19, $_size16);
            for ($_i20 = 0; $_i20 < $_size16; ++$_i20)
            {
              $elem21 = null;
              $xfer += $input->readI64($elem21);
              $this->ids []= $elem21;
            }
            $xfer += $input->readListEnd();
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->num);
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
    $xfer += $output->writeStructBegin('MsgQueryRsp');
    if ($this->ids !== null) {
      if (!is_array($this->ids)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('ids', TType::LST, 1);
      {
        $output->writeListBegin(TType::I64, count($this->ids));
        {
          foreach ($this->ids as $iter22)
          {
            $xfer += $output->writeI64($iter22);
          }
        }
        $output->writeListEnd();
      }
      $xfer += $output->writeFieldEnd();
    }
    if ($this->num !== null) {
      $xfer += $output->writeFieldBegin('num', TType::I32, 2);
      $xfer += $output->writeI32($this->num);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class ffmsg_Msg {
  static $_TSPEC;

  public $id = null;
  public $level = null;
  public $sendfrom = null;
  public $sendto = null;
  public $sendtime = null;
  public $status = null;
  public $mentions = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'id',
          'type' => TType::I64,
          ),
        2 => array(
          'var' => 'level',
          'type' => TType::I32,
          ),
        3 => array(
          'var' => 'sendfrom',
          'type' => TType::I32,
          ),
        4 => array(
          'var' => 'sendto',
          'type' => TType::I32,
          ),
        5 => array(
          'var' => 'sendtime',
          'type' => TType::I32,
          ),
        6 => array(
          'var' => 'status',
          'type' => TType::BYTE,
          ),
        7 => array(
          'var' => 'mentions',
          'type' => TType::LST,
          'etype' => TType::I32,
          'elem' => array(
            'type' => TType::I32,
            ),
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['id'])) {
        $this->id = $vals['id'];
      }
      if (isset($vals['level'])) {
        $this->level = $vals['level'];
      }
      if (isset($vals['sendfrom'])) {
        $this->sendfrom = $vals['sendfrom'];
      }
      if (isset($vals['sendto'])) {
        $this->sendto = $vals['sendto'];
      }
      if (isset($vals['sendtime'])) {
        $this->sendtime = $vals['sendtime'];
      }
      if (isset($vals['status'])) {
        $this->status = $vals['status'];
      }
      if (isset($vals['mentions'])) {
        $this->mentions = $vals['mentions'];
      }
    }
  }

  public function getName() {
    return 'Msg';
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
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->id);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->level);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->sendfrom);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->sendto);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 5:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->sendtime);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 6:
          if ($ftype == TType::BYTE) {
            $xfer += $input->readByte($this->status);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 7:
          if ($ftype == TType::LST) {
            $this->mentions = array();
            $_size23 = 0;
            $_etype26 = 0;
            $xfer += $input->readListBegin($_etype26, $_size23);
            for ($_i27 = 0; $_i27 < $_size23; ++$_i27)
            {
              $elem28 = null;
              $xfer += $input->readI32($elem28);
              $this->mentions []= $elem28;
            }
            $xfer += $input->readListEnd();
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
    $xfer += $output->writeStructBegin('Msg');
    if ($this->id !== null) {
      $xfer += $output->writeFieldBegin('id', TType::I64, 1);
      $xfer += $output->writeI64($this->id);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->level !== null) {
      $xfer += $output->writeFieldBegin('level', TType::I32, 2);
      $xfer += $output->writeI32($this->level);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->sendfrom !== null) {
      $xfer += $output->writeFieldBegin('sendfrom', TType::I32, 3);
      $xfer += $output->writeI32($this->sendfrom);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->sendto !== null) {
      $xfer += $output->writeFieldBegin('sendto', TType::I32, 4);
      $xfer += $output->writeI32($this->sendto);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->sendtime !== null) {
      $xfer += $output->writeFieldBegin('sendtime', TType::I32, 5);
      $xfer += $output->writeI32($this->sendtime);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->status !== null) {
      $xfer += $output->writeFieldBegin('status', TType::BYTE, 6);
      $xfer += $output->writeByte($this->status);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->mentions !== null) {
      if (!is_array($this->mentions)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('mentions', TType::LST, 7);
      {
        $output->writeListBegin(TType::I32, count($this->mentions));
        {
          foreach ($this->mentions as $iter29)
          {
            $xfer += $output->writeI32($iter29);
          }
        }
        $output->writeListEnd();
      }
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

?>