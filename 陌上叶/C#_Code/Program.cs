using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;
using System.Media;

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

            Application.SetCompatibleTextRenderingDefault(false);

            //初始化货物储存仓库
            List list = new List();
            list.init();

            //初始化零食价格
            Util priceUtil = new Util();
            priceUtil.initPrice();

            //语音播放
            
            Util.player2.SoundLocation = @"F:\RuiDer_Code\C#\programs\AutomaticSaleMachine\WindowsFormsApplication2\wav\卡农.wav";
            Util.player2.PlayLooping();
            Application.Run(new ListDisplay());

        }
    }
}
