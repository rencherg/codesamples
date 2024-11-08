from project2_classes.datalog_program import DatalogProgram
from project2_classes.predicate import Predicate
from project2_classes.rule import Rule
from project2_classes.parameter import Parameter
from project3_classes.relation import Relation
from project3_classes.header import Header
from project3_classes.row import Row
from project3_classes.database import Database
from project4_classes.rule_eval import RuleEval

class SCCEval:

    forward_adjacency_list: dict[str, list[str]] = {}
    reverse_adjacency_list: dict[str, list[str]] = {}

    postorder_list:list[str] = []

    scc_index_list:list[list[int]] = []

    def __init__(self):
        ...

    #returns a list of lists of indexes that corresponds to the sccs
    def evaluate_sccs(self, rules:list[Rule])->tuple[str,list[list[int]]]:

        output_str:str = 'Dependency Graph\n'

        self.reset()

        self.evaluate_forward_adjacency_list(rules)

        for key, values in self.forward_adjacency_list.items():
            output_str += key + ':'
            for value in values:
                output_str += value + ','
            if(len(values) > 0):
                output_str = output_str[0:-1]
            output_str += '\n'
        output_str+='\n'

        self.remove_self_loops()

        self.evaluate_reverse_adjacency_list()

        self.postorder_dfs()

        self.scc_dfs()

        return output_str,self.scc_index_list

    def evaluate_forward_adjacency_list(self, rules:list[Rule])->None:

        head_element_name:str
        element_name:str

        for i in range(len(rules)):

            head_element_name = 'R'+str(i)

            self.forward_adjacency_list[head_element_name] = []
            self.reverse_adjacency_list[head_element_name] = []

            for body_predicate in rules[i].body_predicates:
                for j in range(len(rules)):
                    if (body_predicate.get_name() == rules[j].head_predicate.get_name()):
                        element_name = 'R'+str(j)
                        if(element_name not in self.forward_adjacency_list[head_element_name]):

                            insert_index:int = 0

                            for i in range(len(self.forward_adjacency_list[head_element_name])):
                                if(j > int(self.forward_adjacency_list[head_element_name][i][1:len(self.forward_adjacency_list[head_element_name][i])])):

                                    insert_index +=1

                            self.forward_adjacency_list[head_element_name].insert(insert_index,element_name)

    def evaluate_reverse_adjacency_list(self)->None:
        for key in self.forward_adjacency_list.keys():
            for inner_value in self.forward_adjacency_list[key]:
                self.reverse_adjacency_list[inner_value].append(key)

    def postorder_dfs(self)->None:

        stack:list[str] = []
        remaining_rules:list[str] = []
        for key in self.forward_adjacency_list.keys():
            remaining_rules.append(key)

        while (len(remaining_rules) > 0):
            postorder_group:list[str] = []
            stack.append(remaining_rules[0])
            postorder_group = self.postorder_recursion(remaining_rules,stack,postorder_group,remaining_rules[0])
            for key in postorder_group:
                self.postorder_list.append(key)

    def postorder_recursion(self,remaining_rules:list[str],stack:list[str],postorder_group:list[str],current_value:str)->None:
        remaining_rules.remove(current_value)

        #There may need to be sorting here
        availible_leads:list[str] = self.reverse_adjacency_list[current_value]

        if(len(availible_leads) > 0):
            for lead in availible_leads:
                if lead in remaining_rules:
                    stack.append(lead)
                    postorder_group = self.postorder_recursion(remaining_rules,stack,postorder_group,lead)
            
        stack.remove(current_value)
        postorder_group.append(current_value)
        return postorder_group

    def scc_dfs(self)->None:

        stack:list[str] = []
        remaining_postorder:list[str] = []
        for i in range(len(self.postorder_list)-1,-1,-1):
            remaining_postorder.append(self.postorder_list[i])

        while (len(remaining_postorder) > 0):
            scc_list:list[int] = []
            stack.append(remaining_postorder[0])
            scc_list:list = self.dfs_recursion(remaining_postorder,stack,scc_list,remaining_postorder[0])
            scc_list.sort()
            self.scc_index_list.append(scc_list)

    def dfs_recursion(self,remaining_postorder:list[str],stack:list[str],scc_list:list[str],current_value:str)->None:
        remaining_postorder.remove(current_value)

        #There may need to be sorting here
        availible_leads:list[str] = self.forward_adjacency_list[current_value]

        if(len(availible_leads) > 0):
            for lead in availible_leads:
                if lead in remaining_postorder:
                    stack.append(lead)
                    scc_list = self.dfs_recursion(remaining_postorder,stack,scc_list,lead)
            
        stack.remove(current_value)
        scc_list.append(int(current_value[1:len(current_value)]))
        return scc_list

    def remove_self_loops(self)->None:
        for key in self.forward_adjacency_list.keys():
            for value in self.forward_adjacency_list[key]:
                if key == value:
                    self.forward_adjacency_list[key].remove(value)

    def reset(self)->None:
        self.forward_adjacency_list = {}
        self.reverse_adjacency_list = {}
        self.postorder_list = []
        self.scc_index_list = []