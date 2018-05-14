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
    public partial class MainDoor : Form
    {

        private string money;
        public MainDoor()
        {
            InitializeComponent();
        }

        //进入零食列表
        private void button1_Click(object sender, EventArgs e)
        {
            ListDisplay list = new ListDisplay();
            list.Show();
            this.Hide();

            /*
            if (this.money== null)
            {
                MessageBox.Show("您还没有输入金额，请输入消费金额");
            }
            else
            {
                UserUtil.money = Convert.ToInt16(this.money);
                //开始购买
                ListDisplay list = new ListDisplay();
                list.Show();
                this.Hide();
                
            }
             * */
        }


        //进入管理员界面
        private void button3_Click(object sender, EventArgs e)
        {
            //进入管理员界面
            //this.textBox1.Clear();
            DengLu dengLu = new DengLu();
            dengLu.Show();
            this.Hide();
        }

        /*
        private void textBox1_TextChanged(object sender, EventArgs e)
        {
            
            try
            {
                if (Convert.ToInt16(this.textBox1.Text) <= 0)
                {
                    MessageBox.Show("输入金额不正确，请重新输入");
                    this.textBox1.Clear();
                }
                else
                {
                    this.money = this.textBox1.Text;
                } 
            }
            catch (Exception ex)
            {
                MessageBox.Show("输入金额不正确，请重新输入");
                this.textBox1.Clear();
            }
             
        }
         */

        private void MainDoor_Load(object sender, EventArgs e)
        {

        }
    }
}
