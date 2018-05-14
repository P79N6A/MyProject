using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace WindowsFormsApplication2
{
    public partial class purchase : Form
    {
        private string name;
        Button btn1;  //声明一个按钮
        private System.Windows.Forms.TextBox textBox1;
        public purchase(string name)
        {
            this.name = name;
            init(name);
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

        private void btn_Click(object sender,EventArgs e)
        {
            //MessageBox.Show("正在购买处理中，请稍后。。");

            int count = Convert.ToInt16((string)List.list[name]);

            if (count == 0)
                MessageBox.Show("来晚啦，零食售光了～～～下次记得来早些哦～～");
            else
            {
                count--;
                List.list.Remove(name);
                List.list.Add(name, Convert.ToString(count));
                MessageBox.Show("购买成功，正在出货中，请稍候哦～～");
            }

            
            this.Controls.Clear();
            this.Close();
            purchase purchase = new purchase(this.name);
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
            this.Controls.Clear();
            this.Close();
            purchase purchase = new purchase(this.name);
            purchase.Show();
        }

        //点击减号
        private void button2_Click(object sender, EventArgs e)
        {
            if (Util.userPurchaseAmount>0)
            {
                Util.userPurchaseAmount--;
            }
            this.Controls.Clear();
            this.Close();
            purchase purchase = new purchase(this.name);
            purchase.Show();
        }

        private void load(object sender, EventArgs e)
        {

        }

        private void textBox4_TextChanged(object sender, EventArgs e)
        {

        }

        
    }
}
