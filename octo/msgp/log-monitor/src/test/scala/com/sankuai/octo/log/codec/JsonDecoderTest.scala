package com.sankuai.octo.log.codec

import com.sankuai.octo.log.actor.Connector.LogReply
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/5/26.
  */
class JsonDecoderTest extends FunSuite {

  test("testDecode") {
    val str = "{\"result\":\"success\", \"content\":\"2016-05-26 18:03:51,247 com.sankuai.meituan.waimai.thrift.activity.iface.WmActivityOrderThriftIface-5-thread-49 WARN  (ActivityCounter:243) - CountActivityResult PREVIEW : {'activityPrice':26.9,'extras':[{'ctime':0,'discharge_detail':'{\\'act_price\\':0.1,\\'limit_sale\\':1,\\'limit_time\\':\\'00:00-23:59\\',\\'limit_time_sale\\':10,\\'mt_charge\\':0,\\'name\\':\\'\\',\\'online_pay\\':true,\\'origin_price\n\\':7,\\'poi_charge\\':6.9,\\'role_limit_shared\\':0,\\'wm_act_policy_detail_id\\':0,\\'wm_food_id\\':97165953}\n','id':0,'reduce_fee':6.9,'remark':'<E8><B4><AD><E4><B9><B0>:(<E4><B8><89><E6><96><87><E9><B1><BC><E5>\n<AF><BF><E5><8F><B8>)<EF><BC><8C><E5><8E><9F><E4><BB><B7>7.0<E5><85><83>,<E6><B4><BB><E5><8A><A8><E4>\n<BB><B7>0.1<E5><85><83>','setCtime':false,'setDischarge_detail':true,'setGift_name':false,'setId':fals\ne,'setReduce_fee':true,'setRemark':true,'setType':true,'setUtime':false,'setWm_act_poi_id':true,'setWm\n_act_policy_id':true,'setWm_order_id':false,'setWm_poi_id':true,'type':17,'utime':0,'wm_act_poi_id':52\n208900,'wm_act_policy_id':1001,'wm_order_id':0,'wm_poi_id':938007},{'ctime':0,'discharge_detail':'{\\'d\niscount\\':3,\\'mt_charge\\':3,\\'online_pay\\':1,\\'poi_charge\\':0,\\'time_periods\\':[{\\'end_hour\\':20,\\'end\n_minute\\':0,\\'start_hour\\':16,\\'start_minute\\':30}]}','gift_name':'','id':0,'reduce_fee':3,'remark':'\n<E7><BE><8E><E5><9B><A2><E9><85><8D><E9><80><81><E5><87><8F>3.0<E5><85><83>','setCtime':false,'setDisc\nharge_detail':true,'setGift_name':true,'setId':false,'setReduce_fee':true,'setRemark':true,'setType':t\nrue,'setUtime':false,'setWm_act_poi_id':true,'setWm_act_policy_id':true,'setWm_order_id':false,'setWm_\npoi_id':true,'type':18,'utime':0,'wm_act_poi_id':54955714,'wm_act_policy_id':1024,'wm_order_id':0,'wm_\npoi_id':938007}],'extrasIterator':{},'extrasSize':2,'items':[{'activityPrice':0.1,'activityTag':'disco\nunt','attrIds':[],'attrIdsIterator':{},'attrIdsSize':0,'boxCount':0,'boxPrice':0,'cartId':0,'count':1,\n'id':97165953,'name':'<E4><B8><89><E6><96><87><E9><B1><BC><E5><AF><BF><E5><8F><B8>','originalPrice':7,\n'setActivityPrice':true,'setActivityTag':true,'setAttrIds':true,'setBoxCount':true,'setBoxPrice':true,\n'setCartId':true,'setCount':true,'setId':true,'setName':true,'setOriginalPrice':true,'setTagId':true,'\ntagId':18181549},{'activityPrice':26.8,'activityTag':'','attrIds':[],'attrIdsIterator':{},'attrIdsSize\n':0,'boxCount':1,'boxPrice':0,'cartId':0,'count':1,'id':96725294,'name':'<E8><B6><85><E5><80><BC><E4>\n<BC><98><E6><83><A0><E5><A5><97><E9><A4><90>','originalPrice':26.8,'setActivityPrice':true,'setActivit\nyTag':true,'setAttrIds':true,'setBoxCount':true,'setBoxPrice':true,'setCartId':true,'setCount':true,'s\netId':true,'setName':true,'setOriginalPrice':true,'setTagId':true,'tagId':18088477}],'itemsIterator':{\n},'itemsSize':2,'originalPrice':36.8,'setActivityPrice':true,'setExtras':true,'setItems':true,'setLimi\ntPayType':false,'setOriginalPrice':true,'setPigeon':false,'setTip':true,'tip':'[{\\'detail\\':[\\'26.9\\']\n,\\'type\\':\\'COUPON_USE_LIMIT_PRICE\\'},{\\'detail\\':[\\'97165953\\',\\'http://p0.meituan.net/xianfu/7bcb010\n8f928f772a35aad98ea92aabe2048.png\\'],\\'type\\':\\'DISCOUNT_FOOD_ACCEPT_INFO\\'},{\\'detail\\':[\\'FULL_DISCO\nUNT\\',\\'DISCOUNT_FOOD\\'],\\'type\\':\\'ACTIVITY_EXCLUSIVE\\'}]'}\n\"}"
    try {
      val resBeginIndex = str.indexOf("\"result\":\"") + "\"result\":\"".length
      val resEndIndex = str.indexOf("\"", resBeginIndex)
      val res = str.substring(resBeginIndex, resEndIndex)

      val contentBeginIndex = str.indexOf("\"content\":\"") + "\"content\":\"".length
      val contentEndIndex = str.lastIndexOf("\"}")
      val content = str.substring(contentBeginIndex, contentEndIndex)
      val logReply = LogReply(res, content)

      println(logReply)
    } catch {
      case e: Exception =>
        //  跳过解析失败的消息
        e.printStackTrace()
    }
  }

}
