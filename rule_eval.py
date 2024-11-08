from project2_classes.datalog_program import DatalogProgram
from project2_classes.predicate import Predicate
from project2_classes.rule import Rule
from project2_classes.parameter import Parameter
from project3_classes.relation import Relation
from project3_classes.header import Header
from project3_classes.row import Row
from project3_classes.database import Database

class RuleEval:

    def __init__(self):
        ...

    def count_tuples(self, database:Database)->int:

        tuple_count:int = 0

        for scheme in database.relation_list:
            tuple_count += len(scheme.rows)

        return tuple_count
    
    def check_dependent(self,rule:Rule)->bool:
        
        for predicate in rule.body_predicates:
            if(rule.head_predicate.name == predicate.name):
                return True

        return False
    
    def scc_output_string(self, single_scc_index_list:list[int])->str:
        output_str:str = ''
        for index in single_scc_index_list:
            output_str += 'R'+str(index) + ','
        output_str = output_str[0:-1]
        return output_str

    def evaluate_all(self,datalog_program:DatalogProgram, database:Database,scc_index_list:list[list[int]])->str:

        return_string:str = 'Rule Evaluation\n'
        rule_list:list[Rule]

        for scc in scc_index_list:

            return_string+='SCC: '+ self.scc_output_string(scc) +'\n'

            rule_list = []

            for i in range(len(datalog_program.rules)):
                for j in scc:
                    if (i ==j):
                        rule_list.append(datalog_program.rules[i])

            if((len(scc) == 1) and (self.check_dependent(datalog_program.rules[scc[0]]) == False)):
                #Run it just once
                return_string += self.evaluate(rule_list, database)
                return_string += '1 passes: ' + self.scc_output_string(scc) + '\n'
            else:

                pass_count:int = 1

                tuple_count1:int = self.count_tuples(database)

                return_string += self.evaluate(rule_list, database)

                tuple_count2:int = self.count_tuples(database)

                while(tuple_count1 != tuple_count2):
                    pass_count += 1
                    tuple_count1 = tuple_count2
                    return_string += self.evaluate(rule_list, database)
                    tuple_count2 = self.count_tuples(database)

                return_string += str(pass_count) + ' passes: ' + self.scc_output_string(scc) + '\n'

        return_string+= '\nQuery Evaluation\n'

        return return_string

    def evaluate(self,rules:list[Rule], database:Database)->str:

        return_string:str = ''

        result_query_relation_list:list[Relation] = []

        for rule in rules:

            result_query_relation_list = []

            return_string += rule.head_predicate.to_string() + ' :- '

            for predicate in rule.body_predicates:

                if(len(rule.body_predicates) == 1):
                    return_string += predicate.to_string()
                else:
                    return_string += predicate.to_string() + ','
                        
                for relation in database.relation_list:

                    if predicate.get_name() == relation.get_name():

                        result_query_relation_list.append(database.query(relation, predicate))

            if(len(rule.body_predicates) != 1):
                return_string = return_string[0:len(return_string)-1]
            return_string += '.\n'

            joined_relation:Relation
            joined_relation = result_query_relation_list[0]

            if(len(result_query_relation_list) > 1):
                for i in range(len(result_query_relation_list)-1):

                    common_attribute_list:list[str] = []

                    for attribute_a in joined_relation.header.val_list:
                        for attribute_b in result_query_relation_list[i+1].header.val_list:
                            if attribute_a == attribute_b:
                                common_attribute_list.append(attribute_a)

                    joined_relation = self.join_relation(joined_relation, result_query_relation_list[i+1],common_attribute_list)
            
            project_index_list:list[int] = []

            for column in rule.head_predicate.get_parameter_list():
                for i in range(len(joined_relation.header.val_list)):
                    if(column.get_value() == joined_relation.header.val_list[i]):
                        project_index_list.append(i)

            projected_relation:Relation = joined_relation.project(project_index_list)

            for relation in database.relation_list:
                if(relation.get_name() == rule.head_predicate.get_name()):

                    output_tuples:set(Row) = set()

                    #Union
                    for row in projected_relation.rows:
                        if row not in relation.rows:
                            relation.rows.add(row)
                            output_tuples.add(row)

                    for row in sorted(output_tuples):
                        seperator:str = ""
                        return_string += "  "
                        for i in range(len(relation.header.val_list)):
                            return_string += seperator
                            return_string += relation.header.val_list[i]
                            return_string += "="# + str(len(row.val_list))
                            return_string += row.val_list[i]
                            seperator = ", "

                        return_string += "\n"

        return return_string

    #The common attribute in the 2nd relation will be renamed.
    #If there are no attributes in common then nothing will be renamed.
    #Pass 'n/a' as the parameter if nothing is in common
    #Returns the joined new
    def join_relation(self,relation_a:Relation,relation_b:Relation,common_attribute_list:list[str]):

        new_name:str = relation_a.get_name() + '|X|' + relation_b.get_name()

        header_list:list[str] = []

        new_rows:set[Row] = set()

        #No attributes in common
        if(len(common_attribute_list) == 0):

            for i in range(len(relation_a.header.val_list)):
                header_list.append(relation_a.header.val_list[i])

            for i in range(len(relation_b.header.val_list)):
                header_list.append(relation_b.header.val_list[i])

            for row_a in relation_a.rows:
                for row_b in relation_b.rows:
                    new_rows.add(Row(row_a.val_list+row_b.val_list))

            new_header:Header = Header(header_list)

            projected_new_relation:Relation=Relation(new_name, new_header, new_rows)

        #1 or more attributes in common
        else:

            index_a_list:list[int] = list(range(len(common_attribute_list)))
            index_b_list:list[int] = list(range(len(common_attribute_list)))

            for i in range(len(relation_a.header.val_list)):
                for j in range(len(common_attribute_list)):
                    if (relation_a.header.val_list[i] == common_attribute_list[j]):
                        index_a_list[j] = i
                header_list.append(relation_a.header.val_list[i])

            for i in range(len(relation_b.header.val_list)):
                for j in range(len(common_attribute_list)):
                    if (relation_b.header.val_list[i] == common_attribute_list[j]):
                        index_b_list[j] = (i+len(relation_a.header.val_list))
                header_list.append(relation_b.header.val_list[i])

            for row_a in relation_a.rows:
                for row_b in relation_b.rows:

                    new_rows.add(Row(row_a.val_list+row_b.val_list))

            new_header:Header = Header(header_list)

            selected_new_relation:Relation=Relation(new_name, new_header, new_rows)

            for i in range(len(index_a_list)):

                selected_new_relation = selected_new_relation.select2(index_a_list[i],index_b_list[i])

            new_header_index_list:list[int] = []

            for i in range(len(selected_new_relation.header.val_list)):
                if(i not in index_b_list):
                    new_header_index_list.append(i)

            projected_new_relation = selected_new_relation.project(new_header_index_list)  

        return projected_new_relation