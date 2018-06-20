using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Media;
using System.IO.Ports;


namespace WindowsFormsApplication2
{
    public partial class purchase1 : Form
    {
        private string name;
        Button btn1;  //加入购物车
        Button btn2;  //查看购物车
        private System.Windows.Forms.TextBox textBox1;
        SerialPort comm = new SerialPort();
        private StringBuilder Builder = new StringBuilder();//避免在事件处理方法中反复的创建，定义到外面。  
        string result = null;                                  //记录串口返回数据结果
        public purchase1(string name)
        {
            Util.money = 0;
            this.name = name;
            init(name);
            InitializeComponent();
        }

        public purchase1(string name,SerialPort comm)
        {
            Util.money = 0;
            this.name = name;
            init(name);
            this.comm = comm;
            InitializeComponent();
        }

        private void init(string name)
        {
            //btn1
          
            btn1 = new Button(); //初始化
            btn1.Text = "立即支付";  //设置文字
            btn1.Width = 100;  //宽度
            btn1.Height = 50;  //高度
            btn1.Location = new Point(200, 300);   //坐标位置
            btn1.Click += btn_Click;
            this.btn1.BackColor = System.Drawing.Color.Transparent;
            this.btn1.FlatStyle = System.Windows.Forms.FlatStyle.Popup;
            this.btn1.ForeColor = System.Drawing.Color.Black;

            this.Controls.Add(btn1);  //添加到窗体

            
            // 
            // textBox1
            // 
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.textBox1.BackColor = System.Drawing.Color.DarkOrange;
            this.textBox1.Font = new System.Drawing.Font("宋体", 13.8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
            this.textBox1.ForeColor = System.Drawing.Color.Transparent;
            this.textBox1.ReadOnly = true;
            this.textBox1.Location = new System.Drawing.Point(23, 73);
            this.textBox1.Name = "textBox2";
            this.textBox1.Size = new System.Drawing.Size(258, 34);
            this.textBox1.TabIndex = 0;

            //计算剩余数量
            int count = Convert.ToInt16(List.list[name]) - Util.userPurchaseAmount;
            
            this.textBox1.Text = name+" 剩余>>"+(string)Convert.ToString(count);
            this.Controls.Add(textBox1);  //添加到窗体
        }

        //立即购买
        private void btn_Click(object sender,EventArgs e)
        {
           
            int count = Convert.ToInt16((string)List.list[name]);
            if (Util.userPurchaseAmount == 0)
                MessageBox.Show("您还未输入购买数量，请输入购买的数量");
            else if (count == 0)
                MessageBox.Show("来晚啦，零食售光了～～～下次记得来早些哦～～");
            else
            {
                count=count-Util.userPurchaseAmount;
                List.list.Remove(name);
                List.list.Add(name, Convert.ToString(count));
                //MessageBox.Show("购买成功，您一共购买 ["+this.name+"] "+Util.userPurchaseAmount+" 瓶，共消费"+Util.money+"元 。请你确认。。");

                DialogResult dr;
                dr = MessageBox.Show("您一共购买 [" + this.name + "] " + Util.userPurchaseAmount + " 瓶，共消费" + Util.money + "元 。请你确认。。",
                    "确认", MessageBoxButtons.OKCancel,
                MessageBoxIcon.Information);
                if (dr == DialogResult.OK)
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
                    //发送信息
                    Int16 kind = -1;
                    if (name == "冰红茶")
                    {
                        kind = 1;
                    }
                    else if (name == "冰糖雪梨")
                    {
                        kind = 2;
                    }
                    else if (name == "红牛")
                    {
                        kind = 3;
                    }
                    else if (name == "可口可乐")
                    {
                        kind = 4;
                    }
                    else if (name == "脉动")
                    {
                        kind = 5;
                    }
                    else
                    {
                        kind = 6;
                    }
                    String value = "1" + kind + Util.userPurchaseAmount;

                    comm.Write("211");
                    MessageBox.Show("零食正在出机，请您及时取走，欢迎您下次光临"+result);

                }
                else if (dr == DialogResult.Cancel)
                {
                    //不进行任何操作
                }
                else
                    MessageBox.Show("你没有进行任何的操作！", "系统提示4");
                    }

            this.Hide();
            this.Controls.Clear();
            this.Close();
            Util.userPurchaseAmount = 0;
            purchase1 purchase = new purchase1(this.name);
            purchase.Show();
        }

        //点击加号
        private void button1_Click(object sender, EventArgs e)
        {
            //取消
            
            if (Util.userPurchaseAmount < Convert.ToInt16(List.list[this.name]))
            {
                Util.userPurchaseAmount++;
            }
            
            //购买数量更新
            this.textBox3.Text = Convert.ToString(Util.userPurchaseAmount);

            //计算剩余数量
            int count = Convert.ToInt16(List.list[name]) - Util.userPurchaseAmount;
            this.textBox1.Text = name + " 剩余>>" + (string)Convert.ToString(count);    //总数量更新

            //预计金额刷新
            //计算金额
            double x = Convert.ToDouble(Util.price[name]);
            int y = Util.userPurchaseAmount;
            Util.money = x * y;
            this.textBox5.Text = Convert.ToString(x * y) + "元";

        }

        //点击减号
        private void button2_Click(object sender, EventArgs e)
        {
            if (Util.userPurchaseAmount>0)
            {
                Util.userPurchaseAmount--;
            }
            //购买数量更新
            this.textBox3.Text = Convert.ToString(Util.userPurchaseAmount);

            //计算剩余数量
            int count = Convert.ToInt16(List.list[name]) - Util.userPurchaseAmount;
            this.textBox1.Text = name + " 剩余>>" + (string)Convert.ToString(count);

            //预计金额刷新
            //计算金额
            double x = Convert.ToDouble(Util.price[name]);
            int y = Util.userPurchaseAmount;
            Util.money = x * y;
            this.textBox5.Text = Convert.ToString(x * y) + "元";
        }

        private void load(object sender, EventArgs e)
        {
            //comm.PortName = "COM3";
            //comm.BaudRate = 115200;
            //初始化SerialPort对象  
            //comm.NewLine = "/r/n";
            //comm.RtsEnable = true;//根据实际情况吧。
            //添加事件注册  
            comm.DataReceived += comm_DataReceived;

        }
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
                    MessageBox.Show("结果"+result);
                }
                //追加的形式添加到文本框末端，并滚动到最后。  
                //this.richTextBoxjs.AppendText(Builder.ToString());
            }));
        }

        private void textBox4_TextChanged(object sender, EventArgs e)
        {

        }

        
    }
}
