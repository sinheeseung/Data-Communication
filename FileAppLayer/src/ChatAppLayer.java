
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
    private void waitACK() { 
    	//일정 시간씩 기다리며 ACK가 들어오는지 확인
        while (ackChk.size() <= 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ackChk.remove(0);
    }
    public byte[] RemoveCappHeader(byte[] input, int length) {
        byte[] cpyInput = new byte[length - 4];
        System.arraycopy(input, 4, cpyInput, 0, length - 4);
        input = cpyInput;
        return input;
    }

    //test
  /*배열이 1456보다 크면 fragSend를 이용해 단편화를 시켜 송신하고
   * 나머지 경우에 데이터의 정보를 담은 헤더를 붙인 후 하위 계층으로 
   * 데이터를 Send한다*/
    private void fragSend(byte[] input, int length) {
        byte[] bytes = new byte[1456];
        int i = 0;
        m_sHeader.capp_totlen = intToByte2(length);
        m_sHeader.capp_type = (byte) (0x01);
        //첫번째 전송시 type를 0x01로 하여 1456바이트씩 전송
        System.arraycopy(input, 0, bytes, 0, 1456);
        bytes = objToByte(m_sHeader, bytes, 1456);
        this.GetUnderLayer().Send(bytes, bytes.length);

        int maxLen = length / 1456;
        m_sHeader.capp_type = (byte) (0x02);
        m_sHeader.capp_totlen = intToByte2(1456);
        //중간 단편화 데이터는 1456Byte씩 헤더를 붙여 전송(0x02)
        for(i = 1 ;i < maxLen; i++) {
        	waitACK();
        	if(i + 1 < maxLen && length % 1456 == 0)
        		m_sHeader.capp_type = (byte) (0x02);
        	System.arraycopy(input, 1456 * i, bytes, 0,1456);
        	bytes = objToByte(m_sHeader, bytes, 1456);
        	this.GetUnderLayer().Send(bytes, bytes.length);
        }
        
        if (length % 1456 != 0) {
        	waitACK();
            m_sHeader.capp_type = (byte) (0x03);
            m_sHeader.capp_totlen = intToByte2(length%1456);
            bytes = new byte[length % 1456];
            System.arraycopy(input, length - (length%1456), bytes, 0, length % 1456);
            bytes = objToByte(m_sHeader, bytes, bytes.length);
            this.GetUnderLayer().Send(bytes, bytes.length);
        }
    }
 
    public boolean Send(byte[] input, int length) {
        byte[] bytes;
        m_sHeader.capp_totlen = intToByte2(length);
        m_sHeader.capp_type = (byte) (0x00);
        System.out.println("sendlength : " + length);
    	waitACK();
        if(length > 1456)
        	//길이가 1456보다 크면 fragSend를 통해 단편화
        	fragSend(input, length);
        else {
        	//헤더를 붙여 하위 계층으로 데이터 Send
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
        	//전송받은 data가 1456byte미만인 경우 헤더만 제거한 후 상위 계층으로 전송
            data = RemoveCappHeader(input, input.length);
            this.GetUpperLayer(0).Receive(data);
        }
        else{
        	if(tempType == 1) {
        		// 첫 단편화된 data
        		fragBytes = new byte[byte2ToInt(input[0], input[1])];
        		//받은 data의 크기 할당
        		fragCount = 1;
        		tempBytes = RemoveCappHeader (input, input.length);
        		//헤더 제거 
        		System.arraycopy(tempBytes, 0, fragBytes, 0, 1456);
        		//data 병합
        	}else {
            	tempBytes = RemoveCappHeader(input, input.length);
            	//헤더 제거
            	System.arraycopy(tempBytes, 0, fragBytes, (fragCount++) * 1456, byte2ToInt(input[0], input[1]));
            	//data 병합
            	if(tempType == 3) {
            		//단편화 된 data중에서 마지막 data인 경우 상위 계층으로 전송
            		this.GetUpperLayer(0).Receive(fragBytes);
            	}
            }
        }
        this.GetUnderLayer().Send(null, 0); // ack송신
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