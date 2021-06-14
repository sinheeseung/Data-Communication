

import java.util.ArrayList;

public class ChatAppLayer implements BaseLayer {
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    _CHAT_APP m_sHeader;

    private byte[] fragBytes;
    private int fragCount = 0;
    private ArrayList<Boolean> ackChk = new ArrayList<Boolean>();

    private class _CHAT_APP {
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

    public ChatAppLayer(String pName) {
        // super(pName);
        // TODO Auto-generated constructor stub
        pLayerName = pName;
        ResetHeader();
        ackChk.add(true);
    }

    private void ResetHeader() {
        m_sHeader = new _CHAT_APP();
    }

    private byte[] objToByte(_CHAT_APP Header, byte[] input, int length) {
        byte[] buf = new byte[length + 4];

        buf[0] = Header.capp_totlen[0];
        buf[1] = Header.capp_totlen[1];
        buf[2] = Header.capp_type;
        buf[3] = Header.capp_unused;

        if (length >= 0) System.arraycopy(input, 0, buf, 4, length);

        return buf;
    }

    public byte[] RemoveCappHeader(byte[] input, int length) {
        byte[] cpyInput = new byte[length - 4];
        System.arraycopy(input, 4, cpyInput, 0, length - 4);
        input = cpyInput;
        return input;
    }

    private void waitACK() { 
    	//���� �ð��� ��ٸ��� ACK�� �������� Ȯ��
        while (ackChk.size() <= 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ackChk.remove(0);
    }
  /*�迭�� 10���� ũ�� fragSend�� �̿��� ����ȭ�� ���� �۽��ϰ�
   * ������ ��쿡 �������� ������ ���� ����� ���� �� ���� �������� 
   * �����͸� Send�Ѵ�*/
    private void fragSend(byte[] input, int length) {
        byte[] bytes = new byte[10];
        int i = 0;
        m_sHeader.capp_totlen = intToByte2(length);
        m_sHeader.capp_type = (byte) (0x01);
        //ù��° ���۽� type�� 0x01�� �Ͽ� 10����Ʈ�� ����
        System.arraycopy(input, 0, bytes, 0, 10);
        bytes = objToByte(m_sHeader, bytes, 10);
        this.GetUnderLayer().Send(bytes, bytes.length);

        int maxLen = length / 10;
        m_sHeader.capp_type = (byte) (0x02);
        m_sHeader.capp_totlen = intToByte2(10);
        //�߰� ����ȭ �����ʹ� 10Byte�� ����� �ٿ� ����(0x02)
        for(i = 1 ;i < maxLen; i++) {
        	waitACK();
        	if(i + 1 < maxLen && length % 10 == 0)
        		m_sHeader.capp_type = (byte) (0x02);
        	System.arraycopy(input, 10 * i, bytes, 0, 10);
        	bytes = objToByte(m_sHeader, bytes, 10);
        	this.GetUnderLayer().Send(bytes, bytes.length);
        }
        
        if (length % 10 != 0) {
        	waitACK();
            m_sHeader.capp_type = (byte) (0x03);
            m_sHeader.capp_totlen = intToByte2(length%10);
            bytes = new byte[length % 10];
            System.arraycopy(input, length - (length%10), bytes, 0, length % 10);
            bytes = objToByte(m_sHeader, bytes, bytes.length);
            this.GetUnderLayer().Send(bytes, bytes.length);
        }
    }
 
    public boolean Send(byte[] input, int length) {
        byte[] bytes;
        m_sHeader.capp_totlen = intToByte2(length);
        m_sHeader.capp_type = (byte) (0x00);
 
        waitACK();
        if(length > 10)
        	//���̰� 10���� ũ�� fragSend�� ���� ����ȭ
        	fragSend(input, length);
        else {
        	//����� �ٿ� ���� �������� ������ Send
        	bytes = objToByte(m_sHeader, input, input.length);
        	this.GetUnderLayer().Send(bytes, bytes.length);
        }
        return true;
    }
 
    public synchronized boolean Receive(byte[] input) {
        byte[] data, tempBytes;
        int tempType = 0;
        if (input == null) {
        	ackChk.add(true);
        	return true;
        } 
        tempType |= (byte) (input[2] & 0xFF);
        if(tempType == 0) {
        	//���۹��� data�� 10byte�̸��� ��� ����� ������ �� ���� �������� ����
            data = RemoveCappHeader(input, input.length);
            this.GetUpperLayer(0).Receive(data);
        }
        else{
        	if(tempType == 1) {
        		// ù ����ȭ�� data
        		int size = byte2ToInt(input[0], input[1]);
        		fragBytes = new byte[byte2ToInt(input[0], input[1])];
        		//���� data�� ũ�� �Ҵ�
        		fragCount = 1;
        		tempBytes = RemoveCappHeader (input, input.length);
        		//��� ���� 
        		System.arraycopy(tempBytes, 0, fragBytes, 0, 10);
        		//data ����
        	}else {
            	tempBytes = RemoveCappHeader(input, input.length);
            	//��� ����
            	System.arraycopy(tempBytes, 0, fragBytes, (fragCount++) * 10, byte2ToInt(input[0], input[1]));
            	//data ����
            	if(tempType == 3) {
            		//����ȭ �� data�߿��� ������ data�� ��� ���� �������� ����
            		this.GetUpperLayer(0).Receive(fragBytes);
            	}
            }
        }
        this.GetUnderLayer().Send(null, 0); // ack�۽�
        return true;
    }
    
    private byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[0] |= (byte) ((value & 0xFF00) >> 8);
        temp[1] |= (byte) (value & 0xFF);

        return temp;
    }

    private int byte2ToInt(byte value1, byte value2) {
        return (int)((value1 << 8) | (value2));
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
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }
}
