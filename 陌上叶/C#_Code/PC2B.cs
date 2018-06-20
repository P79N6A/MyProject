using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO.Ports;

namespace WindowsFormsApplication2
{

    
    public partial class PC2B : Form
    {
        SerialPort serialPort1 = new SerialPort();
        public PC2B()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            SendData(this.textBox1.Text);
            MessageBox.Show("发送数据成功");
        }


        private void SendData(string data)
        {
            byte[] senddata = new byte[1];
            //senddata[0] = Convert.ToByte(data,2);
            senddata = Encoding.Default.GetBytes(data);
            serialPort1.Write(senddata, 0, 1);
        }

        //打开端口
        private void button2_Click(object sender, EventArgs e)
        {
            SetComConfig();
            MessageBox.Show("打开端口成功");
        }

        //设置端口
        private void SetComConfig()
        {
            serialPort1.PortName = "COM4";          
            serialPort1.BaudRate = 115200;       
            serialPort1.Open();
        }

        //关闭端口
        private void button3_Click(object sender, EventArgs e)
        {
            byte[] data = Convert.FromBase64String(serialPort1.ReadLine());            //读取输入缓冲区

            textBox2.Text = Encoding.Unicode.GetString(data);                         //解码

            serialPort1.Close();
            MessageBox.Show("关闭端口成功");
        }
        private void button3_Click(object sender, System.IO.Ports.SerialDataReceivedEventArgs e)
        {
            ReceivedData();
        }


        private void ReceivedData()
        {
            byte[] sbufdata = new byte[1];
            serialPort1.Read(sbufdata, 0, 1);
            this.textBox2.Invoke((EventHandler)delegate
            {
                //receive.Text = Convert.ToString(sbufdata[0],2);
                this.textBox2.Text = Encoding.Default.GetString(sbufdata);
            });

        }
        private void PC2B_Load(object sender, EventArgs e)
        {

        }

        private void textBox1_TextChanged(object sender, EventArgs e)
        {

        }

        private void textBox2_TextChanged(object sender, EventArgs e)
        {

        }
    }
}
