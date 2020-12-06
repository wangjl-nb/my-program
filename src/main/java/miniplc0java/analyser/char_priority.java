package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class char_priority {
    public static void main(String[] args) throws AnalyzeError {
        analysers tmp = new analysers();
        ArrayList<ExprType> input= new ArrayList<ExprType>();
        ArrayList<ExprType> output= new ArrayList<ExprType>();
        //测试
        input.add(ExprType.NEG);
        input.add(ExprType.FUNC);
        input.add(ExprType.PLUS);
        input.add(ExprType.UINT_LITERAL);
        input.add(ExprType.MUL);
        input.add(ExprType.Var);
        input.add(ExprType.LE);
        input.add(ExprType.L_PAREN);
        input.add(ExprType.FUNC);
        input.add(ExprType.MUL);
        input.add(ExprType.FUNC);
        input.add(ExprType.MINUS);
        input.add(ExprType.FUNC);
        input.add(ExprType.R_PAREN);
        input.add(ExprType.MINUS);
        input.add(ExprType.Var);
        //
        Stack<ExprType> left=new Stack<ExprType>();//输入栈
        Stack<ExprType> right=new Stack<ExprType>();//操作符栈
//        left.push(ExprType.Shape);
//        right.push(ExprType.Shape);
//        System.out.println(str);
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
            if(left.peek()==ExprType.Var||left.peek()==ExprType.FUNC||left.peek()==ExprType.UINT_LITERAL){
                output.add(left.pop());
            }else{
                if(left.peek()==ExprType.R_PAREN){
                    left.pop();
                    while(right.peek()!=ExprType.L_PAREN){
                        output.add(right.pop());
                    }
                    right.pop();
                }else{
                    while(right.size()!=0&&right.peek()!=ExprType.L_PAREN&&map.get(right.peek())>=map.get(left.peek())){
                        output.add(right.pop());
                    }
                    right.push(left.pop());
                }
            }
        }
        for(int i=0;i<output.size();i++){
            System.out.println(output.get(i));
        }
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
        map.put(ExprType.NEG,1);
        map.put(ExprType.MINUS,1);
        map.put(ExprType.MUL,2);
        map.put(ExprType.DIV,2);
        map.put(ExprType.L_PAREN,3);
        map.put(ExprType.LT,0);
        map.put(ExprType.LE,0);
        map.put(ExprType.NEQ,0);
        map.put(ExprType.EQ,0);
        map.put(ExprType.GE,0);
        map.put(ExprType.GT,0);
    }
}
