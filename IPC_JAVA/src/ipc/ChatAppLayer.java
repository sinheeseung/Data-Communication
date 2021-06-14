package ipc;

import java.util.ArrayList;

public class ChatAppLayer implements BaseLayer {
	/*- _CAPP_HEADER() : src , dst ��Ʈ��ȣ�� �װ͵���
	 					 ������ Byte �迭�� ũ�⸦ ����
		Dialog ���� �Է��� String ���� Byte ȭ �� data �� ����� 
		�ٿ��ְ� ���Ϸ��̾�� ������ ������ �ʿ��� ResetHeader (),
		ObjToByte () �޼ҵ� ����*/
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _CAPP_HEADER {
		int capp_src;
		//src port��ȣ
		int capp_dst;
		//dst port��ȣ
		byte[] capp_totlen;
		public _CAPP_HEADER() {
			this.capp_src = 0x00000000;
			this.capp_dst = 0x00000000;
			this.capp_totlen = new byte[2];
		}
	}

	_CAPP_HEADER m_sHeader = new _CAPP_HEADER();

	public ChatAppLayer(String pName) {
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		//header�� �ʱ�ȭ
		for (int i = 0; i < 2; i++) {
			m_sHeader.capp_totlen[i] = (byte) 0x00;
		}
	}

	public byte[] ObjToByte(_CAPP_HEADER Header, byte[] input, int length) {
		//�Է¹��� ���� Byte������ ��ȯ�ؼ� ����
		byte[] buf = new byte[length + 10];
		byte[] srctemp = intToByte4(Header.capp_src);
		//header�� ����� src port��ȣ�� byte���·� ����
		byte[] dsttemp = intToByte4(Header.capp_dst);
		//header�� ����� dst port��ȣ�� byte���·� ����
		buf[0] = dsttemp[0];
		buf[1] = dsttemp[1];
		buf[2] = dsttemp[2];
		buf[3] = dsttemp[3];

		buf[4] = srctemp[0];
		buf[5] = srctemp[1];
		buf[6] = srctemp[2];
		buf[7] = srctemp[3];
		
		buf[8] = (byte) (length % 256);
		buf[9] = (byte) (length / 256);
		
		for (int i = 0; i < length; i++)
			//�Է¹��� ���� buf�� ����
			buf[10 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) {
		//Data�� ChatApp header�� �ٿ��� SocketLayer���� ��ü�� ����.
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 10);
		return true;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		//data���� ChatApp header�и�
		for (int i = 0; i < (input.length - 10); i++) {
			input[i] = input[i + 10];
		}
		return input;
	}

	public synchronized boolean Receive(byte[] input) {
		/*SocketLayer�κ��� ���� ������ �߿��� ChatApp header�� �и��� ��
		destination address�� �ڽ��� �´��� Ȯ�� �� Dialog�� ����*/
		byte[] data;
		byte[] temp_src = intToByte4(m_sHeader.capp_src);
		 //int���� value���� byte�� �ٲ㼭 temp�� ����
		for (int i = 0; i < 4; i++) {
			if (input[i] != temp_src[i]) {
				//�Էµ� ���� ���� ���
				System.out.println("no recieve");
				return false;
			}
		}
		data = RemoveCappHeader(input, input.length);
		this.GetUpperLayer(0).Receive(data);

		return true;
	}

	byte[] intToByte4(int value) {
		//int�� �Էµ� ���� byte���·� ����
		byte[] temp = new byte[4];
		temp[0] |= (byte) ((value & 0xFF000000) >> 24);
		//���� 2byte����
		temp[1] |= (byte) ((value & 0xFF0000) >> 16);
		//�߻��� 2byte����
		temp[2] |= (byte) ((value & 0xFF00) >> 8);
		//������ 2byte����
		temp[3] |= (byte) (value & 0xFF);
		//���� 2byte����

		return temp;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

	public void SetEnetSrcAddress(int srcAddress) {
		// TODO Auto-generated method stub
		m_sHeader.capp_src = srcAddress;
	}

	public void SetEnetDstAddress(int dstAddress) {
		// TODO Auto-generated method stub
		m_sHeader.capp_dst = dstAddress;
	}

}
