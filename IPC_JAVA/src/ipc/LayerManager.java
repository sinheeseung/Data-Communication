package ipc;


import java.util.ArrayList;
import java.util.StringTokenizer;

public class LayerManager {
    /* 어떤 계층적 구조 스택을 만들 수 있도록 프로토콜 관리를 담당하는 클래스
       IPC 과제를 위한 3 가지 Layer( IPCDlg , ChatAppLayer ,
	   SocketLayer 를 연결해주는 객체*/
	private class _NODE {
		private String token;
		private _NODE next;

		public _NODE(String input) {
			this.token = input;
			this.next = null;
		}
	}

	_NODE mp_sListHead;
	_NODE mp_sListTail;

	private int m_nTop;
	private int m_nLayerCount;

	private ArrayList<BaseLayer> mp_Stack = new ArrayList<BaseLayer>();
	private ArrayList<BaseLayer> mp_aLayers = new ArrayList<BaseLayer>();

	public LayerManager() {
		m_nLayerCount = 0;
		mp_sListHead = null;
		mp_sListTail = null;
		m_nTop = -1;
	}

	public void AddLayer(BaseLayer pLayer) {
		// mpalayer에 baselayer를 추가한다.
		mp_aLayers.add(m_nLayerCount++, pLayer);
	}

	public BaseLayer GetLayer(int nindex) {
		// mpalayer에서 값을 가져온다.
		return mp_aLayers.get(nindex);
	}

	public BaseLayer GetLayer(String pName) {
		// mpalayer에서 pName인 레이어를 찾아내서 반환한다.
		for (int i = 0; i < m_nLayerCount; i++) {
			if (pName.compareTo(mp_aLayers.get(i).GetLayerName()) == 0)
				return mp_aLayers.get(i);
		}
		return null;
	}

	public void ConnectLayers(String pcList) {
		MakeList(pcList);
		LinkLayer(mp_sListHead);
		// mp_slisthead를 linklayer에 추가한다.
	}

	private void MakeList(String pcList) {
		// pcList를 " "단위로 끊은 뒤 노드로 만들어 연결
		StringTokenizer tokens = new StringTokenizer(pcList, " ");
		for (; tokens.hasMoreElements();) {
			// 더이상 원소가 없을때 까지 반복
			_NODE pNode = AllocNode(tokens.nextToken());
			// 새로운 노드를 할당한다.
			AddNode(pNode);

		}
	}

	private _NODE AllocNode(String pcName) {
		//새로운 NODE를 할당해준다
		_NODE node = new _NODE(pcName);
		return node;
	}

	private void AddNode(_NODE pNode) {
		if (mp_sListHead == null) {
			// 첫 node인 경우 헤드이면서 꼬리가 된다.
			mp_sListHead = mp_sListTail = pNode;
		} else {
			mp_sListTail.next = pNode;
			mp_sListTail = pNode;
			// 현재 꼬리의 다음 노드로 입력된 노드 설정 후 꼬리를 바꿔줌
		}
	}

	private void Push(BaseLayer pLayer) {
		mp_Stack.add(++m_nTop, pLayer);
	}

	private BaseLayer Pop() {
		BaseLayer pLayer = mp_Stack.get(m_nTop);
		mp_Stack.remove(m_nTop);
		m_nTop--;

		return pLayer;
	}

	private BaseLayer Top() {
		return mp_Stack.get(m_nTop);
	}

	private void LinkLayer(_NODE pNode) {
		//프로토콜간 상/하위 순서 지정
		BaseLayer pLayer = null;

		while (pNode != null) {
			if (pLayer == null)
				pLayer = GetLayer(pNode.token);
			else {
				if (pNode.token.equals("("))
					Push(pLayer);
				else if (pNode.token.equals(")"))
					Pop();
				else {
					char cMode = pNode.token.charAt(0);
					String pcName = pNode.token.substring(1, pNode.token.length());

					pLayer = GetLayer(pcName);

					switch (cMode) { // 상/하위 프로토콜간 데이터 전송 방향
					case '*':
						Top().SetUpperUnderLayer(pLayer);
						break;
					case '+':
						Top().SetUpperLayer(pLayer);
						break;
					case '-':
						Top().SetUnderLayer(pLayer);
						break;
					}
				}
			}
			pNode = pNode.next;
		}
	}

}
