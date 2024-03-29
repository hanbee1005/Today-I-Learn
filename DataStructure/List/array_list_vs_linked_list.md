# ArrayList 와 LinkedList

## ArrayList
### 특징
- 연속된 데이터의 리스트로 중간에 빈 공간이 있으면 안됩니다.
- 내부적으로 `Object[]`을 이용하여 요소를 저장합니다.
- 인덱스를 이용해 요소에 빠르게 접근이 가능합니다.
- 그러나 중간에 요소를 삽입/삭제하는 경우에는 요소들의 위치를 앞뒤로 이동시키기 때문에 느립니다.
- 가변적으로 공간을 늘리거나 줄일 수 있습니다. (기본 capacity = 10)
- 그러나 배열을 copy 하는 방식으로 크기를 늘리기 때문에 오버헤드가 발생합니다.

### vs. 배열
- 크기를 변경할 수 없는 정적 할당(static allocation)
- 인덱스를 이용한 빠른 접근
- 중간에 데이터가 삭제되어도 빈 상태로 유지

## LinkedList
### 특징
- 노드끼리 주소 포인터를 서로 가리키며 참조하는 구조입니다.
- 초기 크기를 지정하지 않고 객체가 저장될 때 동적으로 할당합니다.
- 요소에 접근하기 위해서는 처음부터 참조를 따라 이동해야 하기 때문에 느립니다.
- 중간에 요소를 삽입/삭제하여도 요소들의 위치를 앞뒤로 이동하는 것이 아닌 참조값만 변경하면 됩니다.
- 데이터 뿐만 아니라 참조값도 저장하고 있기 때문에 저장 공간이 낭비된다는 단점이 있습니다.

### 요즘은...
- LinkedList 는 실제 잘 사용되지 않는다!
    - ArrayList의 단점인 중간 요소 삽입/삭제에 대해 내부적으로 최적화가 잘 되어 있어 성능상 큰 차이가 없습니다.
    - 큐 등의 다른 용도로 사용한다고 해도 더 최적화된 ArrayDeQueue 컬렉션을 사용하면 됩니다.

## 시간 복잡도
| 연산 | ArrayList | LinkedList |
|---|:---:|:---:|
| 첫번째 위치에 insert / remove | O(N) | O(1) |
| 마지막 위치에 insert / remove | O(1) / O(N) → 공간이 부족해 배열 복제가 일어나는 경우 | O(1) |
| 중간에 insert / remove | O(N) | O(N) / search_time + O(1) |
| 값으로 search | O(N) | O(N) |
| 인덱스로 값 get | O(1) | O(N) |
| 값으로 remove | O(N) | O(N) |
