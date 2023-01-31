# Graph
- Graph는 vertices와 edge로 구성된 비선형 자료구조입니다.
- vertices는 종종 node라고 불리고 edge는 두 노드 사이를 연결하는 선을 의미합니다.
- 종류
	+ direction
		- edge가 양방향이면 Undirected graph
		- edge가 방향성이 있으면 Directed graph, 자기 자신에게 방향이 가면 self-loop
	+ cycle
		- 방향성을 가진 edge를 따라 돌다가 다시 처음 노드로 돌아가는 경우 Cycle
		- 다시 처음 지점으로 돌아오지 않으면 Acycle
	+ isolated
		- graph에서 고립된(다른 graph와 어떤 연결도 없는 또 다른) node or graph가 있는 경우 disconnected
		- 모든 노드가 1개 이상의 edge를 가지면 connected
		- 모든 노드가 다른 모든 노드들과 edge로 연결되어 있으면 completed
- BFS(Breadth First Search)
	+ 최대한 넓게 이동한 다음 더 이상 갈 수 없을 때 아래로 이동
	<img src="./images/bfs.png" alt="bfs" width="500"/>
- DFS(Depth First Search)
	+ 최대한 깊게 이동한 다음 더 이상 갈 수 없을 때 옆으로 이동
	<img src="./images/dfs.png" alt="dfs" width="500"/>

### 참고
- https://www.geeksforgeeks.org/graph-data-structure-and-algorithms/

### 연관 문제
- [Num. Title](https://github.com/hanbee1005/AlgorithmStudy/blob/master/Leetcode/202301)