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
    public partial class purchase2 : Form
    {
        private string name;
        Button btn1;  //加入购物车
        Button btn2;  //查看购物车
        private System.Windows.Forms.TextBox textBox1;
        public purchase2(string name)
        {
            Util.money = 0;
            this.name = name;
            init(name);
            InitializeComponent();
        }

        private void init(string name)
        {
            //btn1

            btn1 = new Button(); //初始化
            btn1.Text = "加入购物车";  //设置文字
            btn1.Width = 100;  //宽度
            btn1.Height = 50;  //高度
            btn1.Location = new Point(200, 300);   //坐标位置
            btn1.Click += btn1_Click;
            this.btn1.BackColor = System.Drawing.Color.Transparent;
            this.btn1.FlatStyle = System.Windows.Forms.FlatStyle.Popup;
            this.btn1.ForeColor = System.Drawing.Color.Black;

            this.Controls.Add(btn1);  //添加到窗体

            //btn2查看购物车
            btn2 = new Button(); //初始化
            btn2.Text = "查看购物车";  //设置文字
            btn2.Width = 100;  //宽度
            btn2.Height = 50;  //高度
            btn2.Location = new Point(400, 300);   //坐标位置
            btn2.Click += btn2_Click;
            this.btn2.BackColor = System.Drawing.Color.Transparent;
            this.btn2.FlatStyle = System.Windows.Forms.FlatStyle.Popup;
            this.btn2.ForeColor = System.Drawing.Color.Black;

            this.Controls.Add(btn2);  //添加到窗体

            // 
            // textBox1
            // 
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.textBox1.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.textBox1.BackColor = System.Drawing.Color.RoyalBlue; 
            this.textBox1.Font = new System.Drawing.Font("宋体", 18F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
            this.textBox1.ForeColor = System.Drawing.Color.Black;
            this.textBox1.ReadOnly = true;
            this.textBox1.Location = new System.Drawing.Point(23, 73);
            this.textBox1.Name = "textBox2";
            this.textBox1.Size = new System.Drawing.Size(280, 34);
            this.textBox1.TabIndex = 0;

            //计算剩余数量
            int count = Convert.ToInt16(List.tempList[name]) - Util.userPurchaseAmount;

            this.textBox1.Text = name + " 剩余>>" + (string)Convert.ToString(count);
            this.Controls.Add(textBox1);  //添加到窗体
        }

        //加入购物车
        private void btn1_Click(object sender, EventArgs e)
        {

            int counts = Convert.ToInt16((string)List.tempList[name]);
            if (counts == 0)
                MessageBox.Show("来晚啦，零食售光了～～～下次记得来早些哦～～");
            else if (Util.userPurchaseAmount == 0)
                MessageBox.Show("您还未输入购买数量，请输入购买的数量");
            else
            {
                DialogResult dr;
                dr = MessageBox.Show("加入购物车 [" + this.name + "]  " + Util.userPurchaseAmount + " 瓶，请你确认",
                    "确认", MessageBoxButtons.OKCancel,
                MessageBoxIcon.Information);
                if (dr == DialogResult.OK)
                {
                    //购物车
                    int amount = Convert.ToInt16(Util.cart[name]);  //获取该物品在购物车中的数量
                    Util.cart.Remove(name);
                    Util.cart.Add(name, Convert.ToString(Util.userPurchaseAmount + amount));
                    
                    //临时清单
                    int left= Convert.ToInt16(List.tempList[name]) - Util.userPurchaseAmount;
                    List.tempList.Remove(name);
                    List.tempList.Add(name,Convert.ToString(left));
                    MessageBox.Show("零食已加入购物车。");     
                }
                else if (dr == DialogResult.Cancel)
                {
                    //不进行任何操作
                }
                else
                    MessageBox.Show("你没有进行任何的操作！", "系统提示4");
            }

            Util.userPurchaseAmount = 0;
            //购买数量更新
            this.textBox3.Text = Convert.ToString(Util.userPurchaseAmount);
            //计算剩余数量
            int count = Convert.ToInt16(List.tempList[name]) - Util.userPurchaseAmount;
            this.textBox1.Text = name + " 剩余>>" + (string)Convert.ToString(count);    //总数量更新

            //预计金额刷新
            //计算金额
            double x = Convert.ToDouble(Util.price[name]);
            int y = Util.userPurchaseAmount;
            Util.money = x * y;
            this.textBox5.Text = Convert.ToString(x * y) + "元";
            
        }

        //查看购物车
        private void btn2_Click(object sender, EventArgs e)
        {
            this.Hide();
            //刷新购物车页面
            Util.list.Hide();
            Util.list.Show();
            
            
        }


        //点击加号
        private void button1_Click(object sender, EventArgs e)
        {

            if (Convert.ToInt16(List.tempList[name]) > 0)
            {
                if (Util.userPurchaseAmount < Convert.ToInt16(List.list[this.name]))
                {
                    Util.userPurchaseAmount++;
                }

                //购买数量更新
                this.textBox3.Text = Convert.ToString(Util.userPurchaseAmount);

                //计算剩余数量
                int count = Convert.ToInt16(List.tempList[name]) - Util.userPurchaseAmount;
                this.textBox1.Text = name + " 剩余>>" + (string)Convert.ToString(count);    //总数量更新

                //预计金额刷新
                //计算金额
                double x = Convert.ToDouble(Util.price[name]);
                int y = Util.userPurchaseAmount;
                Util.money = x * y;
                this.textBox5.Text = Convert.ToString(x * y) + "元";
            }
            else
            {
                MessageBox.Show(name + "货物已卖光哦");
            }
        }

        //点击减号
        private void button2_Click(object sender, EventArgs e)
        {

            if (Convert.ToInt16(List.tempList[name]) > 0)
            {
                if (Util.userPurchaseAmount > 0)
                {
                    Util.userPurchaseAmount--;
                }
                //购买数量更新
                this.textBox3.Text = Convert.ToString(Util.userPurchaseAmount);

                //计算剩余数量
                int count = Convert.ToInt16(List.tempList[name]) - Util.userPurchaseAmount;
                this.textBox1.Text = name + " 剩余>>" + (string)Convert.ToString(count);

                //预计金额刷新
                //计算金额
                double x = Convert.ToDouble(Util.price[name]);
                int y = Util.userPurchaseAmount;
                Util.money = x * y;
                this.textBox5.Text = Convert.ToString(x * y) + "元";
            }
            else
            {
                MessageBox.Show(name+"货物已卖光哦");
            }
        }

        private void load(object sender, EventArgs e)
        {
            //计算剩余数量
            int count = Convert.ToInt16(List.tempList[name]) - Util.userPurchaseAmount;
            this.textBox1.Text = name + " 剩余>>" + (string)Convert.ToString(count);
            //购买数量
            this.textBox3.Text = Convert.ToString(Util.userPurchaseAmount);
            //计算金额
            double x = Convert.ToDouble(Util.price[name]);
            int y = Util.userPurchaseAmount;
            Util.money = x * y;
            this.textBox5.Text = Convert.ToString(x * y) + "元";
        }
        
        private void textBox4_TextChanged(object sender, EventArgs e)
        {

        }


    }
}
