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
    public partial class ListDisplay : Form
    {
        public ListDisplay()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {

            purchase purchase=new purchase("冰红茶");
            purchase.Show();

            
        }

        private void button2_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("冰糖雪梨");
            purchase.Show();

            
        }

        private void button3_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("红牛");
            purchase.Show();

            
            
        }

       
        private void button4_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("可口可乐");
            purchase.Show();

            
        }

        private void button5_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("脉动");
            purchase.Show();

            
        }
        

        private void button6_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("苏打水");
            purchase.Show();

            
        }

        private void button7_Click(object sender, EventArgs e)
        {
            //退出登录，回到主页面
            MainDoor mainDoor=new MainDoor();
            mainDoor.Show();
            this.Hide();
        }
       

        private void Form2_Load(object sender, EventArgs e)
        {

        }

        private void label1_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("冰红茶");
            purchase.Show();
        }

        private void label5_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("可口可乐");
            purchase.Show();
        }

        private void label4_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("苏打水");
            purchase.Show();
        }

        private void label6_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("脉动");
            purchase.Show();
        }

        private void label3_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("红牛");
            purchase.Show();
        }

        private void label2_Click(object sender, EventArgs e)
        {
            purchase purchase = new purchase("冰糖雪梨");
            purchase.Show();
        }
    }
}
