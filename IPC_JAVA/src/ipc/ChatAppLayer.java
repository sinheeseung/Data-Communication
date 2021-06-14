package ipc;

import java.util.ArrayList;

public class ChatAppLayer implements BaseLayer {
	/*- _CAPP_HEADER() : src , dst 포트번호와 그것들을
	 					 보관할 Byte 배열의 크기를 저장
		Dialog 에서 입력한 String 값의 Byte 화 된 data 에 헤더를 
		붙여주고 소켓레이어로 보내는 과정에 필요한 ResetHeader (),
		ObjToByte () 메소드 구현*/
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _CAPP_HEADER {
		int capp_src;
		//src port번호
		int capp_dst;
		//dst port번호
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
		//header값 초기화
		for (int i = 0; i < 2; i++) {
			m_sHeader.capp_totlen[i] = (byte) 0x00;
		}
	}

	public byte[] ObjToByte(_CAPP_HEADER Header, byte[] input, int length) {
		//입력받은 값을 Byte단위로 변환해서 저장
		byte[] buf = new byte[length + 10];
		byte[] srctemp = intToByte4(Header.capp_src);
		//header에 저장된 src port번호를 byte형태로 저장
		byte[] dsttemp = intToByte4(Header.capp_dst);
		//header에 저장된 dst port번호를 byte형태로 저장
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
			//입력받은 값을 buf에 저장
			buf[10 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) {
		//Data에 ChatApp header를 붙여서 SocketLayer에게 전체를 전달.
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 10);
		return true;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		//data에서 ChatApp header분리
		for (int i = 0; i < (input.length - 10); i++) {
			input[i] = input[i + 10];
		}
		return input;
	}

	public synchronized boolean Receive(byte[] input) {
		/*SocketLayer로부터 받은 데이터 중에서 ChatApp header를 분리한 후
		destination address가 자신이 맞는지 확인 후 Dialog로 전달*/
		byte[] data;
		byte[] temp_src = intToByte4(m_sHeader.capp_src);
		 //int형의 value값을 byte로 바꿔서 temp에 저장
		for (int i = 0; i < 4; i++) {
			if (input[i] != temp_src[i]) {
				//입력된 값이 없는 경우
				System.out.println("no recieve");
				return false;
			}
		}
		data = RemoveCappHeader(input, input.length);
		this.GetUpperLayer(0).Receive(data);

		return true;
	}

	byte[] intToByte4(int value) {
		//int로 입력된 값을 byte형태로 저장
		byte[] temp = new byte[4];
		temp[0] |= (byte) ((value & 0xFF000000) >> 24);
		//상위 2byte저장
		temp[1] |= (byte) ((value & 0xFF0000) >> 16);
		//중상위 2byte저장
		temp[2] |= (byte) ((value & 0xFF00) >> 8);
		//중하위 2byte저장
		temp[3] |= (byte) (value & 0xFF);
		//하위 2byte저장

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
