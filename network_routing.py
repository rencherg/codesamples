def find_shortest_path_with_array(
        graph: dict[int, dict[int, float]],
        source: int,
        target: int
) -> tuple[list[int], float]:
    num_nodes = len(graph)

    distance_list: list[float] = [float('inf')] * num_nodes
    previous_node: list[int] = [None] * num_nodes
    return_list: list[int] = []

    distance_list[source] = 0
    priority_queue_array: list[TreeNode] = [TreeNode(0, source, -1)]

    while priority_queue_array:

        next_node_index = 0
        for i in range(1, len(priority_queue_array)):
            if priority_queue_array[i].total_distance < priority_queue_array[next_node_index].total_distance:
                next_node_index = i

        next_node = priority_queue_array.pop(next_node_index)
        next_node_number = next_node.node_number

        if next_node_number == target:
            break

        for neighbor, weight in graph[next_node_number].items():
            new_distance = distance_list[next_node_number] + weight
            if new_distance < distance_list[neighbor]:
                distance_list[neighbor] = new_distance
                previous_node[neighbor] = next_node_number

                found = False
                for node in priority_queue_array:
                    if node.node_number == neighbor:
                        found = True
                        break
                
                if not found:
                    priority_queue_array.append(TreeNode(new_distance, neighbor, next_node_number))

    if distance_list[target] == float('inf'):
        return [], float('inf')

    current = target
    while current is not None:
        return_list.append(current)
        current = previous_node[current]

    return_list.reverse()
    final_distance = distance_list[target]

    return return_list, final_distance

def find_shortest_path_with_heap(
        graph: dict[int, dict[int, float]],
        source: int,
        target: int
) -> tuple[list[int], float]:
    num_nodes = len(graph)
    distance_list: list[list[float]] = [[float('inf'), None] for _ in range(num_nodes)]
    return_list: list[int] = []
    final_distance: float = 0

    distance_list[source][0] = 0
    distance_list[source][1] = -1
    min_heap = MinHeap(num_nodes)
    min_heap.add_item(TreeNode(0, source, -1))

    while True:
        if len(min_heap.binary_heap) == 0:
            return [], float('inf')
        
        next_node: TreeNode = min_heap.pop_top()
        next_node_number: int = next_node.node_number
        distance_list[next_node_number][0] = next_node.total_distance
        distance_list[next_node_number][1] = next_node.source_node_number

        if next_node_number == target:
            break

        for key in graph[next_node_number]:
            new_distance = graph[next_node_number][key] + next_node.total_distance
            if new_distance < distance_list[key][0]:
                min_heap.add_item(TreeNode(new_distance, key, next_node_number))

    next_found_node_number: int = target
    while next_found_node_number != -1:
        return_list.append(next_found_node_number)
        next_found_node_number = distance_list[next_found_node_number][1]

    return_list.reverse()
    final_distance = distance_list[target][0]

    return return_list, final_distance

class TreeNode:
    def __init__(self, total_distance=0, node_number=0, source_node_number=0):
        self.total_distance = total_distance
        self.node_number = node_number
        self.source_node_number = source_node_number

class MinHeap:
    def __init__(self, size):
        self.binary_heap: list[TreeNode] = []
        self.reference_list: list[int] = [-1] * size

    def add_item(self, node: TreeNode):
        if self.reference_list[node.node_number] > -1:
            original_index = self.reference_list[node.node_number]
            original_node: TreeNode = self.binary_heap[original_index]
            if node.total_distance < original_node.total_distance:
                self.binary_heap[original_index] = node
                self.bubble_up(original_index)
        else:
            self.binary_heap.append(node)
            self.reference_list[node.node_number] = len(self.binary_heap) - 1
            self.bubble_up(len(self.binary_heap) - 1)

    def pop_top(self) -> TreeNode:
        if len(self.binary_heap) == 0:
            return None

        top_node: TreeNode = self.binary_heap[0]
        last_node = self.binary_heap.pop()
        if len(self.binary_heap) > 0:
            self.binary_heap[0] = last_node
            self.reference_list[last_node.node_number] = 0
            self.sink_down(0)
        self.reference_list[top_node.node_number] = -1
        return top_node

    def swap_nodes(self, index_1: int, index_2: int, node_1: TreeNode, node_2: TreeNode):
        self.binary_heap[index_1] = node_2
        self.binary_heap[index_2] = node_1
        if node_1:
            self.reference_list[node_1.node_number] = index_2
        if node_2:
            self.reference_list[node_2.node_number] = index_1

    def bubble_up(self, current_index: int):
        while current_index > 0:
            parent_index = (current_index - 1) // 2
            if self.binary_heap[current_index].total_distance < self.binary_heap[parent_index].total_distance:
                self.swap_nodes(current_index, parent_index, self.binary_heap[current_index], self.binary_heap[parent_index])
                current_index = parent_index
            else:
                break

    def sink_down(self, current_index: int):
        while True:
            l_child_index = 2 * current_index + 1
            r_child_index = 2 * current_index + 2
            smallest = current_index

            if l_child_index < len(self.binary_heap) and self.binary_heap[l_child_index].total_distance < self.binary_heap[smallest].total_distance:
                smallest = l_child_index

            if r_child_index < len(self.binary_heap) and self.binary_heap[r_child_index].total_distance < self.binary_heap[smallest].total_distance:
                smallest = r_child_index

            if smallest != current_index:
                self.swap_nodes(current_index, smallest, self.binary_heap[current_index], self.binary_heap[smallest])
                current_index = smallest
            else:
                break