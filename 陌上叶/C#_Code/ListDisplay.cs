using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO.Ports;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Collections;
using System.Speech.Synthesis;
namespace WindowsFormsApplication2
{
    public partial class ListDisplay : Form
    {
        private StringBuilder Builder = new StringBuilder();//避免在事件处理方法中反复的创建，定义到外面。  
        System.IO.Ports.SerialPort comm=new SerialPort();
        string result = null;                //记录返回结果
        bool hasCard = false;              //记录是否刷卡
        public ListDisplay()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            
            if (hasCard)
            {
                if (result != "r" || result == "all")
                {
                    Util.userPurchaseAmount = 0;
                    purchase2 purchase = new purchase2("冰红茶");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            } 
            else
            {
                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
            
            /*
            Util.userPurchaseAmount = 0;
            purchase2 purchase = new purchase2("冰红茶");
            purchase.Show();
            */
        }

        private void button2_Click(object sender, EventArgs e)
        {
            if (hasCard)
            {
                if (result != "y" || result == "all")
                {
                    Util.userPurchaseAmount = 0;
                    purchase2 purchase = new purchase2("冰糖雪梨");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {
                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
    
        }

        private void button3_Click(object sender, EventArgs e)
        {
            if (hasCard)
            {
                if (result != "g" || result == "all")
                {
                    Util.userPurchaseAmount = 0;
                    purchase2 purchase = new purchase2("红牛");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {
                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
 
        }       
          

       
        private void button4_Click(object sender, EventArgs e)
        {
            
            if (hasCard)
            {
                if (result != "b" || result == "all")
                {
                    Util.userPurchaseAmount = 0;
                    purchase2 purchase = new purchase2("可口可乐");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {
                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
 
        }

        private void button5_Click(object sender, EventArgs e)
        {
            if (hasCard)
            {
                if (result != "p" || result == "all")
                {
                    Util.userPurchaseAmount = 0;
                    purchase2 purchase = new purchase2("脉动");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {
                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
 
            
        }
        
        private void button6_Click(object sender, EventArgs e)
        {
            if (hasCard)
            {
                if (result != "k" || result == "all")
                {
                    Util.userPurchaseAmount = 0;
                    purchase2 purchase = new purchase2("苏打水");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {

                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
        }

        private void button7_Click(object sender, EventArgs e)
        {
            //退出登录，回到主页面
            MainDoor mainDoor=new MainDoor();
            mainDoor.Show();
            this.Hide();
        }

        //管理员管理跳转按钮
        private void button8_Click(object sender, EventArgs e)
        {
            DengLu dengLu = new DengLu();
            dengLu.Show();
            this.Hide();
        }
       

        

        private void label1_Click(object sender, EventArgs e)
        {

            if (hasCard)
            {
                if (result != "r" || result == "all")
                {
                    purchase2 purchase = new purchase2("冰红茶");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {

                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
        }

        private void label5_Click(object sender, EventArgs e)
        {
            

            if (hasCard)
            {
                if (result != "b" || result == "all")
                {
                    purchase2 purchase = new purchase2("可口可乐");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {

                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
        }

        private void label4_Click(object sender, EventArgs e)
        {
            if (hasCard)
            {
                if (result != "k" || result == "all")
                {
                    purchase2 purchase = new purchase2("苏打水");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {

                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
        }

        private void label6_Click(object sender, EventArgs e)
        {

            if (hasCard)
            {
                if (result != "p" || result == "all")
                {
                    purchase2 purchase = new purchase2("脉动");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {

                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
        }

        private void label3_Click(object sender, EventArgs e)
        {
            
            if (hasCard)
            {
                if (result != "g" || result == "all")
                {
                    purchase2 purchase = new purchase2("红牛");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买", "温馨提示");
                }
            }
            else
            {

                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片", "温馨提示");
            }
        }

        private void label2_Click(object sender, EventArgs e)
        {
          
            if (hasCard)
            {
                if (result != "y"||result=="all")
                {
                    purchase2 purchase = new purchase2("冰糖雪梨");
                    purchase.Show();
                }
                else
                {
                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("您对此零食敏感，无法购买。");
                    MessageBox.Show("您对此零食敏感，无法购买","温馨提示");
                }
            }
            else
            {

                SpeechSynthesizer player1 = new SpeechSynthesizer();
                player1.SpeakAsync("请刷卡");
                MessageBox.Show("请刷卡片","温馨提示");
            }
        }

        private void ovalShape1_Click(object sender, EventArgs e)
        {

        }

        System.ComponentModel.ComponentResourceManager resource1 = new ComponentResourceManager(typeof(ListDisplay));
        

        private void ApplyResource()
        {
            foreach (Control ctl in this.Controls)
            {
                resource1.ApplyResources(ctl, ctl.Name);
            }
            this.ResumeLayout(false);
            this.PerformLayout();
            resource1.ApplyResources(this, "$this");
        }

        
        //切换到中文
        private void chineseToolStripMenuItem_Click(object sender, EventArgs e)
        {
            System.Threading.Thread.CurrentThread.CurrentUICulture =
               new System.Globalization.CultureInfo("zh-CN");
            ApplyResource();
        }
        //切换到英文
        private void englishToolStripMenuItem_Click(object sender, EventArgs e)
        {
            System.Threading.Thread.CurrentThread.CurrentUICulture =
                new System.Globalization.CultureInfo("en-US");
            ApplyResource();
        }


        //外来代码
        private void Form2_Load(object sender, EventArgs e)
        {
            //用户数据显示
            this.textBox1.Text = "尚未刷卡";
            //购物车显示
            this.cartTeaNo.Text=(string)Util.cart["冰红茶"];
            this.cartPearNo.Text = (string)Util.cart["冰糖雪梨"];
            this.cartNiuNo.Text = (string)Util.cart["红牛"];
            this.cartLeNo.Text = (string)Util.cart["可口可乐"];
            this.cartDongNo.Text = (string)Util.cart["脉动"];
            this.cartWaterNo.Text = (string)Util.cart["苏打水"];

            //初始化下拉串口名称列表框  
            //string[] ports = SerialPort.GetPortNames();
            //Array.Sort(ports);
            //将其显示到comboPorName控件中去

            //comboBoxck.SelectedIndex = comboBoxck.Items.Count > 0 ? 0 : -1;
            //波特率默认为115200bps
            comm.PortName = "COM6";
            comm.BaudRate = 115200;
            //初始化SerialPort对象  
            Util.serialPort.NewLine = "/r/n";
            Util.serialPort.RtsEnable = true;//根据实际情况吧。
            //添加事件注册  
            comm.DataReceived += comm_DataReceived;
        }
        //接收数据的响应
        void comm_DataReceived(object sender, SerialDataReceivedEventArgs e)
        {
            //获取接收缓冲区中数据的字节数
            int n = comm.BytesToRead;
            byte[] buf = new byte[n];

            //将数据读入buf数组中
            comm.Read(buf, 0, n);

            //先清空
            Builder.Clear();

            //因为要访问ui资源，所以需要使用invoke方式同步ui
            this.Invoke((EventHandler)(delegate
            {
                //ASCII规则转换成字符串  Builder.Append(Encoding.ASCII.GetString(buf));

                //依次的拼接出16进制字符串
                foreach (byte b in buf)
                {
                    Builder.Append(b.ToString("X2") + "");
                    string s = Builder.ToString().TrimEnd();
                    result = s;
                    //this.textBox0.Text = s;
                    if (!isweihufs)
                    {
                        if (result != null && result != "n" && result != "e")
                        {
                            hasCard = true;
                        }
                        else
                        {
                            SpeechSynthesizer player1 = new SpeechSynthesizer();
                            player1.SpeakAsync("插卡有误，请重新插卡。");
                            this.textBox1.Text = "插卡有误，请重新插卡";
                        }

                        if (result == "62")
                        {
                            result = "b";
                            SpeechSynthesizer player1 = new SpeechSynthesizer();
                            player1.SpeakAsync("您对蓝色敏感。");
                            this.textBox1.Text = "您对蓝色敏感";
                        }
                        else if (result == "70")
                        {
                            result = "p";
                            SpeechSynthesizer player1 = new SpeechSynthesizer();
                            player1.SpeakAsync("您对紫色敏感。");
                            this.textBox1.Text = "您对紫色敏感";
                        }
                        else if (result == "72")
                        {
                            result = "r";
                            SpeechSynthesizer player1 = new SpeechSynthesizer();
                            player1.SpeakAsync("您对红色敏感。");
                            this.textBox1.Text = "您对红色敏感";
                        }
                        else if (result == "6B")
                        {
                            result = "k";
                            SpeechSynthesizer player1 = new SpeechSynthesizer();
                            player1.SpeakAsync("您对粉色敏感。");
                            this.textBox1.Text = "您对粉色敏感";
                        }
                        else if (result == "67")
                        {
                            result = "g";
                            SpeechSynthesizer player1 = new SpeechSynthesizer();
                            player1.SpeakAsync("您对绿色敏感。");
                            this.textBox1.Text = "您对绿色敏感";
                        }
                        else if (result == "79")
                        {
                            result = "y";
                            SpeechSynthesizer player1 = new SpeechSynthesizer();
                            player1.SpeakAsync("您对黄色敏感。");
                            this.textBox1.Text = "您对黄色敏感";
                        }
                        else
                        {
                            result = "all";
                            //SpeechSynthesizer player1 = new SpeechSynthesizer();
                            //player1.SpeakAsync("您对任何颜色的零食都不敏感。");
                            this.textBox1.Text = "您对任何颜色的零食都不敏感";
                        }
                    }
                    else
                    {
                        if (weihuT.Text == "3T")
                        {
                            if (result == "30")
                            {
                                weihujs.Text = "红色";
                            }
                            if (result == "31")
                            {
                                weihujs.Text = "黄色";
                            }
                            if (result == "32")
                            {
                                weihujs.Text = "绿色";
                            }
                            if (result == "33")
                            {
                                weihujs.Text = "蓝色";
                            }
                            if (result == "34")
                            {
                                weihujs.Text = "紫色";
                            }
                            if (result == "35")
                            {
                                weihujs.Text = "粉色";
                            }
                            if (result == "65")
                            {
                                weihujs.Text = "没有识别到任何颜色";
                            }
                        }
                        else if (weihuT.Text == "3N")
                        {
                            char[] x = new char[2];
                            x = result.ToCharArray();
                            string target = "";
                            if (x[0] == 'A')
                            {
                                if (x[1] == 'A')
                                {
                                    target = Convert.ToString(10 * 16 + 10);
                                }
                                else if (x[1] == 'B')
                                {
                                    target = Convert.ToString(10 * 16 + 11);
                                }
                                else if (x[1] == 'C')
                                {
                                    target = Convert.ToString(10 * 16 + 12);
                                }
                                else if (x[1] == 'D')
                                {
                                    target = Convert.ToString(10 * 16 + 13);
                                }
                                else if (x[1] == 'E')
                                {
                                    target = Convert.ToString(10 * 16 + 14);
                                }
                                else if (x[1] == 'F')
                                {
                                    target = Convert.ToString(10 * 16 + 15);
                                }
                                else
                                {
                                    target = Convert.ToString(10 * 16 + Convert.ToInt16(x[1].ToString()));
                                }

                            }
                            else if (x[0] == 'B')
                            {
                                if (x[1] == 'A')
                                {
                                    target = Convert.ToString(11 * 16 + 10);
                                }
                                else if (x[1] == 'B')
                                {
                                    target = Convert.ToString(11 * 16 + 11);
                                }
                                else if (x[1] == 'C')
                                {
                                    target = Convert.ToString(11 * 16 + 12);
                                }
                                else if (x[1] == 'D')
                                {
                                    target = Convert.ToString(11 * 16 + 13);
                                }
                                else if (x[1] == 'E')
                                {
                                    target = Convert.ToString(11 * 16 + 14);
                                }
                                else if (x[1] == 'F')
                                {
                                    target = Convert.ToString(11 * 16 + 15);
                                }
                                else
                                {
                                    target = Convert.ToString(11 * 16 + Convert.ToInt16(x[1].ToString()));
                                }

                            }
                            else if (x[0] == 'C')
                            {
                                if (x[1] == 'A')
                                {
                                    target = Convert.ToString(12 * 16 + 10);
                                }
                                else if (x[1] == 'B')
                                {
                                    target = Convert.ToString(12 * 16 + 11);
                                }
                                else if (x[1] == 'C')
                                {
                                    target = Convert.ToString(12 * 16 + 12);
                                }
                                else if (x[1] == 'D')
                                {
                                    target = Convert.ToString(12 * 16 + 13);
                                }
                                else if (x[1] == 'E')
                                {
                                    target = Convert.ToString(12 * 16 + 14);
                                }
                                else if (x[1] == 'F')
                                {
                                    target = Convert.ToString(12 * 16 + 15);
                                }
                                else
                                {
                                    target = Convert.ToString(12 * 16 + Convert.ToInt16(x[1].ToString()));
                                }
                            }
                            else if (x[0] == 'D')
                            {
                                if (x[1] == 'A')
                                {
                                    target = Convert.ToString(13 * 16 + 10);
                                }
                                else if (x[1] == 'B')
                                {
                                    target = Convert.ToString(13 * 16 + 11);
                                }
                                else if (x[1] == 'C')
                                {
                                    target = Convert.ToString(13 * 16 + 12);
                                }
                                else if (x[1] == 'D')
                                {
                                    target = Convert.ToString(13 * 16 + 13);
                                }
                                else if (x[1] == 'E')
                                {
                                    target = Convert.ToString(13 * 16 + 14);
                                }
                                else if (x[1] == 'F')
                                {
                                    target = Convert.ToString(13 * 16 + 15);
                                }
                                else
                                {
                                    target = Convert.ToString(13 * 16 + Convert.ToInt16(x[1].ToString()));
                                }

                            }
                            else if (x[0] == 'E')
                            {
                                if (x[1] == 'A')
                                {
                                    target = Convert.ToString(14 * 16 + 10);
                                }
                                else if (x[1] == 'B')
                                {
                                    target = Convert.ToString(14 * 16 + 11);
                                }
                                else if (x[1] == 'C')
                                {
                                    target = Convert.ToString(14 * 16 + 12);
                                }
                                else if (x[1] == 'D')
                                {
                                    target = Convert.ToString(14 * 16 + 13);
                                }
                                else if (x[1] == 'E')
                                {
                                    target = Convert.ToString(14 * 16 + 14);
                                }
                                else if (x[1] == 'F')
                                {
                                    target = Convert.ToString(14 * 16 + 15);
                                }
                                else
                                {
                                    target = Convert.ToString(14 * 16 + Convert.ToInt16(x[1].ToString()));
                                }

                            }
                            else if (x[0] == 'F')
                            {
                                if (x[1] == 'A')
                                {
                                    target = Convert.ToString(15 * 16 + 10);
                                }
                                else if (x[1] == 'B')
                                {
                                    target = Convert.ToString(15 * 16 + 11);
                                }
                                else if (x[1] == 'C')
                                {
                                    target = Convert.ToString(15 * 16 + 12);
                                }
                                else if (x[1] == 'D')
                                {
                                    target = Convert.ToString(15 * 16 + 13);
                                }
                                else if (x[1] == 'E')
                                {
                                    target = Convert.ToString(15 * 16 + 14);
                                }
                                else if (x[1] == 'F')
                                {
                                    target = "很远";
                                }
                                else
                                {
                                    target = Convert.ToString(15 * 16 + Convert.ToInt16(x[1].ToString()));
                                }


                            }
                            else
                            {
                                if (x[1] == 'A')
                                {
                                    target = Convert.ToString(Convert.ToInt16(x[0].ToString()) * 16 + 10);
                                }
                                else if (x[1] == 'B')
                                {
                                    target = Convert.ToString(Convert.ToInt16(x[0].ToString()) * 16 + 11);
                                }
                                else if (x[1] == 'C')
                                {
                                    target = Convert.ToString(Convert.ToInt16(x[0].ToString()) * 16 + 12);
                                }
                                else if (x[1] == 'D')
                                {
                                    target = Convert.ToString(Convert.ToInt16(x[0].ToString()) * 16 + 13);
                                }
                                else if (x[1] == 'E')
                                {
                                    target = Convert.ToString(Convert.ToInt16(x[0].ToString()) * 16 + 14);
                                }
                                else if (x[1] == 'F')
                                {
                                    target = Convert.ToString(Convert.ToInt16(x[0].ToString()) * 16 + 15);
                                }
                                else
                                {
                                    target = Convert.ToString(Convert.ToInt16(x[0].ToString()) * 16 + Convert.ToInt16(x[1].ToString()));
                                }
                                /*
                                Int32 no = Convert.ToInt16(s);
                                Int32 a1 = no / 10;
                                Int32 a2 = no % 10;
                                target = Convert.ToString(a1 * 16 + a2);
                                //textBox0.Text = target;
                                */
                            }
                            weihujs.Text = "距离：" + target;
                        }
                        else
                        {
                            if (result == "30")
                            {
                                weihujs.Text = "0度";
                            }
                            if (result == "3330")
                            {
                                weihujs.Text = "30度";
                            }
                            if (result == "3630")
                            {
                                weihujs.Text = "60度";
                            }
                            if (result == "3930")
                            {
                                weihujs.Text = "90度";
                            }
                            if (result == "313230")
                            {
                                weihujs.Text = "120度";
                            }
                            if (result == "313530")
                            {
                                weihujs.Text = "150度";
                            }
                            if (result == "313830")
                            {
                                weihujs.Text = "180度";
                            }
                            if (result == "323130")
                            {
                                weihujs.Text = "210度";
                            }
                            if (result == "323430")
                            {
                                weihujs.Text = "240度";
                            }
                            if (result == "323730")
                            {
                                weihujs.Text = "270度";
                            }
                            if (result == "333030")
                            {
                                weihujs.Text = "300度";
                            }
                            if (result == "333330")
                            {
                                weihujs.Text = "330度";
                            }
                            if (result == "333630")
                            {
                                weihujs.Text = "360度";
                            }
                        }
                        
                    }
                    isweihufs = false; 
                }
                //追加的形式添加到文本框末端，并滚动到最后。  
                //this.richTextBoxjs.Text(Builder.ToString());
            }));
        }
        

        //点击发送按钮事件的响应
        private void buttonfs_Click(object sender, EventArgs e)
        {
            //ASCII规则 comm.Write(this.textBoxfs.Text);
            //如果写错了一些，我们允许的，只用正则得到有效的十六进制数  

            /*MatchCollection mc = Regex.Matches(textBoxfs.Text, @"(?i)[\da-f]{2}");
            //填充到这个临时列表中
            List<byte> buf = new List<byte>();
            //依次添加到列表中  
            foreach (Match m in mc)
            {
                buf.Add(byte.Parse(m.Value, System.Globalization.NumberStyles.HexNumber));
            }
            */
            //转换列表为数组后发送 
            try
            {
                if (!comm.IsOpen)
                {
                    comm.Open();
                }
            }
            //打开失败，抛出异常  
            catch (Exception ex)
            {
                //捕获到异常信息，创建一个新的comm对象，之前的不能用了。
                comm = new SerialPort();
            }
            comm.Write("211");
        }
        private void textBox0_TextChanged(object sender, EventArgs e)
        {

        }

        private void label0_Click(object sender, EventArgs e)
        {

        }

        private void panel1_Paint(object sender, System.Windows.Forms.PaintEventArgs e)
        {

        }

        //清空
        private void cartTeaBtn_Click(object sender, System.EventArgs e)
        {
            string countStr=(string)List.list["冰红茶"];
            //零食清单恢复
            List.tempList.Remove("冰红茶");
            List.tempList.Add("冰红茶", countStr);
            //购物车
            Util.cart.Remove("冰红茶");
            Util.cart.Add("冰红茶","0");
            this.cartTeaNo.Text = (string)Util.cart["冰红茶"];    
        }
        //清空
        private void cartPearBtn_Click(object sender, System.EventArgs e)
        {
            //零食清单恢复
            string countStr = (string)List.list["冰糖雪梨"];
            List.tempList.Remove("冰糖雪梨");
            List.tempList.Add("冰糖雪梨", countStr);
            //购物车
            Util.cart.Remove("冰糖雪梨");
            Util.cart.Add("冰糖雪梨", "0");
            this.cartPearNo.Text = (string)Util.cart["冰糖雪梨"];
        }
        //清空
        private void cartNiuBtn_Click(object sender, System.EventArgs e)
        {
            //零食清单恢复
            string countStr = (string)List.list["红牛"];
            List.tempList.Remove("红牛");
            List.tempList.Add("红牛", countStr);
            //购物车
            Util.cart.Remove("红牛");
            Util.cart.Add("红牛", "0");
            this.cartNiuNo.Text = (string)Util.cart["红牛"];
        }
        //清空
        private void cartLeBtn_Click(object sender, System.EventArgs e)
        {
            //零食清单恢复
            string countStr = (string)List.list["可口可乐"];
            List.tempList.Remove("可口可乐");
            List.tempList.Add("可口可乐", countStr);
            //购物车
            Util.cart.Remove("可口可乐");
            Util.cart.Add("可口可乐", "0");
            this.cartLeNo.Text = (string)Util.cart["可口可乐"];
        }
        //清空
        private void cartDongBtn_Click(object sender, System.EventArgs e)
        {
            //零食清单恢复
            string countStr = (string)List.list["脉动"];
            List.tempList.Remove("脉动");
            List.tempList.Add("脉动", countStr);
            //购物车
            Util.cart.Remove("脉动");
            Util.cart.Add("脉动", "0");
            this.cartDongNo.Text = (string)Util.cart["脉动"];
        }
        //清空
        private void cartWaterBtn_Click(object sender, System.EventArgs e)
        {
            //零食清单恢复
            string countStr = (string)List.list["苏打水"];
            List.tempList.Remove("苏打水");
            List.tempList.Add("苏打水", countStr);
            //购物车
            Util.cart.Remove("苏打水");
            Util.cart.Add("苏打水", "0");
            this.cartWaterNo.Text = (string)Util.cart["苏打水"];
        }

        //全部清空
        private void allRm_Click(object sender, System.EventArgs e)
        {
            //恢复原来的零食清单数量
            List.copyList2Temp();
            //购物车清空
            Util.setCart(0,0,0,0,0,0);
            this.cartTeaNo.Text = (string)Util.cart["冰红茶"];
            this.cartPearNo.Text = (string)Util.cart["冰糖雪梨"];
            this.cartNiuNo.Text = (string)Util.cart["红牛"];
            this.cartLeNo.Text = (string)Util.cart["可口可乐"];
            this.cartDongNo.Text = (string)Util.cart["脉动"];
            this.cartWaterNo.Text = (string)Util.cart["苏打水"];
        }

        //刷新购物车
        private void flush_Click(object sender, System.EventArgs e)
        {
            this.cartTeaNo.Text = (string)Util.cart["冰红茶"];
            this.cartPearNo.Text = (string)Util.cart["冰糖雪梨"];
            this.cartNiuNo.Text = (string)Util.cart["红牛"];
            this.cartLeNo.Text = (string)Util.cart["可口可乐"];
            this.cartDongNo.Text = (string)Util.cart["脉动"];
            this.cartWaterNo.Text = (string)Util.cart["苏打水"];
        }

        //立即购买
        private void purchaseBtn_Click(object sender, System.EventArgs e)
        {
            if (Util.cartIsEmpty())
            {
                MessageBox.Show("购物车为空！！", "温馨提示", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            else
            {
                //发送数据至串口
                string value = Convert.ToString(4) + (string)Util.cart["冰红茶"] + (string)Util.cart["冰糖雪梨"] + (string)Util.cart["红牛"] + (string)Util.cart["可口可乐"]
                    + (string)Util.cart["脉动"] + (string)Util.cart["苏打水"];
                try
                {
                    if (!comm.IsOpen)
                    {
                        comm.Open();
                    }
                }
                //打开失败，抛出异常  
                catch (Exception ex)
                {
                    //捕获到异常信息，创建一个新的comm对象，之前的不能用了。
                    comm = new SerialPort();
                }
                try
                {
                    comm.Write(value);

                    SpeechSynthesizer player1 = new SpeechSynthesizer();
                    player1.SpeakAsync("零食正在出机，请及时取走，谢谢。");
                    //comm.Write("");
                    //购买结束后需要重新刷卡
                    hasCard = false;
                    this.textBox1.Text = "尚未刷卡";
                    //购买成功后临时清单的值赋给固定清单
                    List.copyTemp2List();

                    //购物车清空
                    Util.setCart(0, 0, 0, 0, 0, 0);
                    MessageBox.Show("零食正在出机，请您及时取走，欢迎下次光临。", "温馨提示", MessageBoxButtons.OK, MessageBoxIcon.Information);
                }
                catch (Exception ex)
                {
                    MessageBox.Show("系统出错，购买失败。","温馨提示",MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }
        }

        private void weihuT_TextChanged(object sender, System.EventArgs e)
        {

        }

        bool isweihufs = false;
        private void weihufs_Click(object sender, System.EventArgs e)
        {
            try
            {
                if (!comm.IsOpen)
                {
                    comm.Open();
                }
            }
            catch (Exception ex)
            {
                comm = new SerialPort();
            }
            isweihufs = true;
            comm.Write(weihuT.Text);
            //isweihufs = false;
        }

        private void weihujs_TextChanged(object sender, System.EventArgs e)
        {

        }
                //追加的形式添加到文本框末端，并滚动到最后。  
                //this.richTextBoxjs.AppendText(Builder.ToString());
    }
}
