class Solution {
    public int networkDelayTime(int[][] times, int n, int k) {
        n = n+1;
        List<List<int[]>> graph = new ArrayList<>();
        for(int i=0; i<n; i++) 
        {
            graph.add(new ArrayList<>());
        }

        for(int[] edge : times) 
        {
            int u = edge[0];
            int v = edge[1];
            int w = edge[2];
            graph.get(u).add(new int[]{v,w});
        }

        int[] result = new int[n];
        Arrays.fill(result, Integer.MAX_VALUE);
        result[k] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>((a,b) -> a[1]-b[1]);

        pq.add(new int[]{k,0});
        while(pq.size() > 0) 
        {
            int[] top = pq.poll();
            int topNode = top[0];
            int topTime = top[1];
            for(int[] neighbour : graph.get(topNode)) 
            {
                int neighbourNode = neighbour[0];
                int neighbourTime = neighbour[1];
                int totalTime = topTime + neighbourTime;
                if(result[neighbourNode] > totalTime) 
                {
                    pq.add(new int[]{neighbourNode, totalTime});
                    result[neighbourNode] = totalTime;
                }
            }
        }
    
        int minTime = Integer.MIN_VALUE;
        for(int i=1; i<result.length; i++) 
        {
            if(result[i] == Integer.MAX_VALUE) 
            {
                return -1;
            }
            minTime = Math.max(minTime, result[i]);
        }

        return minTime;
    }
}
