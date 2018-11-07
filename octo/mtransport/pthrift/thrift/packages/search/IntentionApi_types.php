<?php
/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
include_once $GLOBALS['THRIFT_ROOT'].'/Thrift.php';

include_once $GLOBALS['THRIFT_ROOT'].'/packages/search/SphinxApi_types.php';

class IntentionItem {
  static $_TSPEC;

  public $rewrite_word = "";
  public $intention_type = 0;
  public $score = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'rewrite_word',
          'type' => TType::STRING,
          ),
        2 => array(
          'var' => 'intention_type',
          'type' => TType::I32,
          ),
        3 => array(
          'var' => 'score',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['rewrite_word'])) {
        $this->rewrite_word = $vals['rewrite_word'];
      }
      if (isset($vals['intention_type'])) {
        $this->intention_type = $vals['intention_type'];
      }
      if (isset($vals['score'])) {
        $this->score = $vals['score'];
      }
    }
  }

  public function getName() {
    return 'IntentionItem';
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
            $xfer += $input->readString($this->rewrite_word);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->intention_type);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->score);
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
    $xfer += $output->writeStructBegin('IntentionItem');
    if ($this->rewrite_word !== null) {
      $xfer += $output->writeFieldBegin('rewrite_word', TType::STRING, 1);
      $xfer += $output->writeString($this->rewrite_word);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->intention_type !== null) {
      $xfer += $output->writeFieldBegin('intention_type', TType::I32, 2);
      $xfer += $output->writeI32($this->intention_type);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->score !== null) {
      $xfer += $output->writeFieldBegin('score', TType::I32, 3);
      $xfer += $output->writeI32($this->score);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class Intention {
  static $_TSPEC;

  public $intention_list = null;
  public $status = null;
  public $error = null;
  public $total = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'intention_list',
          'type' => TType::LST,
          'etype' => TType::STRUCT,
          'elem' => array(
            'type' => TType::STRUCT,
            'class' => 'IntentionItem',
            ),
          ),
        2 => array(
          'var' => 'status',
          'type' => TType::I32,
          ),
        3 => array(
          'var' => 'error',
          'type' => TType::STRING,
          ),
        4 => array(
          'var' => 'total',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['intention_list'])) {
        $this->intention_list = $vals['intention_list'];
      }
      if (isset($vals['status'])) {
        $this->status = $vals['status'];
      }
      if (isset($vals['error'])) {
        $this->error = $vals['error'];
      }
      if (isset($vals['total'])) {
        $this->total = $vals['total'];
      }
    }
  }

  public function getName() {
    return 'Intention';
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
            $this->intention_list = array();
            $_size0 = 0;
            $_etype3 = 0;
            $xfer += $input->readListBegin($_etype3, $_size0);
            for ($_i4 = 0; $_i4 < $_size0; ++$_i4)
            {
              $elem5 = null;
              $elem5 = new IntentionItem();
              $xfer += $elem5->read($input);
              $this->intention_list []= $elem5;
            }
            $xfer += $input->readListEnd();
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->status);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->error);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->total);
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
    $xfer += $output->writeStructBegin('Intention');
    if ($this->intention_list !== null) {
      if (!is_array($this->intention_list)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('intention_list', TType::LST, 1);
      {
        $output->writeListBegin(TType::STRUCT, count($this->intention_list));
        {
          foreach ($this->intention_list as $iter6)
          {
            $xfer += $iter6->write($output);
          }
        }
        $output->writeListEnd();
      }
      $xfer += $output->writeFieldEnd();
    }
    if ($this->status !== null) {
      $xfer += $output->writeFieldBegin('status', TType::I32, 2);
      $xfer += $output->writeI32($this->status);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->error !== null) {
      $xfer += $output->writeFieldBegin('error', TType::STRING, 3);
      $xfer += $output->writeString($this->error);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->total !== null) {
      $xfer += $output->writeFieldBegin('total', TType::I32, 4);
      $xfer += $output->writeI32($this->total);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

?>