# Data_Communication

- Data Communicate  
  **정의**
  - 원거리에 있는 입출력 장치 및 컴퓨터들 간 통신 회선을 통해서, 문자, 숫자, 기호 등의로 표현된 데이터를 상호간 교환하는 통신  

Vmware를 통해 가상 Host를 배치하여 두 장치 사이에서의 Data Communication 실습

### 1. IPC(Inter-Process Communication)
하나의 파일을 다중 프로세스들이 동시에 사용하고자 할 때 그 데이터를 필요로 하는 프로세스에게 전달이 제대로 안될 수 있음.  
이를 해결하기 위해 IPC 사용

![ipc](https://user-images.githubusercontent.com/28483545/122578760-297eab00-d08f-11eb-88fd-cc6d1eb31bb7.png)

### 2. Simplest
**Data Link Layer(Ethernet)**
- 두 포인트(Point-to-Point)간 신뢰성 있는 전송을 보장하기 위한 계층

![sim](https://user-images.githubusercontent.com/28483545/122579099-88dcbb00-d08f-11eb-9c72-523f52791caa.png)

### 3. Stop & Wait

![stop](https://user-images.githubusercontent.com/28483545/122579359-cb9e9300-d08f-11eb-8768-9d887fbc6f21.png)

### 4. Chatting & File Transfer
FileAppLayer 추가구현. header를 통해 file과 chat 구분.

![chat](https://user-images.githubusercontent.com/28483545/122579581-0274a900-d090-11eb-9f6c-2175923daacb.png)
