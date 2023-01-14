package heap;

import java.util.Arrays;
import java.util.PriorityQueue;

public class KthLargestElementInAStream_703 {
    public static void main(String[] args) {
        // ["KthLargest","add","add","add","add","add"]
        // [[1,[]],[-3],[-2],[-4],[0],[4]]

        KthLargest kthLargest = new KthLargest(3, new int[]{4, 5, 8, 2});
        System.out.println(kthLargest.add(3));
        System.out.println(kthLargest.add(5));
        System.out.println(kthLargest.add(10));
        System.out.println(kthLargest.add(9));
        System.out.println(kthLargest.add(4));

//        KthLargest kthLargest = new KthLargest(1, new int[]{});
//        System.out.println(kthLargest.add(-3));
//        System.out.println(kthLargest.add(-2));
//        System.out.println(kthLargest.add(-4));
//        System.out.println(kthLargest.add(0));
//        System.out.println(kthLargest.add(4));
    }

    static class KthLargest {
        private int k;
        private PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        public KthLargest(int k, int[] nums) {
            this.k = k;
            Arrays.stream(nums).forEach(this::add);
        }

        public int add(int val) {
            minHeap.offer(val);
            if (minHeap.size() > k) {
                minHeap.poll();
            }

            return minHeap.peek();
        }
    }
}
