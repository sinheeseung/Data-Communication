import java.util.ArrayList;
   
public class ChatAppLayer implements BaseLayer {
	//send : dlg���� ���� data�� chatapplayer�� header�� �ٿ��� 
	//ethernet layer�� ��������
	//receive : ethernet layer���� ���� �����Ϳ��� ����� ����
	//dlg�� ����
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _CHAT_APP {
		//header�� ������ 2byte�� capp_type,capp_unused 4byte���
		byte[] capp_totlen;
		byte capp_type;
		byte capp_unused;
		byte[] capp_data;

		public _CHAT_APP() {
			this.capp_totlen = new byte[2];
			this.capp_type = 0x00;
			this.capp_unused = 0x00;
			this.capp_data = null;
		}
	}

	_CHAT_APP m_sHeader = new _CHAT_APP();

	public ChatAppLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 2; i++) {
			m_sHeader.capp_totlen[i] = (byte) 0x00;
			m_sHeader.capp_type = 0x00;
			m_sHeader.capp_unused = 0x00;
		}
		m_sHeader.capp_data = null;
	}

	public byte[] ObjToByte(_CHAT_APP Header, byte[] input, int length) {
		//chatapplayer���� send�� �� head�� ���̱� ���� ���
		byte[] buf = new byte[length + 4];
		
		buf[0] = Header.capp_totlen[0];
		buf[1] = Header.capp_totlen[1];
		buf[2] = Header.capp_type;
		buf[3] = Header.capp_unused;
		

		for (int i = 0; i < length; i++)
			buf[4 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) {
		//ObjToByte�� ���� underLayer�� ���� ���� ����� ���� �� ����
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length+4);

		return true;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		//header�� ���� �Ǻ��� ������ ���� simplest �������� �̹Ƿ� data�� 
		//header�� �и��� �� �и��� byte�迭�� return
		byte[] buf = new byte[4];
		byte[] input2 = new byte[input.length-4];
		
		for(int i=0;i<4;i++) {
			buf[i] = input[i];
		}
		for(int i=0;i<input.length-4;i++) {
			input2[i] = input[i+4];
		}
		return input2;

	}

	public synchronized boolean Receive(byte[] input) {
		byte[] data;

		data = RemoveCappHeader(input, input.length);
		this.GetUpperLayer(0).Receive(data);
		// Address Configuration
		return true;
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

}

