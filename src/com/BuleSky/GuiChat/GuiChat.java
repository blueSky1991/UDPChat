package com.BuleSky.GuiChat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GuiChat  extends JFrame {
	private static final int DEFFAULL_PORT = 8899;// 默认的端口号
	// 把主体的窗口分为 NORTH  CENTER SOURTH 三个部分
	private JLabel stateLB;    // 显示监听的状态
	private JTextArea centerTextArea;  // 显示聊天记录
	private JPanel   southPanel;        // 显示最下面的面板
	private JTextArea inputTextArea;  // 显示输入框
	private JPanel bottomPanel;  //放置ip输入框 按钮等
	private JTextField ipTextField;  // IP输入框
	private JTextField remotePortField;  //端口号输入框
	private JButton sendBtn;  // 发送按钮
	private JButton clearBtn; // 清除聊天记录按钮
	private DatagramSocket datagramSocket;// 用于后面功能的实现

	// 设置布局
	private void setUpUI() {
		// 初始化窗口
		setTitle("GUI聊天");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 400);
		setResizable(false);//设置窗口的大小不可调整
		setLocationRelativeTo(null); //窗口居中
		
		
		//  窗口的NORRH部分
		stateLB = new JLabel("当前未启动监听");
		stateLB.setHorizontalAlignment(JLabel.RIGHT);
		
		
		// 窗口的CENTER 部分
		centerTextArea = new JTextArea();
		centerTextArea.setEditable(false);// 不可编辑
        centerTextArea.setBackground(new Color(211, 211, 211));
        
        //窗口的SOUTH部分
        southPanel = new JPanel(new BorderLayout());
        inputTextArea = new JTextArea(5, 20);//内容输入区域
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,5));
        ipTextField = new JTextField("127.0.0.1", 8);
        remotePortField = new JTextField(String.valueOf(DEFFAULL_PORT),3);
        sendBtn = new JButton("发送");
        clearBtn = new JButton("清屏");
        bottomPanel.add(ipTextField);
        bottomPanel.add(remotePortField);
        bottomPanel.add(sendBtn);
        bottomPanel.add(clearBtn);
        southPanel.add(new JScrollPane(inputTextArea),BorderLayout.CENTER);
        southPanel.add(bottomPanel,BorderLayout.SOUTH);
        
        //  添加窗口 NORTH CENTER SOUTH  部分的组件
        add(stateLB,BorderLayout.NORTH);
        add(new JScrollPane(centerTextArea),BorderLayout.CENTER);
        add(southPanel,BorderLayout.SOUTH);
        setVisible(true);
       	
	}
	// 事件的处理信息(发送)
	private void setListener() {
		sendBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			    //获取发送目标的IP地址和端口号
				final String ipAdress  = ipTextField.getText();
				final String remotePort = remotePortField.getText();
				
				//判断IP地址和端口号是否为空
				if (ipAdress == null || ipAdress.trim().equals("") || remotePort == null ||remotePort.trim().equals("")){
				JOptionPane.showMessageDialog(GuiChat.this, "请输入ip地址和端口号");
				return;	
				} 
				
				// 判断程序是否在监听
				if (datagramSocket == null || datagramSocket.isClosed()) {
					JOptionPane.showMessageDialog(GuiChat.this, "监听不成功");
					return;
					
				}
				
				// 获取需要发送的内容
				
				String sendContent = inputTextArea.getText();
				byte [] buf = sendContent.getBytes();
				
				try {
					//将发送的内容显示到自己的聊天记录中
					centerTextArea.append("我对"+ipAdress+":"+remotePort+"说:\n"+inputTextArea.getText()+"\n\n");
					//添加聊天内容后 使滚动条自动滚动到最低端
					centerTextArea.setCaretPosition(centerTextArea.getText().length());
					
					//发送数据
					datagramSocket.send(new DatagramPacket(buf, buf.length,InetAddress.getByName(ipAdress),Integer.parseInt(remotePort)));
					
					inputTextArea.setText("");
					
				} catch (Exception e2) {
					// TODO: handle exception
					JOptionPane.showMessageDialog(GuiChat.this, "发送不成功");
					e2.printStackTrace();
						
				}	
			}
		});
		
		
		clearBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				centerTextArea.setText("");//清空聊天记录的内容
			}
		});
		
		
	}
	
	// 启动监听和接收信息
	private void initSocket() {
		int port =  DEFFAULL_PORT;
		while (true) {
			try {
				if (datagramSocket != null && !datagramSocket.isClosed()) {
					datagramSocket.isClosed();
				}
				
				try {
					port = Integer.parseInt(JOptionPane.showInputDialog(this,"请输入端口号","端口号",JOptionPane.QUESTION_MESSAGE));
					if (port <1 || port > 65535) {
						throw new RuntimeException("端口号超出范围");
					}
					
				} catch (Exception e) {
					// TODO: handle exception
					JOptionPane.showMessageDialog(null, "端口号不正确,请输入1-65535之间的数");
					continue;
				}
				
				datagramSocket = new DatagramSocket(port);
				//开启监听
			     startListen();
			     stateLB.setText("已在"+port+"端口监听");
				break;
				
			} catch (Exception e) {
				// TODO: handle exception
				JOptionPane.showMessageDialog(this, "端口已被占用,请重新设置");
				stateLB.setText("当前还未启动监听");
			}
		}
	
	}
	
	// 开启监听
	private void startListen() {
		
		new Thread(){
			private DatagramPacket packet;
			public void run(){
				
				byte [] buf = new byte[1024];
				packet = new DatagramPacket(buf, buf.length);
				while (!datagramSocket.isClosed()) {
					try {
						//接收聊天信息
						datagramSocket.receive(packet);
						
						//添加到聊天记录上
						centerTextArea.append(packet.getAddress().getHostAddress()+":"+ ((InetSocketAddress)packet.getSocketAddress()).getPort()+"对我说:\n"+new String(packet.getData(),0,packet.getLength())+"\n\n");
						//     使滚动条自动滚到最底部
						 centerTextArea.setCaretPosition(centerTextArea.getText().length());
						 
						
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
				
				
			}
			
			
		}.start();
		
	}
	
	
	public GuiChat(){
		setUpUI();
		initSocket();
		setListener();
	}	
	
}
