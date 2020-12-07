package miniplc0java.analyser;

import miniplc0java.error.*;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    /**
     * 符号表
     */
//    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();
    public StackVar[] stack_vars = new StackVar[1000];
    public stack_point[] stack_points = new stack_point[1000];
    public func[] func_list = new func[1000];//函数列表
    public int stack_top1 = -1;
    public int stack_top2 = -1;
    public int func_top = -1;
    StackVar tmp_fn = null;//记录还没有定义类型的函数
    StackVar tmp_var = null;//记录还没有定义类型的变量
    func tmp_func = null;//目前所在的函数体
    int local_tmp = 0;//当前函数的局部变量个数
    int args_tmp = 0;//当前函数的输入参数个数
    Map<String, func> func_map = new HashMap<String, func>();
    int assign_flag = 1;//1 Unknown,2 int,3 void,4 double

//    Stack<Integer> stack_points=new Stack<Integer>();;

    /**
     * 下一个变量的栈偏移
     */
//    int nextOffset = 0;
    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        stack_point point = new stack_point(0, true);
        stack_points[++stack_top2] = point;
        initial_stack();
        expr_token[++expr_top] = new ArrayList<>();
        func_list[++func_top] = new func(0, 0, stack_top1, 3, false);
        tmp_func = func_list[0];
        tmp_func.func_num = func_top;
        func_map.put(stack_vars[stack_top1].name, func_list[func_top]);//函数映射
        analyseProgram();
        func main_func=func_map.get("main");//加载主函数
        int size=main_func.return_num;
        if(size==3)
            size=0;
        else{
            size=1;
        }
        tmp_func.AddOperations(new Instruction(Operation.stackalloc,size));
        tmp_func.AddOperations(new Instruction(Operation.call,main_func.func_num));
        if(size>0){
            tmp_func.AddOperations(new Instruction(Operation.popn,1));
        }
        return instructions;
    }


    //添加标准库
    public void initial_stack() {
        stack_vars[++stack_top1] = new StackVar(true, true, "getint", 2, new Var_position(0, 1), true);
        func_map.put(stack_vars[stack_top1].name, new func(0, 0, stack_top1, 2, true));
        stack_vars[++stack_top1] = new StackVar(true, true, "getdouble", 4, new Var_position(1, 1), true);
        func_map.put(stack_vars[stack_top1].name, new func(0, 0, stack_top1, 4, true));
        stack_vars[++stack_top1] = new StackVar(true, true, "getchar", 2, new Var_position(2, 1), true);
        func_map.put(stack_vars[stack_top1].name, new func(0, 0, stack_top1, 2, true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putint", 3, new Var_position(3, 1), true);
        func_map.put(stack_vars[stack_top1].name, new func(1, 0, stack_top1, 3, true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putdouble", 3, new Var_position(4, 1), true);
        func_map.put(stack_vars[stack_top1].name, new func(1, 0, stack_top1, 3, true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putchar", 3, new Var_position(5, 1), true);
        func_map.put(stack_vars[stack_top1].name, new func(1, 0, stack_top1, 3, true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putstr", 3, new Var_position(6, 1), true);
        func_map.put(stack_vars[stack_top1].name, new func(1, 0, stack_top1, 3, true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putln", 3, new Var_position(7, 1), true);
        func_map.put(stack_vars[stack_top1].name, new func(0, 0, stack_top1, 3, true));
        stack_vars[++stack_top1] = new StackVar(true, true, "_start", 1, new Var_position(8, 1), true);//初始化函数_start
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            do {
                peekedToken = tokenizer.nextToken();
                System.out.println(peekedToken);
            } while (peekedToken.getTokenType().equals(TokenType.ANNOTATION));
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            Token tmp_token;
            do {
                tmp_token = tokenizer.nextToken();
            } while (tmp_token.getTokenType() == TokenType.ANNOTATION);
            return tmp_token;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    //从总栈获取标识符
    private StackVar stack_get_full(String target) {
        int tmp_top1 = stack_top1, tmp_top2 = stack_top2;
        while (tmp_top2 >= 0) {
            for (int i = stack_points[tmp_top2].points; i <= tmp_top1; i++) {
                if (stack_vars[i].is_string_equal(target))
                    return stack_vars[i];
            }
            tmp_top1 = stack_points[tmp_top2].points - 1;
            tmp_top2--;
        }
        return null;
    }

    //从当前栈获取标识符
    private StackVar stack_get_current(String target) {
        int tmp_top1 = stack_top1, tmp_top2 = stack_top2;

        for (int i = stack_points[tmp_top2].points; i <= tmp_top1; i++) {
            if (stack_vars[i].is_string_equal(target))
                return stack_vars[i];
        }
        return null;
    }

    //新开辟一个函数局部栈
    private void newFnStack() {
        stack_point point = new stack_point(stack_top1 + 1, true);
        stack_points[++stack_top2] = point;
    }

    //新开辟一个语句块局部栈
    private void newStmtStack() {
        stack_point point = new stack_point(stack_top1 + 1, false);
        stack_points[++stack_top2] = point;
    }

    //回退一个栈
    private void deleteStack() {
//        debug_print.print_current_stack(stack_top1, stack_top2, stack_vars, stack_points);
        stack_top1 = stack_points[stack_top2].points - 1;
        stack_top2--;
    }

    /**
     * 添加一个符号
     *
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, Pos curPos, int type, Var_position position,boolean is_fn) throws
            AnalyzeError {
        if (stack_get_current(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            this.stack_vars[++stack_top1] = new StackVar(isConstant, isInitialized, name, type, position,is_fn);
        }
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void declareSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = stack_get_full(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = stack_get_full(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    private boolean isInitialize(String name, Pos curPos) throws AnalyzeError {
        var entry = stack_get_full(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isInitialized();
        }
    }

    /**
     * item -> function | decl_stmt
     * program -> item*
     **/
    private void analyseProgram() throws CompileError {
        while (check(TokenType.LET_KW) || check(TokenType.FN_KW) || check(TokenType.CONST_KW)) {
            if (check(TokenType.FN_KW)) {
                analyseFn();
            } else {
                analyseDeclStmt();
            }
        }
        expect(TokenType.EOF);
//        System.out.println("ok!!!!!");
//        debug_print.print_stack(stack_top1, stack_top2, stack_vars, stack_points);
//        debug_print.print_funcs(func_list, func_top);
    }

    /**
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     * fn fib(x: int) -> int{}
     */
    private void analyseFn() throws CompileError {
        is_fun_first = true;
        expect(TokenType.FN_KW);
        Token fn_name = expect(TokenType.IDENT);
        if (func_map.get(fn_name.getValueString()) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, fn_name.getStartPos());
        } else {
            Var_position position = new Var_position(stack_top1 + 1, 1);
            addSymbol(fn_name.getValueString(), true, true, fn_name.getStartPos(), 1, position,true);
            tmp_fn = stack_vars[stack_top1];
        }
        args_tmp = 0;
        local_tmp = 0;
        func_list[++func_top] = new func(0, 0, stack_top1, 0, false);
        tmp_func = func_list[func_top];
        tmp_func.func_num = func_top;
        func_map.put(fn_name.getValueString(), tmp_func);
        expect(TokenType.L_PAREN);
        newFnStack();
        if (check(TokenType.CONST_KW) || check(TokenType.IDENT))
            analyseFunctionParamList();
        tmp_func.set_args_num(args_tmp);
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);

        int ty = analyseFnTy();
        tmp_fn.setType(ty);
        tmp_func.set_return_num(ty);
//        if (ty == 3)
//            tmp_func.set_return_num(0);
//        else {
//            tmp_func.set_return_num(1);
//        }
        analyseBlockStmt();
        tmp_func.set_locals_num(local_tmp);
        if(tmp_func.return_num==3)
            tmp_func.AddOperations(new Instruction(Operation.ret));
        tmp_func = func_list[0];
    }


    /**
     * function_param_list -> function_param (',' function_param)*
     */
    private void analyseFunctionParamList() throws CompileError {
        analyseFunctionParam();
        args_tmp++;
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            analyseFunctionParam();
            args_tmp++;
        }
    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     */
    private void analyseFunctionParam() throws CompileError {
        Var_position position = new Var_position(args_tmp + 1, 2);
        if (check(TokenType.CONST_KW)) {
            expect(TokenType.CONST_KW);
            Token param = expect(TokenType.IDENT);
            addSymbol(param.getValueString(), true, true, param.getStartPos(), 1, position,false);
        } else {
            Token param = expect(TokenType.IDENT);
            addSymbol(param.getValueString(), true, false, param.getStartPos(), 1, position,false);
        }
        tmp_var = stack_vars[stack_top1];
        expect(TokenType.COLON);
        tmp_var.setType(analyseVarTy());
    }

    /**
     * ty -> IDENT
     */
    private int analyseFnTy() throws CompileError {
        if (check(TokenType.INTEGER)) {
            expect(TokenType.INTEGER);
            return 2;
        } else {
            expect(TokenType.VOID);
            return 3;
        }
    }

    private int analyseVarTy() throws CompileError {
//            if (check(TokenType.INTEGER)) {
        expect(TokenType.INTEGER);
        return 2;
//            }
    }

    /**
     * decl_stmt -> let_decl_stmt | const_decl_stmt
     */
    private void analyseDeclStmt() throws CompileError {
        if (check(TokenType.LET_KW)) {
            analyseLetDeclStmt();
        } else {
            analyseConstDeclStmt();
        }
        local_tmp++;
    }
    /**
     * 获取变量地址
     * */
    //1 global,2 args,3 locals
    private void get_var_addr(Var_position position){
        if(position.var_type==1){
            tmp_func.AddOperations(new Instruction(Operation.globa,position.position));
        }else if(position.var_type==2){
            if(tmp_func.return_num!=3)
                tmp_func.AddOperations(new Instruction(Operation.arga,position.position));
            else{
                tmp_func.AddOperations(new Instruction(Operation.arga,position.position-1));
            }
        }else if(position.var_type==3){
            tmp_func.AddOperations(new Instruction(Operation.loca,position.position));
        }
    }

    /**
     * let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     */
    private void analyseLetDeclStmt() throws CompileError {
        expect(TokenType.LET_KW);
        Token var = expect(TokenType.IDENT);
        Var_position position;
        if (stack_top2 == 0)//全局变量
            position = new Var_position(stack_top1 + 1, 1);
        else
            position = new Var_position(local_tmp, 3);
        addSymbol(var.getValueString(), false, false, var.getStartPos(), 1, position,false);
        tmp_var = stack_vars[stack_top1];
        expect(TokenType.COLON);
        int type = analyseVarTy();
        tmp_var.setType(type);
        if (check(TokenType.ASSIGN)) {
            get_var_addr(position);
            expect(TokenType.ASSIGN);
            assign_flag = type;
            tmp_var.setInitialized(true);
            expr_token[expr_top].clear();
            analyseExpr();
            trans_expr(char_priority.expr_priority(expr_token[expr_top]));
            tmp_func.AddOperations(new Instruction(Operation.store_64));
//            debug_print.print_expr(char_priority.expr_priority(expr_token[expr_top]), true);
        }
        expect(TokenType.SEMICOLON);
        assign_flag = 1;
    }
    //把表达式中的操作加到当前函数中
    //1 global,2 args,3 locals
    //1 Unknown,2 int,3 void,4 double
    private void trans_expr(ArrayList<ExprToken> arrayList) {
        for(int i=0;i<arrayList.size();i++){
            ExprToken tmp=arrayList.get(i);
            if(tmp.type==ExprType.Var){
                Var ttmp=(Var)tmp;
                get_var_addr(ttmp.stack_var.position);
                tmp_func.AddOperations(new Instruction(Operation.load_64));
            }
            else if(tmp.type==ExprType.UINT_LITERAL){
                tmp_func.AddOperations(new Instruction(Operation.push,((Uint)tmp).value));
            }else if(tmp.type==ExprType.FUNC){
                Call_func ttmp=(Call_func)tmp;
                func tmp_fun = ttmp.function;
                if(tmp_fun.return_num!=3)
                    tmp_func.AddOperations(new Instruction(Operation.stackalloc,1));
                else
                    tmp_func.AddOperations(new Instruction(Operation.stackalloc,0));
                boolean tmp_flag=expr_stmt_flag;
                expr_stmt_flag=false;
                for(int j=0;j<ttmp.args.size();j++){
                    trans_expr(ttmp.args.get(j));
                }
                expr_stmt_flag=tmp_flag;
                if(tmp_fun.is_inner_func){
                    tmp_func.AddOperations(new Instruction(Operation.callname,tmp_fun.global_num));
                }else{
                    tmp_func.AddOperations(new Instruction(Operation.call,tmp_fun.func_num));
                }
                if(expr_stmt_flag){
                    if(tmp_fun.return_num!=3)
                        tmp_func.AddOperations(new Instruction(Operation.popn,1));
                }
            }else{
//                System.out.println(tmp.type);
                switch (tmp.type) {
                    case NEG:
                        tmp_func.AddOperations(new Instruction(Operation.neg_i));
                        break;
                    case MUL:
                        tmp_func.AddOperations(new Instruction(Operation.mul_i));
                        break;
                    case DIV:
                        tmp_func.AddOperations(new Instruction(Operation.div_i));
                        break;
                    case MINUS:
                        tmp_func.AddOperations(new Instruction(Operation.sub_i));
                        break;
                    case PLUS:
                        tmp_func.AddOperations(new Instruction(Operation.add_i));
                        break;
                    case EQ:
                        tmp_func.AddOperations(new Instruction(Operation.cmp_i));
                        tmp_func.AddOperations(new Instruction(Operation.not));
                        break;
                    case NEQ:
                        tmp_func.AddOperations(new Instruction(Operation.cmp_i));
                        break;
                    case LE:
                        tmp_func.AddOperations(new Instruction(Operation.cmp_i));
                        tmp_func.AddOperations(new Instruction(Operation.set_gt));
                        tmp_func.AddOperations(new Instruction(Operation.not));
                        break;
                    case GT:
                        tmp_func.AddOperations(new Instruction(Operation.cmp_i));
                        tmp_func.AddOperations(new Instruction(Operation.set_gt));
                        break;
                    case GE:
                        tmp_func.AddOperations(new Instruction(Operation.cmp_i));
                        tmp_func.AddOperations(new Instruction(Operation.set_lt));
                        tmp_func.AddOperations(new Instruction(Operation.not));
                        break;
                    case LT:
                        tmp_func.AddOperations(new Instruction(Operation.cmp_i));
                        tmp_func.AddOperations(new Instruction(Operation.set_lt));
                        break;
                    default:
                        tmp_func.AddOperations(new Instruction(Operation.panic));
                }

            }
        }
    }

    /**
     * const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     */
    private void analyseConstDeclStmt() throws CompileError {
        expect(TokenType.CONST_KW);
        Token var = expect(TokenType.IDENT);
        Var_position position;
        if (stack_top2 == 0)
            position = new Var_position(stack_top1 + 1, 1);
        else
            position = new Var_position(local_tmp, 3);
        addSymbol(var.getValueString(), true, true, var.getStartPos(), 1, position,false);
        tmp_var = stack_vars[stack_top1];
        expect(TokenType.COLON);
        int type = analyseFnTy();
        tmp_var.setType(type);
        assign_flag = type;
        get_var_addr(position);
        expect(TokenType.ASSIGN);
        expr_token[expr_top].clear();
        analyseExpr();
        trans_expr(char_priority.expr_priority(expr_token[expr_top]));
        tmp_func.AddOperations(new Instruction(Operation.store_64));
//        debug_print.print_expr(char_priority.expr_priority(expr_token[expr_top]), true);
        expect(TokenType.SEMICOLON);
        assign_flag = 1;
    }
//------------------------------------------------------------------表达式操作

    /**
     * expr ->
     * operator_expr
     * | negate_expr
     * | assign_expr
     * | as_expr
     * | call_expr
     * | literal_expr
     * | ident_expr
     * | group_expr
     */
    ArrayList[] expr_token = new ArrayList[1000];
    Call_func[] call_func = new Call_func[1000];
    int expr_top = -1;

    private void analyseExpr() throws CompileError {
        Boolean flag = false;
        if (analyseNegateExpr())
            flag = true;
        if (!flag) {
            if (analyseAssignOrIdentOrCallExpr()) {
                flag = true;
            }
        }
        if (!flag) {
            if (analyseLiteralExpr()) {
                flag = true;
            }
        }
        if (!flag) {
            if (analyseGroupExpr()) {
                flag = true;
            }
        }
        if (!flag) {
            throw new AnalyzeError(ErrorCode.IncompleteExpression, peek().getStartPos());
        }
        if (check(TokenType.AS_KW)) {
            expect(TokenType.AS_KW);
            analyseVarTy();
        } else {
            if (analyseBinaryOperator()) {
                analyseExpr();
            }
        }
    }


    /**
     * negate_expr -> '-' expr
     */
    private Boolean analyseNegateExpr() throws CompileError {
        if (check(TokenType.MINUS)) {
            expect(TokenType.MINUS);
            expr_token[expr_top].add(new ExprToken(ExprType.NEG));
            analyseExpr();
            return true;
        }
        return false;
    }

    /**
     * ident_expr -> IDENT
     * -----------------
     * assign_expr -> l_expr '=' expr
     * l_expr -> IDENT
     * -----------------
     * call_expr -> IDENT '(' call_param_list? ')'
     * call_param_list -> expr (',' expr)*
     */
    private boolean analyseAssignOrIdentOrCallExpr() throws CompileError {
        if (check(TokenType.IDENT)) {
            Token var = expect(TokenType.IDENT);
            if (check(TokenType.ASSIGN)) {
                expr_stmt_flag=false;
                expr_token[expr_top].clear();
                expect(TokenType.ASSIGN);
                StackVar tmp_stack_var = stack_get_full(var.getValueString());
                get_var_addr(tmp_stack_var.position);
                if (tmp_stack_var != null && !tmp_stack_var.isConstant) {
                    tmp_stack_var.setInitialized(true);
                    assign_flag = tmp_stack_var.type;
//                    System.out.println(String.format("%s:%d",var.getValueString(),assign_flag));
                    analyseExpr();
                    expr_assgin=true;
                    assign_flag = 1;
                    return true;
                } else {
                    if (tmp_stack_var == null)
                        throw new AnalyzeError(ErrorCode.NotDeclared, var.getStartPos());
                    else {
                        throw new AnalyzeError(ErrorCode.AssignToConstant, var.getStartPos());
                    }
                }
            } else if (check(TokenType.L_PAREN)) {//函数调用
                expect(TokenType.L_PAREN);
                param_cnt = 0;
                func function = func_map.get(var.getValueString());
                if (assign_flag > 1) {
                    if (function.return_num != assign_flag) {
                        throw new AnalyzeError(ErrorCode.InvalidAssignment, peek().getStartPos());
                    }
                }
                Call_func func = new Call_func(ExprType.FUNC, function);
                expr_token[expr_top].add(func);
                call_func[++expr_top] = func;
                expr_token[expr_top] = new ArrayList<>();
                if (!check(TokenType.R_PAREN))
                    analyseCallParamList();
                if (function == null) {
                    throw new AnalyzeError(ErrorCode.NotDeclared, var.getStartPos());
                }
                if (param_cnt != function.args_num) {
                    throw new AnalyzeError(ErrorCode.InvalidParamNum, var.getStartPos());
                }
                expect(TokenType.R_PAREN);
                expr_top--;
                return true;
            } else {
                StackVar tmp_stack_var = stack_get_full(var.getValueString());
                if (assign_flag > 1) {
                    if (tmp_stack_var.type != assign_flag) {
                        throw new AnalyzeError(ErrorCode.InvalidAssignment, peek().getStartPos());
                    }
                }
                if (tmp_stack_var == null) {
                    throw new AnalyzeError(ErrorCode.NotDeclared, var.getStartPos());
                }
                if (!tmp_stack_var.isInitialized) {
                    throw new AnalyzeError(ErrorCode.NotInitialized, var.getStartPos());
                }
                expr_token[expr_top].add(new Var(ExprType.Var, tmp_stack_var));
                return true;
            }
        }
        return false;
    }

    /**
     * private boolean analyseLiteralExpr()
     */
    int param_cnt = 0;

    private void analyseCallParamList() throws CompileError {
        param_cnt++;
        int tmp_cnt=param_cnt;
        analyseExpr();
        param_cnt=tmp_cnt;
        call_func[expr_top].add_arg(char_priority.expr_priority(expr_token[expr_top]));
        while (check(TokenType.COMMA)) {
            expr_token[expr_top].clear();
            param_cnt++;
            tmp_cnt=param_cnt;
            expect(TokenType.COMMA);
            analyseExpr();
            param_cnt=tmp_cnt;
        }
    }

    /**
     * literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL | CHAR_LITERAL
     */
    private boolean analyseLiteralExpr() throws CompileError {
        if (check(TokenType.UINT_LITERAL)) {
            Token tmp = expect(TokenType.UINT_LITERAL);
            if(assign_flag!=2&&expr_top==0){
                throw new AnalyzeError(ErrorCode.InvalidAssignment,peek().getStartPos());
            }
            expr_token[expr_top].add(new Uint(ExprType.UINT_LITERAL, (long) tmp.getValue()));
            return true;
        } else if (check(TokenType.STRING_LITERAL)) {
            expect(TokenType.STRING_LITERAL);
            return true;
        }
        return false;
        //暂不实现字符和浮点数
    }

    /**
     * group_expr -> '(' expr ')'
     */

    private boolean analyseGroupExpr() throws CompileError {
        if (check(TokenType.L_PAREN)) {
            expect(TokenType.L_PAREN);
            expr_token[expr_top].add(new ExprToken(ExprType.L_PAREN));
            analyseExpr();
            expect(TokenType.R_PAREN);
            expr_token[expr_top].add(new ExprToken(ExprType.R_PAREN));
            return true;
        }
        return false;
    }

    /**
     * binary_operator -> '+' | '-' | '*' | '/' | '==' | '!=' | '<' | '>' | '<=' | '>='
     */
    private boolean analyseBinaryOperator() throws CompileError {
        switch (peek().getTokenType()) {
            case PLUS:
                expect(TokenType.PLUS);
                expr_token[expr_top].add(new ExprToken(ExprType.PLUS));
                return true;
            case MINUS:
                expect(TokenType.MINUS);
                expr_token[expr_top].add(new ExprToken(ExprType.MINUS));
                return true;
            case MUL:
                expect(TokenType.MUL);
                expr_token[expr_top].add(new ExprToken(ExprType.MUL));
                return true;
            case DIV:
                expect(TokenType.DIV);
                expr_token[expr_top].add(new ExprToken(ExprType.DIV));
                return true;
            case EQ:
                expect(TokenType.EQ);
                expr_token[expr_top].add(new ExprToken(ExprType.EQ));
                return true;
            case NEQ:
                expect(TokenType.NEQ);
                expr_token[expr_top].add(new ExprToken(ExprType.NEG));
                return true;
            case LE:
                expect(TokenType.LE);
                expr_token[expr_top].add(new ExprToken(ExprType.LE));
                return true;
            case GE:
                expect(TokenType.GE);
                expr_token[expr_top].add(new ExprToken(ExprType.GE));
                return true;
            case GT:
                expect(TokenType.GT);
                expr_token[expr_top].add(new ExprToken(ExprType.GT));
                return true;
            case LT:
                expect(TokenType.LT);
                expr_token[expr_top].add(new ExprToken(ExprType.LT));
                return true;
            default:
                return false;
        }
    }

    //--------------------------------------------------------------------------
//----------------------------------------------------------语句
    boolean is_fun_first = false;//是不是第一次进入函数语句块

    /**
     * block_stmt -> '{' stmt* '}'
     */
    private void analyseBlockStmt() throws CompileError {
        if (!is_fun_first) {
            newStmtStack();
        } else {
            is_fun_first = false;
        }
        expect(TokenType.L_BRACE);
        while (!check(TokenType.R_BRACE))
            analyseStmt();
        expect(TokenType.R_BRACE);
        deleteStack();
    }

    /**
     * stmt ->
     * expr_stmt
     * | decl_stmt
     * | if_stmt
     * | while_stmt
     * | break_stmt
     * | continue_stmt
     * | return_stmt
     * | block_stmt
     * | empty_stmt
     */
    boolean expr_assgin=false;//判断是不是赋值表达式
    boolean expr_stmt_flag=false;
    private void analyseStmt() throws CompileError {
        if (check(TokenType.LET_KW) || check(TokenType.CONST_KW)) {
            analyseDeclStmt();
        } else if (check(TokenType.IF_KW)) {
            analyseIfStmt();
        } else if (check(TokenType.WHILE_KW)) {
            analyseWhileStmt();
        }
//      else if(check(TokenType.BREAK_KW))
//      else if(check(TokenType.CONTINUE_KW))
        else if (check(TokenType.RETURN_KW)) {
            analyseReturnStmt();
        } else if (check(TokenType.L_BRACE)) {
            analyseBlockStmt();
        } else if (check(TokenType.SEMICOLON)) {
            expect(TokenType.SEMICOLON);
        } else {
            expr_token[expr_top].clear();
            expr_stmt_flag=true;
            analyseExpr();
            trans_expr(char_priority.expr_priority(expr_token[expr_top]));
//            debug_print.print_expr(char_priority.expr_priority(expr_token[expr_top]), true);
            expr_stmt_flag=false;
            if(expr_assgin){
                tmp_func.AddOperations(new Instruction(Operation.store_64));
                expr_assgin=false;
            }
            expect(TokenType.SEMICOLON);
        }
    }

    /**
     * return_stmt -> 'return' expr? ';'
     */
    private void analyseReturnStmt() throws CompileError {
        expect(TokenType.RETURN_KW);
        expr_token[expr_top].clear();
        assign_flag=tmp_func.return_num;
//        System.out.println("//"+assign_flag);
        tmp_func.AddOperations(new Instruction(Operation.arga,0));
        analyseExpr();
        trans_expr(char_priority.expr_priority(expr_token[expr_top]));
//        debug_print.print_expr(char_priority.expr_priority(expr_token[expr_top]), true);
        assign_flag=1;
        tmp_func.AddOperations(new Instruction(Operation.store_64));
        tmp_func.AddOperations(new Instruction(Operation.ret));
        expect(TokenType.SEMICOLON);
    }

    /**
     * while_stmt -> 'while' expr block_stmt
     */
    private void analyseWhileStmt() throws CompileError {
        expect(TokenType.WHILE_KW);
        expr_token[expr_top].clear();
        assign_flag = 2;
        int loop_cnt=tmp_func.operations.size();
        analyseExpr();
        trans_expr(char_priority.expr_priority(expr_token[expr_top]));
        Instruction begin=new Instruction(Operation.br,0);
        tmp_func.AddOperations(new Instruction(Operation.br_true,1));
        tmp_func.AddOperations(begin);
        int br_cnt=tmp_func.operations.size();
        assign_flag = 1;
//        debug_print.print_expr(char_priority.expr_priority(expr_token[expr_top]), true);
        analyseBlockStmt();
        tmp_func.AddOperations(new Instruction(Operation.br,loop_cnt-tmp_func.operations.size()));
        begin.setArg1(tmp_func.operations.size()-br_cnt);
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)
     */
    private void analyseIfStmt() throws CompileError {
        expect(TokenType.IF_KW);
        expr_token[expr_top].clear();
        assign_flag = 2;
        analyseExpr();
        trans_expr(char_priority.expr_priority(expr_token[expr_top]));
//        debug_print.print_expr(char_priority.expr_priority(expr_token[expr_top]), true);
        assign_flag = 1;
        Instruction begin=new Instruction(Operation.br,0);
        tmp_func.AddOperations(new Instruction(Operation.br_true,1));
        tmp_func.AddOperations(begin);
        int br_cnt=tmp_func.operations.size();
        analyseBlockStmt();
        begin.setArg1(tmp_func.operations.size()-br_cnt);
        while (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);
            if (check(TokenType.IF_KW)) {
                expect(TokenType.IF_KW);
                expr_token[expr_top].clear();
                assign_flag = 2;
                analyseExpr();
                assign_flag = 1;
                trans_expr(char_priority.expr_priority(expr_token[expr_top]));
//                debug_print.print_expr(char_priority.expr_priority(expr_token[expr_top]), true);
                tmp_func.AddOperations(new Instruction(Operation.br_true,1));
                begin=new Instruction(Operation.br,0);
                tmp_func.AddOperations(begin);
                br_cnt=tmp_func.operations.size();
                analyseBlockStmt();
                begin.setArg1(tmp_func.operations.size()-br_cnt);
            } else {
                analyseBlockStmt();
                break;
            }
        }
    }
}
