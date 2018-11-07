import com.sankuai.octo.aggregator.util.IllegalAppkey
import org.scalatest.{BeforeAndAfter, FunSuite}

class perfSuite extends FunSuite with BeforeAndAfter  {

  test("perf.illegalAppkey") {
    println(IllegalAppkey.illegal("com.sankuai.inf.msgp"))
    println(IllegalAppkey.illegal("Com.sankuai.inf_msgp11-33"))
    println(IllegalAppkey.illegal("\"com.sankuai.inf.msgp"))
    println(IllegalAppkey.illegal("${Octo.appkey}"))
    println(IllegalAppkey.illegal("{Octo.appkey}"))
  }
}
