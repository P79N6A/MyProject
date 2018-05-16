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
    public partial class DengLu : Form
    {
        private string userName;
        private string password;

        public DengLu()
        {
            InitializeComponent();
        }

        private void pictureBox1_Click(object sender, EventArgs e)
        {

        }

        private void textBox2_TextChanged(object sender, EventArgs e)
        {
            this.userName = this.textBox2.Text;
        }

        private void textBox4_TextChanged(object sender, EventArgs e)
        {
            this.password = this.textBox4.Text;
        }

        private void textBox1_TextChanged(object sender, EventArgs e)
        {

        }

        //登录
        private void button1_Click(object sender, EventArgs e)
        {
            //MessageBox.Show("用户名:"+this.userName);
            if(Util.userName.Equals(this.userName))
            {
                if (Util.password.Equals(this.password))
                {
                    ManagerUtil managerUtil = new ManagerUtil();
                    managerUtil.Show();
                    this.Hide();
                }
                else
                {
                    MessageBox.Show("登录失败，密码错误。。");
                }
                   
            }
            else
            {
                MessageBox.Show("登录失败，该用户不存在。。");
            }
        }

        //退出
        private void button2_Click(object sender, EventArgs e)
        {
            ListDisplay list=new ListDisplay();
            list.Show();
            this.Hide();
        }
    }
}
