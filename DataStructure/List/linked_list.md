# Linked List
- 추상적 자료형인 리스트를 구현한 자료구조로, Linked List라는 말 그대로 어떤 데이터 덩어리(이하 노드Node)를 저장할 때 그 다음 순서의 자료가 있는 위치를 데이터에 포함시키는 방식으로 자료를 저장한다.         
- 배열에 비해 데이터의 추가/삽입 및 삭제가 용이하나 순차적으로 탐색하지 않으면 특정 위치의 요소에 접근할 수 없어 일반적으로 탐색 속도가 떨어진다. 즉 탐색 또는 정렬을 자주 하면 배열을, 추가/삭제가 많으면 연결 리스트를 사용하는 것이 유리하다. 단, 연결 리스트라고 해서 반드시 순차 탐색만 해야 한다는 법은 없다. B+tree 자료구조는 연결 리스트에 힙을 더한 것 같은 모양새를 갖고 있는데 이 자료구조는 데이터의 추가/삭제/정렬/조회 모두에 유리하다.
- 종류
	+ 단순 연결 리스트
		- 다음 노드에 대한 참조만을 가진 가장 단순한 형태의 연결 리스트이다. 가장 마지막 원소를 찾으려면 얄짤없이 리스트 끝까지 찾아가야 하기 때문에(O(n)), 마지막 원소를 가리키는 참조를 따로 가지는 형태의 변형도 있다. 보통 큐를 구현할 때 이런 방법을 쓴다.
		- 이 자료구조는 Head노드를 참조하는 주소를 잃어버릴 경우 데이터 전체를 못 쓰게 되는 단점이 있다. 다음 노드를 참조하는 주소 중 하나가 잘못되는 경우에도 체인이 끊어진 양 거기부터 뒤쪽 자료들을 유실한다. 따라서 안정적인 자료구조는 아니다.
	+ 이중 연결 리스트
		- 다음 노드의 참조뿐만 아니라 이전 노드의 참조도 같이 가리키게 하면 이중 연결 리스트가 된다. 뒤로 탐색하는 게 빠르다는 단순한 장점 이외에도 한 가지 장점이 더 있는데, 단순 연결 리스트는 현재 가리키고 있는 노드를 삭제하는 게 한 번에 안 되고 O(n)이 될 수밖에 없는데 비해[4]이중 연결 리스트에서 현재 노드를 삭제하는 것은 훨씬 간단하다. 대신 관리해야 할 참조가 두 개나 있기 때문에 삽입이나 정렬의 경우 작업량이 더 많고 자료구조의 크기가 약간 더 커진다.
	+ 원형 연결 리스트
		- 단순 연결 리스트에서 마지막 원소가 널 대신 처음 원소를 가리키게 하면 원형 연결 리스트가 된다. 이와 비슷하게, 이중 연결 리스트의 처음과 끝을 서로 이으면 이중 원형 연결 리스트를 만들 수 있다.
		- 스트림 버퍼의 구현에 많이 사용한다. 이미 할당된 메모리 공간을 삭제하고 재할당하는 부담이 없기 때문에 큐를 구현하는 데에도 적합하다.
- 분석
	+ 배열과는 달리 첫번째 데이터의 추가/삭제가 O(1)의 시간안에 수행된다. 배열의 경우 데이터를 추가 또는 삭제할 때 해당 지점 뒤쪽의 데이터를 모두 이동해야 하나 연결 리스트는 그럴 필요가 없다. 하지만, 첫번째가 아닌 중간에 있는 데이터들의 추가/삭제는 해당 데이터를 검색하는데까지 시간이 걸리기 때문에 O(n)의 수행시간을 갖게된다.
	+ 다만 탐색시에는 문제가 되는데 각 데이터에 한번에 접근할 수가 없어 항상 순차적으로 도달해야 한다. 이 때문에 데이터 검색에 쥐약이다. 정렬은 O(nlogn)시간에 가능하다. 이 단점을 해결한 것이 위에서 여러 번 언급한 그 B+tree.

## 연관 문제
- [leetcode 141. Linked List Cycle](https://github.com/hanbee1005/AlgorithmStudy/blob/master/Leetcode/202301/LinkedListCycle_141.java)
- [leetcode 142. Linked List Cycle II](https://github.com/hanbee1005/AlgorithmStudy/blob/master/Leetcode/202301/LinkedListCycleII_142.java)
- [leetcode 83. Remove Duplicates from Sorted List](https://github.com/hanbee1005/AlgorithmStudy/blob/master/Leetcode/202301/RemoveDuplicatesFromSortedList_83.java)
- [leetcode 82. Remove Duplicates from Sorted List II](https://github.com/hanbee1005/AlgorithmStudy/blob/master/Leetcode/202301/RemoveDuplicatesFromSortedListII_82.java)
- [leetcode 2. Add Two Numbers](https://github.com/hanbee1005/AlgorithmStudy/blob/master/Leetcode/202301/AddTwoNumbers_2.java)
- [leetcode 989. Add to Array Form of Integer](https://github.com/hanbee1005/AlgorithmStudy/blob/master/Leetcode/202302/AddToArrayFormOfInteger_989.java)
