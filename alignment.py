def align(
        seq1: str,
        seq2: str,
        match_award=-3,
        indel_penalty=5,
        sub_penalty=1,
        banded_width=-1,
        gap='-'
) -> tuple[float, str | None, str | None]:
    """
        Align seq1 against seq2 using Needleman-Wunsch
        Put seq1 on left (j) and seq2 on top (i)
        => matrix[i][j]
        :param seq1: the first sequence to align; should be on the "left" of the matrix
        :param seq2: the second sequence to align; should be on the "top" of the matrix
        :param match_award: how many points to award a match
        :param indel_penalty: how many points to award a gap in either sequence
        :param sub_penalty: how many points to award a substitution
        :param banded_width: banded_width * 2 + 1 is the width of the banded alignment; -1 indicates full alignment
        :param gap: the character to use to represent gaps in the alignment strings
        :return: alignment cost, alignment 1, alignment 2
    """

    m = len(seq1)
    n = len(seq2)

    aligned_seq1, aligned_seq2 = '', ''

    if(banded_width==-1):

        score_list = [[ScoreListNode(None, 0) for _ in range(n+1)] for _ in range(m+1)]

        for i in range(1, m+1):
            score_list[i][0] = ScoreListNode(score_list[i-1][0], i*5)
        
        for j in range(1, n+1):
            score_list[0][j] = ScoreListNode(score_list[0][j-1], j*5)

        for i in range(1, len(score_list)):
            for j in range(1, len(score_list[0])):
                if seq1[i-1] == seq2[j-1]:
                    cost = match_award
                else:
                    cost = sub_penalty

                deletion_cost = score_list[i-1][j].get_score() + indel_penalty
                insertion_cost = score_list[i][j-1].get_score() + indel_penalty
                substitution_cost = score_list[i-1][j-1].get_score() + cost

                min_distance = min(substitution_cost, deletion_cost, insertion_cost)

                if(min_distance == substitution_cost):
                    score_list[i][j] = ScoreListNode(score_list[i-1][j-1], substitution_cost)
                elif(min_distance == deletion_cost):
                    score_list[i][j] = ScoreListNode(score_list[i-1][j], deletion_cost)
                elif(min_distance == insertion_cost):
                    score_list[i][j] = ScoreListNode(score_list[i][j-1], insertion_cost)
                else:
                    score_list[i][j] = ScoreListNode(None, float('inf'))

        score = score_list[-1][-1].get_score()

        i, j = m, n
        while i > 0 or j > 0:
            current_node = score_list[i][j]
            if i > 0 and j > 0 and current_node.get_prev_node() == score_list[i - 1][j - 1]:
                aligned_seq1 = seq1[i - 1] + aligned_seq1
                aligned_seq2 = seq2[j - 1] + aligned_seq2
                i -= 1
                j -= 1
            elif i > 0 and current_node.get_prev_node() == score_list[i - 1][j]:
                aligned_seq1 = seq1[i - 1] + aligned_seq1
                aligned_seq2 = gap + aligned_seq2
                i -= 1
            elif j > 0 and current_node.get_prev_node() == score_list[i][j-1]:
                aligned_seq1 = gap + aligned_seq1
                aligned_seq2 = seq2[j - 1] + aligned_seq2
                j -= 1

    else:

        score_list = [{} for _ in range(n + 1)]

        score_list[0][0] = ScoreListNode(None, 0)

        for j in range(1, min(n + 1, banded_width + 1)):
            score_list[0][j] = ScoreListNode(score_list[0][j - 1], j * 5)

        for i in range(1, n + 1):
            score_list[i] = {}

            score_list[i][0] = ScoreListNode(score_list[i - 1][0], i * 5)

            adjusted_start_index = max(1, i - banded_width)
            adjusted_end_index = min(n, i + banded_width)

            for j in range(adjusted_start_index, adjusted_end_index + 1):
                if seq1[i - 1] == seq2[j - 1]:
                    cost = match_award
                else:
                    cost = sub_penalty

                deletion_cost = score_list[i - 1].get(j, ScoreListNode(None, float('inf'))).get_score() + indel_penalty
                insertion_cost = score_list[i].get(j - 1, ScoreListNode(None, float('inf'))).get_score() + indel_penalty
                substitution_cost = score_list[i - 1].get(j - 1, ScoreListNode(None, float('inf'))).get_score() + cost

                min_distance = min(substitution_cost, deletion_cost,insertion_cost)

                if(min_distance == substitution_cost):
                    score_list[i][j] = ScoreListNode(score_list[i-1][j-1], substitution_cost)
                elif(min_distance == insertion_cost):
                    score_list[i][j] = ScoreListNode(score_list[i][j-1], insertion_cost)
                elif(min_distance == deletion_cost):
                    score_list[i][j] = ScoreListNode(score_list[i-1][j], deletion_cost)
                
                else:
                    score_list[i][j] = ScoreListNode(None, float('inf'))

        score = score_list[m][n].get_score()

        i, j = m, n

        while i > 0 or j > 0:
            current_node = score_list[i][j]
            if i > 0 and j > 0 and current_node.get_prev_node() == score_list[i - 1].get(j - 1, None):
                aligned_seq1 = seq1[i - 1] + aligned_seq1
                aligned_seq2 = seq2[j - 1] + aligned_seq2
                i -= 1
                j -= 1
            elif i > 0 and current_node.get_prev_node() == score_list[i - 1].get(j, None):
                aligned_seq1 = seq1[i - 1] + aligned_seq1
                aligned_seq2 = gap + aligned_seq2
                i -= 1
            elif j > 0 and current_node.get_prev_node() == score_list[i].get(j-1, None):
                aligned_seq1 = gap + aligned_seq1
                aligned_seq2 = seq2[j - 1] + aligned_seq2
                j -= 1

    return score, aligned_seq1, aligned_seq2

class ScoreListNode:
    def __init__(self, prev_node, score):
        self.prev_node = prev_node
        self.score = score

    def get_score(self):
        return self.score
    
    def get_prev_node(self):
        return self.prev_node
    