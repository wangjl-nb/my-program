package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class char_priority {
    public static ArrayList<ExprToken> expr_priority(ArrayList<ExprToken> input) throws AnalyzeError {
        analysers tmp = new analysers();

        ArrayList<ExprToken> output= new ArrayList<ExprToken>();
        //测试
//        input.add(ExprType.NEG);
//        input.add(ExprType.FUNC);
//        input.add(ExprType.PLUS);
//        input.add(ExprType.UINT_LITERAL);
//        input.add(ExprType.MUL);
//        input.add(ExprType.Var);
//        input.add(ExprType.LE);
//        input.add(ExprType.L_PAREN);
//        input.add(ExprType.FUNC);
//        input.add(ExprType.MUL);
//        input.add(ExprType.FUNC);
//        input.add(ExprType.MINUS);
//        input.add(ExprType.FUNC);
//        input.add(ExprType.R_PAREN);
//        input.add(ExprType.MINUS);
//        input.add(ExprType.Var);
        //
        Stack<ExprToken> left=new Stack<ExprToken>();//输入栈
        Stack<ExprToken> right=new Stack<ExprToken>();//操作符栈
        analysers analyser=new analysers();
        Map<ExprType,Integer> map=analyser.map;
        for(int i=input.size()-1;i>=0;i--){
            left.push(input.get(i));
        }
        while(left.size()!=0||right.size()!=0){
//            System.out.println(right.peek());
            if(left.size()==0){
                while(right.size()>0){
                    output.add(right.pop());
                }
                break;
            }
            if(left.peek().type==ExprType.Var||left.peek().type==ExprType.FUNC||left.peek().type==ExprType.UINT_LITERAL){
                output.add(left.pop());
            }else{
                if(left.peek().type==ExprType.R_PAREN){
                    left.pop();
                    while(right.peek().type!=ExprType.L_PAREN){
                        output.add(right.pop());
                    }
                    right.pop();
                }else{
                    while(right.size()!=0&&right.peek().type!=ExprType.L_PAREN&&map.get(right.peek().type)>=map.get(left.peek().type)){
                        if(map.get(right.peek().type)==map.get(left.peek().type)&&right.peek().type==ExprType.NEG){
                            break;
                        }
                        output.add(right.pop());
                    }
                    right.push(left.pop());
                }
            }
        }
        return output;
    }
}
class analysers {
    /**
     *      +,- *,/   i,func   (   )   #    op
     *   +   >   <     <       <   >   >    >
     *   *   >   >     <       <   >   >    >
     *   i   >   >                 >   >    >
     *   (   <   <     <       <   =   >    >
     *   )   >   >                 >   >    >
     *   #   <   <     <       <   <   =    <
     *   op  <   <     <       <   <   >    =
     * */
//    int[][] priority = {
//            {0, 1, 1, 1, 0, 0, 0},//> 0,< 1,= 2
//            {0, 0, 1, 1, 0, 0, 0},
//            {0, 0,-1,-1, 0, 0, 0},
//            {1, 1, 1, 1, 2, 0, 0},
//            {0, 0,-1,-1, 0, 0, 0},
//            {1, 1, 1, 1, 1, 2, 1},
//            {1, 1, 1, 1, 1, 0, 2},
//    };
    Map<ExprType,Integer> map;
    analysers(){
        map=new HashMap<ExprType, Integer>();
        map.put(ExprType.PLUS,1);
        map.put(ExprType.NEG,4);
        map.put(ExprType.MINUS,1);
        map.put(ExprType.MUL,2);
        map.put(ExprType.DIV,2);
        map.put(ExprType.L_PAREN,5);
        map.put(ExprType.LT,0);
        map.put(ExprType.LE,0);
        map.put(ExprType.NEQ,0);
        map.put(ExprType.EQ,0);
        map.put(ExprType.GE,0);
        map.put(ExprType.GT,0);
        map.put(ExprType.IOTF,3);
        map.put(ExprType.FTOI,3);
    }
}
