package ipc;
//�����̶� ��Ʈ��ũ ȯ�濡 ���� �� �� �ֵ��� ������� �����

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
//�� Ŭ������ IP ���� �ּ� (IP �ּ� + ��Ʈ ��ȣ)�� ������ 

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SocketLayer implements BaseLayer {
	/*
	 * setClientPort(int dstAddress) SetServerPort(int dstAddress)�� ���� ��Ʈ�� �����ϴ� �޼ҵ�
	 * send �Լ��� ���� Ŭ���̷�Ʈ �ʱ�ȭ �� input data�� ���濡�� ������ ���� ���μ��������� receive()�Լ��� ���� ����
	 * �������� �÷����� �غ� ��
	 */

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public int dst_port;
	public int src_port;

	public SocketLayer(String pName) {
		// SocketLayer�� �̸��� ����
		// super(pName);
		pLayerName = pName;

	}

	public void setClientPort(int dstAddress) {
		// ClientPort�� dstAddress�� setting
		this.dst_port = dstAddress;
	}

	public void setServerPort(int srcAddress) {
		// ServerPort�� srcAddress�� setting
		this.src_port = srcAddress;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// baseLayer�� pUnderLayer�� UnderLayer setting
		if (pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// baseLayer�� pUpperLayer�� UpperLayer setting
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);

	}

	@Override
	public String GetLayerName() {
		// pLayerName�� return(getter)
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// UnderLayer�� return(getter)
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		//// UpperLayer�� return(getter)
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			// ã������ nindex�� �߸��Ȱ�� , nUpperLayerCount������ nindex�� �� ū ���, nUpperLayerCount����
			// �߸��Ȱ��
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}

	public boolean Send(byte[] input, int length) {
		// Client�κ��� ���޵� �޽����� ���� �� �����͸� ChatAppLayer�� ����
		try (Socket client = new Socket()) {

			InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", dst_port);
			// IP �ּҿ� ��Ʈ ��ȣ�� ���� ���� ����
			client.connect(ipep);
			// client ������ ipep�� ����
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
		// ChatAppLayer�� ���� ���� �����͸� Server�� ����
		while (true)
			try (ServerSocket server = new ServerSocket()) {
				InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", server_port);
				// port�� ���� ������ ������ �Ѵ�
				server.bind(ipep);
				// ���� ���� ���ε�
				// �ü���� Ư�� ��Ʈ ��ȣ�� ���������� ����ϵ��� ����� ���� ���ϰ� ��Ʈ��ȣ�� �����ؾ��Ѵ�
				// bind�Լ��� ���� ���Ͽ� ��Ʈ��ȣ�� �ο��Ѵ�.

				System.out.println("Initialize complete");
				Socket client = server.accept();
				// client�� server�� ������ �޾Ƶ��δ�.
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
