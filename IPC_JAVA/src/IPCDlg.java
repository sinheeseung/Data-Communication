package ipc;

import java.awt.Color;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class IPCDlg extends JFrame implements BaseLayer {
	/* Main(String[] args ){} 모든 레이어를 추가하고 연결하는
	   메소드를 불러 상하위 순서 지정
	   setAddressListener 버튼 클릭 시 행동을 지정해주는 메소드
	   Receive(byte[] input){} 올려보낸 input 을 수신측에서 잘
	   받았다고 표시해주는 함수*/
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;

	private static LayerManager m_LayerMgr = new LayerManager();
	private JTextField ChattingWrite;

	Container contentPane;

	JTextArea ChattingArea;
	JTextArea srcAddress;
	JTextArea dstAddress;

	JLabel lblsrc;
	JLabel lbldst;

	JButton Setting_Button;
	JButton Chat_send_Button;

	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;

	String Text;

	public static void main(String[] args) {
		m_LayerMgr.AddLayer(new SocketLayer("Socket"));
		m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
		m_LayerMgr.AddLayer(new IPCDlg("GUI"));
		//socket, chat, gui layer 추가
		m_LayerMgr.ConnectLayers(" Socket ( *Chat ( *GUI ) ) ");
		//상 하위 순서 지정
	}

	public IPCDlg(String pName) {
		pLayerName = pName;

		setTitle("IPC");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();// chatting write panel
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ChattingArea = new JTextArea();
		ChattingArea.setEditable(false);
		ChattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(ChattingArea);// chatting edit

		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingWrite = new JTextField();
		ChattingWrite.setBounds(2, 2, 250, 20);// 249
		chattingInputPanel.add(ChattingWrite);
		ChattingWrite.setColumns(10);// writing area

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(380, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(10, 96, 170, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);

		lblsrc = new JLabel("Source Address");
		lblsrc.setBounds(10, 75, 170, 20);
		settingPanel.add(lblsrc);

		srcAddress = new JTextArea();
		srcAddress.setBounds(2, 2, 170, 20);
		sourceAddressPanel.add(srcAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		lbldst = new JLabel("Destination Address");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstAddress = new JTextArea();
		dstAddress.setBounds(2, 2, 170, 20);
		destinationAddressPanel.add(dstAddress);// dst address

		Setting_Button = new JButton("Setting");// setting
		Setting_Button.setBounds(80, 270, 100, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting

		Chat_send_Button = new JButton("Send");
		Chat_send_Button.setBounds(270, 230, 80, 20);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);// chatting send button
		setVisible(true);

	}
	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == Setting_Button) {
				if(Setting_Button.getText() == "Reset") {
					//reset 버튼을 누른 경우
					srcAddress.setText("");
					dstAddress.setText("");
					//sender, receiver 주소 초기화
					dstAddress.setEnabled(true);
					srcAddress.setEnabled(true);
					//sender, receiver의 주소 변경가능
					Setting_Button.setText("Setting");
					//다시 주소 setting 해줘야하므로 reset버튼에서 setting버튼으로 변경
				}
				else {
					String Ssrc = srcAddress.getText();
					String Sdst = dstAddress.getText();
					int src = Integer.parseInt(Ssrc);
					int dst = Integer.parseInt(Sdst);
					//source, dst address 입력받아 정수형으로 저장
					((SocketLayer) m_LayerMgr.GetLayer("Socket")).setClientPort(dst);
					//DstAddress 의 text 를 SocketLayer Client port 에 저장
					((SocketLayer) m_LayerMgr.GetLayer("Socket")).setServerPort(src);
					//SrcAddress 의 text 를 SocketLayer Server port 에 저장
					((ChatAppLayer) m_LayerMgr.GetLayer("Chat")).SetEnetSrcAddress(src);
					//SrcAddress 의 text 를 ChatAppLayer header 에 저장					
					((ChatAppLayer) m_LayerMgr.GetLayer("Chat")).SetEnetDstAddress(dst);
					//DstAddress 의 text 를 ChatAppLayer header 에 저장				
					((SocketLayer) m_LayerMgr.GetLayer("Socket")).Receive();
					//SocketLayer 의 서버를 실행시킴 SocketLayer Thread 동작
					Setting_Button.setText("Reset");
					//sender와 receiver의 주소를 설정 후 setting 버튼에서 reset버튼으로 변경
					dstAddress.setEnabled(false);
					srcAddress.setEnabled(false);
					//주소 설정이 완료 되었으므로 reset버튼을 누르기 전까지 변경 불가  
				}
			}
			if(e.getSource() == Chat_send_Button) {
				// send버튼을 눌렀을 때 
				if(Setting_Button.getText() == "Reset") {
					//주소 설정이 완료 된 경우
					String writtenChat = ChattingWrite.getText();
					//sender가 보낼 message를 입력받음
					ChattingArea.append("[Send] : " + writtenChat + "\n");
					//sender의 chatting 화면에 [Send] + message를 출력함
					byte[] sendingChat = writtenChat.getBytes();
					((ChatAppLayer) m_LayerMgr.GetLayer("Chat")).Send(sendingChat, sendingChat.length);
					//ChatAppLayer에 Send() 호출해서 String 을 Byte 형식으로 변경해서 보낸다
				}
				else {
					//주소 값이 없는 경우
					ChattingArea.append("주소 설정 오류");
				}
			}
			 
		}
	}
	public boolean Receive(byte[] input) {			
		
		ChattingArea.append("[Rcvd] : "+ new String(input) + "\n");
		//receiver의 chatting 화면에 [Rcvd] + sender가 보낸 message 출력
		return true;
	}
	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}
	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);		
	}
	@Override
	public String GetLayerName() {		
		return pLayerName;
	}
	@Override
	public BaseLayer GetUnderLayer() {
		if (p_UnderLayer == null)
			
			return null;
		
		return p_UnderLayer;
	}
	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			
			return null;
		return p_aUpperLayer.get(nindex);
		
	}
	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}
}
