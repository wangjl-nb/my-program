package miniplc0java.tokenizer;

public enum TokenType {
    /** 空 */
    None,
    /** int类型*/
    INTEGER,
    /** void类型*/
    VOID,
    /** 无符号整数 */
    UINT_LITERAL,
    /** 字符串 */
    STRING_LITERAL,
    /** 标识符 */
    IDENT,
    /** 函数 */
    FN_KW,
    /** Let */
    LET_KW,
    /** AS */
    AS_KW,
    /** Const */
    CONST_KW,
    /** WHILE */
    WHILE_KW,
    /** IF */
    IF_KW,
    /** ELSE */
    ELSE_KW,
    /** RETURN */
    RETURN_KW,
    /** 加号 */
    PLUS,
    /** 减号 */
    MINUS,
    /** 乘号 */
    MUL,
    /** 除号 */
    DIV,
    /** 赋值等号 */
    ASSIGN,
    /** 等号 */
    EQ,
    /** 不等号 */
    NEQ,
    /** 小于号 */
    LT,
    /** 大于号 */
    GT,
    /** 小于等于号 */
    LE,
    /** 大于等于号 */
    GE,
    /** 分号 */
    SEMICOLON,
    /** 左括号 */
    L_PAREN,
    /** 右括号 */
    R_PAREN,
    /** 左大括号 */
    L_BRACE,
    /** 右大括号 */
    R_BRACE,
    /** 箭头 */
    ARROW,
    /** 逗号 */
    COMMA,
    /** 冒号 */
    COLON,
    /** 注释 */
    ANNOTATION,
    /** 文件尾 */
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case None:
                return "NullToken";
            case INTEGER:
                return "ValueType:INTEGER";
            case VOID:
                return "ValueType:VOID";
            case UINT_LITERAL:
                return "UintNum";
            case STRING_LITERAL:
                return "String";
            case IDENT:
                return "Ident";
            case EOF:
                return "EOF";
            case FN_KW:
                return "KeyWord:fn";
            case LET_KW:
                return "KeyWord:let";
            case AS_KW:
                return "KeyWord:as";
            case CONST_KW:
                return "KeyWord:const";
            case WHILE_KW:
                return "KeyWord:while";
            case IF_KW:
                return "KeyWord:if";
            case ELSE_KW:
                return "KeyWord:else";
            case RETURN_KW:
                return "KeyWord:return";
            case PLUS:
                return "Operation:PLUS";
            case MINUS:
                return "Operation:MINUS";
            case MUL:
                return "Operation:MUL";
            case DIV:
                return "Operation:DIV";
            case ASSIGN:
                return "Operation:ASSIGN";
            case EQ:
                return "Operation:EQ";
            case NEQ:
                return "Operation:NEQ";
            case LT:
                return "Operation:LT";
            case GT:
                return "Operation:GT";
            case LE:
                return "Operation:LE";
            case GE:
                return "Operation:GE";
            case SEMICOLON:
                return "Operation:SEMICOLON";
            case L_PAREN:
                return "Operation:L_PAREN";
            case R_PAREN:
                return "Operation:R_PAREN";
            case L_BRACE:
                return "Operation:L_BRACE";
            case R_BRACE:
                return "Operation:R_BRACE";
            case ARROW:
                return "Operation:ARROW";
            case COLON:
                return "Operation:COLON";
            case COMMA:
                return "Operation:COMMA";
            case ANNOTATION:
                return "Annotation";
            default:
                return "InvalidToken";
        }
    }
}
