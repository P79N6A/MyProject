<?php
/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
include_once $GLOBALS['THRIFT_ROOT'].'/Thrift.php';

include_once $GLOBALS['THRIFT_ROOT'].'/packages/userpush/userpush_types.php';

interface UserPushIf {
  public function getRecommend($userid);
  public function getRecommend2($userid);
  public function getFollowerSimilar($userid);
  public function getFollowerSVDSimilar($userid);
  public function getFinderRecommend($userid);
}

class UserPushClient implements UserPushIf {
  protected $input_ = null;
  protected $output_ = null;

  protected $seqid_ = 0;

  public function __construct($input, $output=null) {
    $this->input_ = $input;
    $this->output_ = $output ? $output : $input;
  }

  public function getRecommend($userid)
  {
    $this->send_getRecommend($userid);
    return $this->recv_getRecommend();
  }

  public function send_getRecommend($userid)
  {
    $args = new userpush_UserPush_getRecommend_args();
    $args->userid = $userid;
    $bin_accel = ($this->output_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_write_binary');
    if ($bin_accel)
    {
      thrift_protocol_write_binary($this->output_, 'getRecommend', TMessageType::CALL, $args, $this->seqid_, $this->output_->isStrictWrite());
    }
    else
    {
      $this->output_->writeMessageBegin('getRecommend', TMessageType::CALL, $this->seqid_);
      $args->write($this->output_);
      $this->output_->writeMessageEnd();
      $this->output_->getTransport()->flush();
    }
  }

  public function recv_getRecommend()
  {
    $bin_accel = ($this->input_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_read_binary');
    if ($bin_accel) $result = thrift_protocol_read_binary($this->input_, 'userpush_UserPush_getRecommend_result', $this->input_->isStrictRead());
    else
    {
      $rseqid = 0;
      $fname = null;
      $mtype = 0;

      $this->input_->readMessageBegin($fname, $mtype, $rseqid);
      if ($mtype == TMessageType::EXCEPTION) {
        $x = new TApplicationException();
        $x->read($this->input_);
        $this->input_->readMessageEnd();
        throw $x;
      }
      $result = new userpush_UserPush_getRecommend_result();
      $result->read($this->input_);
      $this->input_->readMessageEnd();
    }
    if ($result->success !== null) {
      return $result->success;
    }
    throw new Exception("getRecommend failed: unknown result");
  }

  public function getRecommend2($userid)
  {
    $this->send_getRecommend2($userid);
    return $this->recv_getRecommend2();
  }

  public function send_getRecommend2($userid)
  {
    $args = new userpush_UserPush_getRecommend2_args();
    $args->userid = $userid;
    $bin_accel = ($this->output_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_write_binary');
    if ($bin_accel)
    {
      thrift_protocol_write_binary($this->output_, 'getRecommend2', TMessageType::CALL, $args, $this->seqid_, $this->output_->isStrictWrite());
    }
    else
    {
      $this->output_->writeMessageBegin('getRecommend2', TMessageType::CALL, $this->seqid_);
      $args->write($this->output_);
      $this->output_->writeMessageEnd();
      $this->output_->getTransport()->flush();
    }
  }

  public function recv_getRecommend2()
  {
    $bin_accel = ($this->input_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_read_binary');
    if ($bin_accel) $result = thrift_protocol_read_binary($this->input_, 'userpush_UserPush_getRecommend2_result', $this->input_->isStrictRead());
    else
    {
      $rseqid = 0;
      $fname = null;
      $mtype = 0;

      $this->input_->readMessageBegin($fname, $mtype, $rseqid);
      if ($mtype == TMessageType::EXCEPTION) {
        $x = new TApplicationException();
        $x->read($this->input_);
        $this->input_->readMessageEnd();
        throw $x;
      }
      $result = new userpush_UserPush_getRecommend2_result();
      $result->read($this->input_);
      $this->input_->readMessageEnd();
    }
    if ($result->success !== null) {
      return $result->success;
    }
    throw new Exception("getRecommend2 failed: unknown result");
  }

  public function getFollowerSimilar($userid)
  {
    $this->send_getFollowerSimilar($userid);
    return $this->recv_getFollowerSimilar();
  }

  public function send_getFollowerSimilar($userid)
  {
    $args = new userpush_UserPush_getFollowerSimilar_args();
    $args->userid = $userid;
    $bin_accel = ($this->output_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_write_binary');
    if ($bin_accel)
    {
      thrift_protocol_write_binary($this->output_, 'getFollowerSimilar', TMessageType::CALL, $args, $this->seqid_, $this->output_->isStrictWrite());
    }
    else
    {
      $this->output_->writeMessageBegin('getFollowerSimilar', TMessageType::CALL, $this->seqid_);
      $args->write($this->output_);
      $this->output_->writeMessageEnd();
      $this->output_->getTransport()->flush();
    }
  }

  public function recv_getFollowerSimilar()
  {
    $bin_accel = ($this->input_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_read_binary');
    if ($bin_accel) $result = thrift_protocol_read_binary($this->input_, 'userpush_UserPush_getFollowerSimilar_result', $this->input_->isStrictRead());
    else
    {
      $rseqid = 0;
      $fname = null;
      $mtype = 0;

      $this->input_->readMessageBegin($fname, $mtype, $rseqid);
      if ($mtype == TMessageType::EXCEPTION) {
        $x = new TApplicationException();
        $x->read($this->input_);
        $this->input_->readMessageEnd();
        throw $x;
      }
      $result = new userpush_UserPush_getFollowerSimilar_result();
      $result->read($this->input_);
      $this->input_->readMessageEnd();
    }
    if ($result->success !== null) {
      return $result->success;
    }
    throw new Exception("getFollowerSimilar failed: unknown result");
  }

  public function getFollowerSVDSimilar($userid)
  {
    $this->send_getFollowerSVDSimilar($userid);
    return $this->recv_getFollowerSVDSimilar();
  }

  public function send_getFollowerSVDSimilar($userid)
  {
    $args = new userpush_UserPush_getFollowerSVDSimilar_args();
    $args->userid = $userid;
    $bin_accel = ($this->output_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_write_binary');
    if ($bin_accel)
    {
      thrift_protocol_write_binary($this->output_, 'getFollowerSVDSimilar', TMessageType::CALL, $args, $this->seqid_, $this->output_->isStrictWrite());
    }
    else
    {
      $this->output_->writeMessageBegin('getFollowerSVDSimilar', TMessageType::CALL, $this->seqid_);
      $args->write($this->output_);
      $this->output_->writeMessageEnd();
      $this->output_->getTransport()->flush();
    }
  }

  public function recv_getFollowerSVDSimilar()
  {
    $bin_accel = ($this->input_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_read_binary');
    if ($bin_accel) $result = thrift_protocol_read_binary($this->input_, 'userpush_UserPush_getFollowerSVDSimilar_result', $this->input_->isStrictRead());
    else
    {
      $rseqid = 0;
      $fname = null;
      $mtype = 0;

      $this->input_->readMessageBegin($fname, $mtype, $rseqid);
      if ($mtype == TMessageType::EXCEPTION) {
        $x = new TApplicationException();
        $x->read($this->input_);
        $this->input_->readMessageEnd();
        throw $x;
      }
      $result = new userpush_UserPush_getFollowerSVDSimilar_result();
      $result->read($this->input_);
      $this->input_->readMessageEnd();
    }
    if ($result->success !== null) {
      return $result->success;
    }
    throw new Exception("getFollowerSVDSimilar failed: unknown result");
  }

  public function getFinderRecommend($userid)
  {
    $this->send_getFinderRecommend($userid);
    return $this->recv_getFinderRecommend();
  }

  public function send_getFinderRecommend($userid)
  {
    $args = new userpush_UserPush_getFinderRecommend_args();
    $args->userid = $userid;
    $bin_accel = ($this->output_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_write_binary');
    if ($bin_accel)
    {
      thrift_protocol_write_binary($this->output_, 'getFinderRecommend', TMessageType::CALL, $args, $this->seqid_, $this->output_->isStrictWrite());
    }
    else
    {
      $this->output_->writeMessageBegin('getFinderRecommend', TMessageType::CALL, $this->seqid_);
      $args->write($this->output_);
      $this->output_->writeMessageEnd();
      $this->output_->getTransport()->flush();
    }
  }

  public function recv_getFinderRecommend()
  {
    $bin_accel = ($this->input_ instanceof TProtocol::$TBINARYPROTOCOLACCELERATED) && function_exists('thrift_protocol_read_binary');
    if ($bin_accel) $result = thrift_protocol_read_binary($this->input_, 'userpush_UserPush_getFinderRecommend_result', $this->input_->isStrictRead());
    else
    {
      $rseqid = 0;
      $fname = null;
      $mtype = 0;

      $this->input_->readMessageBegin($fname, $mtype, $rseqid);
      if ($mtype == TMessageType::EXCEPTION) {
        $x = new TApplicationException();
        $x->read($this->input_);
        $this->input_->readMessageEnd();
        throw $x;
      }
      $result = new userpush_UserPush_getFinderRecommend_result();
      $result->read($this->input_);
      $this->input_->readMessageEnd();
    }
    if ($result->success !== null) {
      return $result->success;
    }
    throw new Exception("getFinderRecommend failed: unknown result");
  }

}

// HELPER FUNCTIONS AND STRUCTURES

class userpush_UserPush_getRecommend_args {
  static $_TSPEC;

  public $userid = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'userid',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['userid'])) {
        $this->userid = $vals['userid'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getRecommend_args';
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
            $xfer += $input->readI32($this->userid);
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
    $xfer += $output->writeStructBegin('UserPush_getRecommend_args');
    if ($this->userid !== null) {
      $xfer += $output->writeFieldBegin('userid', TType::I32, 1);
      $xfer += $output->writeI32($this->userid);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class userpush_UserPush_getRecommend_result {
  static $_TSPEC;

  public $success = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        0 => array(
          'var' => 'success',
          'type' => TType::MAP,
          'ktype' => TType::I32,
          'vtype' => TType::DOUBLE,
          'key' => array(
            'type' => TType::I32,
          ),
          'val' => array(
            'type' => TType::DOUBLE,
            ),
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['success'])) {
        $this->success = $vals['success'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getRecommend_result';
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
        case 0:
          if ($ftype == TType::MAP) {
            $this->success = array();
            $_size0 = 0;
            $_ktype1 = 0;
            $_vtype2 = 0;
            $xfer += $input->readMapBegin($_ktype1, $_vtype2, $_size0);
            for ($_i4 = 0; $_i4 < $_size0; ++$_i4)
            {
              $key5 = 0;
              $val6 = 0.0;
              $xfer += $input->readI32($key5);
              $xfer += $input->readDouble($val6);
              $this->success[$key5] = $val6;
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
    $xfer += $output->writeStructBegin('UserPush_getRecommend_result');
    if ($this->success !== null) {
      if (!is_array($this->success)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('success', TType::MAP, 0);
      {
        $output->writeMapBegin(TType::I32, TType::DOUBLE, count($this->success));
        {
          foreach ($this->success as $kiter7 => $viter8)
          {
            $xfer += $output->writeI32($kiter7);
            $xfer += $output->writeDouble($viter8);
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

class userpush_UserPush_getRecommend2_args {
  static $_TSPEC;

  public $userid = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'userid',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['userid'])) {
        $this->userid = $vals['userid'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getRecommend2_args';
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
            $xfer += $input->readI32($this->userid);
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
    $xfer += $output->writeStructBegin('UserPush_getRecommend2_args');
    if ($this->userid !== null) {
      $xfer += $output->writeFieldBegin('userid', TType::I32, 1);
      $xfer += $output->writeI32($this->userid);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class userpush_UserPush_getRecommend2_result {
  static $_TSPEC;

  public $success = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        0 => array(
          'var' => 'success',
          'type' => TType::MAP,
          'ktype' => TType::I32,
          'vtype' => TType::DOUBLE,
          'key' => array(
            'type' => TType::I32,
          ),
          'val' => array(
            'type' => TType::DOUBLE,
            ),
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['success'])) {
        $this->success = $vals['success'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getRecommend2_result';
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
        case 0:
          if ($ftype == TType::MAP) {
            $this->success = array();
            $_size9 = 0;
            $_ktype10 = 0;
            $_vtype11 = 0;
            $xfer += $input->readMapBegin($_ktype10, $_vtype11, $_size9);
            for ($_i13 = 0; $_i13 < $_size9; ++$_i13)
            {
              $key14 = 0;
              $val15 = 0.0;
              $xfer += $input->readI32($key14);
              $xfer += $input->readDouble($val15);
              $this->success[$key14] = $val15;
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
    $xfer += $output->writeStructBegin('UserPush_getRecommend2_result');
    if ($this->success !== null) {
      if (!is_array($this->success)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('success', TType::MAP, 0);
      {
        $output->writeMapBegin(TType::I32, TType::DOUBLE, count($this->success));
        {
          foreach ($this->success as $kiter16 => $viter17)
          {
            $xfer += $output->writeI32($kiter16);
            $xfer += $output->writeDouble($viter17);
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

class userpush_UserPush_getFollowerSimilar_args {
  static $_TSPEC;

  public $userid = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'userid',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['userid'])) {
        $this->userid = $vals['userid'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getFollowerSimilar_args';
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
            $xfer += $input->readI32($this->userid);
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
    $xfer += $output->writeStructBegin('UserPush_getFollowerSimilar_args');
    if ($this->userid !== null) {
      $xfer += $output->writeFieldBegin('userid', TType::I32, 1);
      $xfer += $output->writeI32($this->userid);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class userpush_UserPush_getFollowerSimilar_result {
  static $_TSPEC;

  public $success = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        0 => array(
          'var' => 'success',
          'type' => TType::MAP,
          'ktype' => TType::I32,
          'vtype' => TType::DOUBLE,
          'key' => array(
            'type' => TType::I32,
          ),
          'val' => array(
            'type' => TType::DOUBLE,
            ),
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['success'])) {
        $this->success = $vals['success'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getFollowerSimilar_result';
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
        case 0:
          if ($ftype == TType::MAP) {
            $this->success = array();
            $_size18 = 0;
            $_ktype19 = 0;
            $_vtype20 = 0;
            $xfer += $input->readMapBegin($_ktype19, $_vtype20, $_size18);
            for ($_i22 = 0; $_i22 < $_size18; ++$_i22)
            {
              $key23 = 0;
              $val24 = 0.0;
              $xfer += $input->readI32($key23);
              $xfer += $input->readDouble($val24);
              $this->success[$key23] = $val24;
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
    $xfer += $output->writeStructBegin('UserPush_getFollowerSimilar_result');
    if ($this->success !== null) {
      if (!is_array($this->success)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('success', TType::MAP, 0);
      {
        $output->writeMapBegin(TType::I32, TType::DOUBLE, count($this->success));
        {
          foreach ($this->success as $kiter25 => $viter26)
          {
            $xfer += $output->writeI32($kiter25);
            $xfer += $output->writeDouble($viter26);
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

class userpush_UserPush_getFollowerSVDSimilar_args {
  static $_TSPEC;

  public $userid = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'userid',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['userid'])) {
        $this->userid = $vals['userid'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getFollowerSVDSimilar_args';
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
            $xfer += $input->readI32($this->userid);
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
    $xfer += $output->writeStructBegin('UserPush_getFollowerSVDSimilar_args');
    if ($this->userid !== null) {
      $xfer += $output->writeFieldBegin('userid', TType::I32, 1);
      $xfer += $output->writeI32($this->userid);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class userpush_UserPush_getFollowerSVDSimilar_result {
  static $_TSPEC;

  public $success = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        0 => array(
          'var' => 'success',
          'type' => TType::MAP,
          'ktype' => TType::I32,
          'vtype' => TType::DOUBLE,
          'key' => array(
            'type' => TType::I32,
          ),
          'val' => array(
            'type' => TType::DOUBLE,
            ),
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['success'])) {
        $this->success = $vals['success'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getFollowerSVDSimilar_result';
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
        case 0:
          if ($ftype == TType::MAP) {
            $this->success = array();
            $_size27 = 0;
            $_ktype28 = 0;
            $_vtype29 = 0;
            $xfer += $input->readMapBegin($_ktype28, $_vtype29, $_size27);
            for ($_i31 = 0; $_i31 < $_size27; ++$_i31)
            {
              $key32 = 0;
              $val33 = 0.0;
              $xfer += $input->readI32($key32);
              $xfer += $input->readDouble($val33);
              $this->success[$key32] = $val33;
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
    $xfer += $output->writeStructBegin('UserPush_getFollowerSVDSimilar_result');
    if ($this->success !== null) {
      if (!is_array($this->success)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('success', TType::MAP, 0);
      {
        $output->writeMapBegin(TType::I32, TType::DOUBLE, count($this->success));
        {
          foreach ($this->success as $kiter34 => $viter35)
          {
            $xfer += $output->writeI32($kiter34);
            $xfer += $output->writeDouble($viter35);
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

class userpush_UserPush_getFinderRecommend_args {
  static $_TSPEC;

  public $userid = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'userid',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['userid'])) {
        $this->userid = $vals['userid'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getFinderRecommend_args';
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
            $xfer += $input->readI32($this->userid);
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
    $xfer += $output->writeStructBegin('UserPush_getFinderRecommend_args');
    if ($this->userid !== null) {
      $xfer += $output->writeFieldBegin('userid', TType::I32, 1);
      $xfer += $output->writeI32($this->userid);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class userpush_UserPush_getFinderRecommend_result {
  static $_TSPEC;

  public $success = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        0 => array(
          'var' => 'success',
          'type' => TType::MAP,
          'ktype' => TType::I32,
          'vtype' => TType::DOUBLE,
          'key' => array(
            'type' => TType::I32,
          ),
          'val' => array(
            'type' => TType::DOUBLE,
            ),
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['success'])) {
        $this->success = $vals['success'];
      }
    }
  }

  public function getName() {
    return 'UserPush_getFinderRecommend_result';
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
        case 0:
          if ($ftype == TType::MAP) {
            $this->success = array();
            $_size36 = 0;
            $_ktype37 = 0;
            $_vtype38 = 0;
            $xfer += $input->readMapBegin($_ktype37, $_vtype38, $_size36);
            for ($_i40 = 0; $_i40 < $_size36; ++$_i40)
            {
              $key41 = 0;
              $val42 = 0.0;
              $xfer += $input->readI32($key41);
              $xfer += $input->readDouble($val42);
              $this->success[$key41] = $val42;
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
    $xfer += $output->writeStructBegin('UserPush_getFinderRecommend_result');
    if ($this->success !== null) {
      if (!is_array($this->success)) {
        throw new TProtocolException('Bad type in structure.', TProtocolException::INVALID_DATA);
      }
      $xfer += $output->writeFieldBegin('success', TType::MAP, 0);
      {
        $output->writeMapBegin(TType::I32, TType::DOUBLE, count($this->success));
        {
          foreach ($this->success as $kiter43 => $viter44)
          {
            $xfer += $output->writeI32($kiter43);
            $xfer += $output->writeDouble($viter44);
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

?>