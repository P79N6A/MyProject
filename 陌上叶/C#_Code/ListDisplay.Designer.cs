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
    partial class ListDisplay
    {
        string price1=(string)Util.price["冰红茶"];
        string price2=(string)Util.price["冰糖雪梨"];
        string price3=(string)Util.price["红牛"];
        string price4=(string)Util.price["可口可乐"];
        string price5=(string)Util.price["脉动"];
        string price6=(string)Util.price["苏打水"];
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ListDisplay));
            this.button1 = new System.Windows.Forms.Button();
            this.button2 = new System.Windows.Forms.Button();
            this.button3 = new System.Windows.Forms.Button();
            this.button4 = new System.Windows.Forms.Button();
            this.button5 = new System.Windows.Forms.Button();
            this.button6 = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.dateTimePicker1 = new System.Windows.Forms.DateTimePicker();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.button8 = new System.Windows.Forms.Button();
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.菜单ToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.chineseToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.englishToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.buttonfs = new System.Windows.Forms.Button();
            this.comm = new System.IO.Ports.SerialPort(this.components);
            this.panel1 = new System.Windows.Forms.Panel();
            this.flush = new System.Windows.Forms.Label();
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.label7 = new System.Windows.Forms.Label();
            this.title = new System.Windows.Forms.LinkLabel();
            this.purchaseBtn = new System.Windows.Forms.Label();
            this.allRm = new System.Windows.Forms.Label();
            this.cartWaterBtn = new System.Windows.Forms.Label();
            this.cartDongBtn = new System.Windows.Forms.Label();
            this.cartLeBtn = new System.Windows.Forms.Label();
            this.cartNiuBtn = new System.Windows.Forms.Label();
            this.cartPearBtn = new System.Windows.Forms.Label();
            this.cartTeaBtn = new System.Windows.Forms.Label();
            this.cartWaterNo = new System.Windows.Forms.Label();
            this.cartDongNo = new System.Windows.Forms.Label();
            this.cartLeNo = new System.Windows.Forms.Label();
            this.cartNiuNo = new System.Windows.Forms.Label();
            this.cartPearNo = new System.Windows.Forms.Label();
            this.cartTeaNo = new System.Windows.Forms.Label();
            this.cartWater = new System.Windows.Forms.Label();
            this.cartDong = new System.Windows.Forms.Label();
            this.cartLe = new System.Windows.Forms.Label();
            this.cartNiu = new System.Windows.Forms.Label();
            this.cartPear = new System.Windows.Forms.Label();
            this.cartTea = new System.Windows.Forms.Label();
            this.Cart = new System.Windows.Forms.Label();
            this.textBox0 = new System.Windows.Forms.TextBox();
            this.weihu = new System.Windows.Forms.Label();
            this.weihuT = new System.Windows.Forms.TextBox();
            this.weihufs = new System.Windows.Forms.Label();
            this.weihujs = new System.Windows.Forms.TextBox();
            this.menuStrip1.SuspendLayout();
            this.panel1.SuspendLayout();
            this.SuspendLayout();
            // 
            // button1
            // 
            this.button1.BackColor = System.Drawing.Color.Transparent;
            this.button1.BackgroundImage = global::WindowsFormsApplication2.Properties.Resources.冰红茶3;
            resources.ApplyResources(this.button1, "button1");
            this.button1.Name = "button1";
            this.button1.UseVisualStyleBackColor = false;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // button2
            // 
            this.button2.BackColor = System.Drawing.Color.Transparent;
            this.button2.BackgroundImage = global::WindowsFormsApplication2.Properties.Resources.冰糖雪梨1;
            resources.ApplyResources(this.button2, "button2");
            this.button2.Name = "button2";
            this.button2.UseVisualStyleBackColor = false;
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // button3
            // 
            this.button3.BackColor = System.Drawing.Color.Transparent;
            this.button3.BackgroundImage = global::WindowsFormsApplication2.Properties.Resources.红牛;
            resources.ApplyResources(this.button3, "button3");
            this.button3.Name = "button3";
            this.button3.UseVisualStyleBackColor = false;
            this.button3.Click += new System.EventHandler(this.button3_Click);
            // 
            // button4
            // 
            this.button4.BackColor = System.Drawing.Color.Transparent;
            this.button4.BackgroundImage = global::WindowsFormsApplication2.Properties.Resources.可口可乐;
            resources.ApplyResources(this.button4, "button4");
            this.button4.Name = "button4";
            this.button4.UseVisualStyleBackColor = false;
            this.button4.Click += new System.EventHandler(this.button4_Click);
            // 
            // button5
            // 
            this.button5.BackColor = System.Drawing.Color.Transparent;
            this.button5.BackgroundImage = global::WindowsFormsApplication2.Properties.Resources.脉动1;
            resources.ApplyResources(this.button5, "button5");
            this.button5.Name = "button5";
            this.button5.UseVisualStyleBackColor = false;
            this.button5.Click += new System.EventHandler(this.button5_Click);
            // 
            // button6
            // 
            this.button6.BackColor = System.Drawing.Color.Transparent;
            this.button6.BackgroundImage = global::WindowsFormsApplication2.Properties.Resources.苏打水1;
            resources.ApplyResources(this.button6, "button6");
            this.button6.Name = "button6";
            this.button6.UseVisualStyleBackColor = false;
            this.button6.Click += new System.EventHandler(this.button6_Click);
            // 
            // label1
            // 
            resources.ApplyResources(this.label1, "label1");
            this.label1.BackColor = System.Drawing.Color.White;
            this.label1.ForeColor = System.Drawing.Color.Black;
            this.label1.Name = "label1";
            this.label1.Click += new System.EventHandler(this.label1_Click);
            // 
            // label2
            // 
            resources.ApplyResources(this.label2, "label2");
            this.label2.BackColor = System.Drawing.Color.White;
            this.label2.ForeColor = System.Drawing.Color.Black;
            this.label2.Name = "label2";
            this.label2.Click += new System.EventHandler(this.label2_Click);
            // 
            // label3
            // 
            resources.ApplyResources(this.label3, "label3");
            this.label3.BackColor = System.Drawing.Color.White;
            this.label3.ForeColor = System.Drawing.Color.Black;
            this.label3.Name = "label3";
            this.label3.Click += new System.EventHandler(this.label3_Click);
            // 
            // label4
            // 
            resources.ApplyResources(this.label4, "label4");
            this.label4.BackColor = System.Drawing.Color.White;
            this.label4.ForeColor = System.Drawing.Color.Black;
            this.label4.Name = "label4";
            this.label4.Click += new System.EventHandler(this.label4_Click);
            // 
            // label5
            // 
            resources.ApplyResources(this.label5, "label5");
            this.label5.BackColor = System.Drawing.Color.White;
            this.label5.ForeColor = System.Drawing.Color.Black;
            this.label5.Name = "label5";
            this.label5.Click += new System.EventHandler(this.label5_Click);
            // 
            // label6
            // 
            resources.ApplyResources(this.label6, "label6");
            this.label6.BackColor = System.Drawing.Color.White;
            this.label6.ForeColor = System.Drawing.Color.Black;
            this.label6.Name = "label6";
            this.label6.Click += new System.EventHandler(this.label6_Click);
            // 
            // dateTimePicker1
            // 
            resources.ApplyResources(this.dateTimePicker1, "dateTimePicker1");
            this.dateTimePicker1.CalendarMonthBackground = System.Drawing.Color.Transparent;
            this.dateTimePicker1.CalendarTitleBackColor = System.Drawing.Color.Transparent;
            this.dateTimePicker1.CalendarTitleForeColor = System.Drawing.Color.Transparent;
            this.dateTimePicker1.CalendarTrailingForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(192)))), ((int)(((byte)(192)))), ((int)(((byte)(255)))));
            this.dateTimePicker1.Name = "dateTimePicker1";
            this.dateTimePicker1.Value = new System.DateTime(2018, 5, 16, 16, 42, 19, 0);
            // 
            // button8
            // 
            this.button8.BackColor = System.Drawing.Color.Transparent;
            resources.ApplyResources(this.button8, "button8");
            this.button8.ForeColor = System.Drawing.Color.Black;
            this.button8.Name = "button8";
            this.button8.UseVisualStyleBackColor = false;
            this.button8.Click += new System.EventHandler(this.button8_Click);
            // 
            // menuStrip1
            // 
            this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.菜单ToolStripMenuItem});
            resources.ApplyResources(this.menuStrip1, "menuStrip1");
            this.menuStrip1.Name = "menuStrip1";
            // 
            // 菜单ToolStripMenuItem
            // 
            this.菜单ToolStripMenuItem.Checked = true;
            this.菜单ToolStripMenuItem.CheckState = System.Windows.Forms.CheckState.Checked;
            this.菜单ToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.chineseToolStripMenuItem,
            this.englishToolStripMenuItem});
            resources.ApplyResources(this.菜单ToolStripMenuItem, "菜单ToolStripMenuItem");
            this.菜单ToolStripMenuItem.Name = "菜单ToolStripMenuItem";
            // 
            // chineseToolStripMenuItem
            // 
            this.chineseToolStripMenuItem.Name = "chineseToolStripMenuItem";
            resources.ApplyResources(this.chineseToolStripMenuItem, "chineseToolStripMenuItem");
            this.chineseToolStripMenuItem.Click += new System.EventHandler(this.chineseToolStripMenuItem_Click);
            // 
            // englishToolStripMenuItem
            // 
            this.englishToolStripMenuItem.Name = "englishToolStripMenuItem";
            resources.ApplyResources(this.englishToolStripMenuItem, "englishToolStripMenuItem");
            this.englishToolStripMenuItem.Click += new System.EventHandler(this.englishToolStripMenuItem_Click);
            // 
            // buttonfs
            // 
            this.buttonfs.BackColor = System.Drawing.Color.Red;
            resources.ApplyResources(this.buttonfs, "buttonfs");
            this.buttonfs.ForeColor = System.Drawing.Color.Black;
            this.buttonfs.Name = "buttonfs";
            this.buttonfs.UseVisualStyleBackColor = false;
            this.buttonfs.Click += new System.EventHandler(this.buttonfs_Click);
            // 
            // panel1
            // 
            this.panel1.BackColor = System.Drawing.Color.RoyalBlue;
            this.panel1.Controls.Add(this.flush);
            this.panel1.Controls.Add(this.textBox1);
            this.panel1.Controls.Add(this.label7);
            this.panel1.Controls.Add(this.title);
            this.panel1.Controls.Add(this.purchaseBtn);
            this.panel1.Controls.Add(this.allRm);
            this.panel1.Controls.Add(this.cartWaterBtn);
            this.panel1.Controls.Add(this.cartDongBtn);
            this.panel1.Controls.Add(this.cartLeBtn);
            this.panel1.Controls.Add(this.cartNiuBtn);
            this.panel1.Controls.Add(this.cartPearBtn);
            this.panel1.Controls.Add(this.cartTeaBtn);
            this.panel1.Controls.Add(this.cartWaterNo);
            this.panel1.Controls.Add(this.cartDongNo);
            this.panel1.Controls.Add(this.cartLeNo);
            this.panel1.Controls.Add(this.cartNiuNo);
            this.panel1.Controls.Add(this.cartPearNo);
            this.panel1.Controls.Add(this.cartTeaNo);
            this.panel1.Controls.Add(this.cartWater);
            this.panel1.Controls.Add(this.cartDong);
            this.panel1.Controls.Add(this.cartLe);
            this.panel1.Controls.Add(this.cartNiu);
            this.panel1.Controls.Add(this.cartPear);
            this.panel1.Controls.Add(this.cartTea);
            this.panel1.Controls.Add(this.Cart);
            this.panel1.Controls.Add(this.buttonfs);
            this.panel1.ForeColor = System.Drawing.Color.Black;
            resources.ApplyResources(this.panel1, "panel1");
            this.panel1.Name = "panel1";
            this.panel1.Paint += new System.Windows.Forms.PaintEventHandler(this.panel1_Paint);
            // 
            // flush
            // 
            resources.ApplyResources(this.flush, "flush");
            this.flush.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.flush.ForeColor = System.Drawing.Color.Black;
            this.flush.Name = "flush";
            this.flush.Click += new System.EventHandler(this.flush_Click);
            // 
            // textBox1
            // 
            resources.ApplyResources(this.textBox1, "textBox1");
            this.textBox1.Name = "textBox1";
            // 
            // label7
            // 
            resources.ApplyResources(this.label7, "label7");
            this.label7.Name = "label7";
            // 
            // title
            // 
            resources.ApplyResources(this.title, "title");
            this.title.BackColor = System.Drawing.Color.Transparent;
            this.title.ForeColor = System.Drawing.Color.Black;
            this.title.LinkColor = System.Drawing.Color.Black;
            this.title.Name = "title";
            this.title.TabStop = true;
            // 
            // purchaseBtn
            // 
            resources.ApplyResources(this.purchaseBtn, "purchaseBtn");
            this.purchaseBtn.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.purchaseBtn.ForeColor = System.Drawing.Color.Black;
            this.purchaseBtn.Name = "purchaseBtn";
            this.purchaseBtn.Click += new System.EventHandler(this.purchaseBtn_Click);
            // 
            // allRm
            // 
            resources.ApplyResources(this.allRm, "allRm");
            this.allRm.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.allRm.ForeColor = System.Drawing.Color.Black;
            this.allRm.Name = "allRm";
            this.allRm.Click += new System.EventHandler(this.allRm_Click);
            // 
            // cartWaterBtn
            // 
            resources.ApplyResources(this.cartWaterBtn, "cartWaterBtn");
            this.cartWaterBtn.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.cartWaterBtn.ForeColor = System.Drawing.Color.Black;
            this.cartWaterBtn.Name = "cartWaterBtn";
            this.cartWaterBtn.Click += new System.EventHandler(this.cartWaterBtn_Click);
            // 
            // cartDongBtn
            // 
            resources.ApplyResources(this.cartDongBtn, "cartDongBtn");
            this.cartDongBtn.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.cartDongBtn.ForeColor = System.Drawing.Color.Black;
            this.cartDongBtn.Name = "cartDongBtn";
            this.cartDongBtn.Click += new System.EventHandler(this.cartDongBtn_Click);
            // 
            // cartLeBtn
            // 
            resources.ApplyResources(this.cartLeBtn, "cartLeBtn");
            this.cartLeBtn.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.cartLeBtn.ForeColor = System.Drawing.Color.Black;
            this.cartLeBtn.Name = "cartLeBtn";
            this.cartLeBtn.Click += new System.EventHandler(this.cartLeBtn_Click);
            // 
            // cartNiuBtn
            // 
            resources.ApplyResources(this.cartNiuBtn, "cartNiuBtn");
            this.cartNiuBtn.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.cartNiuBtn.ForeColor = System.Drawing.Color.Black;
            this.cartNiuBtn.Name = "cartNiuBtn";
            this.cartNiuBtn.Click += new System.EventHandler(this.cartNiuBtn_Click);
            // 
            // cartPearBtn
            // 
            resources.ApplyResources(this.cartPearBtn, "cartPearBtn");
            this.cartPearBtn.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.cartPearBtn.ForeColor = System.Drawing.Color.Black;
            this.cartPearBtn.Name = "cartPearBtn";
            this.cartPearBtn.Click += new System.EventHandler(this.cartPearBtn_Click);
            // 
            // cartTeaBtn
            // 
            resources.ApplyResources(this.cartTeaBtn, "cartTeaBtn");
            this.cartTeaBtn.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.cartTeaBtn.ForeColor = System.Drawing.Color.Black;
            this.cartTeaBtn.Name = "cartTeaBtn";
            this.cartTeaBtn.Click += new System.EventHandler(this.cartTeaBtn_Click);
            // 
            // cartWaterNo
            // 
            resources.ApplyResources(this.cartWaterNo, "cartWaterNo");
            this.cartWaterNo.Name = "cartWaterNo";
            // 
            // cartDongNo
            // 
            resources.ApplyResources(this.cartDongNo, "cartDongNo");
            this.cartDongNo.Name = "cartDongNo";
            // 
            // cartLeNo
            // 
            resources.ApplyResources(this.cartLeNo, "cartLeNo");
            this.cartLeNo.Name = "cartLeNo";
            // 
            // cartNiuNo
            // 
            resources.ApplyResources(this.cartNiuNo, "cartNiuNo");
            this.cartNiuNo.Name = "cartNiuNo";
            // 
            // cartPearNo
            // 
            resources.ApplyResources(this.cartPearNo, "cartPearNo");
            this.cartPearNo.Name = "cartPearNo";
            // 
            // cartTeaNo
            // 
            resources.ApplyResources(this.cartTeaNo, "cartTeaNo");
            this.cartTeaNo.Name = "cartTeaNo";
            // 
            // cartWater
            // 
            resources.ApplyResources(this.cartWater, "cartWater");
            this.cartWater.ForeColor = System.Drawing.Color.Black;
            this.cartWater.Name = "cartWater";
            // 
            // cartDong
            // 
            resources.ApplyResources(this.cartDong, "cartDong");
            this.cartDong.ForeColor = System.Drawing.Color.Black;
            this.cartDong.Name = "cartDong";
            // 
            // cartLe
            // 
            resources.ApplyResources(this.cartLe, "cartLe");
            this.cartLe.ForeColor = System.Drawing.Color.Black;
            this.cartLe.Name = "cartLe";
            // 
            // cartNiu
            // 
            resources.ApplyResources(this.cartNiu, "cartNiu");
            this.cartNiu.ForeColor = System.Drawing.Color.Black;
            this.cartNiu.Name = "cartNiu";
            // 
            // cartPear
            // 
            resources.ApplyResources(this.cartPear, "cartPear");
            this.cartPear.ForeColor = System.Drawing.Color.Black;
            this.cartPear.Name = "cartPear";
            // 
            // cartTea
            // 
            resources.ApplyResources(this.cartTea, "cartTea");
            this.cartTea.ForeColor = System.Drawing.Color.Black;
            this.cartTea.Name = "cartTea";
            // 
            // Cart
            // 
            resources.ApplyResources(this.Cart, "Cart");
            this.Cart.ForeColor = System.Drawing.Color.Black;
            this.Cart.Name = "Cart";
            // 
            // textBox0
            // 
            resources.ApplyResources(this.textBox0, "textBox0");
            this.textBox0.Name = "textBox0";
            this.textBox0.TextChanged += new System.EventHandler(this.textBox0_TextChanged);
            // 
            // weihu
            // 
            resources.ApplyResources(this.weihu, "weihu");
            this.weihu.ForeColor = System.Drawing.Color.Black;
            this.weihu.Name = "weihu";
            // 
            // weihuT
            // 
            resources.ApplyResources(this.weihuT, "weihuT");
            this.weihuT.Name = "weihuT";
            this.weihuT.TextChanged += new System.EventHandler(this.weihuT_TextChanged);
            // 
            // weihufs
            // 
            resources.ApplyResources(this.weihufs, "weihufs");
            this.weihufs.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.weihufs.Cursor = System.Windows.Forms.Cursors.Default;
            this.weihufs.ForeColor = System.Drawing.Color.Black;
            this.weihufs.Name = "weihufs";
            this.weihufs.Click += new System.EventHandler(this.weihufs_Click);
            // 
            // weihujs
            // 
            resources.ApplyResources(this.weihujs, "weihujs");
            this.weihujs.Name = "weihujs";
            this.weihujs.TextChanged += new System.EventHandler(this.weihujs_TextChanged);
            // 
            // ListDisplay
            // 
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.Color.White;
            this.Controls.Add(this.weihujs);
            this.Controls.Add(this.weihufs);
            this.Controls.Add(this.weihuT);
            this.Controls.Add(this.weihu);
            this.Controls.Add(this.panel1);
            this.Controls.Add(this.button8);
            this.Controls.Add(this.dateTimePicker1);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.button6);
            this.Controls.Add(this.button5);
            this.Controls.Add(this.button4);
            this.Controls.Add(this.button3);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.menuStrip1);
            this.Controls.Add(this.textBox0);
            this.Cursor = System.Windows.Forms.Cursors.Default;
            this.ForeColor = System.Drawing.Color.Transparent;
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedToolWindow;
            this.HelpButton = true;
            this.Name = "ListDisplay";
            this.Load += new System.EventHandler(this.Form2_Load);
            this.menuStrip1.ResumeLayout(false);
            this.menuStrip1.PerformLayout();
            this.panel1.ResumeLayout(false);
            this.panel1.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.Button button3;
        private System.Windows.Forms.Button button4;
        private System.Windows.Forms.Button button5;
        private System.Windows.Forms.Button button6;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.DateTimePicker dateTimePicker1;
        private System.Windows.Forms.Timer timer1;
        private System.Windows.Forms.Button button8;
        private MenuStrip menuStrip1;
        private ToolStripMenuItem 菜单ToolStripMenuItem;
        private ToolStripMenuItem chineseToolStripMenuItem;
        private ToolStripMenuItem englishToolStripMenuItem;

        //private System.Windows.Forms.Label labelck;
        //private System.Windows.Forms.RichTextBox richTextBoxjs;
        private System.Windows.Forms.Button buttonfs;
        private Panel panel1;
        private TextBox textBox0;
        private Label purchaseBtn;
        private Label allRm;
        private Label cartWaterBtn;
        private Label cartDongBtn;
        private Label cartLeBtn;
        private Label cartNiuBtn;
        private Label cartPearBtn;
        private Label cartTeaBtn;
        private Label cartWaterNo;
        private Label cartDongNo;
        private Label cartLeNo;
        private Label cartNiuNo;
        private Label cartPearNo;
        public  Label cartTeaNo;
        private Label cartWater;
        private Label cartDong;
        private Label cartLe;
        private Label cartNiu;
        private Label cartPear;
        private Label cartTea;
        private Label Cart;
        private LinkLabel title;
        private Label label7;
        private TextBox textBox1;
        private Label flush;
        private Label weihu;
        private TextBox weihuT;
        private Label weihufs;
        private TextBox weihujs;
        
    }
}