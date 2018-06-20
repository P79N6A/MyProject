using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;

namespace WindowsFormsApplication2
{
    class List
    {
        //购买后清单
        public static  Hashtable list = new Hashtable();
        //临时清单
        public static Hashtable tempList=new Hashtable();
        public void init()
        {
            list.Add("冰红茶", "2");            //key为id,value为数量
            list.Add("冰糖雪梨", "2");
            list.Add("红牛", "2");
            list.Add("可口可乐", "2");
            list.Add("脉动", "2");
            list.Add("苏打水","2");
        }

        public static void copyList2Temp()
        {
            tempList.Clear();
            string a1 = (string)list["冰红茶"];
            string a2 = (string)list["冰糖雪梨"];
            string a3 = (string)list["红牛"];
            string a4 = (string)list["可口可乐"];
            string a5 = (string)list["脉动"];
            string a6 = (string)list["苏打水"];
            tempList.Add("冰红茶",a1);
            tempList.Add("冰糖雪梨", a2);
            tempList.Add("红牛",a3);
            tempList.Add("可口可乐",a4);
            tempList.Add("脉动",a5);
            tempList.Add("苏打水",a6);
        }

        public static void copyTemp2List()
        {
            list.Clear();
            string a1 = (string)tempList["冰红茶"];
            string a2 = (string)tempList["冰糖雪梨"];
            string a3 = (string)tempList["红牛"];
            string a4 = (string)tempList["可口可乐"];
            string a5 = (string)tempList["脉动"];
            string a6 = (string)tempList["苏打水"];
            list.Add("冰红茶", a1);
            list.Add("冰糖雪梨", a2);
            list.Add("红牛", a3);
            list.Add("可口可乐", a4);
            list.Add("脉动", a5);
            list.Add("苏打水", a6);
        }

        
    }
}
