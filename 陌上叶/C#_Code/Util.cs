using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;

namespace WindowsFormsApplication2
{
    class Util
    {
        

        //管理员登录用户名与密码
        public static string userName = "root";
        public static string password = "root";
        //管理员设定零食数量和价格
        public static Hashtable price = new Hashtable();
        //初始化市场价位
        public void initPrice()
        {
            price.Add("冰红茶", "3.0");            //key为id,value为数量
            price.Add("冰糖雪梨", "3.0");
            price.Add("红牛", "5.0");
            price.Add("可口可乐", "3.0");
            price.Add("脉动", "5.0");
            price.Add("苏打水", "2.5");
        }

        

        //用户购物数量
        public static int userPurchaseAmount = 0;
        //用户输入金额
        public static int money;

    }
}
