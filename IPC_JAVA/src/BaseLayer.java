package ipc;

import java.util.ArrayList;

interface BaseLayer {
	/* 각 Layer 의 기본적인 틀을 잡아주고
       상 하위 계층 사이에 통신을 가능하게 해주는 계층적 구조를
	   구현한 인터페이스
	   BaseLayer 를 상속받는 모든 클래스에서는 실제적으로
       Send() 와 Receive() 함수를 자신의 클래스에 맞게 정의 후 호출*/
	public final int m_nUpperLayerCount = 0;
	public final String m_pLayerName = null;
	public final BaseLayer mp_UnderLayer = null;
	public final ArrayList<BaseLayer> mp_aUpperLayer = new ArrayList<BaseLayer>();
	public String GetLayerName();
	public BaseLayer GetUnderLayer();
	public BaseLayer GetUpperLayer(int nindex);
	public void SetUnderLayer(BaseLayer pUnderLayer);
	public void SetUpperLayer(BaseLayer pUpperLayer);
	public default void SetUnderUpperLayer(BaseLayer pUULayer) {
	}
	public void SetUpperUnderLayer(BaseLayer pUULayer);
	public default boolean Send(byte[] input, int length) {
		return false;
	}
	public default boolean Send(String filename) {
		return false;
	}
	public default boolean Receive(byte[] input) {
		return false;
	}
	public default boolean Receive() {
		return false;
	}
}


