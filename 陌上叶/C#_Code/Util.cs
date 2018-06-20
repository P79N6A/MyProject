using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using System.IO.Ports;

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
        public static void initPrice()
        {
            price.Add("冰红茶", "3.0");            //key为id,value为数量
            price.Add("冰糖雪梨", "3.0");
            price.Add("红牛", "5.0");
            price.Add("可口可乐", "3.0");
            price.Add("脉动", "5.0");
            price.Add("苏打水", "2.5");
        }

        

        public static void  initAngle()
        {
            angle=0;
        }

        //用户购物数量
        public static int userPurchaseAmount = 0;
        //用户输入金额
        public static double money;


        //语音播放欢迎光临
        public static System.Media.SoundPlayer player1= new System.Media.SoundPlayer();

        public static void initPlayer1()
        {
            player1.SoundLocation = @"C:\Users\DELL\Desktop\RuiDer\自主售货机项目截屏\player1.wav";
        }

        //语音播放请刷卡
        public static System.Media.SoundPlayer player2 = new System.Media.SoundPlayer();

        public static void initPlayer2()
        {
            player2.SoundLocation = @"C:\Users\DELL\Desktop\RuiDer\自主售货机项目截屏\player2.wav";
        }
        //语音播放刷卡成功
        public static System.Media.SoundPlayer player3 = new System.Media.SoundPlayer();

        public static void initPlayer3()
        {
            //player3.SoundLocation = @"F:\RuiDer_Code\C#\programs\AutomaticSaleMachine\WindowsFormsApplication2\wav\player3.wav";
        }

        //语音播放购买成功
        public static System.Media.SoundPlayer player4 = new System.Media.SoundPlayer();

        public static void initPlayer4()
        {
            player4.SoundLocation = @"C:\Users\DELL\Desktop\RuiDer\自主售货机项目截屏\player4.wav";
        }
        

        //舵机角度控制
        public static Int16 angle;

        //端口
        public static SerialPort serialPort = new SerialPort();
        //端口设置
        public static void openPort()
        {
            string[] ports = SerialPort.GetPortNames();
            Array.Sort(ports);
            //serialPort.PortName = ports[0];
            serialPort.PortName = "COM3";
            serialPort.BaudRate = 115200;   
            try
            {
                serialPort.Open();
            }
            //打开失败，抛出异常  
            catch (Exception ex)
            {
                //捕获到异常信息，创建一个新的comm对象，之前的不能用了。
                serialPort = new SerialPort();
            }
        }
        public static void closePort()
        {
            serialPort.Close();
        }

        //购物车
        public static Hashtable cart = new Hashtable();


        //初始化购物车
        public static void initCart()
        {
            cart.Add("冰红茶", "0");            
            cart.Add("冰糖雪梨", "0");
            cart.Add("红牛", "0");
            cart.Add("可口可乐", "0");
            cart.Add("脉动", "0");
            cart.Add("苏打水", "0");
        }

        //设置购物车
        public static void setCart(Int16 a1,Int16 a2,Int16 a3,Int16 a4,Int16 a5,Int16 a6)
        {
            cart.Clear();
            cart.Add("冰红茶", a1.ToString());            
            cart.Add("冰糖雪梨", a2.ToString());
            cart.Add("红牛", a3.ToString());
            cart.Add("可口可乐", a4.ToString());
            cart.Add("脉动", a5.ToString());
            cart.Add("苏打水", a6.ToString());
        }
        //购物车是否为空
        public static bool cartIsEmpty()
        {
            Int16 a1=Convert.ToInt16(cart["冰红茶"]);
            Int16 a2=Convert.ToInt16(cart["冰糖雪梨"]);
            Int16 a3=Convert.ToInt16(cart["红牛"]);
            Int16 a4=Convert.ToInt16(cart["可口可乐"]);
            Int16 a5=Convert.ToInt16(cart["脉动"]);
            Int16 a6=Convert.ToInt16(cart["苏打水"]);
            return (a1 == 0) && (a2 == 0) && (a3 == 0) && (a4 == 0) && (a5 == 0) && (a6 == 0);
        }
        

        //主页面
        public static ListDisplay list=new ListDisplay();
            
    }
}
