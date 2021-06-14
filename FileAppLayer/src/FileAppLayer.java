
import java.io.*;
import java.util.ArrayList;

public class FileAppLayer implements BaseLayer {
    private int count = 0;
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    private String fileName; // ���� �̸�
    private int receivedLength = 0; // ������ �������� ũ��
    private int targetLength = 0; // �����ؾ��ϴ� ������ �� ũ��

    private File file; // ������ ����
    private ArrayList<byte[]> fileByteList; // ������ ���� ������(���� ��)
    private ArrayList<byte[]> fileSortList; // ������ ������ ���� �ϴµ� ����ϴ� ����Ʈ

    public FileAppLayer(String pName) {
        // TODO Auto-generated constructor stub
        pLayerName = pName;
        fileByteList = new ArrayList();
    }

    public class _FAPP_HEADER {
        byte[] fapp_totlen;
        byte[] fapp_type;
        byte fapp_msg_type;
        byte fapp_unused;
        byte[] fapp_seq_num;
        byte[] fapp_data;

        public _FAPP_HEADER() {
            this.fapp_totlen = new byte[4];
            this.fapp_type = new byte[2];
            this.fapp_msg_type = 0x00;
            this.fapp_unused = 0x00;
            this.fapp_seq_num = new byte[4];
            this.fapp_data = null;
        }
    }

    _FAPP_HEADER m_sHeader = new _FAPP_HEADER();

    private void setFragmentation(int type){
        if(type == 0) { // ó��
            m_sHeader.fapp_type[0] = (byte) 0x0;
            m_sHeader.fapp_type[1] = (byte) 0x0;
        }
        else if(type == 1) { // �߰�
            m_sHeader.fapp_type[0] = (byte) 0x0;
            m_sHeader.fapp_type[1] = (byte) 0x1;
        }
        else if(type == 2) { // ��
            m_sHeader.fapp_type[0] = (byte) 0x0;
            m_sHeader.fapp_type[1] = (byte) 0x2;
        }
    }

    public void setFileMsgType(int type) { // fapp_msg_type ���� ����
        m_sHeader.fapp_msg_type = (byte) type;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    } // �����̸� ������

    // ���� ũ�� ������
    public void setFileSize(int fileSize) {
        m_sHeader.fapp_totlen[0] = (byte)(0xff&(fileSize >> 24));
        m_sHeader.fapp_totlen[1] = (byte)(0xff&(fileSize >> 16));
        m_sHeader.fapp_totlen[2] = (byte)(0xff&(fileSize >> 8));
        m_sHeader.fapp_totlen[3] = (byte)(0xff & fileSize);
    }

    public int calcSeqNum(byte[] input) { // �� ��° Frame���� ���(Frame�� 0������ ����)
        int seqNum = 0;
        seqNum += (input[8] & 0xff) << 24;
        seqNum += (input[9] & 0xff) << 16;
        seqNum += (input[10] & 0xff) << 8;
        seqNum += (input[11] & 0xff);

        return seqNum;
    }

    public int calcFileFullLength(byte[] input) {
        int fullLength = 0;
        fullLength += (input[0] & 0xff) << 24;
        fullLength += (input[1] & 0xff) << 16;
        fullLength += (input[2] & 0xff) << 8;
        fullLength += (input[3] & 0xff);
        return fullLength;
    }


    public boolean fileInfoSend(byte[] input, int length) { // ���� ���� �۽� �Լ�
        this.setFileMsgType(0); // ���� ���� �۽����� ��Ÿ��
        this.Send(input, length); // ���� ���� �۽�

        return true;
    }

    // �������� �� �޾Ҵ��� Ȯ�� ��, ��� ��Ȯ�� ���������� ������ �����ϴ� �Լ�
    public boolean sortFileList(int lastFrameNumber) {
        // ��� �������� �޾Ҵ��� Ȯ��
        if((fileByteList.size() - 1 != lastFrameNumber) || (receivedLength != targetLength)) {
            ((ChatFileDlg)this.GetUpperLayer(0)).ChattingArea.append("���� ���� ����\n");
            return false;
        }

        // ArrayList�� SeqNum�� Index�� �������� �����Ͽ� ���� ����
        fileSortList = new ArrayList<byte[]>();
        for(int checkSeqNum = 0; checkSeqNum < (lastFrameNumber + 1); ++checkSeqNum) {
            byte[] checkByteArray = fileByteList.remove(0);
            int arraySeqNum = this.calcSeqNum(checkByteArray);
            fileSortList.add(arraySeqNum, checkByteArray);
        }

        return true;
    }

    public void setAndStartSendFile() {
        ChatFileDlg upperLayer = (ChatFileDlg) this.GetUpperLayer(0);
        File sendFile = upperLayer.getFile();
        int sendTotalLength; // �������ϴ� �� ũ��
        int sendedLength; // ���� ���� ũ��
        this.resetSeqNum();

        try (FileInputStream fileInputStream = new FileInputStream(sendFile)) {
            sendedLength = 0;
            BufferedInputStream fileReader = new BufferedInputStream(fileInputStream);
            sendTotalLength = (int)sendFile.length();
            this.setFileSize(sendTotalLength);
            byte[] sendData =new byte[1448];
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(sendTotalLength);
            if(sendTotalLength <= 1448) {
                // ���� ���� �۽�
                setFragmentation(0);
                this.setFileMsgType(0);
                this.fileInfoSend(sendFile.getName().getBytes(), sendFile.getName().getBytes().length);

                // ���� ������ �۽�
                this.setFileMsgType(1);
                fileReader.read(sendData);
                this.Send(sendData, sendData.length);
                sendedLength += sendData.length;
                ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(sendedLength);
            } else {
                sendedLength = 0;
                // ���� ���� �۽�
                this.setFragmentation(0);
                this.setFileMsgType(0);
                this.fileInfoSend(sendFile.getName().getBytes(), sendFile.getName().getBytes().length);

                // ���� ������ �۽�
                this.setFileMsgType(1);
                this.setFragmentation(1);
                while(fileReader.read(sendData) != -1 && (sendedLength + 1448 < sendTotalLength)) {
                    this.Send(sendData, 1448);
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendedLength += 1448;
                    this.increaseSeqNum();
                    ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(sendedLength);
                }

                byte[] getRealDataFrame = new byte[sendTotalLength - sendedLength];
                this.setFragmentation(2);
                fileReader.read(sendData);

                for(int index = 0; index < getRealDataFrame.length; ++index) {
                    getRealDataFrame[index] = sendData[index];
                }

                this.Send(getRealDataFrame, getRealDataFrame.length);
                sendedLength += getRealDataFrame.length;
                count = 0;
                ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(sendedLength);
            }
            fileInputStream.close();
            fileReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] RemoveCappHeader(byte[] input, int length) { // FileApp�� Header�� �������ִ� �Լ�
        byte[] buf = new byte[length - 12];
        for(int dataIndex = 0; dataIndex < length - 12; ++dataIndex)
            buf[dataIndex] = input[12 + dataIndex];

        return buf;
    }

    public synchronized boolean Receive(byte[] input) { // �����͸� ���� ó�� �Լ�
        byte[] data;

        if(checkReceiveFileInfo(input)) { // ������ ������ ���� ���
            data = RemoveCappHeader(input, input.length); // Header���ֱ�
            String fileName = new String(data);
            fileName = fileName.trim();
            targetLength = calcFileFullLength(input); // �޾ƾ� �ϴ� �� ũ�� �ʱ�ȭ
            file = new File("./" + fileName); //�޴� ���..

            // Progressbar �ʱ�ȭ
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMinimum(0);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(targetLength);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);

            // ���� ũ��) �ʱ�ȭ
            receivedLength = 0;
        } else {
            // ����ȭ�� ���� ���� �����͸� ���� ���
            if (checkNoFragmentation(input)) {
                data = RemoveCappHeader(input, input.length);
                fileByteList.add(this.calcSeqNum(input), data);
                try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    fileOutputStream.write(fileByteList.get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // ����ȭ�� ������ �����͸� ���� ���

                // ������ ������ ����
                fileByteList.add(input);
                receivedLength += (input.length - 12); // ����� ���̴� ����

                // ������ ������ ����
                if(checkLastDataFrame(input)) {
                    int lastFrameNumber = this.calcSeqNum(input);

                    if(sortFileList(lastFrameNumber)) {
                        try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                            for (int frameCount = 0; frameCount < (lastFrameNumber + 1); ++frameCount) {
                                data = RemoveCappHeader(fileSortList.get(frameCount), fileSortList.get(frameCount).length);
                                fileOutputStream.write(data);
                            }
                            ((ChatFileDlg)this.GetUpperLayer(0)).ChattingArea.append("���� ���� �� ���� �Ϸ�\n");
                            fileByteList = new ArrayList();
                        } catch (FileNotFoundException e) {
                            ((ChatFileDlg)this.GetUpperLayer(0)).ChattingArea.append("���� ���� ����\n");
                            e.printStackTrace();
                        } catch (IOException e) {
                            ((ChatFileDlg)this.GetUpperLayer(0)).ChattingArea.append("���� ���� ����\n");
                            e.printStackTrace();
                        }
                    }
                }
                ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(receivedLength); // Progressbar ����
            }
        }

        return true;
    }

    public void resetSeqNum() {
        this.m_sHeader.fapp_seq_num[0] = (byte)0x0;
        this.m_sHeader.fapp_seq_num[1] = (byte)0x0;
        this.m_sHeader.fapp_seq_num[2] = (byte)0x0;
        this.m_sHeader.fapp_seq_num[3] = (byte)0x0;
    }

    public void increaseSeqNum() { // Frame ��ȣ ���� �Լ�(Send�� Frame ��ȣ �� ����)
        if((this.m_sHeader.fapp_seq_num[3] & 0xff) < 255)
            ++this.m_sHeader.fapp_seq_num[3];
        else if((this.m_sHeader.fapp_seq_num[2] & 0xff) < 255) {
            ++this.m_sHeader.fapp_seq_num[2];
            this.m_sHeader.fapp_seq_num[3] = 0;
        } else if((this.m_sHeader.fapp_seq_num[1] & 0xff) < 255) {
            ++this.m_sHeader.fapp_seq_num[1];
            this.m_sHeader.fapp_seq_num[2] = 0;
            this.m_sHeader.fapp_seq_num[3] = 0;
        } else if((this.m_sHeader.fapp_seq_num[0] & 0xff) < 255) {
            ++this.m_sHeader.fapp_seq_num[0];
            this.m_sHeader.fapp_seq_num[1] = 0;
            this.m_sHeader.fapp_seq_num[2] = 0;
            this.m_sHeader.fapp_seq_num[3] = 0;
        }
    }

    public boolean Send(byte[] input, int length) { // ������ �۽� �Լ�
        byte[] bytes = this.ObjToByte(m_sHeader, input, length);
        ((EthernetLayer)this.GetUnderLayer()).fileSend(bytes, length + 12);
        return true;
    }

    private byte[] ObjToByte(_FAPP_HEADER m_sHeader, byte[] input, int length) {
        byte[] buf = new byte[length + 12];
        buf[0] = m_sHeader.fapp_totlen[0];
        buf[1] = m_sHeader.fapp_totlen[1];
        buf[2] = m_sHeader.fapp_totlen[2];
        buf[3] = m_sHeader.fapp_totlen[3];
        buf[4] = m_sHeader.fapp_type[0];
        buf[5] = m_sHeader.fapp_type[1];
        buf[6] = m_sHeader.fapp_msg_type;
        buf[7] = m_sHeader.fapp_unused;
        buf[8] = m_sHeader.fapp_seq_num[0];
        buf[9] = m_sHeader.fapp_seq_num[1];
        buf[10] = m_sHeader.fapp_seq_num[2];
        buf[11] = m_sHeader.fapp_seq_num[3];

        for(int dataIndex = 0; dataIndex < length; ++dataIndex)
            buf[12 + dataIndex] = input[dataIndex];

        return buf;
    }
    public boolean checkReceiveFileInfo(byte[] input) {
        if(input[6] == (byte)0x00)
            return true;

        return false;
    }
    public boolean checkLastDataFrame(byte[] input) { // ������ Frame���� Ȯ��
        if(input[4] == (byte) 0x0 && input[5] == (byte)0x0)
            return true;
        else if(input[4] == (byte) 0x0 && input[5] == (byte)0x2)
            return true;
        else
            return false;
    }

    public boolean checkNoFragmentation(byte[] input) { // File �����Ͱ� ����ȭ�� �������� �ʾҴ��� �˻��ϴ� �Լ�
        if(input[4] == (byte) 0x00 && input[5] == (byte)0x0)
            return true;

        return false;
    }



    @Override
    public String GetLayerName() {
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        if(p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        if(nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        if(pUnderLayer == null)
            return;
        this.p_UnderLayer = pUnderLayer;
    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        if(pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }
}