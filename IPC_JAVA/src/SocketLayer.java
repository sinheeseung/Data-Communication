package ipc;
//소켓이란 네트워크 환경에 연결 할 수 있도록 만들어진 연결부

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
//이 클래스는 IP 소켓 주소 (IP 주소 + 포트 번호)를 구현함 

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SocketLayer implements BaseLayer {
	/*
	 * setClientPort(int dstAddress) SetServerPort(int dstAddress)는 받은 포트를 저장하는 메소드
	 * send 함수를 통해 클라이런트 초기화 및 input data를 상대방에게 보내고 상대방 프로세스에서는 receive()함수를 통해 상위
	 * 계층으로 올려보낼 준비를 함
	 */

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public int dst_port;
	public int src_port;

	public SocketLayer(String pName) {
		// SocketLayer의 이름을 설정
		// super(pName);
		pLayerName = pName;

	}

	public void setClientPort(int dstAddress) {
		// ClientPort를 dstAddress로 setting
		this.dst_port = dstAddress;
	}

	public void setServerPort(int srcAddress) {
		// ServerPort를 srcAddress로 setting
		this.src_port = srcAddress;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// baseLayer의 pUnderLayer로 UnderLayer setting
		if (pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// baseLayer의 pUpperLayer로 UpperLayer setting
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);

	}

	@Override
	public String GetLayerName() {
		// pLayerName을 return(getter)
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// UnderLayer를 return(getter)
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		//// UpperLayer를 return(getter)
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			// 찾으려는 nindex가 잘못된경우 , nUpperLayerCount값보다 nindex가 더 큰 경우, nUpperLayerCount값이
			// 잘못된경우
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}

	public boolean Send(byte[] input, int length) {
		// Client로부터 전달된 메시지를 받은 후 데이터를 ChatAppLayer로 전달
		try (Socket client = new Socket()) {

			InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", dst_port);
			// IP 주소와 포트 번호를 통해 소켓 설정
			client.connect(ipep);
			// client 소켓을 ipep로 연결
			try (OutputStream sender = client.getOutputStream();) {
				sender.write(input, 0, length);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean Receive() {
		Receive_Thread thread = new Receive_Thread(this.GetUpperLayer(0), src_port);
		Thread obj = new Thread(thread);
		obj.start();

		return false;

	}
}

class Receive_Thread implements Runnable {
	byte[] data;
	BaseLayer UpperLayer;
	int server_port;
	public Receive_Thread(BaseLayer m_UpperLayer, int src_port) {
		UpperLayer = m_UpperLayer;
		server_port = src_port;
	}
	@Override
	public void run() {
		// ChatAppLayer로 전송 받은 데이터를 Server로 전송
		while (true)
			try (ServerSocket server = new ServerSocket()) {
				InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", server_port);
				// port를 통해 서버와 연결을 한다
				server.bind(ipep);
				// 서버 소켓 바인딩
				// 운영체제가 특정 포트 번호를 서버소켓이 사용하도록 만들기 위해 소켓과 포트번호를 결합해야한다
				// bind함수를 통해 소켓에 포트번호를 부여한다.

				System.out.println("Initialize complete");
				Socket client = server.accept();
				// client와 server의 연결을 받아들인다.
				System.out.println("Connection");
				try (InputStream reciever = client.getInputStream();) {
					data = new byte[1528]; // Ethernet Maxsize + Ethernet Headersize;
					reciever.read(data, 0, data.length);
					System.out.println(" * " + data);
					UpperLayer.Receive(data);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
	}
}
