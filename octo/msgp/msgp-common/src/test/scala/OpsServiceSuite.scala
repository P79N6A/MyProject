import com.sankuai.msgp.common.service.org.OpsService
import org.scalatest.{BeforeAndAfter, FunSuite}

class OpsServiceSuite  extends FunSuite with BeforeAndAfter{
  test("getOwnerByIp") {
    val ip = "10.21.247.31"

    println(OpsService.getOwnerByIp(ip))
  }
}
