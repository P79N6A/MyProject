<?php
/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
include_once $GLOBALS['THRIFT_ROOT'].'/Thrift.php';


class UserGeoPrefReq {
  static $_TSPEC;

  public $uuid = "";
  public $userid = -1;
  public $cityid = -1;
  public $length = 8;
  public $stscene = 0;
  public $session = null;
  public $serviceid = 1;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'uuid',
          'type' => TType::STRING,
          ),
        2 => array(
          'var' => 'userid',
          'type' => TType::I32,
          ),
        3 => array(
          'var' => 'cityid',
          'type' => TType::I32,
          ),
        4 => array(
          'var' => 'length',
          'type' => TType::I32,
          ),
        5 => array(
          'var' => 'stscene',
          'type' => TType::I32,
          ),
        6 => array(
          'var' => 'session',
          'type' => TType::LST,
          'etype' => TType::STRING,
          'elem' => array(
            'type' => TType::STRING,
            ),
          ),
        7 => array(
          'var' => 'serviceid',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['uuid'])) {
        $this->uuid = $vals['uuid'];
      }
      if (isset($vals['userid'])) {
        $this->userid = $vals['userid'];
      }
      if (isset($vals['cityid'])) {
        $this->cityid = $vals['cityid'];
      }
      if (isset($vals['length'])) {
        $this->length = $vals['length'];
      }
      if (isset($vals['stscene'])) {
        $this->stscene = $vals['stscene'];
      }
      if (isset($vals['session'])) {
        $this->session = $vals['session'];
      }
      if (isset($vals['serviceid'])) {
        $this->serviceid = $vals['serviceid'];
      }
    }
  }

  public function getName() {
    return 'UserGeoPrefReq';
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
            $xfer += $input->readString($this->uuid);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->userid);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->cityid);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->length);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 5:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->stscene);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 6:
          if ($ftype == TType::LST) {
            $this->session = array();
            $_size0 = 0;
            $_etype3 = 0;
            $xfer += $input->readListBegin($_etype3, $_size0);
            for ($_i4 = 0; $_i4 < $_size0; ++$_i4)
            {
              $elem5 = null;
              $xfer += $input->readString($elem5);
              $this->session []= $elem5;
            }
            $xfer += $input->readListEnd();
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 7:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->serviceid);
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
    $xfer += $output->writeStructBegin('UserGeoPrefReq');
    if ($this->uuid !== null) {
      $xfer += $output->writeFieldBegin('uuid', TType::STRING, 1);
      $xfer += $output->writeString($this->uuid);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->userid !== null) {
      $xfer += $output->writeFieldBegin('userid', TType::I32, 2);
      $xfer += $output->writeI32($this->userid);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->cityid !== null) {
      $xfer += $output->writeFieldBegin('cityid', TType::I32, 3);
      $xfer += $output->writeI32($this->cityid);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->length !== null) {
      $xfer += $output->writeFieldBegin('length', TType::I32, 4);
      $xfer += $output->writeI32($this->length);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->stscene !== null) {
      $xfer += $output->writeFieldBegin('stscene', TType::I32, 5);
      $xfer += $output->writeI32($this->stscene);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->session !== null) {
      if (!is_array($this->session)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('session', TType::LST, 6);
      {
        $output->writeListBegin(TType::STRING, count($this->session));
        {
          foreach ($this->session as $iter6)
          {
            $xfer += $output->writeString($iter6);
          }
        }
        $output->writeListEnd();
      }
      $xfer += $output->writeFieldEnd();
    }
    if ($this->serviceid !== null) {
      $xfer += $output->writeFieldBegin('serviceid', TType::I32, 7);
      $xfer += $output->writeI32($this->serviceid);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class GeoPref {
  static $_TSPEC;

  public $geotagid = null;
  public $geoslug = null;
  public $name = null;
  public $score = null;
  public $tag = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'geotagid',
          'type' => TType::I32,
          ),
        2 => array(
          'var' => 'geoslug',
          'type' => TType::STRING,
          ),
        3 => array(
          'var' => 'name',
          'type' => TType::STRING,
          ),
        4 => array(
          'var' => 'score',
          'type' => TType::DOUBLE,
          ),
        5 => array(
          'var' => 'tag',
          'type' => TType::STRING,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['geotagid'])) {
        $this->geotagid = $vals['geotagid'];
      }
      if (isset($vals['geoslug'])) {
        $this->geoslug = $vals['geoslug'];
      }
      if (isset($vals['name'])) {
        $this->name = $vals['name'];
      }
      if (isset($vals['score'])) {
        $this->score = $vals['score'];
      }
      if (isset($vals['tag'])) {
        $this->tag = $vals['tag'];
      }
    }
  }

  public function getName() {
    return 'GeoPref';
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
            $xfer += $input->readI32($this->geotagid);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->geoslug);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->name);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::DOUBLE) {
            $xfer += $input->readDouble($this->score);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 5:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->tag);
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
    $xfer += $output->writeStructBegin('GeoPref');
    if ($this->geotagid !== null) {
      $xfer += $output->writeFieldBegin('geotagid', TType::I32, 1);
      $xfer += $output->writeI32($this->geotagid);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->geoslug !== null) {
      $xfer += $output->writeFieldBegin('geoslug', TType::STRING, 2);
      $xfer += $output->writeString($this->geoslug);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->name !== null) {
      $xfer += $output->writeFieldBegin('name', TType::STRING, 3);
      $xfer += $output->writeString($this->name);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->score !== null) {
      $xfer += $output->writeFieldBegin('score', TType::DOUBLE, 4);
      $xfer += $output->writeDouble($this->score);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->tag !== null) {
      $xfer += $output->writeFieldBegin('tag', TType::STRING, 5);
      $xfer += $output->writeString($this->tag);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class UserGeoPrefRes {
  static $_TSPEC;

  public $geo_list = null;
  public $tag = null;
  public $rid = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'geo_list',
          'type' => TType::LST,
          'etype' => TType::STRUCT,
          'elem' => array(
            'type' => TType::STRUCT,
            'class' => 'GeoPref',
            ),
          ),
        2 => array(
          'var' => 'tag',
          'type' => TType::STRING,
          ),
        3 => array(
          'var' => 'rid',
          'type' => TType::I64,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['geo_list'])) {
        $this->geo_list = $vals['geo_list'];
      }
      if (isset($vals['tag'])) {
        $this->tag = $vals['tag'];
      }
      if (isset($vals['rid'])) {
        $this->rid = $vals['rid'];
      }
    }
  }

  public function getName() {
    return 'UserGeoPrefRes';
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
            $this->geo_list = array();
            $_size7 = 0;
            $_etype10 = 0;
            $xfer += $input->readListBegin($_etype10, $_size7);
            for ($_i11 = 0; $_i11 < $_size7; ++$_i11)
            {
              $elem12 = null;
              $elem12 = new GeoPref();
              $xfer += $elem12->read($input);
              $this->geo_list []= $elem12;
            }
            $xfer += $input->readListEnd();
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->tag);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->rid);
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
    $xfer += $output->writeStructBegin('UserGeoPrefRes');
    if ($this->geo_list !== null) {
      if (!is_array($this->geo_list)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('geo_list', TType::LST, 1);
      {
        $output->writeListBegin(TType::STRUCT, count($this->geo_list));
        {
          foreach ($this->geo_list as $iter13)
          {
            $xfer += $iter13->write($output);
          }
        }
        $output->writeListEnd();
      }
      $xfer += $output->writeFieldEnd();
    }
    if ($this->tag !== null) {
      $xfer += $output->writeFieldBegin('tag', TType::STRING, 2);
      $xfer += $output->writeString($this->tag);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->rid !== null) {
      $xfer += $output->writeFieldBegin('rid', TType::I64, 3);
      $xfer += $output->writeI64($this->rid);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

?>
