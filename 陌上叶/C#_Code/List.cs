using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;

namespace WindowsFormsApplication2
{
    class List
    {
        public static  Hashtable list = new Hashtable();

        public void init()
        {
            list.Add("冰红茶", "10");            //key为id,value为数量
            list.Add("冰糖雪梨", "10");
            list.Add("红牛", "10");
            list.Add("可口可乐", "10");
            list.Add("脉动", "10");
            list.Add("苏打水","10");
        }

    }
}
