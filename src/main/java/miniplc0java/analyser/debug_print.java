package miniplc0java.analyser;

import java.util.ArrayList;

public class debug_print {
    public static void print_stack(int stack_top1,int stack_top2,StackVar[] stack_vars,stack_point[] stack_points){
        String[] type={"Unknown","int","void","double"};
        String[] position_type={"global","arg","local"};
        while(stack_top2>=0) {
            for (int i = stack_top1; i >= stack_points[stack_top2].points; i--) {
                System.out.println(stack_vars[i].name + '\t' + type[stack_vars[i].type-1]+'\t'+
                        position_type[stack_vars[i].position.var_type-1]+'\t'+stack_vars[i].position.position);
            }
            if(stack_points[stack_top2].is_fn){
                System.out.print("fn");
            }
            System.out.println("------------------"+stack_top2);
            stack_top1=stack_points[stack_top2].points-1;
            stack_top2--;
        }
    }
    public static void print_current_stack(int stack_top1,int stack_top2,StackVar[] stack_vars,stack_point[] stack_points){
        String[] type={"Unknown","int","void","double"};
        String[] position_type={"global","arg","local"};
        for (int i = stack_top1; i >= stack_points[stack_top2].points; i--) {
            System.out.println(stack_vars[i].name + '\t' + type[stack_vars[i].type-1]+'\t'+
                    position_type[stack_vars[i].position.var_type-1]+'\t'+stack_vars[i].position.position);
        }
        if(stack_points[stack_top2].is_fn){
            System.out.print("fn");
        }else{
            System.out.print("++");
        }
        System.out.println("++++++++++++++++"+stack_top2);
    }
    public static void print_funcs(func[] func_list,int func_top){
        func tmp;
        for(int i=0;i<=func_top;i++){
            tmp=func_list[i];
            System.out.println("("+tmp.func_num+")");
            String str=new StringBuilder().append("fn [").append(tmp.global_num).append("] ")
                    .append(tmp.locals_num).append(" ").append(tmp.args_num).append(" -> ")
                    .append(tmp.return_num).toString();
            System.out.println(str);
        }
    }
    public static void print_expr(ArrayList<ExprToken> exprTokens,Boolean flag){
        for(int i=0;i<exprTokens.size();i++){
            if(exprTokens.get(i).type==ExprType.UINT_LITERAL){
                System.out.print(((Uint)exprTokens.get(i)).value+" ");
            }else if(exprTokens.get(i).type==ExprType.Var) {
                System.out.print(((Var) exprTokens.get(i)).stack_var.name + " ");
            }
            else if(exprTokens.get(i).type==ExprType.FUNC){
                System.out.print(exprTokens.get(i).type+" ");
                for(int j=0;j<((Call_func) (exprTokens.get(i))).args.size();j++){
                    System.out.print("( ");
                    print_expr(((Call_func) (exprTokens.get(i))).args.get(j),false);
                    System.out.print(") ");
                }
            }else{
                System.out.print(exprTokens.get(i).type+" ");
            }
        }
        if(flag)
            System.out.print("\n");
    }
}
