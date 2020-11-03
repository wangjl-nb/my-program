import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class main {
    public static void main(String[] args) {
        analyser tmp = new analyser();
        Scanner input = new Scanner(System.in);
        String str=input.nextLine();
        Stack<Character> left=new Stack<Character>();
        Stack<Character> right=new Stack<Character>();
        left.push('#');
        right.push('#');
        System.out.println(str);
        for(int i=str.length()-1;i>=0;i--){
            right.push(str.charAt(i));
        }
        while(!(right.peek()=='#'&&left.peek()=='N'&&left.size()==2)){
            if(tmp.map.get(right.peek())==null){
                System.out.println('E');
                break;
            }
            char r_ch=right.peek(),l_ch,tmp_ch;
            if(tmp.map.get(left.peek())==null){
                tmp_ch=left.pop();
                if(tmp.map.get(left.peek())==null){
                    System.out.println('E');
                    break;
                }else{
                    l_ch=left.peek();
                    left.push(tmp_ch);
                }
            }else{
                l_ch=left.peek();
            }
            int cmp=tmp.priority[tmp.map.get(l_ch)][tmp.map.get(r_ch)];
            if(cmp>=1){
                char ch=right.pop();
                left.push(ch);
                System.out.format("I%c\n",ch);
            }else if(cmp==-1){
                System.out.println('E');
                break;
            }else{
                if(l_ch=='i'){
                    left.pop();
                    left.push('N');
                    System.out.println('R');
                    continue;
                }
                else if(l_ch==')'){
                    if(left.pop()==')'){
                        if(left.pop()=='N'){
                            if(left.pop()=='('){
                                left.push('N');
                                System.out.println('R');
                                continue;
                            }
                        }
                    }
                    System.out.println("RE");
                    break;
                }
                else if(l_ch=='+'){
                    if(left.pop()=='N'){
                        if(left.pop()=='+'){
                            if(left.pop()=='N'){
                                left.push('N');
                                System.out.println('R');
                                continue;
                            }
                        }
                    }
                    System.out.println("RE");
                    break;
                }
                else if(l_ch=='*'){
                    if(left.pop()=='N'){
                        if(left.pop()=='*'){
                            if(left.pop()=='N'){
                                left.push('N');
                                System.out.println('R');
                                continue;
                            }
                        }
                    }
                    System.out.println("RE");
                    break;
                }
                else{
                    System.out.println("RE");
                    break;
                }
            }
//            System.out.format("%c %c\n",l_ch,r_ch);
        }
    }
}
class analyser {
    /*
     *       +   *   i   (   )   #
     *   +   >   <   <   <   >   >
     *   *   >   >   <   <   >   >
     *   i   >   >           >   >
     *   (   <   <   <   <   =   >
     *   )   >   >           >   >
     *   #   <   <   <   <   <   =
     * */
    int[][] priority = {
            {0, 1, 1, 1, 0, 0},//> 0,< 1,= 2
            {0, 0, 1, 1, 0, 0},
            {0, 0,-1,-1, 0, 0},
            {1, 1, 1, 1, 2, 0},
            {0, 0,-1,-1, 0, 0},
            {1, 1, 1, 1, 1, 2}
    };
    Map<Character,Integer> map;
    analyser(){
        map=new HashMap<Character, Integer>();
        map.put('+',0);
        map.put('*',1);
        map.put('i',2);
        map.put('(',3);
        map.put(')',4);
        map.put('#',5);
    }
}
