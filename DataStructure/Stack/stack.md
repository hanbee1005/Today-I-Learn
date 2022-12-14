# Stack
- 후입선출(後入先出/Last In First Out—LIFO) 특성을 가지는 자료구조(Data Structure)
- 메모리에 새로 들어오는 데이터의 위치가 메모리 말단(일명 '탑 포인터')이고, 써먹기 위해 내보내는 데이터 역시 메모리 말단을 거칩니다.
- 스택의 추상자료형(Abstract Data Type—ADT)을 살펴보면, 입력연산은 푸시(Push), 출력연산은 팝(Pop)이라고 부릅니다. 조회연산은 피크(Peek)라고 하는데, 탑 포인터가 가리키는 데이터를 조회(확인)만 할 뿐, 탑의 순번(順番/인덱스—Index)은 변화시키지 않는 연산을 의미합니다.
- 스택은 **힙 영역 메모리에서 일반적인 데이터를 저장하는 스택**과 **스택 영역 메모리에서 프로그램의 각 분기점에 변수와 같은 정보를 저장하기 위한 스택**이라는 두 가지 의미로 사용될 수 있므로 유의해야 한다.

### 구현
- 배열을 이용해서 구현할 때
	+ 처음으로 스택을 위한 배열을 하나 잡아 놓고, index 값을 0으로 잡습니다. index가 0이면 스택이 비어 있는 것이고, 스택에 뭔가를 집어넣을 때는 index의 자리에 집어넣고 index를 하나 올리면 됩니다. index가 초기 배열 크기 이상이면 스택이 꽉 찬 것입니다. 스택에서 뭔가를 뺄 때는 index에 있던 값을 돌려주고 index를 하나 뺍니다.
- 연결 리스트를 이용해서 구현할 때
	+ 메모리 상에 아이템을 위한 공간을 할당하고 새로운 아이템이 추가될 때 마다 포인터로 연결하기만 하면 됩니다. 연결 리스트로 구현하게 된다면 물리 메모리 상에는 순서와 관계 없이 여기저기에 무작위로 배치되고 포인터로 가리키게 될 것입니다.

### 활용
- 스택은 콜 스택(Call Stack)이라 하여 컴퓨터 프로그램의 서브루틴에 대한 정보를 저장하는 자료구조에도 널리 활용됩니다. 컴파일러가 출력하는 에러도 스택처럼 맨 마지막 에러가 가장 먼저 출력되는 순서를 보입니다.
- 스택은 메모리 영역에서 LIFO 형태로 할당하고 접근하는 구조인 아키텍처 레벨의 하드웨어 스택의 이름으로도 널리 사용됩니다. 
- 이외에도 꽉 찬 스택에 요소를 삽입하고자 할 때 스택에 요소가 넘쳐서 에러가 발생하는 것을 스택 버퍼 오버플로(Stack Buffer Overflow)라고 부릅니다.

### 종류
- Ascending stack VS Descending stack
- Empty stack VS Full stack
	+ Empty stack은 스택 포인터가 마지막 빈 slot을 가리키는 것이고
	+ Full stack은 스택 포인터가 마지막 데이터 아이템을 가리키는 것입니다.

### 연관 문제
- [leetcode 20. Valid Parentheses](https://github.com/hanbee1005/AlgorithmStudy/blob/master/Leetcode/202301/ValidParentheses_20.java)
- [leetcode 206. Reverse Linked List](https://github.com/hanbee1005/AlgorithmStudy/blob/master/Leetcode/202301/ReverseLinkedList_206.java)