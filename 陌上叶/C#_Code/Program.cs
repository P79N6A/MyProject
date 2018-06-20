using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;
using System.Media;
using System.Speech.Synthesis;
namespace WindowsFormsApplication2
{
    static class Program
    {
        /// <summary>
        /// 应用程序的主入口点。
        /// </summary>
        [STAThread]
        static void Main()
        {

            Application.EnableVisualStyles();
            try
            {
                Application.SetCompatibleTextRenderingDefault(false);
            }
            catch (Exception ex)
            {

            }
            //初始化货物储存仓库
            List list = new List();
            list.init();

            //临时清单代替固定清单
            List.copyList2Temp();

            //初始化零食价格
            Util.initPrice();

            //语音播放初始化
            SpeechSynthesizer player1=new SpeechSynthesizer();
            player1.SpeakAsync("欢迎使用");
            //初始化舵机角度
            Util.initAngle();

            //初始化购物车
            Util.initCart();

            Application.Run(Util.list);
            //Application.Run(new Form3());
        }
    }
}
