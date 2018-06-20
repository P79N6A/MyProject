using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Collections;
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

namespace WindowsFormsApplication2
{
    public partial class ManagerUtil2 : Form
    {
        
        public ManagerUtil2()
        {
            
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            Hashtable reList = null;
            Hashtable rePrice = null;

            try
            {
                reList = List.list;
                rePrice = Util.price;

                //零食数量
                List.list.Clear();
                List.list.Add("冰红茶", (string)this.textBox9.Text);            //key为id,value为数量
                List.list.Add("冰糖雪梨", (string)this.textBox10.Text);
                List.list.Add("红牛", (string)this.textBox11.Text);
                List.list.Add("可口可乐", (string)this.textBox12.Text);
                List.list.Add("脉动", (string)this.textBox13.Text);
                List.list.Add("苏打水", (string)this.textBox14.Text);

                //零食价格
                Util.price.Clear();
                Util.price.Add("冰红茶", this.textBox16.Text);            //key为id,value为数量
                Util.price.Add("冰糖雪梨", this.textBox17.Text);
                Util.price.Add("红牛", this.textBox18.Text);
                Util.price.Add("可口可乐", this.textBox19.Text);
                Util.price.Add("脉动", this.textBox20.Text);
                Util.price.Add("苏打水", this.textBox21.Text);

                MessageBox.Show("设置成功");
                
            }
            catch (Exception ex)
            {
                List.list = reList;
                Util.price = rePrice;
                MessageBox.Show(ex.Message+"所有设置失败");
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            //返回
            DengLu dengLu = new DengLu();
            dengLu.Show();
            this.Hide();
        }
        //数量
        //冰红茶
        private void textBox9_TextChanged(object sender, EventArgs e)
        {

        }

        //冰糖雪梨
        private void textBox10_TextChanged(object sender, EventArgs e)
        {


        }

        //红牛
        private void textBox11_TextChanged(object sender, EventArgs e)
        {

        }

        //可口可乐
        private void textBox12_TextChanged(object sender, EventArgs e)
        {

        }

        //脉动
        private void textBox13_TextChanged(object sender, EventArgs e)
        {

        }

        //苏打水
        private void textBox14_TextChanged(object sender, EventArgs e)
        {

        }

        //价格

        //冰红茶
        private void textBox16_TextChanged(object sender, EventArgs e)
        {

        }

        //冰汤雪梨
        private void textBox17_TextChanged(object sender, EventArgs e)
        {

        }
        //红牛
        private void textBox18_TextChanged(object sender, EventArgs e)
        {

        }

        //可口可乐
        private void textBox19_TextChanged(object sender, EventArgs e)
        {

        }
        //脉动
        private void textBox20_TextChanged(object sender, EventArgs e)
        {

        }
        //苏打水
        private void textBox21_TextChanged(object sender, EventArgs e)
        {

        }

        
        //点击发送按钮事件的响应
        private void buttonfs_Click(object sender, EventArgs e)
        {

        }



        private void ManagerUtil2_Load(object sender, EventArgs e)
        {
            //数量显示设置
            this.textBox9.Text = (string)List.list["冰红茶"];
            this.textBox10.Text = (string)List.list["冰糖雪梨"];
            this.textBox11.Text = (string)List.list["红牛"];
            this.textBox12.Text = (string)List.list["可口可乐"];
            this.textBox13.Text = (string)List.list["脉动"];
            this.textBox14.Text = (string)List.list["苏打水"];

            //价格显示设置
            this.textBox16.Text = (string)Util.price["冰红茶"];
            this.textBox17.Text = (string)Util.price["冰糖雪梨"];
            this.textBox18.Text = (string)Util.price["红牛"];
            this.textBox19.Text = (string)Util.price["可口可乐"];
            this.textBox20.Text = (string)Util.price["脉动"];
            this.textBox21.Text = (string)Util.price["苏打水"];


        }

        private void textBox0_TextChanged(object sender, System.EventArgs e)
        {

        }

        private void textBoxfs_TextChanged(object sender, System.EventArgs e)
        {

        }

        private void textBox22_TextChanged(object sender, System.EventArgs e)
        {

        }

        
    }
}
