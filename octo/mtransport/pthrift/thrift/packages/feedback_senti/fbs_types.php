<?php
/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
include_once $GLOBALS['THRIFT_ROOT'].'/Thrift.php';


class FSLabel {
  static $_TSPEC;

  public $label = null;
  public $isPositive = null;
  public $count = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'label',
          'type' => TType::STRING,
          ),
        2 => array(
          'var' => 'isPositive',
          'type' => TType::I32,
          ),
        3 => array(
          'var' => 'count',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['label'])) {
        $this->label = $vals['label'];
      }
      if (isset($vals['isPositive'])) {
        $this->isPositive = $vals['isPositive'];
      }
      if (isset($vals['count'])) {
        $this->count = $vals['count'];
      }
    }
  }

  public function getName() {
    return 'FSLabel';
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
            $xfer += $input->readString($this->label);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->isPositive);
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
    $xfer += $output->writeStructBegin('FSLabel');
    if ($this->label !== null) {
      $xfer += $output->writeFieldBegin('label', TType::STRING, 1);
      $xfer += $output->writeString($this->label);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->isPositive !== null) {
      $xfer += $output->writeFieldBegin('isPositive', TType::I32, 2);
      $xfer += $output->writeI32($this->isPositive);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->count !== null) {
      $xfer += $output->writeFieldBegin('count', TType::I32, 3);
      $xfer += $output->writeI32($this->count);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class FSItem {
  static $_TSPEC;

  public $fsItemId = null;
  public $feedbackId = null;
  public $startPos = null;
  public $endPos = null;
  public $phrase = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'fsItemId',
          'type' => TType::I64,
          ),
        2 => array(
          'var' => 'feedbackId',
          'type' => TType::I64,
          ),
        3 => array(
          'var' => 'startPos',
          'type' => TType::I32,
          ),
        4 => array(
          'var' => 'endPos',
          'type' => TType::I32,
          ),
        5 => array(
          'var' => 'phrase',
          'type' => TType::STRING,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['fsItemId'])) {
        $this->fsItemId = $vals['fsItemId'];
      }
      if (isset($vals['feedbackId'])) {
        $this->feedbackId = $vals['feedbackId'];
      }
      if (isset($vals['startPos'])) {
        $this->startPos = $vals['startPos'];
      }
      if (isset($vals['endPos'])) {
        $this->endPos = $vals['endPos'];
      }
      if (isset($vals['phrase'])) {
        $this->phrase = $vals['phrase'];
      }
    }
  }

  public function getName() {
    return 'FSItem';
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
            $xfer += $input->readI64($this->fsItemId);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I64) {
            $xfer += $input->readI64($this->feedbackId);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->startPos);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->endPos);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 5:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->phrase);
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
    $xfer += $output->writeStructBegin('FSItem');
    if ($this->fsItemId !== null) {
      $xfer += $output->writeFieldBegin('fsItemId', TType::I64, 1);
      $xfer += $output->writeI64($this->fsItemId);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->feedbackId !== null) {
      $xfer += $output->writeFieldBegin('feedbackId', TType::I64, 2);
      $xfer += $output->writeI64($this->feedbackId);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->startPos !== null) {
      $xfer += $output->writeFieldBegin('startPos', TType::I32, 3);
      $xfer += $output->writeI32($this->startPos);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->endPos !== null) {
      $xfer += $output->writeFieldBegin('endPos', TType::I32, 4);
      $xfer += $output->writeI32($this->endPos);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->phrase !== null) {
      $xfer += $output->writeFieldBegin('phrase', TType::STRING, 5);
      $xfer += $output->writeString($this->phrase);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class FSRichLabel {
  static $_TSPEC;

  public $label = null;
  public $isPositive = null;
  public $count = null;
  public $feedbacks = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'label',
          'type' => TType::STRING,
          ),
        2 => array(
          'var' => 'isPositive',
          'type' => TType::I32,
          ),
        3 => array(
          'var' => 'count',
          'type' => TType::I32,
          ),
        4 => array(
          'var' => 'feedbacks',
          'type' => TType::LST,
          'etype' => TType::STRUCT,
          'elem' => array(
            'type' => TType::STRUCT,
            'class' => 'FSItem',
            ),
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['label'])) {
        $this->label = $vals['label'];
      }
      if (isset($vals['isPositive'])) {
        $this->isPositive = $vals['isPositive'];
      }
      if (isset($vals['count'])) {
        $this->count = $vals['count'];
      }
      if (isset($vals['feedbacks'])) {
        $this->feedbacks = $vals['feedbacks'];
      }
    }
  }

  public function getName() {
    return 'FSRichLabel';
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
            $xfer += $input->readString($this->label);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->isPositive);
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
          if ($ftype == TType::LST) {
            $this->feedbacks = array();
            $_size0 = 0;
            $_etype3 = 0;
            $xfer += $input->readListBegin($_etype3, $_size0);
            for ($_i4 = 0; $_i4 < $_size0; ++$_i4)
            {
              $elem5 = null;
              $elem5 = new FSItem();
              $xfer += $elem5->read($input);
              $this->feedbacks []= $elem5;
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
    $xfer += $output->writeStructBegin('FSRichLabel');
    if ($this->label !== null) {
      $xfer += $output->writeFieldBegin('label', TType::STRING, 1);
      $xfer += $output->writeString($this->label);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->isPositive !== null) {
      $xfer += $output->writeFieldBegin('isPositive', TType::I32, 2);
      $xfer += $output->writeI32($this->isPositive);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->count !== null) {
      $xfer += $output->writeFieldBegin('count', TType::I32, 3);
      $xfer += $output->writeI32($this->count);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->feedbacks !== null) {
      if (!is_array($this->feedbacks)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('feedbacks', TType::LST, 4);
      {
        $output->writeListBegin(TType::STRUCT, count($this->feedbacks));
        {
          foreach ($this->feedbacks as $iter6)
          {
            $xfer += $iter6->write($output);
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